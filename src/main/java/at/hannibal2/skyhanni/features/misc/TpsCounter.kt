package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.concurrent.fixedRateTimer

@SkyHanniModule
object TpsCounter {

    private val config get() = SkyHanniMod.feature.gui

    private const val MIN_DATA_AMOUNT = 5
    private const val WAIT_AFTER_WORLD_SWITCH = 6

    private var packetsFromLastSecond = 0
    private var tpsList = mutableListOf<Int>()
    private var ignoreFirstTicks = WAIT_AFTER_WORLD_SWITCH
    private var hasPacketReceived = false

    private var display = ""

    init {
        // TODO use SecondPassedEvent + passedSince
        fixedRateTimer(name = "skyhanni-tps-counter-seconds", period = 1000L) {
            if (!LorenzUtils.inSkyBlock) return@fixedRateTimer
            if (packetsFromLastSecond == 0) return@fixedRateTimer

            if (ignoreFirstTicks > 0) {
                ignoreFirstTicks--
                val current = ignoreFirstTicks + MIN_DATA_AMOUNT
                display = "§eTPS: §f(${current}s)"
                packetsFromLastSecond = 0
                return@fixedRateTimer
            }

            tpsList.add(packetsFromLastSecond)
            packetsFromLastSecond = 0
            if (tpsList.size > 10) {
                tpsList = tpsList.drop(1).toMutableList()
            }

            display = if (tpsList.size < MIN_DATA_AMOUNT) {
                val current = MIN_DATA_AMOUNT - tpsList.size
                "§eTPS: §f(${current}s)"
            } else {
                val sum = tpsList.sum().toDouble()
                var tps = (sum / tpsList.size).round(1)
                if (tps > 20) tps = 20.0
                val color = getColor(tps)
                "§eTPS: $color$tps"
            }
        }
        // TODO use DelayedRun
        fixedRateTimer(name = "skyhanni-tps-counter-ticks", period = 50L) {
            if (!LorenzUtils.inSkyBlock) return@fixedRateTimer

            if (hasPacketReceived) {
                hasPacketReceived = false
                packetsFromLastSecond++
            }
        }
    }

    fun tpsCommand() {
        if (display.isEmpty()) {
            ChatUtils.chat("§cNo tps data available, make sure you have the setting on.")
            return
        }
        ChatUtils.chat(display)
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        tpsList.clear()
        packetsFromLastSecond = 0
        ignoreFirstTicks = WAIT_AFTER_WORLD_SWITCH
        display = ""
    }

    @HandleEvent(priority = HandleEvent.LOW, receiveCancelled = true)
    fun onPacketReceive(event: PacketReceivedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        hasPacketReceived = true
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.tpsDisplayPosition.renderString(display, posLabel = "Tps Display")
    }

    private fun isEnabled() = LorenzUtils.onHypixel && config.tpsDisplay &&
        (LorenzUtils.inSkyBlock || OutsideSbFeature.TPS_DISPLAY.isSelected())

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.tpsDisplayEnabled", "gui.tpsDisplay")
        event.move(2, "misc.tpsDisplayPosition", "gui.tpsDisplayPosition")
    }

    private fun getColor(tps: Double) = when {
        tps > 19.8 -> "§2"
        tps > 19 -> "§a"
        tps > 17.5 -> "§6"
        tps > 12 -> "§c"

        else -> "§4"
    }
}
