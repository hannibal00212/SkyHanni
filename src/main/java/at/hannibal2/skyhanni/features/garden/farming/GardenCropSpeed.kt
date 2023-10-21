package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.GardenCropMilestones.getCounter
import at.hannibal2.skyhanni.data.GardenCropMilestones.setCounter
import at.hannibal2.skyhanni.data.MayorElection
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.PreProfileSwitchEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils.editCopy
import at.hannibal2.skyhanni.utils.jsonobjects.DicerDropsJson
import at.hannibal2.skyhanni.utils.jsonobjects.DicerDropsJson.DicerType
import kotlin.concurrent.fixedRateTimer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GardenCropSpeed {
    private val config get() = SkyHanniMod.feature.garden
    private val cropsPerSecond: MutableMap<CropType, Int>? get() = GardenAPI.config?.cropsPerSecond
    private val latestBlocksPerSecond: MutableMap<CropType, Double>? get() = GardenAPI.config?.latestBlocksPerSecond

    var lastBrokenCrop: CropType? = null
    var lastBrokenTime = 0L

    var averageBlocksPerSecond = 0.0

    private var blocksSpeedList = listOf<Int>()
    private var blocksBroken = 0
    private var secondsStopped = 0

    private val melonDicer = mutableListOf<Double>()
    private val pumpkinDicer = mutableListOf<Double>()
    var latestMelonDicer = 0.0
    var latestPumpkinDicer = 0.0


    init {
        fixedRateTimer(name = "skyhanni-crop-milestone-speed", period = 1000L) {
            if (isEnabled()) {
                if (GardenAPI.mushroomCowPet) {
                    CropType.MUSHROOM.setCounter(CropType.MUSHROOM.getCounter() + blocksBroken)
                }
                checkSpeed()
                update()
            }
        }
    }

    @SubscribeEvent
    fun onPreProfileSwitch(event: PreProfileSwitchEvent) {
        lastBrokenCrop = null
    }

    @SubscribeEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        if (isEnabled()) {
            resetSpeed()
            update()
        }
    }

    private fun update() {
        GardenCropMilestoneDisplay.update()
    }

    @SubscribeEvent
    fun onBlockClick(event: CropClickEvent) {
        if (event.clickType != ClickType.LEFT_CLICK) return

        lastBrokenCrop = event.crop
        lastBrokenTime = System.currentTimeMillis()
        blocksBroken++
    }

    private fun checkSpeed() {
        val blocksBroken = blocksBroken.coerceAtMost(20)
        this.blocksBroken = 0

        if (blocksBroken == 0) {
            if (blocksSpeedList.isEmpty()) return
            secondsStopped++
        } else {
            if (secondsStopped >= config.cropMilestones.blocksBrokenResetTime) {
                resetSpeed()
            }
            blocksSpeedList = blocksSpeedList.editCopy {
                while (secondsStopped > 0) {
                    this.add(0)
                    secondsStopped -= 1
                }
                this.add(blocksBroken)
                if (this.size == 2) {
                    this.removeFirst()
                    this.add(blocksBroken)
                }
            }
            averageBlocksPerSecond = if (blocksSpeedList.size > 1) {
                blocksSpeedList.dropLast(1).average()
            } else 0.0
            GardenAPI.getCurrentlyFarmedCrop()?.let {
                val heldTool = InventoryUtils.getItemInHand()
                val toolName = heldTool?.getInternalName()?.asString()
                if (toolName?.contains("DICER") == true) {
                    val lastCrop = lastBrokenCrop?.cropName?.lowercase() ?: "NONE"
                    if (toolName.lowercase().contains(lastCrop)) {
                        val tier = when {
                            toolName.endsWith("DICER") -> 0
                            toolName.endsWith("DICER_2") -> 1
                            toolName.endsWith("DICER_3") -> 2
                            else -> -1
                        }
                        if (tier != -1 && melonDicer.isNotEmpty() && pumpkinDicer.isNotEmpty()) {
                            if (it == CropType.MELON) {
                                latestMelonDicer = melonDicer[tier]
                            } else if (it == CropType.PUMPKIN) {
                                latestPumpkinDicer = pumpkinDicer[tier]
                            }
                        }
                    }
                }
                if (averageBlocksPerSecond > 1) {
                    latestBlocksPerSecond?.put(it, averageBlocksPerSecond)
                }
            }
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        try {
            val data = event.getConstant<DicerDropsJson>("DicerDrops") ?: throw Exception()
            calculateAverageDicer(melonDicer, data.MELON)
            calculateAverageDicer(pumpkinDicer, data.PUMPKIN)
            SkyHanniMod.repo.successfulConstants.add("DicerDrops")
        } catch (_: Exception) {
            SkyHanniMod.repo.unsuccessfulConstants.add("DicerDrops")
        }
    }

    private fun calculateAverageDicer(dicerList: MutableList<Double>, data: DicerType) {
        dicerList.clear()
        for (dropType in data.drops) {
            val chance = dropType.chance / data.totalChance.toDouble()
            for ((index, amount) in dropType.amount.withIndex()) {
                val dropAmount = amount * chance
                if (index < dicerList.size) {
                    dicerList[index] += dropAmount
                } else {
                    dicerList.add(dropAmount)
                }
            }
        }
    }

    fun getRecentBPS(): Double {
        val size = blocksSpeedList.size
        return if (size <= 1) {
            0.0
        } else {
            val startIndex = if (size >= 6) size - 6 else 0
            val validValues = blocksSpeedList.subList(startIndex, size)
            validValues.dropLast(1).average()
        }
    }

    private fun resetSpeed() {
        averageBlocksPerSecond = 0.0
        blocksSpeedList = emptyList()
        secondsStopped = 0
    }

    fun finneganPerkActive(): Boolean {
        val forcefullyEnabledAlwaysFinnegan = config.forcefullyEnabledAlwaysFinnegan
        val perkActive = MayorElection.isPerkActive("Finnegan", "Farming Simulator")
        return forcefullyEnabledAlwaysFinnegan || perkActive
    }

    fun isEnabled() = GardenAPI.inGarden()

    fun CropType.getSpeed() = cropsPerSecond?.get(this)

    fun CropType.setSpeed(speed: Int) {
        cropsPerSecond?.put(this, speed)
    }

    fun CropType.getLatestBlocksPerSecond() = latestBlocksPerSecond?.get(this)

    fun isSpeedDataEmpty() = cropsPerSecond?.values?.sum()?.let { it == 0 } ?: true

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3,"garden.blocksBrokenResetTime", "garden.cropMilestones.blocksBrokenResetTime")
    }
}
