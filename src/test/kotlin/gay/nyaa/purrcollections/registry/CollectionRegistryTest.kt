package gay.nyaa.purrcollections.registry

import gay.nyaa.purrcollections.domain.*
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CollectionRegistryTest {
    private val testCollection =
        Collection(
            id = CollectionId("test", "TEST"),
            type = CollectionType.MINING,
            material = Material.STONE,
            displayName = "Test",
            tiers = listOf(CollectionTier(1, 100, emptyList())),
        )

    @Test
    fun `register and get collection`() {
        val registry = CollectionRegistry()
        registry.register(testCollection)

        val retrieved = registry.get(testCollection.id)
        assertNotNull(retrieved)
        assertEquals(testCollection.id, retrieved.id)
    }

    @Test
    fun `getByMaterial returns correct collection`() {
        val registry = CollectionRegistry()
        registry.register(testCollection)

        val retrieved = registry.getByMaterial(Material.STONE)
        assertNotNull(retrieved)
        assertEquals(testCollection.id, retrieved.id)
    }

    @Test
    fun `getByEntityType returns correct collection`() {
        val registry = CollectionRegistry()
        val zombieCollection =
            Collection(
                id = CollectionId("test", "ZOMBIE"),
                type = CollectionType.COMBAT,
                entityType = EntityType.ZOMBIE,
                displayName = "Zombie",
                tiers = listOf(CollectionTier(1, 100, emptyList())),
            )
        registry.register(zombieCollection)

        val retrieved = registry.getByEntityType(EntityType.ZOMBIE)
        assertNotNull(retrieved)
        assertEquals(zombieCollection.id, retrieved.id)
    }

    @Test
    fun `getByType returns collections of type`() {
        val registry = CollectionRegistry()
        registry.register(testCollection)

        val miningCollections = registry.getByType(CollectionType.MINING)
        assertEquals(1, miningCollections.size)
        assertTrue(miningCollections.any { it.id == testCollection.id })
    }

    @Test
    fun `get returns null for unregistered collection`() {
        val registry = CollectionRegistry()
        val retrieved = registry.get(CollectionId("test", "UNKNOWN"))
        assertNull(retrieved)
    }
}
