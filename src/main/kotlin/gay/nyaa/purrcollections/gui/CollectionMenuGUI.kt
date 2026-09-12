package gay.nyaa.purrcollections.gui

import com.purrcore.i18n.I18n
import gay.nyaa.purrcollections.CollectionManager
import gay.nyaa.purrcollections.domain.Collection
import gay.nyaa.purrcollections.domain.CollectionType
import gay.nyaa.purrcollections.progression.ProgressionCalculator
import gay.nyaa.purrcollections.registry.CollectionRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CollectionMenuGUI(
    private val registry: CollectionRegistry,
    private val manager: CollectionManager,
    private val i18n: I18n,
) {
    fun openCategoryMenu(player: Player) {
        val inv = Bukkit.createInventory(null, 27, Component.text("Collections"))

        inv.setItem(10, createCategoryItem(CollectionType.MINING))
        inv.setItem(12, createCategoryItem(CollectionType.FARMING))
        inv.setItem(14, createCategoryItem(CollectionType.FORAGING))
        inv.setItem(16, createCategoryItem(CollectionType.COMBAT))

        player.openInventory(inv)
    }

    fun openCollectionList(
        player: Player,
        type: CollectionType,
    ) {
        val collections = registry.getByType(type)
        val inv = Bukkit.createInventory(null, 54, Component.text(type.name))

        collections.forEachIndexed { idx, collection ->
            if (idx < 54) {
                inv.setItem(idx, createCollectionItem(player, collection))
            }
        }

        player.openInventory(inv)
    }

    private fun createCategoryItem(type: CollectionType): ItemStack {
        val material =
            when (type) {
                CollectionType.MINING -> Material.DIAMOND_PICKAXE
                CollectionType.FARMING -> Material.GOLDEN_HOE
                CollectionType.FORAGING -> Material.IRON_AXE
                CollectionType.FISHING -> Material.FISHING_ROD
                CollectionType.COMBAT -> Material.DIAMOND_SWORD
            }

        val item = ItemStack(material)
        val meta = item.itemMeta
        meta.displayName(Component.text(type.name).color(NamedTextColor.YELLOW))
        item.itemMeta = meta
        return item
    }

    private fun createCollectionItem(
        player: Player,
        collection: Collection,
    ): ItemStack {
        val playerCollections = manager.getPlayerCollections(player.uniqueId)
        val progress = playerCollections?.getProgress(collection.id)

        val material = collection.material ?: Material.PAPER
        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.displayName(Component.text(collection.displayName).color(NamedTextColor.GOLD))

        val lore = mutableListOf<Component>()
        val currentTier = progress?.currentTier ?: 0
        val totalGathered = progress?.totalGathered ?: 0

        val (current, target) = ProgressionCalculator.calculateProgress(totalGathered, collection, currentTier)
        lore.add(Component.text("Tier: $currentTier / ${collection.maxTier()}").color(NamedTextColor.GRAY))
        lore.add(Component.text("Progress: $current / $target").color(NamedTextColor.GRAY))

        meta.lore(lore)
        item.itemMeta = meta
        return item
    }
}
