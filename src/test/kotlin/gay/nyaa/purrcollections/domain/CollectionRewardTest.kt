package gay.nyaa.purrcollections.domain

import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purrskills.skill.Skill
import gay.nyaa.purrskills.stats.StatType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionRewardTest {
    @Test
    fun `SkillXP reward creation`() {
        val reward = CollectionReward.SkillXP(Skill.MINING, 100)
        assertEquals(Skill.MINING, reward.skill)
        assertEquals(100, reward.amount)
    }

    @Test
    fun `RecipeUnlock reward creation`() {
        val itemId = ItemId("purr_items", "TEST_ITEM")
        val reward = CollectionReward.RecipeUnlock(itemId)
        assertEquals(itemId, reward.itemId)
    }

    @Test
    fun `StatBonus reward creation`() {
        val reward = CollectionReward.StatBonus(StatType.MINING_SPEED, 5.0, true)
        assertEquals(StatType.MINING_SPEED, reward.stat)
        assertEquals(5.0, reward.value)
        assertTrue(reward.permanent)
    }

    @Test
    fun `StatBonus temporary reward`() {
        val reward = CollectionReward.StatBonus(StatType.MINING_FORTUNE, 3.0, false)
        assertEquals(StatType.MINING_FORTUNE, reward.stat)
        assertEquals(3.0, reward.value)
        assertFalse(reward.permanent)
    }

    @Test
    fun `ItemReward creation`() {
        val itemId = ItemId("purr_items", "DIAMOND_PICKAXE")
        val reward = CollectionReward.ItemReward(itemId, 5)
        assertEquals(itemId, reward.itemId)
        assertEquals(5, reward.amount)
    }

    @Test
    fun `CoinReward creation`() {
        val reward = CollectionReward.CoinReward(500)
        assertEquals(500, reward.amount)
    }

    @Test
    fun `rewards are sealed class`() {
        val rewards: List<CollectionReward> = listOf(
            CollectionReward.SkillXP(Skill.MINING, 100),
            CollectionReward.RecipeUnlock(ItemId("test", "ITEM")),
            CollectionReward.StatBonus(StatType.MINING_SPEED, 5.0, true),
            CollectionReward.ItemReward(ItemId("test", "ITEM"), 1),
            CollectionReward.CoinReward(100)
        )

        assertEquals(5, rewards.size)
        assertTrue(rewards.all { it is CollectionReward })
    }
}
