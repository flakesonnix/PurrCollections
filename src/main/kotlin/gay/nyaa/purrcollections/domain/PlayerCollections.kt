package gay.nyaa.purrcollections.domain

import java.time.Instant
import java.util.UUID

data class PlayerCollections(
    val playerUuid: UUID,
    val collections: Map<CollectionId, CollectionProgress>,
) {
    fun getProgress(collectionId: CollectionId): CollectionProgress? = collections[collectionId]

    fun withProgress(progress: CollectionProgress): PlayerCollections =
        copy(collections = collections + (progress.collectionId to progress))
}

data class CollectionProgress(
    val collectionId: CollectionId,
    val totalGathered: Int,
    val currentTier: Int,
    val unlockedAt: Instant,
) {
    init {
        require(totalGathered >= 0) { "Total gathered cannot be negative" }
        require(currentTier >= 0) { "Current tier cannot be negative" }
    }

    fun withGathered(amount: Int): CollectionProgress =
        copy(totalGathered = totalGathered + amount)

    fun withTier(tier: Int): CollectionProgress =
        copy(currentTier = tier)
}
