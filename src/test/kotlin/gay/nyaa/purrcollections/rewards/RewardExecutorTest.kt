package gay.nyaa.purrcollections.rewards

import com.purrcore.i18n.I18n
import gay.nyaa.purrcollections.domain.CollectionReward
import gay.nyaa.purrcollections.rewards.impl.RecipeUnlockHandler
import gay.nyaa.purrcollections.rewards.impl.SkillXPRewardHandler
import gay.nyaa.purrcollections.rewards.impl.StatBonusHandler
import gay.nyaa.purreconomy.api.PurrEconomyAPI
import gay.nyaa.purreconomy.domain.EconomyResult
import gay.nyaa.purreconomy.domain.TransactionSource
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purrskills.skill.Skill
import gay.nyaa.purrskills.stats.StatType
import io.mockk.*
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RewardExecutorTest {
    private val mockSkillXPHandler = mockk<SkillXPRewardHandler>(relaxed = true)
    private val mockRecipeUnlockHandler = mockk<RecipeUnlockHandler>(relaxed = true)
    private val mockStatBonusHandler = mockk<StatBonusHandler>(relaxed = true)
    private val mockEconomyAPI = mockk<PurrEconomyAPI>(relaxed = true)
    private val mockI18n = mockk<I18n>(relaxed = true)
    private val mockPlayer = mockk<Player>(relaxed = true)
    private val mockInventory = mockk<PlayerInventory>(relaxed = true)

    private lateinit var executor: RewardExecutor

    private val testUuid = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        every { mockPlayer.uniqueId } returns testUuid
        every { mockPlayer.inventory } returns mockInventory
        every { mockInventory.addItem(any()) } returns java.util.HashMap()
        every { mockI18n.t(any<String>()) } returns "Translated"
        every { mockI18n.t(any<String>(), *anyVararg()) } returns "Translated"

        executor = RewardExecutor(
            mockSkillXPHandler,
            mockRecipeUnlockHandler,
            mockStatBonusHandler,
            mockEconomyAPI,
            mockI18n
        )
    }

    @Test
    fun `execute SkillXP reward succeeds`() {
        val reward = CollectionReward.SkillXP(Skill.MINING, 100)

        val result = executor.execute(mockPlayer, "Test Collection", listOf(reward))

        assertTrue(result)
        verify { mockSkillXPHandler.handle(mockPlayer, reward) }
        verify { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `execute RecipeUnlock reward succeeds`() {
        val reward = CollectionReward.RecipeUnlock(ItemId("test", "ITEM"))

        val result = executor.execute(mockPlayer, "Test Collection", listOf(reward))

        assertTrue(result)
        verify { mockRecipeUnlockHandler.handle(mockPlayer, reward) }
        verify { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `execute StatBonus reward succeeds`() {
        val reward = CollectionReward.StatBonus(StatType.MINING_SPEED, 5.0, true)

        val result = executor.execute(mockPlayer, "Test Collection", listOf(reward))

        assertTrue(result)
        verify { mockStatBonusHandler.handle(mockPlayer, reward) }
        verify { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `execute CoinReward succeeds`() {
        val reward = CollectionReward.CoinReward(100)
        every { 
            mockEconomyAPI.add(mockPlayer, 100L, any(), TransactionSource.COLLECTION) 
        } returns EconomyResult.Success(100L)

        val result = executor.execute(mockPlayer, "Test Collection", listOf(reward))

        assertTrue(result)
        verify { 
            mockEconomyAPI.add(mockPlayer, 100L, "Collection reward: Test Collection", TransactionSource.COLLECTION)
        }
        verify { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `execute CoinReward fails when economy unavailable`() {
        val executorNoEconomy = RewardExecutor(
            mockSkillXPHandler,
            mockRecipeUnlockHandler,
            mockStatBonusHandler,
            null, // No economy API
            mockI18n
        )
        val reward = CollectionReward.CoinReward(100)

        val result = executorNoEconomy.execute(mockPlayer, "Test Collection", listOf(reward))

        assertFalse(result)
        verify { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `execute CoinReward fails when economy returns error`() {
        val reward = CollectionReward.CoinReward(100)
        every { 
            mockEconomyAPI.add(any(), any(), any(), any()) 
        } returns EconomyResult.InvalidAmount(0L)

        val result = executor.execute(mockPlayer, "Test Collection", listOf(reward))

        assertFalse(result)
        verify { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `execute ItemReward fails when PurrItems unavailable`() {
        mockkStatic(org.bukkit.Bukkit::class)
        every { org.bukkit.Bukkit.getPluginManager() } returns mockk {
            every { getPlugin("PurrItems") } returns null
        }

        val reward = CollectionReward.ItemReward(ItemId("test", "ITEM"), 1)
        val result = executor.execute(mockPlayer, "Test Collection", listOf(reward))

        assertFalse(result)
        verify { mockPlayer.sendMessage(any<String>()) }

        unmockkStatic(org.bukkit.Bukkit::class)
    }

    @Test
    fun `execute multiple rewards all succeed`() {
        val rewards = listOf(
            CollectionReward.SkillXP(Skill.MINING, 100),
            CollectionReward.RecipeUnlock(ItemId("test", "ITEM")),
            CollectionReward.StatBonus(StatType.MINING_SPEED, 5.0, true)
        )

        val result = executor.execute(mockPlayer, "Test Collection", rewards)

        assertTrue(result)
        verify { mockSkillXPHandler.handle(any(), any()) }
        verify { mockRecipeUnlockHandler.handle(any(), any()) }
        verify { mockStatBonusHandler.handle(any(), any()) }
        verify(exactly = 3) { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `execute returns false when one reward fails`() {
        val rewards = listOf(
            CollectionReward.SkillXP(Skill.MINING, 100),
            CollectionReward.CoinReward(100) // This will fail
        )
        every { 
            mockEconomyAPI.add(any(), any(), any(), any()) 
        } returns EconomyResult.InvalidAmount(0L)

        val result = executor.execute(mockPlayer, "Test Collection", rewards)

        assertFalse(result)
        // Only one message for the successful reward
        verify(atLeast = 1) { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `execute empty rewards list succeeds`() {
        val result = executor.execute(mockPlayer, "Test Collection", emptyList())

        assertTrue(result)
        verify(exactly = 0) { mockPlayer.sendMessage(any<String>()) }
    }

    @Test
    fun `formatReward includes skill name for SkillXP`() {
        val reward = CollectionReward.SkillXP(Skill.MINING, 100)
        every { mockI18n.t("reward.skill-xp", *anyVararg()) } returns "100 Mining XP"

        executor.execute(mockPlayer, "Test", listOf(reward))

        verify { 
            mockI18n.t("reward.skill-xp", 
                "amount" to "100", 
                "skill" to "Mining"
            ) 
        }
    }

    @Test
    fun `formatReward includes item id for RecipeUnlock`() {
        val reward = CollectionReward.RecipeUnlock(ItemId("test", "ITEM"))
        every { mockI18n.t("reward.recipe-unlock", *anyVararg()) } returns "Recipe: test:ITEM"

        executor.execute(mockPlayer, "Test", listOf(reward))

        verify { 
            mockI18n.t("reward.recipe-unlock", "item" to "test:ITEM") 
        }
    }

    @Test
    fun `formatReward includes stat info for StatBonus`() {
        val reward = CollectionReward.StatBonus(StatType.MINING_SPEED, 5.0, true)
        every { mockI18n.t("reward.stat-bonus", *anyVararg()) } returns "+5.0 Mining Speed"

        executor.execute(mockPlayer, "Test", listOf(reward))

        verify { 
            mockI18n.t("reward.stat-bonus", 
                "value" to "5.0", 
                "stat" to "MINING SPEED"
            ) 
        }
    }

    @Test
    fun `formatReward includes amount for ItemReward`() {
        mockkStatic(org.bukkit.Bukkit::class)
        every { org.bukkit.Bukkit.getPluginManager() } returns mockk {
            every { getPlugin("PurrItems") } returns null
        }

        val reward = CollectionReward.ItemReward(ItemId("test", "ITEM"), 5)
        executor.execute(mockPlayer, "Test", listOf(reward))

        // Should still call i18n even if reward fails
        verify(atLeast = 0) { mockI18n.t(any<String>(), *anyVararg()) }

        unmockkStatic(org.bukkit.Bukkit::class)
    }

    @Test
    fun `formatReward includes amount for CoinReward`() {
        val reward = CollectionReward.CoinReward(500)
        every { mockI18n.t("reward.coins", *anyVararg()) } returns "500 coins"
        every { 
            mockEconomyAPI.add(any(), any(), any(), any()) 
        } returns EconomyResult.Success(500L)

        executor.execute(mockPlayer, "Test", listOf(reward))

        verify { 
            mockI18n.t("reward.coins", "amount" to "500") 
        }
    }
}
