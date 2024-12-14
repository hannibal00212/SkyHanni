package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.cropmilestones.NextConfig
import at.hannibal2.skyhanni.config.features.garden.cropmilestones.NextConfig.BestTypeEntry
import at.hannibal2.skyhanni.data.GardenCropMilestones
import at.hannibal2.skyhanni.data.GardenCropMilestones.getCounter
import at.hannibal2.skyhanni.data.GardenCropMilestones.isMaxed
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenAPI.addCropIcon
import at.hannibal2.skyhanni.features.garden.GardenNextJacobContest
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getSpeed
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object GardenBestCropTime {

    var display = emptyList<List<Any>>()

    private val config get() = GardenAPI.config.cropMilestones
    val timeTillNextCrop = mutableMapOf<CropType, Long>()

    fun reset() {
        timeTillNextCrop.clear()
        updateTimeTillNextCrop()
    }

    fun updateTimeTillNextCrop() {
        val useOverflow = config.overflow.bestCropTime
        for (crop in CropType.entries) {
            val speed = crop.getSpeed() ?: continue
            if (crop.isMaxed(useOverflow)) continue

            val counter = crop.getCounter()
            val currentTier = GardenCropMilestones.getTierForCropCount(counter, crop, allowOverflow = true)

            val cropsForCurrentTier = GardenCropMilestones.getCropsForTier(currentTier, crop)
            val nextTier = if (config.bestShowMaxedNeeded.get()) 46 else currentTier + 1
            val cropsForNextTier = GardenCropMilestones.getCropsForTier(nextTier, crop)

            val have = counter - cropsForCurrentTier
            val need = cropsForNextTier - cropsForCurrentTier

            val missing = need - have
            val missingTimeSeconds = missing / speed
            val millis = missingTimeSeconds * 1000
            timeTillNextCrop[crop] = millis
        }
    }

    fun drawBestDisplay(currentCrop: CropType?): List<List<Any>> {
        val newList = mutableListOf<List<Any>>()
        if (timeTillNextCrop.size < CropType.entries.size) {
            updateTimeTillNextCrop()
        }

        val gardenExp = config.next.bestType == NextConfig.BestTypeEntry.GARDEN_EXP
        val useOverflow = config.overflow.bestCropTime
        val sorted = if (gardenExp) {
            val helpMap = mutableMapOf<CropType, Long>()
            for ((crop, time) in timeTillNextCrop) {
                if (crop.isMaxed(useOverflow)) continue
                val currentTier =
                    GardenCropMilestones.getTierForCropCount(crop.getCounter(), crop, allowOverflow = true)
                val gardenExpForTier = getGardenExpForTier(currentTier + 1)
                val fakeTime = time / gardenExpForTier
                helpMap[crop] = fakeTime
            }
            helpMap.sorted()
        } else {
            timeTillNextCrop.sorted()
        }


        if (!config.next.bestHideTitle) {
            val title = if (gardenExp) "§2Garden Experience" else "§bSkyBlock Level"
            if (config.next.bestCompact) {
                newList.addAsSingletonList("§eBest Crop Time")
            } else {
                newList.addAsSingletonList("§eBest Crop Time §7($title§7)")
            }
        }

        if (!config.progress) {
            newList.addAsSingletonList("§cCrop Milestone Progress Display is disabled!")
            return newList
        }

        if (sorted.isEmpty()) {
            newList.addAsSingletonList("§cFarm crops to add them to this list!")
            return newList
        }

        var number = 0
        for (crop in sorted.keys) {
            if (crop.isMaxed(useOverflow)) continue
            val millis = timeTillNextCrop[crop]?.milliseconds ?: continue
            // TODO, change functionality to use enum rather than ordinals
            val biggestUnit = TimeUnit.entries[config.highestTimeFormat.get().ordinal]
            val duration = millis.format(biggestUnit, maxUnits = 2)
            val isCurrent = crop == currentCrop
            number++
            if (number > config.next.showOnlyBest && (!config.next.showCurrent || !isCurrent)) continue

            val list = mutableListOf<Any>()
            if (!config.next.bestCompact) {
                list.add("§7$number# ")
            }
            list.addCropIcon(crop)

            val color = if (isCurrent) "§e" else "§7"
            val contestFormat = if (GardenNextJacobContest.isNextCrop(crop)) "§n" else ""
            val currentTier = GardenCropMilestones.getTierForCropCount(crop.getCounter(), crop, allowOverflow = true)
            val nextTier = if (config.bestShowMaxedNeeded.get()) 46 else currentTier + 1

            val cropName = if (!config.next.bestCompact) crop.cropName + " " else ""
            val tier = if (!config.next.bestCompact) "$currentTier➜$nextTier§r " else ""
            list.add("$color$contestFormat$cropName$tier§b$duration")

            if (gardenExp && !config.next.bestCompact) {
                val gardenExpForTier = getGardenExpForTier(nextTier)
                list.add(" §7(§2$gardenExpForTier §7Exp)")
            }
            newList.add(list)
        }
        return newList
    }

    private fun getGardenExpForTier(gardenLevel: Int) = if (gardenLevel > 30) 300 else gardenLevel * 10

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.cropMilestoneBestType", "garden.cropMilestones.next.bestType")
        event.move(3, "garden.cropMilestoneShowOnlyBest", "garden.cropMilestones.next.showOnlyBest")
        event.move(3, "garden.cropMilestoneShowCurrent", "garden.cropMilestones.next.showCurrent")
        event.move(3, "garden.cropMilestoneBestCompact", "garden.cropMilestones.next.bestCompact")
        event.move(3, "garden.cropMilestoneBestHideTitle", "garden.cropMilestones.next.bestHideTitle")

        event.transform(17, "garden.cropMilestones.next.bestType") { element ->
            ConfigUtils.migrateIntToEnum(element, BestTypeEntry::class.java)
        }
    }
}
