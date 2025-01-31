package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.enums.OutsideSBFeature
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TpsCounter {

    private val config get() = SkyHanniMod.feature.gui

    private val ignorePacketDelay = 5.seconds
    private val minimumSecondsDisplayDelay = 10.seconds

    private var packetsFromLastSecond = 0
    private val tpsList = mutableListOf<Int>()
    private var hasRemovedFirstSecond = false

    private var hasReceivedPacket = false

    var tps: Double? = null
        private set

    private var display: String? = null

    private val timeSinceWorldSwitch get() = LorenzUtils.lastWorldSwitch.passedSince()
    private val tilCalculated: String
        get() =
            "§fCalculating... §7(${(10.seconds - timeSinceWorldSwitch).inWholeSeconds}s)"

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (shouldIgnore()) {
            updateDisplay()
            return
        }

        if (packetsFromLastSecond != 0) {
            if (hasRemovedFirstSecond) tpsList.add(packetsFromLastSecond)
            hasRemovedFirstSecond = true
        }
        packetsFromLastSecond = 0

        if (tpsList.size > 10) tpsList.removeAt(0)

        updateDisplay()
    }

    private fun updateDisplay() {
        val timeUntil = minimumSecondsDisplayDelay - timeSinceWorldSwitch
        val text = if (timeUntil.isPositive()) {
            "§f(${timeUntil.inWholeSeconds}s)"
        } else {
            val sum = tpsList.sum().toDouble()
            val newTps = (sum / tpsList.size).roundTo(1).coerceIn(0.0..20.0)
            tps = newTps
            val legacyColor = format(newTps)
            "$legacyColor$newTps"
        }
        display = "§eTPS: $text"
    }

    private fun tpsCommand() {
        val tpsMessage = tps?.let { "${format(it)}$it" } ?: tilCalculated
        ChatUtils.chat("§eTPS: $tpsMessage")
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (hasReceivedPacket) {
            packetsFromLastSecond++
            hasReceivedPacket = false
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        tpsList.clear()
        tps = null
        packetsFromLastSecond = 0
        display = null
        hasRemovedFirstSecond = false
    }

    @HandleEvent(priority = HandleEvent.HIGHEST, receiveCancelled = true)
    fun onPacketReceive(event: PacketReceivedEvent) {
        hasReceivedPacket = true
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.tpsDisplayPosition.renderString(display, posLabel = "Tps Display")
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shtps") {
            description = "Informs in chat about the server ticks per second (TPS)."
            category = CommandCategory.USERS_ACTIVE
            callback { tpsCommand() }
        }
    }

    private fun shouldIgnore() = timeSinceWorldSwitch < ignorePacketDelay

    private fun isEnabled() = LorenzUtils.onHypixel &&
        config.tpsDisplay &&
        (LorenzUtils.inSkyBlock || OutsideSBFeature.TPS_DISPLAY.isSelected())

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.tpsDisplayEnabled", "gui.tpsDisplay")
        event.move(2, "misc.tpsDisplayPosition", "gui.tpsDisplayPosition")
    }

    private fun format(tps: Double): String {
        if (!tps.isFinite()) printError(tps)
        return getColor(tps)
    }

    private fun getColor(tps: Double) = when {
        tps > 19.8 -> "§2"
        tps > 19 -> "§a"
        tps > 17.5 -> "§6"
        tps > 12 -> "§c"

        else -> "§4"
    }

    private fun printError(tps: Double) {
        ErrorManager.logErrorStateWithData(
            "TPS calculation got an error",
            "tps is $tps",
            "tps" to tps,
            "packetsFromLastSecond" to packetsFromLastSecond,
            "hasRemovedFirstSecond" to hasRemovedFirstSecond,
            "hasReceivedPacket" to hasReceivedPacket,
            "tpsList" to tpsList,
            "timeSinceWorldSwitch" to timeSinceWorldSwitch,
        )
    }
}
