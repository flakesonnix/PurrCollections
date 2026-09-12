package gay.nyaa.purrcollections.db

import com.purrcore.db.Database
import gay.nyaa.purrcollections.domain.CollectionId
import gay.nyaa.purrcollections.domain.CollectionProgress
import gay.nyaa.purrcollections.domain.PlayerCollections
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID
import java.util.logging.Logger

class CollectionRepository(
    private val database: Database,
    private val logger: Logger,
) {
    fun migrate() {
        database.getConnection().use { conn ->
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS player_collections (
                    player_uuid VARCHAR(36),
                    collection_id VARCHAR(64),
                    total_gathered INT,
                    current_tier INT,
                    unlocked_at BIGINT,
                    PRIMARY KEY (player_uuid, collection_id)
                )
                """.trimIndent(),
            )

            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS collection_unlocks (
                    player_uuid VARCHAR(36),
                    unlock_type VARCHAR(32),
                    unlock_id VARCHAR(64),
                    unlocked_at BIGINT,
                    PRIMARY KEY (player_uuid, unlock_type, unlock_id)
                )
                """.trimIndent(),
            )
        }
    }

    fun loadPlayerCollections(uuid: UUID): PlayerCollections {
        database.getConnection().use { conn ->
            val stmt = conn.prepareStatement("SELECT * FROM player_collections WHERE player_uuid = ?")
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()

            val collections = mutableMapOf<CollectionId, CollectionProgress>()
            while (rs.next()) {
                val progress = mapProgress(rs)
                collections[progress.collectionId] = progress
            }

            return PlayerCollections(uuid, collections)
        }
    }

    fun saveProgress(
        uuid: UUID,
        progress: CollectionProgress,
    ) {
        database.getConnection().use { conn ->
            val stmt =
                conn.prepareStatement(
                    """
                    INSERT INTO player_collections (player_uuid, collection_id, total_gathered, current_tier, unlocked_at)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (player_uuid, collection_id) DO UPDATE SET
                        total_gathered = excluded.total_gathered,
                        current_tier = excluded.current_tier
                    """.trimIndent(),
                )
            stmt.setString(1, uuid.toString())
            stmt.setString(2, progress.collectionId.toString())
            stmt.setInt(3, progress.totalGathered)
            stmt.setInt(4, progress.currentTier)
            stmt.setLong(5, progress.unlockedAt.toEpochMilli())
            stmt.executeUpdate()
        }
    }

    fun loadRecipeUnlocks(uuid: UUID): Set<String> {
        database.getConnection().use { conn ->
            val stmt =
                conn.prepareStatement(
                    "SELECT unlock_id FROM collection_unlocks WHERE player_uuid = ? AND unlock_type = 'RECIPE'",
                )
            stmt.setString(1, uuid.toString())
            val rs = stmt.executeQuery()

            val unlocks = mutableSetOf<String>()
            while (rs.next()) {
                unlocks.add(rs.getString("unlock_id"))
            }
            return unlocks
        }
    }

    fun saveRecipeUnlock(
        uuid: UUID,
        itemId: String,
    ) {
        database.getConnection().use { conn ->
            val stmt =
                conn.prepareStatement(
                    """
                    INSERT INTO collection_unlocks (player_uuid, unlock_type, unlock_id, unlocked_at)
                    VALUES (?, 'RECIPE', ?, ?)
                    ON CONFLICT (player_uuid, unlock_type, unlock_id) DO NOTHING
                    """.trimIndent(),
                )
            stmt.setString(1, uuid.toString())
            stmt.setString(2, itemId)
            stmt.setLong(3, System.currentTimeMillis())
            stmt.executeUpdate()
        }
    }

    private fun mapProgress(rs: ResultSet): CollectionProgress =
        CollectionProgress(
            collectionId = CollectionId.parse(rs.getString("collection_id")),
            totalGathered = rs.getInt("total_gathered"),
            currentTier = rs.getInt("current_tier"),
            unlockedAt = Instant.ofEpochMilli(rs.getLong("unlocked_at")),
        )
}
