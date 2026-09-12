package gay.nyaa.purrcollections.integration

import gay.nyaa.purrcollections.domain.*
import gay.nyaa.purrcollections.registry.CollectionRegistry
import gay.nyaa.purrcollections.db.CollectionRepository
import gay.nyaa.purrcollections.progression.ProgressionCalculator
import org.bukkit.Material
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for collection tier unlock idempotency.
 * Critical: Ensures rewards are granted exactly once even after server restarts/crashes.
 */
class CollectionIdempotencyTest {
    
    @Test
    fun `tier unlock is idempotent - no double reward on crash`() {
        // Simulate: Player reaches tier threshold → Tier unlocked → Server crashes before full commit
        val collection = createTestCollection()
        
        val progress1 = CollectionProgress(
            collectionId = collection.id,
            totalGathered = 100,  // Exactly tier 1 threshold
            currentTier = 0,
            unlockedAt = Instant.now()
        )
        
        val result1 = ProgressionCalculator.checkTierUnlock(progress1, collection)
        assertEquals(1, result1.unlockedTiers.size)
        assertEquals(1, result1.newTier)
        
        // Simulate second check after restart (progress already at tier 1)
        val progress2 = progress1.withTier(1)
        val result2 = ProgressionCalculator.checkTierUnlock(progress2, collection)
        assertEquals(0, result2.unlockedTiers.size)  // No new unlocks
        assertEquals(1, result2.newTier)  // Still tier 1
    }
    
    @Test
    fun `multiple tier unlocks at once are all granted`() {
        val collection = createTestCollection()
        
        // Player collects 600 items while offline → should unlock tier 1, 2, 3
        val progress = CollectionProgress(
            collectionId = collection.id,
            totalGathered = 600,
            currentTier = 0,
            unlockedAt = Instant.now()
        )
        
        val result = ProgressionCalculator.checkTierUnlock(progress, collection)
        assertEquals(3, result.unlockedTiers.size)
        assertEquals(3, result.newTier)
    }
    
    @Test
    fun `progression stops at max tier`() {
        val collection = createTestCollection()
        
        val progress = CollectionProgress(
            collectionId = collection.id,
            totalGathered = 10000,  // Way beyond max tier
            currentTier = 0,
            unlockedAt = Instant.now()
        )
        
        val result = ProgressionCalculator.checkTierUnlock(progress, collection)
        assertEquals(4, result.unlockedTiers.size)  // Only 4 tiers exist
        assertEquals(4, result.newTier)
    }
    
    private fun createTestCollection() = Collection(
        id = CollectionId("test", "COBBLESTONE"),
        type = CollectionType.MINING,
        material = Material.COBBLESTONE,
        displayName = "Cobblestone",
        tiers = listOf(
            CollectionTier(1, 100, emptyList()),
            CollectionTier(2, 250, emptyList()),
            CollectionTier(3, 500, emptyList()),
            CollectionTier(4, 1000, emptyList()),
        )
    )
}
