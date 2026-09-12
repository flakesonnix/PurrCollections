package gay.nyaa.purrcollections.collections

import gay.nyaa.purrcollections.domain.CollectionReward
import gay.nyaa.purrcollections.domain.CollectionTier
import gay.nyaa.purrcollections.domain.CollectionType
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purrskills.skill.Skill
import gay.nyaa.purrskills.stats.StatType
import org.bukkit.Material
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MiningCollectionsTest {
    @Test
    fun `cobblestone collection is properly defined`() {
        val collection = MiningCollections.cobblestone()

        assertEquals("purr_collections:COBBLESTONE", collection.id.toString())
        assertEquals(CollectionType.MINING, collection.type)
        assertEquals(Material.COBBLESTONE, collection.material)
        assertEquals("Cobblestone", collection.displayName)
        assertEquals(4, collection.tiers.size)
    }

    @Test
    fun `cobblestone tier 1 gives skill xp`() {
        val collection = MiningCollections.cobblestone()
        val tier1 = collection.getTier(1)

        assertNotNull(tier1)
        assertEquals(50, tier1.requiredAmount)
        assertEquals(1, tier1.rewards.size)
        assertTrue(tier1.rewards[0] is CollectionReward.SkillXP)
        assertEquals(Skill.MINING, (tier1.rewards[0] as CollectionReward.SkillXP).skill)
        assertEquals(5, (tier1.rewards[0] as CollectionReward.SkillXP).amount)
    }

    @Test
    fun `cobblestone tier 2 unlocks recipe`() {
        val collection = MiningCollections.cobblestone()
        val tier2 = collection.getTier(2)

        assertNotNull(tier2)
        assertEquals(250, tier2.requiredAmount)
        assertEquals(2, tier2.rewards.size)
        
        val recipeReward = tier2.rewards.find { it is CollectionReward.RecipeUnlock }
        assertNotNull(recipeReward)
        assertEquals(
            ItemId("purr_items", "COBBLESTONE_GENERATOR"),
            (recipeReward as CollectionReward.RecipeUnlock).itemId
        )
    }

    @Test
    fun `cobblestone tier 3 gives stat bonus`() {
        val collection = MiningCollections.cobblestone()
        val tier3 = collection.getTier(3)

        assertNotNull(tier3)
        assertEquals(1000, tier3.requiredAmount)
        
        val statBonus = tier3.rewards.find { it is CollectionReward.StatBonus }
        assertNotNull(statBonus)
        assertEquals(StatType.MINING_SPEED, (statBonus as CollectionReward.StatBonus).stat)
        assertEquals(5.0, statBonus.value)
        assertTrue(statBonus.permanent)
    }

    @Test
    fun `cobblestone tier 4 gives mining fortune`() {
        val collection = MiningCollections.cobblestone()
        val tier4 = collection.getTier(4)

        assertNotNull(tier4)
        assertEquals(5000, tier4.requiredAmount)
        
        val statBonus = tier4.rewards.find { it is CollectionReward.StatBonus }
        assertNotNull(statBonus)
        assertEquals(StatType.MINING_FORTUNE, (statBonus as CollectionReward.StatBonus).stat)
        assertEquals(2.0, statBonus.value)
        assertTrue(statBonus.permanent)
    }

    @Test
    fun `cobblestone tiers are progressive`() {
        val collection = MiningCollections.cobblestone()
        val tiers = collection.tiers

        // Verify tier progression
        for (i in 0 until tiers.size - 1) {
            assertTrue(tiers[i].requiredAmount < tiers[i + 1].requiredAmount,
                "Tier ${tiers[i].tier} amount should be less than tier ${tiers[i + 1].tier}")
        }
    }

    @Test
    fun `coal collection is properly defined`() {
        val collection = MiningCollections.coal()

        assertEquals("purr_collections:COAL", collection.id.toString())
        assertEquals(CollectionType.MINING, collection.type)
        assertEquals(Material.COAL_ORE, collection.material)
        assertEquals("Coal", collection.displayName)
        assertEquals(3, collection.tiers.size)
    }

    @Test
    fun `coal tier 1 gives skill xp`() {
        val collection = MiningCollections.coal()
        val tier1 = collection.getTier(1)

        assertNotNull(tier1)
        assertEquals(50, tier1.requiredAmount)
        assertTrue(tier1.rewards[0] is CollectionReward.SkillXP)
        assertEquals(10, (tier1.rewards[0] as CollectionReward.SkillXP).amount)
    }

    @Test
    fun `coal tier 2 gives more xp`() {
        val collection = MiningCollections.coal()
        val tier1 = collection.getTier(1)
        val tier2 = collection.getTier(2)

        assertNotNull(tier1)
        assertNotNull(tier2)
        
        val tier1XP = (tier1.rewards[0] as CollectionReward.SkillXP).amount
        val tier2XP = (tier2.rewards[0] as CollectionReward.SkillXP).amount
        
        assertTrue(tier2XP > tier1XP, "Tier 2 XP should be greater than tier 1")
    }

    @Test
    fun `coal tier 3 gives fortune bonus`() {
        val collection = MiningCollections.coal()
        val tier3 = collection.getTier(3)

        assertNotNull(tier3)
        assertEquals(1000, tier3.requiredAmount)
        
        val statBonus = tier3.rewards[0] as CollectionReward.StatBonus
        assertEquals(StatType.MINING_FORTUNE, statBonus.stat)
        assertEquals(3.0, statBonus.value)
        assertTrue(statBonus.permanent)
    }

    @Test
    fun `all returns all mining collections`() {
        val collections = MiningCollections.all()

        assertEquals(2, collections.size)
        assertTrue(collections.any { it.id.toString() == "purr_collections:COBBLESTONE" })
        assertTrue(collections.any { it.id.toString() == "purr_collections:COAL" })
    }

    @Test
    fun `all collections have unique IDs`() {
        val collections = MiningCollections.all()
        val ids = collections.map { it.id }.toSet()

        assertEquals(collections.size, ids.size, "All collections should have unique IDs")
    }

    @Test
    fun `all collections have valid materials`() {
        val collections = MiningCollections.all()

        collections.forEach { collection ->
            assertNotNull(collection.material, "Collection ${collection.id} should have a material")
        }
    }

    @Test
    fun `all collections are MINING type`() {
        val collections = MiningCollections.all()

        collections.forEach { collection ->
            assertEquals(CollectionType.MINING, collection.type)
        }
    }

    @Test
    fun `all collections have at least one tier`() {
        val collections = MiningCollections.all()

        collections.forEach { collection ->
            assertTrue(collection.tiers.isNotEmpty(), "Collection ${collection.id} should have tiers")
        }
    }
}
