package gay.nyaa.purrcollections.db

import com.purrcore.db.Database
import gay.nyaa.purrcollections.domain.CollectionId
import gay.nyaa.purrcollections.domain.CollectionProgress
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.*
import java.time.Instant
import java.util.UUID
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CollectionRepositoryTest {
    private val mockDatabase = mockk<Database>()
    private val mockLogger = mockk<Logger>(relaxed = true)
    private val mockConnection = mockk<Connection>(relaxed = true)
    private val mockStatement = mockk<Statement>(relaxed = true)
    private val mockPreparedStatement = mockk<PreparedStatement>(relaxed = true)
    private val mockResultSet = mockk<ResultSet>(relaxed = true)

    private lateinit var repository: CollectionRepository

    private val testUuid = UUID.randomUUID()
    private val testCollectionId = CollectionId("test", "TEST")

    @BeforeEach
    fun setup() {
        every { mockDatabase.getConnection() } returns mockConnection
        every { mockConnection.createStatement() } returns mockStatement
        every { mockConnection.prepareStatement(any()) } returns mockPreparedStatement
        every { mockStatement.execute(any()) } returns true
        every { mockPreparedStatement.executeUpdate() } returns 1
        every { mockPreparedStatement.executeQuery() } returns mockResultSet
        every { mockResultSet.next() } returns false

        repository = CollectionRepository(mockDatabase, mockLogger)
    }

    @Test
    fun `migrate creates tables`() {
        repository.migrate()

        verify(exactly = 2) { mockStatement.execute(any()) }
        verify { mockConnection.close() }
    }

    @Test
    fun `loadPlayerCollections returns empty for new player`() {
        every { mockResultSet.next() } returns false

        val result = repository.loadPlayerCollections(testUuid)

        assertEquals(testUuid, result.playerUuid)
        assertEquals(0, result.collections.size)
        verify { mockPreparedStatement.setString(1, testUuid.toString()) }
        verify { mockConnection.close() }
    }

    @Test
    fun `loadPlayerCollections returns existing progress`() {
        every { mockResultSet.next() } returnsMany listOf(true, false)
        every { mockResultSet.getString("collection_id") } returns "test:TEST"
        every { mockResultSet.getInt("total_gathered") } returns 100
        every { mockResultSet.getInt("current_tier") } returns 1
        every { mockResultSet.getLong("unlocked_at") } returns System.currentTimeMillis()

        val result = repository.loadPlayerCollections(testUuid)

        assertEquals(testUuid, result.playerUuid)
        assertEquals(1, result.collections.size)
        assertNotNull(result.getProgress(testCollectionId))
        assertEquals(100, result.getProgress(testCollectionId)?.totalGathered)
        assertEquals(1, result.getProgress(testCollectionId)?.currentTier)
    }

    @Test
    fun `loadPlayerCollections handles multiple collections`() {
        val collection1 = CollectionId("test", "COLLECTION1")
        val collection2 = CollectionId("test", "COLLECTION2")

        every { mockResultSet.next() } returnsMany listOf(true, true, false)
        every { mockResultSet.getString("collection_id") } returnsMany listOf(
            "test:COLLECTION1",
            "test:COLLECTION2"
        )
        every { mockResultSet.getInt("total_gathered") } returnsMany listOf(100, 200)
        every { mockResultSet.getInt("current_tier") } returnsMany listOf(1, 2)
        every { mockResultSet.getLong("unlocked_at") } returns System.currentTimeMillis()

        val result = repository.loadPlayerCollections(testUuid)

        assertEquals(2, result.collections.size)
        assertEquals(100, result.getProgress(collection1)?.totalGathered)
        assertEquals(200, result.getProgress(collection2)?.totalGathered)
    }

    @Test
    fun `saveProgress inserts new progress`() {
        val progress = CollectionProgress(
            collectionId = testCollectionId,
            totalGathered = 100,
            currentTier = 1,
            unlockedAt = Instant.now()
        )

        repository.saveProgress(testUuid, progress)

        verify { mockPreparedStatement.setString(1, testUuid.toString()) }
        verify { mockPreparedStatement.setString(2, testCollectionId.toString()) }
        verify { mockPreparedStatement.setInt(3, 100) }
        verify { mockPreparedStatement.setInt(4, 1) }
        verify { mockPreparedStatement.setLong(5, any()) }
        verify { mockPreparedStatement.executeUpdate() }
        verify { mockConnection.close() }
    }

    @Test
    fun `saveProgress updates existing progress`() {
        val progress = CollectionProgress(
            collectionId = testCollectionId,
            totalGathered = 500,
            currentTier = 2,
            unlockedAt = Instant.now()
        )

        repository.saveProgress(testUuid, progress)

        verify { mockPreparedStatement.executeUpdate() }
        verify { mockConnection.close() }
    }

    @Test
    fun `loadRecipeUnlocks returns empty for new player`() {
        every { mockResultSet.next() } returns false

        val result = repository.loadRecipeUnlocks(testUuid)

        assertEquals(0, result.size)
        verify { mockPreparedStatement.setString(1, testUuid.toString()) }
        verify { mockConnection.close() }
    }

    @Test
    fun `loadRecipeUnlocks returns existing unlocks`() {
        every { mockResultSet.next() } returnsMany listOf(true, true, false)
        every { mockResultSet.getString("unlock_id") } returnsMany listOf("test:ITEM1", "test:ITEM2")

        val result = repository.loadRecipeUnlocks(testUuid)

        assertEquals(2, result.size)
        assertTrue(result.contains("test:ITEM1"))
        assertTrue(result.contains("test:ITEM2"))
        verify { mockConnection.close() }
    }

    @Test
    fun `saveRecipeUnlock inserts new unlock`() {
        repository.saveRecipeUnlock(testUuid, "test:ITEM")

        verify { mockPreparedStatement.setString(1, testUuid.toString()) }
        verify { mockPreparedStatement.setString(2, "test:ITEM") }
        verify { mockPreparedStatement.setLong(3, any()) }
        verify { mockPreparedStatement.executeUpdate() }
        verify { mockConnection.close() }
    }

    @Test
    fun `saveRecipeUnlock handles conflict (idempotency)`() {
        // Should use ON CONFLICT DO NOTHING
        repository.saveRecipeUnlock(testUuid, "test:ITEM")

        verify { mockPreparedStatement.executeUpdate() }
    }

    @Test
    fun `repository handles database connection properly`() {
        val progress = CollectionProgress(testCollectionId, 100, 1, Instant.now())

        repository.saveProgress(testUuid, progress)

        verify { mockDatabase.getConnection() }
        verify { mockConnection.close() }
    }

    @Test
    fun `migrate SQL contains expected table structure`() {
        val capturedStatements = mutableListOf<String>()
        every { mockStatement.execute(capture(capturedStatements)) } returns true

        repository.migrate()

        assertTrue(capturedStatements.any { it.contains("player_collections") })
        assertTrue(capturedStatements.any { it.contains("collection_unlocks") })
        assertTrue(capturedStatements.any { it.contains("player_uuid") })
        assertTrue(capturedStatements.any { it.contains("collection_id") })
        assertTrue(capturedStatements.any { it.contains("total_gathered") })
        assertTrue(capturedStatements.any { it.contains("current_tier") })
        assertTrue(capturedStatements.any { it.contains("unlocked_at") })
    }
}
