package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.TooltipTweaksConfig.CropTooltipFortuneEntry
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.garden.FarmingFortuneDisplay
import at.hannibal2.skyhanni.features.garden.FarmingFortuneDisplay.getAbilityFortune
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenAPI.getCropType
import at.hannibal2.skyhanni.features.garden.fortuneguide.FFGuideGUI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasDivanPowderCoating
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.text.DecimalFormat
import kotlin.math.roundToInt

@SkyHanniModule
object ToolTooltipTweaksMining {

    private val config get() = GardenAPI.config.tooltipTweak

    private val tooltipMiningFortunePattern =
        "^§5§o§7Mining Fortune: §a\\+([\\d.]+)(?: §2\\(\\+\\d\\))?(?: §9\\(\\+(\\d+)\\))?$".toRegex()
    private val counterStartLine = setOf("§5§o§6Logarithmic Counter", "§5§o§6Collection Analysis")
    private val reforgeEndLine = setOf("§5§o", "§5§o§7chance for multiple crops.")
    private const val ABILITY_DESCRIPTION_START = "§5§o§7These boots gain §a+2❈ Defense"
    private const val ABILITY_DESCRIPTION_END = "§5§o§7Skill level."

    private val statFormatter = DecimalFormat("0.##")

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val itemStack = event.itemStack
        val itemLore = itemStack.getLore()
        val internalName = itemStack.getInternalName()
        val toolFortune = MiningStatsDisplay.getToolFortune(internalName)
        val reforgeName = itemStack.getReforgeName()?.firstLetterUppercase()

        val fortuneFortune = MiningStatsDisplay.getFortuneFortune(itemStack)
        val omeletteFortune = MiningStatsDisplay.getOmeletteFortune(itemStack)
        val polarvoidFortune = MiningStatsDisplay.getPolarVoidFortune(itemStack)
        val engineFortune = MiningStatsDisplay.getEngineFortune(itemStack)
        val divanPowderCoatingFortune = MiningStatsDisplay.getDivanPowderCoatingFortune(itemStack)

        //val lapidaryFortune = FarmingFortuneDisplay.getHarvestingFortune(itemStack)

        val efficiencySpeed = MiningStatsDisplay.getEfficiencySpeed(itemStack)
        val dpcMSpeed = itemStack.hasDivanPowderCoating()


        val iterator = event.toolTip.listIterator()

        var removingFarmhandDescription = false
        var removingCounterDescription = false
        var removingReforgeDescription = false
        var removingAbilityDescription = false

        for (line in iterator) {
            val match = tooltipMiningFortunePattern.matchEntire(line)?.groups
            if (match != null) {

                MiningStatsDisplay.loadFortuneLineData(itemStack, fortuneFortune)

                val displayedFortune = MiningStatsDisplay.displayedFortune
                val reforgeFortune = MiningStatsDisplay.reforgeFortune
                val gemstoneFortune = MiningStatsDisplay.gemstoneFortune
                //val baseFortune = FarmingFortuneDisplay.itemBaseFortune


                val totalFortune = displayedFortune + toolFortune

                val reforgeString = if (reforgeFortune != 0.0) " §9(+${reforgeFortune.formatStat()})" else ""
                val cropString = if (toolFortune != 0.0) " §6[+${toolFortune.roundToInt()}]" else ""

                val fortuneLine = when (config.cropTooltipFortune) {
                    CropTooltipFortuneEntry.DEFAULT ->
                        "§7Farming Fortune: §a+${displayedFortune.formatStat()}$reforgeString"
                    CropTooltipFortuneEntry.SHOW ->
                        "§7Farming Fortune: §a+${displayedFortune.formatStat()}$reforgeString$cropString"
                    else ->
                        "§7Farming Fortune: §a+${totalFortune.formatStat()}$reforgeString$cropString"
                }
                iterator.set(fortuneLine)

                if (config.fortuneTooltipKeybind.isKeyHeld()) {
                    //iterator.addStat("  §7Base: §6+", baseFortune)
                    iterator.addStat("  §7Tool: §6+", toolFortune)
                    iterator.addStat("  §7${reforgeName?.removeColor()}: §9+", reforgeFortune)
                    iterator.addStat("  §7Gemstone: §d+", gemstoneFortune)
                    iterator.addStat("  §7Fortune: §6+", fortuneFortune)
                    iterator.addStat("  §7Drill Engine: §a+", engineFortune)
                    iterator.addStat("  §7Omelette: §a+", omeletteFortune)
                    iterator.addStat("  §7Polarvoid Books: §2+", polarvoidFortune)
                    if (itemLore.contains("Of Divan")) {
                        iterator.addStat("  §7Divan's Powder Coating: §6+", divanPowderCoatingFortune)
                    }
                }
            }
            // Beware, dubious control flow beyond these lines
            if (config.compactToolTooltips || FFGuideGUI.isInGui()) {
                if (line.startsWith("§5§o§7§8Bonus ")) removingFarmhandDescription = true
                if (removingFarmhandDescription) {
                    iterator.remove()
                    removingFarmhandDescription = line != "§5§o"
                } else if (removingCounterDescription && !line.startsWith("§5§o§7You have")) {
                    iterator.remove()
                } else {
                    removingCounterDescription = false
                }
                if (counterStartLine.contains(line)) removingCounterDescription = true

                if (line == "§5§o§9Blessed Bonus") removingReforgeDescription = true
                if (removingReforgeDescription) {
                    iterator.remove()
                    removingReforgeDescription = !reforgeEndLine.contains(line)
                }
                if (line == "§5§o§9Bountiful Bonus") removingReforgeDescription = true

                if (FFGuideGUI.isInGui()) {
                    if (line.contains("Click to ") || line.contains("§7§8This item can be reforged!") || line.contains("Dyed")) {
                        iterator.remove()
                    }

                    if (line == ABILITY_DESCRIPTION_START) {
                        removingAbilityDescription = true
                    }
                    if (removingAbilityDescription) {
                        iterator.remove()
                        if (line == ABILITY_DESCRIPTION_END) {
                            removingAbilityDescription = false
                        }
                    }
                }
            }
        }
    }

    private fun Number.formatStat() = statFormatter.format(this)

    private fun MutableListIterator<String>.addStat(description: String, value: Number) {
        if (value.toDouble() != 0.0) {
            add("$description${value.formatStat()}")
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.compactToolTooltips", "garden.tooltipTweak.compactToolTooltips")
        event.move(3, "garden.fortuneTooltipKeybind", "garden.tooltipTweak.fortuneTooltipKeybind")
        event.move(3, "garden.cropTooltipFortune", "garden.tooltipTweak.cropTooltipFortune")

        event.transform(15, "garden.tooltipTweak.cropTooltipFortune") { element ->
            ConfigUtils.migrateIntToEnum(element, CropTooltipFortuneEntry::class.java)
        }
    }
}
