package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.ItemsJson
import at.hannibal2.skyhanni.events.ItemInHandChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.events.fishing.FishingBobberCastEvent
import at.hannibal2.skyhanni.events.fishing.FishingBobberInLiquidEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyFishManager
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyFishManager.getFilletValue
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.projectile.EntityFishHook
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack

@SkyHanniModule
object FishingApi {

    /**
     * REGEX-TEST: BRONZE_HUNTER_HELMET
     * REGEX-TEST: SILVER_HUNTER_CHESTPLATE
     * REGEX-TEST: GOLD_HUNTER_LEGGINGS
     * REGEX-TEST: DIAMOND_HUNTER_BOOTS
     */
    private val trophyArmorNames by RepoPattern.pattern(
        "fishing.trophyfishing.armor",
        "(?:BRONZE|SILVER|GOLD|DIAMOND)_HUNTER_(?:HELMET|CHESTPLATE|LEGGINGS|BOOTS)",
    )

    val lavaBlocks = listOf(Blocks.lava, Blocks.flowing_lava)
    private val waterBlocks = listOf(Blocks.water, Blocks.flowing_water)

    var lastCastTime = SimpleTimeMark.farPast()
    var holdingRod = false
    var holdingLavaRod = false
    var holdingWaterRod = false

    private var lavaRods = listOf<NeuInternalName>()
    private var waterRods = listOf<NeuInternalName>()

    var bobber: EntityFishHook? = null
    var bobberHasTouchedLiquid = false

    var wearingTrophyArmor = false

    @HandleEvent(onlyOnSkyblock = true)
    fun onJoinWorld(event: EntityEnterWorldEvent<EntityFishHook>) {
        if (!holdingRod) return
        if (event.entity.angler != Minecraft.getMinecraft().thePlayer) return

        lastCastTime = SimpleTimeMark.now()
        bobber = event.entity
        bobberHasTouchedLiquid = false
        FishingBobberCastEvent(event.entity).post()
    }

    private fun resetBobber() {
        bobber = null
        bobberHasTouchedLiquid = false
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        resetBobber()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: SkyHanniTickEvent) {
        if (event.isMod(5)) {
            wearingTrophyArmor = isWearingTrophyArmor()
        }

        val bobber = bobber ?: return
        if (bobber.isDead) {
            resetBobber()
        } else {
            if (!bobberHasTouchedLiquid) {
                val isWater = when {
                    bobber.isInLava && holdingLavaRod -> false
                    bobber.isInWater && holdingWaterRod -> true
                    else -> return
                }

                bobberHasTouchedLiquid = true
                FishingBobberInLiquidEvent(bobber, isWater).post()
            }
        }
    }

    fun ItemStack.isFishingRod() = getInternalName().isFishingRod()
    fun NeuInternalName.isFishingRod() = isLavaRod() || isWaterRod()

    fun NeuInternalName.isLavaRod() = this in lavaRods

    fun NeuInternalName.isWaterRod() = this in waterRods

    fun ItemStack.isBait(): Boolean = stackSize == 1 && getItemCategoryOrNull() == ItemCategory.BAIT

    @HandleEvent
    fun onItemInHandChange(event: ItemInHandChangeEvent) {
        // TODO correct rod type per island water/lava
        holdingRod = event.newItem.isFishingRod()
        holdingLavaRod = event.newItem.isLavaRod()
        holdingWaterRod = event.newItem.isWaterRod()
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ItemsJson>("Items")
        lavaRods = data.lavaFishingRods
        waterRods = data.waterFishingRods
    }

    private fun getAllowedBlocks() = if (holdingLavaRod) lavaBlocks else waterBlocks

    fun getFilletPerTrophy(internalName: NeuInternalName): Int {
        val internal = internalName.asString()
        val trophyFishName = internal.substringBeforeLast("_")
            .replace("_", "").lowercase()
        val trophyRarityName = internal.substringAfterLast("_")
        val info = TrophyFishManager.getInfo(trophyFishName)
        val rarity = TrophyRarity.getByName(trophyRarityName) ?: TrophyRarity.BRONZE
        return info?.getFilletValue(rarity) ?: 0
    }

    fun isFishing(checkRodInHand: Boolean = true) =
        (IsFishingDetection.isFishing || (checkRodInHand && holdingRod)) && !DungeonApi.inDungeon()

    fun seaCreatureCount(entity: EntityArmorStand): Int {
        val name = entity.name
        // a dragon, will always be fought
        if (name == "Reindrake") return 0

        // a npc shop
        if (name == "§5Frosty the Snow Blaster") return 0

        if (name == "Frosty") {
            val npcLocation = LorenzVec(-1.5, 76.0, 92.5)
            if (entity.getLorenzVec().distance(npcLocation) < 1) {
                return 0
            }
        }

        val isSummonedSoul = name.contains("'")
        val hasFishingMobName = SeaCreatureManager.allFishingMobs.keys.any { name.contains(it) }
        if (!hasFishingMobName || isSummonedSoul) return 0

        if (name == "Sea Emperor" || name == "Rider of the Deep") {
            return 2
        }
        return 1
    }

    private fun isWearingTrophyArmor(): Boolean = InventoryUtils.getArmor().all {
        trophyArmorNames.matches(it?.getInternalName()?.asString())
    }
}
