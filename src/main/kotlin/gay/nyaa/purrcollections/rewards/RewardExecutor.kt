package gay.nyaa.purrcollections.rewards

import com.purrcore.i18n.I18n
import gay.nyaa.purrcollections.domain.CollectionReward
import gay.nyaa.purrcollections.rewards.impl.RecipeUnlockHandler
import gay.nyaa.purrcollections.rewards.impl.SkillXPRewardHandler
import gay.nyaa.purrcollections.rewards.impl.StatBonusHandler
import org.bukkit.entity.Player

/**
 * Executes collection rewards.
 *
 * CRITICAL: Returns Boolean to indicate success/failure.
 * - true = ALL rewards were actually executed successfully
 * - false = AT LEAST ONE reward failed (not executed)
 *
 * Callers MUST check this return value to prevent claiming rewards that weren't granted.
 */
class RewardExecutor(
    private val skillXPHandler: SkillXPRewardHandler,
    private val recipeUnlockHandler: RecipeUnlockHandler,
    private val statBonusHandler: StatBonusHandler,
    private val economyAPI: gay.nyaa.purreconomy.api.PurrEconomyAPI?,
    private val i18n: I18n,
) {
    /**
     * Execute rewards for reaching a collection tier.
     *
     * @return true if ALL rewards succeeded, false if ANY failed
     */
    fun execute(
        player: Player,
        collectionName: String,
        rewards: List<CollectionReward>,
    ): Boolean {
        var allSucceeded = true

        rewards.forEach { reward ->
            val success = executeReward(player, collectionName, reward)
            if (!success) {
                allSucceeded = false
            } else {
                // Send reward message only if successful
                val rewardText = formatReward(reward)
                player.sendMessage(i18n.t("collection.reward", "reward" to rewardText))
            }
        }

        return allSucceeded
    }

    private fun executeReward(
        player: Player,
        collectionName: String,
        reward: CollectionReward,
    ): Boolean =
        when (reward) {
            is CollectionReward.SkillXP -> {
                skillXPHandler.handle(player, reward)
                true
            }
            is CollectionReward.RecipeUnlock -> {
                recipeUnlockHandler.handle(player, reward)
                true
            }
            is CollectionReward.StatBonus -> {
                statBonusHandler.handle(player, reward)
                true
            }
            is CollectionReward.ItemReward -> executeItemReward(player, reward)
            is CollectionReward.CoinReward -> executeCoinReward(player, collectionName, reward)
        }

    private fun executeCoinReward(
        player: Player,
        collectionName: String,
        reward: CollectionReward.CoinReward,
    ): Boolean {
        val api = economyAPI
        if (api == null) {
            player.sendMessage(i18n.t("collection.economy-unavailable"))
            return false
        }

        val result =
            api.add(
                player,
                reward.amount.toLong(),
                "Collection reward: $collectionName",
                gay.nyaa.purreconomy.domain.TransactionSource.COLLECTION,
            )

        return when (result) {
            is gay.nyaa.purreconomy.domain.EconomyResult.Success -> true
            else -> {
                player.sendMessage(i18n.t("collection.reward-failed"))
                false
            }
        }
    }

    private fun executeItemReward(
        player: Player,
        reward: CollectionReward.ItemReward,
    ): Boolean {
        return try {
            // Try to create item via PurrItems plugin
            val purrItemsPlugin = org.bukkit.Bukkit.getPluginManager().getPlugin("PurrItems")
            if (purrItemsPlugin != null) {
                val itemSerializerClass = Class.forName("gay.nyaa.purritems.serialization.ItemSerializer")
                val itemRegistryClass = Class.forName("gay.nyaa.purritems.registry.ItemRegistry")
                
                // Get ItemRegistry from plugin
                val pluginClass = purrItemsPlugin.javaClass
                val registryField = pluginClass.getDeclaredField("itemRegistry")
                registryField.isAccessible = true
                val itemRegistry = registryField.get(purrItemsPlugin)
                
                // Get definition from registry
                val getMethod = itemRegistryClass.getMethod("get", gay.nyaa.purritems.domain.ItemId::class.java)
                val definition = getMethod.invoke(itemRegistry, reward.itemId)
                
                if (definition != null) {
                    // Get ItemSerializer and create ItemStack
                    val serializerField = pluginClass.getDeclaredField("itemSerializer")
                    serializerField.isAccessible = true
                    val itemSerializer = serializerField.get(purrItemsPlugin)
                    
                    val createMethod = itemSerializerClass.getMethod(
                        "createItemStack",
                        gay.nyaa.purritems.domain.ItemDefinition::class.java,
                        Int::class.javaPrimitiveType
                    )
                    val itemStack = createMethod.invoke(itemSerializer, definition, reward.amount) as org.bukkit.inventory.ItemStack
                    
                    // Give to player
                    val remaining = player.inventory.addItem(itemStack)
                    if (remaining.isNotEmpty()) {
                        remaining.values.forEach { leftover ->
                            player.world.dropItemNaturally(player.location, leftover)
                        }
                    }
                    true
                } else {
                    player.sendMessage(i18n.t("collection.item-not-found", "item" to reward.itemId.toString()))
                    false
                }
            } else {
                player.sendMessage(i18n.t("collection.purritems-unavailable"))
                false
            }
        } catch (e: Exception) {
            player.sendMessage(i18n.t("collection.reward-failed"))
            false
        }
    }

    private fun formatReward(reward: CollectionReward): String =
        when (reward) {
            is CollectionReward.SkillXP ->
                i18n.t(
                    "reward.skill-xp",
                    "amount" to reward.amount.toString(),
                    "skill" to reward.skill.name.lowercase().replaceFirstChar { it.uppercase() },
                )
            is CollectionReward.RecipeUnlock ->
                i18n.t("reward.recipe-unlock", "item" to reward.itemId.toString())
            is CollectionReward.StatBonus ->
                i18n.t(
                    "reward.stat-bonus",
                    "value" to reward.value.toString(),
                    "stat" to reward.stat.name.replace("_", " "),
                )
            is CollectionReward.ItemReward ->
                i18n.t("reward.item", "amount" to reward.amount.toString(), "item" to reward.itemId.toString())
            is CollectionReward.CoinReward ->
                i18n.t("reward.coins", "amount" to reward.amount.toString())
        }
}
