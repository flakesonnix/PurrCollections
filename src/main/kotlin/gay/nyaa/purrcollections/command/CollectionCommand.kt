package gay.nyaa.purrcollections.command

import gay.nyaa.purrcollections.gui.CollectionMenuGUI
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CollectionCommand(private val gui: CollectionMenuGUI) : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can use this command")
            return true
        }

        gui.openCategoryMenu(sender)
        return true
    }
}
