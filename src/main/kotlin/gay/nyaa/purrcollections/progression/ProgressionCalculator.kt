package gay.nyaa.purrcollections.progression

import gay.nyaa.purrcollections.domain.Collection
import gay.nyaa.purrcollections.domain.CollectionProgress
import gay.nyaa.purrcollections.domain.CollectionTier

object ProgressionCalculator {
    data class TierUnlockResult(
        val unlockedTiers: List<CollectionTier>,
        val newTier: Int,
    )

    fun checkTierUnlock(
        progress: CollectionProgress,
        collection: Collection,
    ): TierUnlockResult {
        val unlockedTiers = mutableListOf<CollectionTier>()
        var currentTier = progress.currentTier
        val totalGathered = progress.totalGathered

        // Check all tiers up to max
        for (tier in (currentTier + 1)..collection.maxTier()) {
            val tierDef = collection.getTier(tier) ?: break
            if (totalGathered >= tierDef.requiredAmount) {
                unlockedTiers.add(tierDef)
                currentTier = tier
            } else {
                break
            }
        }

        return TierUnlockResult(unlockedTiers, currentTier)
    }

    fun calculateProgress(
        totalGathered: Int,
        collection: Collection,
        currentTier: Int,
    ): Pair<Int, Int> {
        val nextTier = collection.getNextTier(currentTier) ?: return totalGathered to 0
        return totalGathered to nextTier.requiredAmount
    }
}
