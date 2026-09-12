package gay.nyaa.purrcollections.domain

import org.bukkit.Material
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CollectionTest {
    @Test
    fun `collection requires material or entityType`() {
        assertThrows<IllegalArgumentException> {
            Collection(
                id = CollectionId("test", "TEST"),
                type = CollectionType.MINING,
                displayName = "Test",
                tiers = listOf(CollectionTier(1, 100, emptyList())),
            )
        }
    }

    @Test
    fun `getTier returns correct tier`() {
        val collection =
            Collection(
                id = CollectionId("test", "TEST"),
                type = CollectionType.MINING,
                material = Material.STONE,
                displayName = "Test",
                tiers =
                    listOf(
                        CollectionTier(1, 100, emptyList()),
                        CollectionTier(2, 500, emptyList()),
                    ),
            )

        assertNotNull(collection.getTier(1))
        assertNotNull(collection.getTier(2))
        assertNull(collection.getTier(3))
    }

    @Test
    fun `maxTier returns highest tier`() {
        val collection =
            Collection(
                id = CollectionId("test", "TEST"),
                type = CollectionType.MINING,
                material = Material.STONE,
                displayName = "Test",
                tiers =
                    listOf(
                        CollectionTier(1, 100, emptyList()),
                        CollectionTier(2, 500, emptyList()),
                        CollectionTier(3, 1000, emptyList()),
                    ),
            )

        assertEquals(3, collection.maxTier())
    }
}
