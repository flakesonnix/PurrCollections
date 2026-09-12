package gay.nyaa.purrcollections.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CollectionIdTest {
    @Test
    fun `valid collection id creation`() {
        val id = CollectionId("test_namespace", "TEST_KEY")
        assertEquals("test_namespace", id.namespace)
        assertEquals("TEST_KEY", id.key)
    }

    @Test
    fun `toString formats correctly`() {
        val id = CollectionId("purr_collections", "COBBLESTONE")
        assertEquals("purr_collections:COBBLESTONE", id.toString())
    }

    @Test
    fun `namespace cannot be blank`() {
        assertThrows<IllegalArgumentException> {
            CollectionId("", "TEST")
        }
    }

    @Test
    fun `key cannot be blank`() {
        assertThrows<IllegalArgumentException> {
            CollectionId("test", "")
        }
    }

    @Test
    fun `namespace must be lowercase`() {
        assertThrows<IllegalArgumentException> {
            CollectionId("Test", "TEST")
        }

        assertThrows<IllegalArgumentException> {
            CollectionId("TEST", "TEST")
        }
    }

    @Test
    fun `key must be uppercase`() {
        assertThrows<IllegalArgumentException> {
            CollectionId("test", "test")
        }

        assertThrows<IllegalArgumentException> {
            CollectionId("test", "Test")
        }
    }

    @Test
    fun `namespace allows underscores and numbers`() {
        val id = CollectionId("test_namespace_123", "TEST")
        assertEquals("test_namespace_123", id.namespace)
    }

    @Test
    fun `key allows underscores and numbers`() {
        val id = CollectionId("test", "TEST_KEY_123")
        assertEquals("TEST_KEY_123", id.key)
    }

    @Test
    fun `namespace rejects invalid characters`() {
        assertThrows<IllegalArgumentException> {
            CollectionId("test-namespace", "TEST")
        }

        assertThrows<IllegalArgumentException> {
            CollectionId("test.namespace", "TEST")
        }

        assertThrows<IllegalArgumentException> {
            CollectionId("test namespace", "TEST")
        }
    }

    @Test
    fun `key rejects invalid characters`() {
        assertThrows<IllegalArgumentException> {
            CollectionId("test", "TEST-KEY")
        }

        assertThrows<IllegalArgumentException> {
            CollectionId("test", "TEST.KEY")
        }

        assertThrows<IllegalArgumentException> {
            CollectionId("test", "TEST KEY")
        }
    }

    @Test
    fun `parse valid string`() {
        val id = CollectionId.parse("purr_collections:COBBLESTONE")
        assertEquals("purr_collections", id.namespace)
        assertEquals("COBBLESTONE", id.key)
    }

    @Test
    fun `parse with numbers`() {
        val id = CollectionId.parse("test123:TEST456")
        assertEquals("test123", id.namespace)
        assertEquals("TEST456", id.key)
    }

    @Test
    fun `parse rejects invalid format - no colon`() {
        assertThrows<IllegalArgumentException> {
            CollectionId.parse("invalid")
        }
    }

    @Test
    fun `parse rejects invalid format - multiple colons`() {
        assertThrows<IllegalArgumentException> {
            CollectionId.parse("test:key:extra")
        }
    }

    @Test
    fun `parse rejects invalid format - empty parts`() {
        assertThrows<IllegalArgumentException> {
            CollectionId.parse(":TEST")
        }

        assertThrows<IllegalArgumentException> {
            CollectionId.parse("test:")
        }
    }

    @Test
    fun `parse validates namespace format`() {
        assertThrows<IllegalArgumentException> {
            CollectionId.parse("Test:KEY")
        }
    }

    @Test
    fun `parse validates key format`() {
        assertThrows<IllegalArgumentException> {
            CollectionId.parse("test:key")
        }
    }

    @Test
    fun `data class equality works`() {
        val id1 = CollectionId("test", "KEY")
        val id2 = CollectionId("test", "KEY")
        val id3 = CollectionId("test", "OTHER")

        assertEquals(id1, id2)
        assert(id1 != id3)
    }

    @Test
    fun `data class copy works`() {
        val id1 = CollectionId("test", "KEY")
        val id2 = id1.copy(key = "OTHER")

        assertEquals("test", id2.namespace)
        assertEquals("OTHER", id2.key)
    }

    @Test
    fun `parse and toString are symmetric`() {
        val original = "purr_collections:COAL_ORE"
        val id = CollectionId.parse(original)
        val result = id.toString()

        assertEquals(original, result)
    }
}
