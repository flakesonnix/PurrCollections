package gay.nyaa.purrcollections.collections

import gay.nyaa.purrcollections.domain.*
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purrskills.skill.Skill
import gay.nyaa.purrskills.stats.StatType
import org.bukkit.Material

object MiningCollections {
    fun cobblestone() =
        Collection(
            id = CollectionId("purr_collections", "COBBLESTONE"),
            type = CollectionType.MINING,
            material = Material.COBBLESTONE,
            displayName = "Cobblestone",
            tiers =
                listOf(
                    CollectionTier(
                        1,
                        50,
                        listOf(
                            CollectionReward.SkillXP(Skill.MINING, 5),
                        ),
                    ),
                    CollectionTier(
                        2,
                        250,
                        listOf(
                            CollectionReward.SkillXP(Skill.MINING, 10),
                            CollectionReward.RecipeUnlock(ItemId("purr_items", "COBBLESTONE_GENERATOR")),
                        ),
                    ),
                    CollectionTier(
                        3,
                        1000,
                        listOf(
                            CollectionReward.StatBonus(StatType.MINING_SPEED, 5.0, permanent = true),
                        ),
                    ),
                    CollectionTier(
                        4,
                        5000,
                        listOf(
                            CollectionReward.StatBonus(StatType.MINING_FORTUNE, 2.0, permanent = true),
                        ),
                    ),
                ),
        )

    fun coal() =
        Collection(
            id = CollectionId("purr_collections", "COAL"),
            type = CollectionType.MINING,
            material = Material.COAL_ORE,
            displayName = "Coal",
            tiers =
                listOf(
                    CollectionTier(1, 50, listOf(CollectionReward.SkillXP(Skill.MINING, 10))),
                    CollectionTier(2, 250, listOf(CollectionReward.SkillXP(Skill.MINING, 25))),
                    CollectionTier(3, 1000, listOf(CollectionReward.StatBonus(StatType.MINING_FORTUNE, 3.0, true))),
                ),
        )

    fun all() = listOf(cobblestone(), coal())
}
