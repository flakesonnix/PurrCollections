package gay.nyaa.purrcollections.domain

import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purrskills.skill.Skill
import gay.nyaa.purrskills.stats.StatType

sealed class CollectionReward {
    data class SkillXP(val skill: Skill, val amount: Int) : CollectionReward()

    data class RecipeUnlock(val itemId: ItemId) : CollectionReward()

    data class StatBonus(
        val stat: StatType,
        val value: Double,
        val permanent: Boolean,
    ) : CollectionReward()

    data class ItemReward(val itemId: ItemId, val amount: Int) : CollectionReward()

    data class CoinReward(val amount: Int) : CollectionReward()
}
