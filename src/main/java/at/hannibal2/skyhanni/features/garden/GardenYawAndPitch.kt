package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenYawAndPitch {

    private val config get() = GardenAPI.config.yawPitchDisplay
    private var lastChange = SimpleTimeMark.farPast()
    private var lastYaw = 0f
    private var lastPitch = 0f

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!LorenzUtils.onHypixel) return
        if (!isEnabled()) return
        if (GardenAPI.hideExtraGuis()) return
        if (GardenAPI.toolInHand == null && !config.showWithoutTool) return

        val player = Minecraft.getMinecraft().thePlayer
        val yaw = LocationUtils.calculatePlayerYaw()
        val pitch = player.rotationPitch

        if (yaw != lastYaw || pitch != lastPitch) {
            lastChange = SimpleTimeMark.now()
        }
        lastYaw = yaw
        lastPitch = pitch

        if (!config.showAlways && lastChange.passedSince() > config.timeout.seconds) return

        val yawText = yaw.roundTo(config.yawPrecision).toBigDecimal().toPlainString()
        val pitchText = pitch.roundTo(config.pitchPrecision).toBigDecimal().toPlainString()
        val displayList = listOf(
            "§aYaw: §f$yawText",
            "§aPitch: §f$pitchText",
        )
        if (GardenAPI.inGarden()) {
            config.pos.renderStrings(displayList, posLabel = "Yaw and Pitch")
        } else {
            config.posOutside.renderStrings(displayList, posLabel = "Yaw and Pitch")
        }
    }

    @SubscribeEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        lastChange = SimpleTimeMark.farPast()
    }

    private fun isEnabled() =
        config.enabled && ((OutsideSbFeature.YAW_AND_PITCH.isSelected() && !LorenzUtils.inSkyBlock) ||
            (LorenzUtils.inSkyBlock && (GardenAPI.inGarden() || config.showOutsideGarden)))

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(18, "garden.yawPitchDisplay.showEverywhere", "garden.yawPitchDisplay.showOutsideGarden")
    }
}
