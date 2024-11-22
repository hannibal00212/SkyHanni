package at.hannibal2.skyhanni.features.misc.pets

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.ReflectionUtils.makeAccessible
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPetExp
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PetExpTooltip {

    private val config get() = SkyHanniMod.feature.misc.pets.petExperienceToolTip
    private const val LEVEL_100_COMMON = 5_624_785
    private const val LEVEL_100_LEGENDARY = 25_353_230
    private const val LEVEL_200_LEGENDARY = 210_255_385

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onItemTooltipLow(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.petDisplay) return
        if (!KeyboardManager.isShiftKeyDown() && !config.showAlways) return

        val itemStack = event.itemStack
        val petExperience = itemStack.getPetExp()?.roundTo(1) ?: return
        val name = itemStack.name
        try {

            val index = findIndex(event.toolTip) ?: return

            val (maxLevel, maxXp) = getMaxValues(name, petExperience)

            val percentage = petExperience / maxXp
            val percentageFormat = LorenzUtils.formatPercentage(percentage)

            if (percentage < 1) {
                event.toolTip.add(index, " ")
                val progressBar = StringUtils.progressBar(percentage)
                val isBelowLegendary = itemStack.getItemRarityOrNull()?.let { it < LorenzRarity.LEGENDARY } ?: false
                val addLegendaryColor = if (isBelowLegendary) "§6" else ""
                event.toolTip.add(
                    index,
                    "$progressBar §e${petExperience.addSeparators()}§6/§e${maxXp.shortFormat()}"
                )
                event.toolTip.add(index, "§7Progress to ${addLegendaryColor}Level $maxLevel: §e$percentageFormat")
            }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Could not add pet exp tooltip",
                "itemStack" to itemStack,
                "item name" to name,
                "petExperience" to petExperience,
                "toolTip" to event.toolTip,
                "index" to findIndex(event.toolTip),
                "getLore" to itemStack.getLore(),
            )
        }
    }

    private fun findIndex(toolTip: List<String>): Int? {
        var index = toolTip.indexOfFirst { it.contains("MAX LEVEL") }
        if (index != -1) {
            return index + 2
        }

        index = toolTip.indexOfFirst { it.contains("Progress to Level") }
        if (index != -1) {

            val offset = if (PlatformUtils.isNeuLoaded() && isNeuExtendedExpEnabled) 4 else 3
            return index + offset
        }

        return null
    }

    private val isNeuExtendedExpEnabled get() = fieldPetExtendExp.get(objectNeuTooltipTweaks) as Boolean

    private val objectNeuTooltipTweaks by lazy {
        val field = NotEnoughUpdates.INSTANCE.config.javaClass.getDeclaredField("tooltipTweaks")
        field.makeAccessible().get(NotEnoughUpdates.INSTANCE.config)
    }

    private val fieldPetExtendExp by lazy {
        objectNeuTooltipTweaks.javaClass.getDeclaredField("petExtendExp").makeAccessible()
    }

    private fun getMaxValues(petName: String, petExperience: Double): Pair<Int, Int> {
        val useGoldenDragonLevels =
            petName.contains("Golden Dragon") && (!config.showGoldenDragonEgg || petExperience >= LEVEL_100_LEGENDARY)

        val maxLevel = if (useGoldenDragonLevels) 200 else 100

        val maxXp = when {
            useGoldenDragonLevels -> LEVEL_200_LEGENDARY
            petName.contains("Bingo") -> LEVEL_100_COMMON

            else -> LEVEL_100_LEGENDARY
        }

        return Pair(maxLevel, maxXp)
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "misc.petExperienceToolTip.petDisplay", "misc.pets.petExperienceToolTip.petDisplay")
        event.move(3, "misc.petExperienceToolTip.showAlways", "misc.pets.petExperienceToolTip.showAlways")
        event.move(
            3,
            "misc.petExperienceToolTip.showGoldenDragonEgg",
            "misc.pets.petExperienceToolTip.showGoldenDragonEgg"
        )
    }
}
