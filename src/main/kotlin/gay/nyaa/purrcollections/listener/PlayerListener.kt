package gay.nyaa.purrcollections.listener

import gay.nyaa.purrcollections.CollectionManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(private val manager: CollectionManager) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        manager.loadPlayerCollections(event.player.uniqueId)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        manager.unloadPlayerCollections(event.player.uniqueId)
    }
}
