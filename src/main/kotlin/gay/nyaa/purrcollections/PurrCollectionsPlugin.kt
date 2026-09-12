package gay.nyaa.purrcollections

import com.purrcore.PurrCorePlugin
import gay.nyaa.purrcollections.collections.Collections
import gay.nyaa.purrcollections.command.CollectionCommand
import gay.nyaa.purrcollections.db.CollectionRepository
import gay.nyaa.purrcollections.gui.CollectionMenuGUI
import gay.nyaa.purrcollections.listener.CombatListener
import gay.nyaa.purrcollections.listener.FarmingListener
import gay.nyaa.purrcollections.listener.MiningListener
import gay.nyaa.purrcollections.listener.PlayerListener
import gay.nyaa.purrcollections.registry.CollectionRegistry
import gay.nyaa.purrcollections.rewards.RewardExecutor
import gay.nyaa.purrcollections.rewards.impl.RecipeUnlockHandler
import gay.nyaa.purrcollections.rewards.impl.SkillXPRewardHandler
import gay.nyaa.purrcollections.rewards.impl.StatBonusHandler
import gay.nyaa.purrskills.PurrSkillsPlugin
import org.bukkit.plugin.java.JavaPlugin

class PurrCollectionsPlugin : JavaPlugin() {
    private lateinit var collectionManager: CollectionManager
    private lateinit var recipeUnlockHandler: RecipeUnlockHandler

    override fun onEnable() {
        val purrCore = server.pluginManager.getPlugin("PurrCore") as? PurrCorePlugin
        if (purrCore == null) {
            logger.severe("PurrCore not found! Disabling...")
            server.pluginManager.disablePlugin(this)
            return
        }

        val purrSkills = server.pluginManager.getPlugin("PurrSkills") as? PurrSkillsPlugin
        if (purrSkills == null) {
            logger.severe("PurrSkills not found! Disabling...")
            server.pluginManager.disablePlugin(this)
            return
        }

        val database = purrCore.database
        val i18n = purrCore.i18n
        val skillManager = purrSkills.skillManager
        val statsManager = purrSkills.statsManager

        val repository = CollectionRepository(database, logger)
        repository.migrate()

        val registry = CollectionRegistry()
        Collections.all().forEach { registry.register(it) }
        logger.info("Registered ${registry.size()} collections")

        val skillXPHandler = SkillXPRewardHandler(skillManager)
        recipeUnlockHandler = RecipeUnlockHandler()
        val statBonusHandler = StatBonusHandler(statsManager)

        // Try to get PurrEconomy API (soft-depend)
        val economyAPI = tryGetEconomyAPI()

        val rewardExecutor =
            RewardExecutor(
                skillXPHandler,
                recipeUnlockHandler,
                statBonusHandler,
                economyAPI,
                i18n,
            )

        collectionManager = CollectionManager(registry, repository, rewardExecutor, recipeUnlockHandler, i18n)

        server.pluginManager.registerEvents(PlayerListener(collectionManager), this)
        server.pluginManager.registerEvents(MiningListener(registry, collectionManager), this)
        server.pluginManager.registerEvents(FarmingListener(registry, collectionManager), this)
        server.pluginManager.registerEvents(CombatListener(registry, collectionManager), this)

        val gui = CollectionMenuGUI(registry, collectionManager, i18n)
        getCommand("collection")?.setExecutor(CollectionCommand(gui))

        logger.info("PurrCollections enabled!")
    }

    override fun onDisable() {
        logger.info("PurrCollections disabled!")
    }

    fun getCollectionManager(): CollectionManager = collectionManager

    fun getRecipeUnlockHandler(): RecipeUnlockHandler = recipeUnlockHandler

    /**
     * Try to get PurrEconomy API (soft-depend).
     * Returns null if not available.
     */
    private fun tryGetEconomyAPI(): gay.nyaa.purreconomy.api.PurrEconomyAPI? {
        val economyPlugin = server.pluginManager.getPlugin("PurrEconomy") ?: run {
            logger.info("PurrEconomy not found - coin rewards will be unavailable")
            return null
        }

        if (!economyPlugin.isEnabled) {
            logger.warning("PurrEconomy found but not enabled - coin rewards will be unavailable")
            return null
        }

        return try {
            val plugin = economyPlugin as gay.nyaa.purreconomy.PurrEconomyPlugin
            logger.info("PurrEconomy integration enabled")
            plugin.getAPI()
        } catch (e: Exception) {
            logger.warning("Failed to get PurrEconomy API: ${e.message}")
            null
        }
    }
}
