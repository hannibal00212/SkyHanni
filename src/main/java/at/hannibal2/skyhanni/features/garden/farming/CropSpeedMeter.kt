package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.data.GardenCropMilestones.getCounter
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.CropMilestoneUpdateEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CropSpeedMeter {

    private var display = emptyList<String>()
    private var currentCrop: CropType? = null
    private var currentBlocks = 0
    private var snapshot = emptyList<String>()

    var enabled = false
    private var startCrops = mapOf<CropType, Long>()

    @SubscribeEvent
    fun onCropClick(event: CropClickEvent) {
        if (!isEnabled()) return
        if (startCrops.isEmpty()) return

        val crop = event.crop
        if (currentCrop != crop) {
            currentCrop = crop
            currentBlocks = 0
            snapshot = emptyList()
        }
        breakBlock()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(15)) return

        updateDisplay()
    }

    private fun updateDisplay() {
        display = renderDisplay()
    }

    private fun renderDisplay(): MutableList<String> {
        val list = mutableListOf<String>()
        list.add("§7Crop Speed Meter")
        if (startCrops.isEmpty()) {
            list.add("§cOpen §e/cropmilestones §cto start!")
            return list
        }

        if (currentCrop == null) {
            list.add("§cStart breaking blocks!")
            return list
        }
        currentCrop?.let {
            list.add(" §7Current ${it.cropName} counter: §e${currentBlocks.addSeparators()}")
        }

        if (snapshot.isNotEmpty()) {
            list += snapshot
        } else {
            list.add("§cOpen §e/cropmilestones §cagain to calculate!")
        }

        return list
    }

    @SubscribeEvent
    fun onCropMilestoneUpdate(event: CropMilestoneUpdateEvent) {
        if (!isEnabled()) return
        val counters = mutableMapOf<CropType, Long>()
        for (cropType in CropType.entries) {
            counters[cropType] = cropType.getCounter()
        }
        if (startCrops.isEmpty()) {
            startCrops = counters
            currentCrop = null
            snapshot = emptyList()
        } else {
            currentCrop?.let {
                val crops = it.getCounter() - startCrops[it]!!
                val blocks = currentBlocks
                val cropsPerBlocks = (crops.toDouble() / blocks.toDouble()).roundTo(3)

                val list = mutableListOf<String>()
                list.add("")
                list.add("§6Calculation results")
                list.add(" §7Crops collected: " + crops.addSeparators())
                list.add(" §7Blocks broken: " + blocks.addSeparators())
                list.add(" §7Crops per Block: " + cropsPerBlocks.addSeparators())

                val baseDrops = it.baseDrops
                val farmingFortune = (cropsPerBlocks * 100 / baseDrops).roundTo(3)

                list.add(" §7Calculated farming Fortune: §e" + farmingFortune.addSeparators())
                list.add("§cOpen /cropmilestones again to recalculate!")

                snapshot = list
                updateDisplay()
            }
        }
    }

    private fun breakBlock() {
        currentBlocks++
    }

    fun toggle() {
        enabled = !enabled
        ChatUtils.chat("Crop Speed Meter " + if (enabled) "§aEnabled" else "§cDisabled")
        startCrops = emptyMap()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        GardenAPI.config.cropSpeedMeterPos.renderStrings(display, posLabel = "Crop Speed Meter")
    }

    fun isEnabled() = enabled && GardenAPI.inGarden()
}
