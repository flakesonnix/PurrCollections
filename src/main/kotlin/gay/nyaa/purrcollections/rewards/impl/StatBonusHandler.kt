package gay.nyaa.purrcollections.rewards.impl

import gay.nyaa.purrcollections.domain.CollectionReward
import gay.nyaa.purrcollections.rewards.RewardHandler
import gay.nyaa.purrskills.stats.StatModifier
import gay.nyaa.purrskills.stats.StatSource
import gay.nyaa.purrskills.stats.StatsManager
import org.bukkit.entity.Player

class StatBonusHandler(private val statsManager: StatsManager) :
    RewardHandler<CollectionReward.StatBonus> {
    override fun handle(
        player: Player,
        reward: CollectionReward.StatBonus,
    ) {
        if (!reward.permanent) return // Only permanent bonuses for now

        // Use appropriate collection source based on stat type category
        val source =
            when {
                reward.stat.name.startsWith("MINING") -> StatSource.COLLECTION_MINING
                reward.stat.name.startsWith("FARMING") -> StatSource.COLLECTION_FARMING
                reward.stat.name.startsWith("FORAGING") -> StatSource.COLLECTION_FORAGING
                reward.stat.name.startsWith("COMBAT") -> StatSource.COLLECTION_COMBAT
                reward.stat.name.startsWith("FISHING") -> StatSource.COLLECTION_FISHING
                else -> StatSource.OTHER
            }

        val modifier = StatModifier.additive(reward.stat, reward.value, source)
        statsManager.addModifier(player.uniqueId, modifier)
    }
}
