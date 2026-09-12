package gay.nyaa.purrcollections.domain

data class CollectionTier(
    val tier: Int,
    val requiredAmount: Int,
    val rewards: List<CollectionReward>,
) {
    init {
        require(tier > 0) { "Tier must be positive" }
        require(requiredAmount > 0) { "Required amount must be positive" }
    }
}
