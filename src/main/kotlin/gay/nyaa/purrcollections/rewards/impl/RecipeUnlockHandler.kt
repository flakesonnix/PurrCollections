package gay.nyaa.purrcollections.rewards.impl

import gay.nyaa.purrcollections.domain.CollectionReward
import gay.nyaa.purrcollections.rewards.RewardHandler
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class RecipeUnlockHandler : RewardHandler<CollectionReward.RecipeUnlock> {
    private val unlockedRecipes = ConcurrentHashMap<UUID, MutableSet<String>>()

    override fun handle(
        player: Player,
        reward: CollectionReward.RecipeUnlock,
    ) {
        val recipes = unlockedRecipes.computeIfAbsent(player.uniqueId) { ConcurrentHashMap.newKeySet() }
        recipes.add(reward.itemId.toString())
    }

    fun hasUnlocked(
        uuid: UUID,
        itemId: String,
    ): Boolean = unlockedRecipes[uuid]?.contains(itemId) ?: false

    fun clearUnlocks(uuid: UUID) {
        unlockedRecipes.remove(uuid)
    }

    fun loadUnlocks(
        uuid: UUID,
        itemIds: Set<String>,
    ) {
        unlockedRecipes[uuid] = ConcurrentHashMap.newKeySet<String>().apply { addAll(itemIds) }
    }
}
