package gay.nyaa.purrcollections.progression

import gay.nyaa.purrcollections.domain.*
import org.bukkit.Material
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class ProgressionCalculatorTest {
    private val testCollection =
        Collection(
            id = CollectionId("test", "TEST"),
            type = CollectionType.MINING,
            material = Material.STONE,
            displayName = "Test",
            tiers =
                listOf(
                    CollectionTier(1, 100, emptyList()),
                    CollectionTier(2, 500, emptyList()),
                    CollectionTier(3, 1000, emptyList()),
                ),
        )

    @Test
    fun `checkTierUnlock returns empty when not enough progress`() {
        val progress =
            CollectionProgress(
                collectionId = testCollection.id,
                totalGathered = 50,
                currentTier = 0,
                unlockedAt = Instant.now(),
            )

        val result = ProgressionCalculator.checkTierUnlock(progress, testCollection)
        assertEquals(0, result.unlockedTiers.size)
        assertEquals(0, result.newTier)
    }

    @Test
    fun `checkTierUnlock unlocks single tier`() {
        val progress =
            CollectionProgress(
                collectionId = testCollection.id,
                totalGathered = 150,
                currentTier = 0,
                unlockedAt = Instant.now(),
            )

        val result = ProgressionCalculator.checkTierUnlock(progress, testCollection)
        assertEquals(1, result.unlockedTiers.size)
        assertEquals(1, result.newTier)
    }

    @Test
    fun `checkTierUnlock unlocks multiple tiers at once`() {
        val progress =
            CollectionProgress(
                collectionId = testCollection.id,
                totalGathered = 600,
                currentTier = 0,
                unlockedAt = Instant.now(),
            )

        val result = ProgressionCalculator.checkTierUnlock(progress, testCollection)
        assertEquals(2, result.unlockedTiers.size)
        assertEquals(2, result.newTier)
    }

    @Test
    fun `calculateProgress returns correct values`() {
        val (current, target) = ProgressionCalculator.calculateProgress(250, testCollection, 1)
        assertEquals(250, current)
        assertEquals(500, target)
    }
}
