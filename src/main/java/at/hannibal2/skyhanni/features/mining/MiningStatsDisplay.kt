package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getDrillUpgrades
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPolarvoidBookCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasDivanPowderCoating
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack

@SkyHanniModule
object MiningStatsDisplay {
    private val config get() = GardenAPI.config.farmingFortunes

    private val patternGroup = RepoPattern.group("mining.fortunedisplay")
    private val universalTabFortunePattern by patternGroup.pattern(
        "tablist.universal",
        " Mining Fortune: §r§6☘(?<fortune>\\d+)",
    )

    @Suppress("MaxLineLength")
    private val miningFortunePattern by RepoPattern.pattern(
        "garden.tooltip.miningfortune",
        "§7Mining Fortune: §a",
    )

    var displayedFortune = 0.0
    var reforgeFortune = 0.0
    var gemstoneFortune = 0.0
    var itemBaseFortune = 0.0
    var fortuneFortune = 0.0
    var engineFortune = 0.0
    var omeletteFortune = 0.0

    private fun isEnabled(): Boolean = GardenAPI.inGarden() && config.display

    //fun getToolFortune(tool: ItemStack?): Double = getToolFortune(tool?.getInternalName())
    fun getBaseFortune(internalName: NEUInternalName?): Double {
        if (internalName == null) return 0.0
        val string = internalName.asString()
        if (string == "THEORETICAL_HOE") {
            return 0.0
        }
        return if (string.contains("MITHRIL_PICKAXE")) {
            if (string.contains("FRACTURED")) {
                2.0
            } else if (string.contains("BANDAGED")) {
                4.0
            } else if (string.contains("REFINED")) {
                10.0
            } else {
                7.0
            }
        } else if (string.contains("TITANIUM_DRILL")) {
            if (string.endsWith("1")) {
                25.0
            } else if (string.endsWith("2")) {
                40.0
            } else if (string.endsWith("3")) {
                70.0
            } else 120.0
        } else when (string) {
            "BINGO NIMBUS_2000" -> 100.0
            "DIVANS_DRILL" -> 150.0
            else -> 0.0
        }
    }

    fun getFortuneFortune(tool: ItemStack?) = listOf(0.0, 10.0, 20.0, 30.0, 45.0)[tool?.getEnchantments()?.get("fortune") ?: 0]
    fun getOmeletteFortune(tool: ItemStack?): Double {
        val drillUpgrade = tool?.getDrillUpgrades() ?: return 0.0
        for (internalName in drillUpgrade) {
            if (internalName.itemName == "SUNNY_SIDE_GOBLIN_OMELETTE") {
                return 50.0
            } else if (internalName.itemName == "STARFALL_SEASONING") {
                return 10.0
            }
        }
        return 0.0
    }
    fun getPolarVoidFortune(tool: ItemStack?) = if ((tool?.getPolarvoidBookCount() ?: 0) > 0) 5 else 0
    fun getEngineFortune(tool: ItemStack?): Double {
        val drillUpgrade = tool?.getDrillUpgrades() ?: return 0.0
        for (internalName in drillUpgrade) {
            if (internalName.itemName.startsWith("MITHRIL")) {
                return 5.0
            } else if (internalName.itemName.startsWith("TITANIUM")) {
                return 15.0
            } else if (internalName.itemName.startsWith("RUBY")) {
                return 30.0
            } else if (internalName.itemName.startsWith("SAPPHIRE")) {
                return 50.0
            } else if (internalName.itemName.startsWith("AMBER")) {
                return 100.0
            }
        }
        return 0.0
    }

    fun getDivanPowderCoatingFortune(tool: ItemStack?) = if (tool?.hasDivanPowderCoating() == true) 10 else 0

    //fun getEfficiencySpeed(tool: ItemStack?) = (tool?.getEnchantments()?.get("efficiency") ?: 0) * 20 + 10
    //fun getOmeletteSpeed(tool: ItemStack?) : Double {
    //    val drillUpgrade = tool?.getDrillUpgrades() ?: return 0.0
    //    for (internalName in drillUpgrade) {
    //        if (internalName.itemName == "STARFALL_SEASONING") {
    //            return 25.0
    //        } else 0.0
    //    }
    //    return 0.0
    //}
    //fun getPolarVoidSpeed(tool: ItemStack?) = (tool?.getPolarvoidBookCount() ?: 0) * 10.0
    //fun getEngineSpeed(tool: ItemStack?) : Double {
    //    val drillUpgrade = tool?.getDrillUpgrades() ?: return 0.0
    //    for (internalName in drillUpgrade) {
    //        if (internalName.itemName.startsWith("MITHRIL")) {
    //            return 75.0
    //        } else if (internalName.itemName.startsWith("TITANIUM")) {
    //            return 150.0
    //        } else if (internalName.itemName.startsWith("RUBY")) {
    //            return 250.0
    //        } else if (internalName.itemName.startsWith("SAPPHIRE")) {
    //            return 400.0
    //       } else if (internalName.itemName.startsWith("AMBER")) {
    //            return 600.0
    //        }
    //    }
    //    return 0.0
    //}
    //fun getDivanPowderCoatingSpeed(tool: ItemStack?) = if (tool?.hasDivanPowderCoating() == true) 500 else 0

    fun loadFortuneLineData(tool: ItemStack?) {
        displayedFortune = 0.0
        reforgeFortune = 0.0
        gemstoneFortune = 0.0
        itemBaseFortune = 0.0
        fortuneFortune = 0.0
        engineFortune = 0.0
        omeletteFortune = 0.0

        // TODO code cleanup (after ff rework)

        val lore = tool?.getLore() ?: return
        for (line in lore) {
            miningFortunePattern.matchMatcher(line) {
                displayedFortune = group("display")?.toDouble() ?: 0.0
                reforgeFortune = groupOrNull("reforge")?.toDouble() ?: 0.0
                gemstoneFortune = groupOrNull("gemstone")?.toDouble() ?: 0.0
            } ?: continue


            //itemBaseFortune = 10.0 //if ((tool.getInternalName()).itemName.contains("JUNGLE_PICKAXE")) 5 else 0.0

            //}// else if (tool.getInternalName().contains("ZORROS_CAPE")) {

        }
    }

     */
}

//fun getCurrentFarmingFortune() = tabFortuneUniversal + tabFortuneCrop

//fun CropType.getLatestTrueFarmingFortune() = latestFF?.get(this)

/*

@HandleEvent
fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
    event.move(3, "garden.farmingFortuneDisplay", "garden.farmingFortunes.display")
    event.move(3, "garden.farmingFortuneDropMultiplier", "garden.farmingFortunes.dropMultiplier")
    event.move(3, "garden.farmingFortunePos", "garden.farmingFortunes.pos")
}


