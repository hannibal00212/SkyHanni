package at.hannibal2.skyhanni.features.garden.inventory

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.GardenCropMilestones
import at.hannibal2.skyhanni.data.GardenCropMilestones.getCounter
import at.hannibal2.skyhanni.events.CropMilestoneUpdateEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.indexOfFirst
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.StringUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GardenCropMilestoneInventory {

    private var average = -1.0
    private val config get() = GardenAPI.config

    @SubscribeEvent
    fun onCropMilestoneUpdate(event: CropMilestoneUpdateEvent) {
        if (!config.number.averageCropMilestone) return

        val tiers = mutableListOf<Double>()
        for (cropType in CropType.entries) {
            val counter = cropType.getCounter()
            val allowOverflow = config.cropMilestones.overflow.inventoryStackSize
            val tier = GardenCropMilestones.getTierForCropCount(counter, cropType, allowOverflow)
            tiers.add(tier.toDouble())
        }
        average = (tiers.sum() / CropType.entries.size).roundTo(2)
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        average = -1.0
    }

    @SubscribeEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (average == -1.0) return

        if (event.slot.slotNumber == 38) {
            event.offsetY = -23
            event.offsetX = -50
            event.alignLeft = false
            event.stackTip = "§6Average Crop Milestone: §e$average"
        }
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.tooltipTweak.cropMilestoneTotalProgress) return

        val crop = GardenCropMilestones.getCropTypeByLore(event.itemStack) ?: return
        val tier = GardenCropMilestones.getTierForCropCount(crop.getCounter(), crop)
        if (tier >= 20) return

        val maxTier = GardenCropMilestones.getMaxTier()
        val maxCounter = GardenCropMilestones.getCropsForTier(maxTier, crop)

        val index = event.toolTip.indexOfFirst(
            "§5§o§7Rewards:",
        ) ?: return

        val counter = crop.getCounter().toDouble()
        val percentage = counter / maxCounter
        val percentageFormat = LorenzUtils.formatPercentage(percentage)

        event.toolTip.add(index, " ")
        val progressBar = StringUtils.progressBar(percentage, 19)
        event.toolTip.add(index, "$progressBar §e${counter.addSeparators()}§6/§e${maxCounter.shortFormat()}")
        event.toolTip.add(index, "§7Progress to Tier $maxTier: §e$percentageFormat")
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.numberAverageCropMilestone", "garden.number.averageCropMilestone")
        event.move(3, "garden.cropMilestoneTotalProgress", "garden.tooltipTweak.cropMilestoneTotalProgress")
    }
}
