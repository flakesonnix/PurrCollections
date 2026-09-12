package gay.nyaa.purrcollections.rewards

import gay.nyaa.purrcollections.domain.CollectionReward
import org.bukkit.entity.Player

interface RewardHandler<T : CollectionReward> {
    fun handle(
        player: Player,
        reward: T,
    )
}
