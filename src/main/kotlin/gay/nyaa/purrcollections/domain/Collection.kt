package gay.nyaa.purrcollections.domain

import org.bukkit.Material
import org.bukkit.entity.EntityType

data class Collection(
    val id: CollectionId,
    val type: CollectionType,
    val material: Material? = null,
    val entityType: EntityType? = null,
    val displayName: String,
    val tiers: List<CollectionTier>,
) {
    init {
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(material != null || entityType != null) { "Must specify material or entityType" }
        require(tiers.isNotEmpty()) { "Must have at least one tier" }
        require(tiers.sortedBy { it.tier } == tiers) { "Tiers must be sorted by tier number" }
    }

    fun getTier(tierNumber: Int): CollectionTier? = tiers.find { it.tier == tierNumber }

    fun getNextTier(currentTier: Int): CollectionTier? = tiers.find { it.tier == currentTier + 1 }

    fun maxTier(): Int = tiers.maxOf { it.tier }
}
