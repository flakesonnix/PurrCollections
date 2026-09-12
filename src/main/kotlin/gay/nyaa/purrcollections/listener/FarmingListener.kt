package gay.nyaa.purrcollections.listener

import gay.nyaa.purrcollections.CollectionManager
import gay.nyaa.purrcollections.registry.CollectionRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class FarmingListener(
    private val registry: CollectionRegistry,
    private val manager: CollectionManager,
) : Listener {
    @EventHandler
    fun onCropHarvest(event: BlockBreakEvent) {
        val collection = registry.getByMaterial(event.block.type) ?: return
        if (collection.type != gay.nyaa.purrcollections.domain.CollectionType.FARMING) return
        manager.increment(event.player, collection.id, 1)
    }
}
