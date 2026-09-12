package gay.nyaa.purrcollections

import com.purrcore.i18n.I18n
import gay.nyaa.purrcollections.db.CollectionRepository
import gay.nyaa.purrcollections.domain.Collection
import gay.nyaa.purrcollections.domain.CollectionId
import gay.nyaa.purrcollections.domain.CollectionProgress
import gay.nyaa.purrcollections.domain.CollectionReward
import gay.nyaa.purrcollections.domain.CollectionTier
import gay.nyaa.purrcollections.domain.PlayerCollections
import gay.nyaa.purrcollections.progression.ProgressionCalculator
import gay.nyaa.purrcollections.registry.CollectionRegistry
import gay.nyaa.purrcollections.rewards.RewardExecutor
import gay.nyaa.purrcollections.rewards.impl.RecipeUnlockHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CollectionManager(
    private val registry: CollectionRegistry,
    private val repository: CollectionRepository,
    private val rewardExecutor: RewardExecutor,
    private val recipeUnlockHandler: RecipeUnlockHandler,
    private val i18n: I18n,
) {
    private val playerCollectionsCache = ConcurrentHashMap<UUID, PlayerCollections>()

    fun loadPlayerCollections(uuid: UUID) {
        val collections = repository.loadPlayerCollections(uuid)
        playerCollectionsCache[uuid] = collections

        val recipes = repository.loadRecipeUnlocks(uuid)
        recipeUnlockHandler.loadUnlocks(uuid, recipes)
    }

    fun unloadPlayerCollections(uuid: UUID) {
        playerCollectionsCache.remove(uuid)
        recipeUnlockHandler.clearUnlocks(uuid)
    }

    fun increment(
        player: Player,
        collectionId: CollectionId,
        amount: Int,
    ) {
        val collection = registry.get(collectionId) ?: return
        val playerCollections = playerCollectionsCache[player.uniqueId] ?: return

        val currentProgress =
            playerCollections.getProgress(collectionId) ?: CollectionProgress(
                collectionId = collectionId,
                totalGathered = 0,
                currentTier = 0,
                unlockedAt = Instant.now(),
            )

        val newProgress = currentProgress.withGathered(amount)
        val unlockResult = ProgressionCalculator.checkTierUnlock(newProgress, collection)

        if (unlockResult.unlockedTiers.isNotEmpty()) {
            val updatedProgress = newProgress.withTier(unlockResult.newTier)
            playerCollectionsCache[player.uniqueId] = playerCollections.withProgress(updatedProgress)
            repository.saveProgress(player.uniqueId, updatedProgress)

            unlockResult.unlockedTiers.forEach { tier ->
                notifyTierUnlock(player, collection, tier)
                
                // Execute rewards - check success
                val rewardsSucceeded = rewardExecutor.execute(player, collection.displayName, tier.rewards)
                
                if (rewardsSucceeded) {
                    saveRewardUnlocks(player.uniqueId, tier.rewards)
                } else {
                    player.sendMessage(i18n.t("collection.reward-failed"))
                }
            }
        } else {
            playerCollectionsCache[player.uniqueId] = playerCollections.withProgress(newProgress)
            repository.saveProgress(player.uniqueId, newProgress)
        }
    }

    fun getPlayerCollections(uuid: UUID): PlayerCollections? = playerCollectionsCache[uuid]

    fun hasUnlockedRecipe(
        uuid: UUID,
        itemId: String,
    ): Boolean = recipeUnlockHandler.hasUnlocked(uuid, itemId)

    private fun notifyTierUnlock(
        player: Player,
        collection: Collection,
        tier: CollectionTier,
    ) {
        val title =
            i18n.t(
                "collection.tier-unlocked",
                "collection" to collection.displayName,
                "tier" to tier.tier.toString(),
            )

        player.showTitle(
            Title.title(
                Component.text(title),
                Component.empty(),
                Title.Times.times(
                    Duration.ofMillis(500),
                    Duration.ofSeconds(3),
                    Duration.ofMillis(500),
                ),
            ),
        )
    }

    private fun saveRewardUnlocks(
        uuid: UUID,
        rewards: List<CollectionReward>,
    ) {
        rewards.forEach { reward ->
            if (reward is CollectionReward.RecipeUnlock) {
                repository.saveRecipeUnlock(uuid, reward.itemId.toString())
            }
        }
    }
}
