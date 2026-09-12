package gay.nyaa.purrcollections.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class PlayerCollectionsTest {
    private val testUuid = UUID.randomUUID()
    private val testCollectionId = CollectionId("test", "TEST")

    @Test
    fun `empty player collections`() {
        val playerCollections = PlayerCollections(testUuid, emptyMap())
        assertEquals(testUuid, playerCollections.playerUuid)
        assertEquals(0, playerCollections.collections.size)
        assertNull(playerCollections.getProgress(testCollectionId))
    }

    @Test
    fun `player collections with progress`() {
        val progress = CollectionProgress(
            collectionId = testCollectionId,
            totalGathered = 100,
            currentTier = 1,
            unlockedAt = Instant.now()
        )
        val playerCollections = PlayerCollections(testUuid, mapOf(testCollectionId to progress))

        assertEquals(1, playerCollections.collections.size)
        assertNotNull(playerCollections.getProgress(testCollectionId))
    }

    @Test
    fun `withProgress creates new instance`() {
        val original = PlayerCollections(testUuid, emptyMap())
        val progress = CollectionProgress(
            collectionId = testCollectionId,
            totalGathered = 100,
            currentTier = 1,
            unlockedAt = Instant.now()
        )

        val updated = original.withProgress(progress)

        assertNotSame(original, updated)
        assertEquals(0, original.collections.size)
        assertEquals(1, updated.collections.size)
    }

    @Test
    fun `withProgress updates existing progress`() {
        val progress1 = CollectionProgress(
            collectionId = testCollectionId,
            totalGathered = 100,
            currentTier = 1,
            unlockedAt = Instant.now()
        )
        val playerCollections = PlayerCollections(testUuid, mapOf(testCollectionId to progress1))

        val progress2 = CollectionProgress(
            collectionId = testCollectionId,
            totalGathered = 500,
            currentTier = 2,
            unlockedAt = Instant.now()
        )
        val updated = playerCollections.withProgress(progress2)

        assertEquals(1, updated.collections.size)
        assertEquals(500, updated.getProgress(testCollectionId)?.totalGathered)
        assertEquals(2, updated.getProgress(testCollectionId)?.currentTier)
    }

    @Test
    fun `multiple collection progress tracking`() {
        val collection1 = CollectionId("test", "COLLECTION1")
        val collection2 = CollectionId("test", "COLLECTION2")

        val progress1 = CollectionProgress(collection1, 100, 1, Instant.now())
        val progress2 = CollectionProgress(collection2, 200, 2, Instant.now())

        val playerCollections = PlayerCollections(
            testUuid,
            mapOf(collection1 to progress1, collection2 to progress2)
        )

        assertEquals(2, playerCollections.collections.size)
        assertEquals(100, playerCollections.getProgress(collection1)?.totalGathered)
        assertEquals(200, playerCollections.getProgress(collection2)?.totalGathered)
    }
}

class CollectionProgressTest {
    private val testCollectionId = CollectionId("test", "TEST")

    @Test
    fun `valid progress creation`() {
        val progress = CollectionProgress(
            collectionId = testCollectionId,
            totalGathered = 100,
            currentTier = 1,
            unlockedAt = Instant.now()
        )

        assertEquals(testCollectionId, progress.collectionId)
        assertEquals(100, progress.totalGathered)
        assertEquals(1, progress.currentTier)
    }

    @Test
    fun `totalGathered cannot be negative`() {
        assertThrows<IllegalArgumentException> {
            CollectionProgress(
                collectionId = testCollectionId,
                totalGathered = -1,
                currentTier = 0,
                unlockedAt = Instant.now()
            )
        }
    }

    @Test
    fun `currentTier cannot be negative`() {
        assertThrows<IllegalArgumentException> {
            CollectionProgress(
                collectionId = testCollectionId,
                totalGathered = 0,
                currentTier = -1,
                unlockedAt = Instant.now()
            )
        }
    }

    @Test
    fun `withGathered adds amount`() {
        val progress = CollectionProgress(testCollectionId, 100, 1, Instant.now())
        val updated = progress.withGathered(50)

        assertEquals(150, updated.totalGathered)
        assertEquals(1, updated.currentTier)
        assertEquals(100, progress.totalGathered) // Original unchanged
    }

    @Test
    fun `withTier updates tier`() {
        val progress = CollectionProgress(testCollectionId, 500, 1, Instant.now())
        val updated = progress.withTier(2)

        assertEquals(500, updated.totalGathered)
        assertEquals(2, updated.currentTier)
        assertEquals(1, progress.currentTier) // Original unchanged
    }

    @Test
    fun `progress is immutable`() {
        val original = CollectionProgress(testCollectionId, 100, 1, Instant.now())
        val modified1 = original.withGathered(50)
        val modified2 = original.withTier(2)

        // All should be different instances
        assertNotSame(original, modified1)
        assertNotSame(original, modified2)
        assertNotSame(modified1, modified2)

        // Original should be unchanged
        assertEquals(100, original.totalGathered)
        assertEquals(1, original.currentTier)
    }

    @Test
    fun `zero tier is valid for new collection`() {
        val progress = CollectionProgress(testCollectionId, 0, 0, Instant.now())
        assertEquals(0, progress.currentTier)
        assertEquals(0, progress.totalGathered)
    }
}
