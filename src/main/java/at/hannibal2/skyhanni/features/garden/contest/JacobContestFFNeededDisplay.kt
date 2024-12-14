package at.hannibal2.skyhanni.features.garden.contest

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.RenderItemTooltipEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.FarmingFortuneDisplay.getLatestTrueFarmingFortune
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getLatestBlocksPerSecond
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Container
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object JacobContestFFNeededDisplay {

    private val config get() = GardenAPI.config
    private var display: Renderable? = null
    private var lastToolTipTime = SimpleTimeMark.farPast()
    private val cache = mutableMapOf<ItemStack, Renderable>()

    @HandleEvent
    fun onRenderItemTooltip(event: RenderItemTooltipEvent) {
        if (!isEnabled()) return

        if (!InventoryUtils.openInventoryName().contains("Your Contests")) return
        val stack = event.stack

        val oldData = cache[stack]
        if (oldData != null) {
            display = oldData
            lastToolTipTime = SimpleTimeMark.now()
            return
        }

        val time = FarmingContestAPI.getSbTimeFor(stack.name) ?: return
        val contest = FarmingContestAPI.getContestAtTime(time) ?: return

        val newDisplay = drawDisplay(contest)
        display = newDisplay
        cache[stack] = newDisplay
        lastToolTipTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        cache.clear()
    }

    private fun drawDisplay(contest: FarmingContest) = Container.vertical {
        string("§6Minimum Farming Fortune needed")
        spacer()

        val crop = contest.crop

        horizontal {
            string("§7For this ")
            item(crop.icon)
            string("§7${crop.cropName} contest:")
        }

        for (bracket in ContestBracket.entries) {
            string(getLine(bracket, contest.brackets, crop))
        }

        spacer()

        val (size, averages) = FarmingContestAPI.calculateAverages(crop)

        horizontal {
            string("§7For the last §e$size ")
            item(crop.icon)
            string("§7${crop.cropName} contests:")
        }

        for (bracket in ContestBracket.entries) {
            string(getLine(bracket, averages, crop))
        }

        spacer()

        var blocksPerSecond = crop.getLatestBlocksPerSecond()
        if (blocksPerSecond == null) {
            horizontal {
                string("§cNo ")
                item(crop.icon)
                string("§cblocks/second data,")
            }
            string("§cassuming 19.9 instead.")
            spacer()
        } else if (blocksPerSecond < 15.0) {
            val bps = blocksPerSecond
            horizontal {
                string("§7Your latest ")
                item(crop.icon)
                string("§7blocks/second: §e${bps.roundTo(2)}")
            }
            string("§cThis is too low, showing 19.9 Blocks/second instead!")

            blocksPerSecond = 19.9
            spacer()

        }

        val trueFF = crop.getLatestTrueFarmingFortune()
        if (trueFF == null) {
            string("§cNo latest true FF saved!")
        } else {
            val farmingFortune = formatFarmingFortune(trueFF)

            horizontal {
                string("§6Your ")
                item(crop.icon)
                string("§6FF: $farmingFortune")
            }
        }

        spacer()

        if (blocksPerSecond == null || trueFF == null) {
            string("§cMissing data from above!")
        } else {
            val predictedScore =
                ((100.0 + trueFF) * blocksPerSecond * crop.baseDrops * 20 * 60 / 100).toInt().addSeparators()

            horizontal {
                string("§6Predicted ")
                item(crop.icon)
                string("§6crops: $predictedScore")
            }
        }
    }

    private fun formatFarmingFortune(farmingFortune: Double) = ceil(farmingFortune).addSeparators()

    private fun getLine(bracket: ContestBracket, map: Map<ContestBracket, Int>, crop: CropType): String {
        val counter = map[bracket] ?: return " ${bracket.displayName}§f: §8Not found!"
        val blocksPerSecond = crop.getRealBlocksPerSecond()
        val cropsPerSecond = counter.toDouble() / blocksPerSecond / 60
        val farmingFortune = (cropsPerSecond * 100 / 20 / crop.baseDrops) - 100
        val format = formatFarmingFortune(farmingFortune.coerceAtLeast(0.0))
        return " ${bracket.displayName}§f: §6$format FF §7(${counter.addSeparators()} crops)"
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!FarmingContestAPI.inInventory) return
        if (lastToolTipTime.passedSince() > 200.milliseconds) return
        config.farmingFortuneForContestPos.renderRenderable(display, posLabel = "Jacob Contest Crop Data")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.farmingFortuneForContest
}

private fun CropType.getRealBlocksPerSecond(): Double {
    val bps = getLatestBlocksPerSecond() ?: 20.0
    return if (bps < 15.0) {
        return 19.9
    } else bps
}
