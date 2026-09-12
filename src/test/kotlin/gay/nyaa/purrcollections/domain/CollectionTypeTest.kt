package gay.nyaa.purrcollections.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CollectionTypeTest {
    @Test
    fun `all collection types have display keys`() {
        CollectionType.entries.forEach { type ->
            assertNotNull(type.displayKey)
            assert(type.displayKey.startsWith("collection.type."))
        }
    }

    @Test
    fun `collection types are properly defined`() {
        assertEquals(5, CollectionType.entries.size)
        assertEquals("collection.type.mining", CollectionType.MINING.displayKey)
        assertEquals("collection.type.farming", CollectionType.FARMING.displayKey)
        assertEquals("collection.type.foraging", CollectionType.FORAGING.displayKey)
        assertEquals("collection.type.fishing", CollectionType.FISHING.displayKey)
        assertEquals("collection.type.combat", CollectionType.COMBAT.displayKey)
    }

    @Test
    fun `collection type valueOf works`() {
        assertEquals(CollectionType.MINING, CollectionType.valueOf("MINING"))
        assertEquals(CollectionType.FARMING, CollectionType.valueOf("FARMING"))
        assertEquals(CollectionType.FORAGING, CollectionType.valueOf("FORAGING"))
        assertEquals(CollectionType.FISHING, CollectionType.valueOf("FISHING"))
        assertEquals(CollectionType.COMBAT, CollectionType.valueOf("COMBAT"))
    }
}
