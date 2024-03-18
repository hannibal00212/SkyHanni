package at.hannibal2.skyhanni.features.garden.farming.lane

import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLaneAPI.getValue
import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLaneAPI.setValue
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object LaneDisplay {
    private val config get() = GardenAPI.config.farmingLane

    val lanes = mutableMapOf<CropType, FarmingLane>()

    var currentLane: FarmingLane? = null
    private var oldValue: Double? = null
    private var remainingDistance = 0.0

    private var display = listOf<String>()
    private var timeRemaining: Duration? = null
    private var lastSpeed = 0.0
    private var validSpeed = false
    private var lastTimeFarming = SimpleTimeMark.farPast()
    private var lastDirection = 0

    @SubscribeEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        val crop = event.crop
        currentLane = lanes[crop]
        if (crop != null && currentLane == null) {
            if (config.distanceDisplay || config.laneSwitchNotification.enabled) {
                ChatUtils.clickableChat(
                    "No ${crop.cropName} lane defined yet! Use §e/shlanedetection",
                    command = "shlanedetection"
                )
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!GardenAPI.inGarden()) return
        if (!event.isMod(2)) return

        val lane = currentLane ?: return
        val direction = lane.direction
        val min = lane.min
        val max = lane.max
        val position = direction.getValue(LocationUtils.playerLocation())
        val outside = position !in min..max
        if (outside) {
            display = emptyList()
            return
        }

        val oldValue = oldValue ?: run {
            oldValue = position
            return
        }
        val diff = oldValue - position
        LaneDisplay.oldValue = position

        val newDirection = if (diff > 0) {
            1
        } else if (diff < 0) {
            -1
        } else {
            0
        }

        remainingDistance = when (newDirection) {
            1 -> (min - position).absoluteValue
            -1 -> (max - position).absoluteValue
            else -> remainingDistance
        }

        if (newDirection != lastDirection) {
            // reset farming time, to prevent wrong lane warnings
            lastTimeFarming = SimpleTimeMark.farPast()
            lastDirection = newDirection
        }

        if (!GardenAPI.isCurrentlyFarming()) return

        if (config.distanceDisplay) {
            display = buildList {
                add("§7Distance until Switch: §e${remainingDistance.round(1)}")
                val color = if (validSpeed) "§b" else "§8"
                add("§7Time remaining: $color${timeRemaining?.format()}")
            }
        }
        if (config.laneSwitchNotification.enabled) {
            sendWarning()
        }
    }

    private fun sendWarning() {
        val speedPerSecond = LocationUtils.distanceFromPreviousTick().round(2)
        if (speedPerSecond == 0.0) return
        val speedTooSlow = speedPerSecond < 1
        if (speedTooSlow) {
            validSpeed = false
//             validFarming = false
            return
        }
        // only use time if it is consistent
        if (lastSpeed != speedPerSecond) {
            lastSpeed = speedPerSecond
            validSpeed = false
//             lastTimeFarming = SimpleTimeMark.farPast()
//             validFarming = false
            return
        }

        validSpeed = true

        val timeRemaining = (remainingDistance / speedPerSecond).seconds
        val switchSettings = config.laneSwitchNotification
        LaneDisplay.timeRemaining = timeRemaining
        val warnAt = switchSettings.secondsBefore.seconds
        if (timeRemaining >= warnAt) {
            lastTimeFarming = SimpleTimeMark.now()
            return
        }

        // When the player was not inside the farm yet
        if (lastTimeFarming.passedSince() > warnAt) return

        with(switchSettings) {
            LorenzUtils.sendTitle(text.replace("&", "§"), 1.seconds)
        }
        playUserSound()
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!GardenAPI.inGarden()) return
        if (!config.cornerWaypoints) return

        val lane = currentLane ?: return
        val direction = lane.direction
        val location = LocationUtils.playerLocation()
        val min = direction.setValue(location, lane.min)
        val max = direction.setValue(location, lane.max)

        event.drawWaypointFilled(min, LorenzColor.YELLOW.toColor(), beacon = true)
        event.drawDynamicText(min, "§eLane Corner", 1.5)
        event.drawWaypointFilled(max, LorenzColor.YELLOW.toColor(), beacon = true)
        event.drawDynamicText(max, "§eLane Corner", 1.5)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!GardenAPI.inGarden()) return
        if (!config.distanceDisplay) return

        config.distanceUntilSwitchPosition.renderStrings(display, posLabel = "Lane Display")
    }

    @JvmStatic
    fun playUserSound() {
        with(config.laneSwitchNotification.sound) {
            SoundUtils.createSound(name, pitch).playSound()
        }
    }
}
