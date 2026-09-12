package gay.nyaa.purrcollections.listener

import gay.nyaa.purrcollections.CollectionManager
import gay.nyaa.purrcollections.registry.CollectionRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class CombatListener(
    private val registry: CollectionRegistry,
    private val manager: CollectionManager,
) : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (killer !is Player) return

        val collection = registry.getByEntityType(event.entityType) ?: return
        manager.increment(killer, collection.id, 1)
    }
}
