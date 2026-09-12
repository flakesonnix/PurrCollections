package gay.nyaa.purrcollections.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CollectionTierTest {
    @Test
    fun `tier must be positive`() {
        assertThrows<IllegalArgumentException> {
            CollectionTier(0, 100, emptyList())
        }

        assertThrows<IllegalArgumentException> {
            CollectionTier(-1, 100, emptyList())
        }
    }

    @Test
    fun `required amount must be positive`() {
        assertThrows<IllegalArgumentException> {
            CollectionTier(1, 0, emptyList())
        }

        assertThrows<IllegalArgumentException> {
            CollectionTier(1, -100, emptyList())
        }
    }

    @Test
    fun `valid tier creation`() {
        val tier = CollectionTier(1, 100, emptyList())
        assertEquals(1, tier.tier)
        assertEquals(100, tier.requiredAmount)
        assertEquals(0, tier.rewards.size)
    }

    @Test
    fun `tier with rewards`() {
        val rewards = listOf(
            CollectionReward.CoinReward(100)
        )
        val tier = CollectionTier(1, 100, rewards)
        assertEquals(1, tier.rewards.size)
    }

    @Test
    fun `tier progression is valid`() {
        val tier1 = CollectionTier(1, 100, emptyList())
        val tier2 = CollectionTier(2, 500, emptyList())
        val tier3 = CollectionTier(3, 1000, emptyList())

        assert(tier1.requiredAmount < tier2.requiredAmount)
        assert(tier2.requiredAmount < tier3.requiredAmount)
    }
}
