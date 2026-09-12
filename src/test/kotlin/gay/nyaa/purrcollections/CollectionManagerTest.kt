package gay.nyaa.purrcollections

import com.purrcore.i18n.I18n
import gay.nyaa.purrcollections.db.CollectionRepository
import gay.nyaa.purrcollections.domain.*
import gay.nyaa.purrcollections.registry.CollectionRegistry
import gay.nyaa.purrcollections.rewards.RewardExecutor
import gay.nyaa.purrcollections.rewards.impl.RecipeUnlockHandler
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purrskills.skill.Skill
import io.mockk.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CollectionManagerTest {
    private val mockRegistry = mockk<CollectionRegistry>()
    private val mockRepository = mockk<CollectionRepository>(relaxed = true)
    private val mockRewardExecutor = mockk<RewardExecutor>(relaxed = true)
    private val mockRecipeUnlockHandler = mockk<RecipeUnlockHandler>(relaxed = true)
    private val mockI18n = mockk<I18n>(relaxed = true)
    private val mockPlayer = mockk<Player>(relaxed = true)

    private lateinit var manager: CollectionManager

    private val testUuid = UUID.randomUUID()
    private val testCollectionId = CollectionId("test", "TEST")
    private val testCollection = Collection(
        id = testCollectionId,
        type = CollectionType.MINING,
        material = Material.STONE,
        displayName = "Test Stone",
        tiers = listOf(
            CollectionTier(1, 100, listOf(CollectionReward.SkillXP(Skill.MINING, 10))),
            CollectionTier(2, 500, listOf(CollectionReward.RecipeUnlock(ItemId("test", "ITEM")))),
            CollectionTier(3, 1000, listOf(CollectionReward.CoinReward(100)))
        )
    )

    @BeforeEach
    fun setup() {
        every { mockPlayer.uniqueId } returns testUuid
        every { mockI18n.t(any<String>()) } returns "Translated message"
        every { mockI18n.t(any<String>(), *anyVararg()) } returns "Translated message"
        every { mockRegistry.get(testCollectionId) } returns testCollection
        every { mockRepository.loadPlayerCollections(any()) } returns PlayerCollections(testUuid, emptyMap())
        every { mockRepository.loadRecipeUnlocks(any()) } returns emptySet()
        every { mockRewardExecutor.execute(any(), any(), any()) } returns true

        manager = CollectionManager(
            mockRegistry,
            mockRepository,
            mockRewardExecutor,
            mockRecipeUnlockHandler,
            mockI18n
        )
    }

    @Test
    fun `loadPlayerCollections loads from repository`() {
        val progress = CollectionProgress(testCollectionId, 100, 1, Instant.now())
        val playerCollections = PlayerCollections(testUuid, mapOf(testCollectionId to progress))
        every { mockRepository.loadPlayerCollections(testUuid) } returns playerCollections
        every { mockRepository.loadRecipeUnlocks(testUuid) } returns setOf("test:ITEM")

        manager.loadPlayerCollections(testUuid)

        verify { mockRepository.loadPlayerCollections(testUuid) }
        verify { mockRepository.loadRecipeUnlocks(testUuid) }
        verify { mockRecipeUnlockHandler.loadUnlocks(testUuid, setOf("test:ITEM")) }

        val loaded = manager.getPlayerCollections(testUuid)
        assertNotNull(loaded)
        assertEquals(1, loaded.collections.size)
    }

    @Test
    fun `unloadPlayerCollections clears cache`() {
        manager.loadPlayerCollections(testUuid)
        manager.unloadPlayerCollections(testUuid)

        verify { mockRecipeUnlockHandler.clearUnlocks(testUuid) }

        val loaded = manager.getPlayerCollections(testUuid)
        assertNull(loaded)
    }

    @Test
    fun `increment creates new collection progress`() {
        manager.loadPlayerCollections(testUuid)

        manager.increment(mockPlayer, testCollectionId, 50)

        verify {
            mockRepository.saveProgress(
                testUuid,
                match<CollectionProgress> {
                    it.collectionId == testCollectionId &&
                    it.totalGathered == 50 &&
                    it.currentTier == 0
                }
            )
        }

        val collections = manager.getPlayerCollections(testUuid)
        assertEquals(50, collections?.getProgress(testCollectionId)?.totalGathered)
    }

    @Test
    fun `increment unlocks tier when threshold reached`() {
        manager.loadPlayerCollections(testUuid)

        manager.increment(mockPlayer, testCollectionId, 100)

        verify {
            mockRewardExecutor.execute(
                mockPlayer,
                "Test Stone",
                match<List<CollectionReward>> { it.size == 1 && it[0] is CollectionReward.SkillXP }
            )
        }

        verify {
            mockRepository.saveProgress(
                testUuid,
                match<CollectionProgress> {
                    it.currentTier == 1 &&
                    it.totalGathered == 100
                }
            )
        }

        verify { mockPlayer.showTitle(any<net.kyori.adventure.title.Title>()) }
    }

    @Test
    fun `increment unlocks multiple tiers at once`() {
        manager.loadPlayerCollections(testUuid)

        manager.increment(mockPlayer, testCollectionId, 600)

        // Should unlock tier 1 and tier 2
        verify(exactly = 2) {
            mockRewardExecutor.execute(mockPlayer, any(), any())
        }

        verify(exactly = 2) {
            mockPlayer.showTitle(any<net.kyori.adventure.title.Title>())
        }

        val collections = manager.getPlayerCollections(testUuid)
        assertEquals(2, collections?.getProgress(testCollectionId)?.currentTier)
        assertEquals(600, collections?.getProgress(testCollectionId)?.totalGathered)
    }

    @Test
    fun `increment adds to existing progress`() {
        val existingProgress = CollectionProgress(testCollectionId, 50, 0, Instant.now())
        val playerCollections = PlayerCollections(testUuid, mapOf(testCollectionId to existingProgress))
        every { mockRepository.loadPlayerCollections(testUuid) } returns playerCollections

        manager.loadPlayerCollections(testUuid)
        manager.increment(mockPlayer, testCollectionId, 60)

        val collections = manager.getPlayerCollections(testUuid)
        assertEquals(110, collections?.getProgress(testCollectionId)?.totalGathered)
        assertEquals(1, collections?.getProgress(testCollectionId)?.currentTier) // Should unlock tier 1
    }

    @Test
    fun `increment saves recipe unlocks`() {
        manager.loadPlayerCollections(testUuid)

        // Unlock tier 2 which has a recipe unlock reward
        manager.increment(mockPlayer, testCollectionId, 500)

        verify {
            mockRepository.saveRecipeUnlock(testUuid, "test:ITEM")
        }
    }

    @Test
    fun `increment handles reward execution failure`() {
        every { mockRewardExecutor.execute(any(), any(), any()) } returns false

        manager.loadPlayerCollections(testUuid)
        manager.increment(mockPlayer, testCollectionId, 100)

        verify { mockPlayer.sendMessage("Translated message") } // Error message
        verify(exactly = 0) {
            mockRepository.saveRecipeUnlock(any(), any())
        }
    }

    @Test
    fun `increment ignores unknown collection`() {
        every { mockRegistry.get(testCollectionId) } returns null

        manager.loadPlayerCollections(testUuid)
        manager.increment(mockPlayer, testCollectionId, 100)

        // Should not save anything
        verify(exactly = 0) {
            mockRepository.saveProgress(any(), any())
        }
    }

    @Test
    fun `increment ignores unloaded player`() {
        // Don't load player collections
        manager.increment(mockPlayer, testCollectionId, 100)

        verify(exactly = 0) {
            mockRepository.saveProgress(any(), any())
        }
    }

    @Test
    fun `hasUnlockedRecipe delegates to handler`() {
        every { mockRecipeUnlockHandler.hasUnlocked(testUuid, "test:ITEM") } returns true

        val result = manager.hasUnlockedRecipe(testUuid, "test:ITEM")

        assertEquals(true, result)
        verify { mockRecipeUnlockHandler.hasUnlocked(testUuid, "test:ITEM") }
    }

    @Test
    fun `getPlayerCollections returns null for unloaded player`() {
        val collections = manager.getPlayerCollections(testUuid)
        assertNull(collections)
    }
}
