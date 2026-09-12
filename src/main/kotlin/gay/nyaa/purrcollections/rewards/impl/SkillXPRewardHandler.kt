package gay.nyaa.purrcollections.rewards.impl

import gay.nyaa.purrcollections.domain.CollectionReward
import gay.nyaa.purrcollections.rewards.RewardHandler
import gay.nyaa.purrskills.SkillManager
import org.bukkit.entity.Player

class SkillXPRewardHandler(private val skillManager: SkillManager) :
    RewardHandler<CollectionReward.SkillXP> {
    override fun handle(
        player: Player,
        reward: CollectionReward.SkillXP,
    ) {
        skillManager.awardSkillXp(player, reward.skill, reward.amount)
    }
}
