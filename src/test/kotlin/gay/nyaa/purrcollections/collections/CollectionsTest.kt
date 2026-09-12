package gay.nyaa.purrcollections.collections

import gay.nyaa.purrcollections.domain.CollectionType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CollectionsTest {
    @Test
    fun `all returns collections from all sources`() {
        val collections = Collections.all()

        assertTrue(collections.isNotEmpty(), "Should have at least some collections")
    }

    @Test
    fun `all collections have unique IDs`() {
        val collections = Collections.all()
        val ids = collections.map { it.id.toString() }.toSet()

        assertEquals(collections.size, ids.size, "All collections should have unique IDs")
    }

    @Test
    fun `all collections have valid types`() {
        val collections = Collections.all()

        collections.forEach { collection ->
            assertNotNull(collection.type)
            assertTrue(CollectionType.entries.contains(collection.type))
        }
    }

    @Test
    fun `all collections have display names`() {
        val collections = Collections.all()

        collections.forEach { collection ->
            assertTrue(collection.displayName.isNotBlank(), 
                "Collection ${collection.id} should have a display name")
        }
    }

    @Test
    fun `all collections have at least one tier`() {
        val collections = Collections.all()

        collections.forEach { collection ->
            assertTrue(collection.tiers.isNotEmpty(), 
                "Collection ${collection.id} should have at least one tier")
        }
    }

    @Test
    fun `all collections have either material or entity type`() {
        val collections = Collections.all()

        collections.forEach { collection ->
            assertTrue(collection.material != null || collection.entityType != null,
                "Collection ${collection.id} should have either material or entity type")
        }
    }

    @Test
    fun `collections include mining collections`() {
        val collections = Collections.all()

        val miningCollections = collections.filter { it.type == CollectionType.MINING }
        assertTrue(miningCollections.isNotEmpty(), "Should have at least one mining collection")
    }

    @Test
    fun `all tier requirements are positive and progressive`() {
        val collections = Collections.all()

        collections.forEach { collection ->
            val tiers = collection.tiers.sortedBy { it.tier }
            
            // All requirements should be positive
            tiers.forEach { tier ->
                assertTrue(tier.requiredAmount > 0, 
                    "Collection ${collection.id} tier ${tier.tier} should have positive requirement")
            }
            
            // Requirements should be progressive
            for (i in 0 until tiers.size - 1) {
                assertTrue(tiers[i].requiredAmount < tiers[i + 1].requiredAmount,
                    "Collection ${collection.id} tier requirements should be progressive")
            }
        }
    }
}
