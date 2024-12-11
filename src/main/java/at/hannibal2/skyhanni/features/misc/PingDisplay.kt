package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S37PacketStatistics
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.round

@SkyHanniModule
object PingDisplay {

    private val config get() = SkyHanniMod.feature.gui

    private val mc: Minecraft = Minecraft.getMinecraft()
    private var lastPingAt: Long = -1L
    private var invokedCommand = false
    private var autoPingerEnabled = false
    private var display: String? = null

    var latestPing: Double = 0.0


    fun sendPing(command: Boolean) {
        if (lastPingAt > 0) {
            if (invokedCommand) {
                ChatUtils.chat("§cAlready pinging!")
                return
            }
        }
        mc.thePlayer.sendQueue.networkManager.sendPacket(
            C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS)
        )
        lastPingAt = System.nanoTime()
        invokedCommand = command
    }

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        if (autoPingerEnabled) return
        lastPingAt = -1L
        invokedCommand = false
        startPingUpdater()
    }


    @HandleEvent
    fun onPacketReceived(event: PacketReceivedEvent) {
        val packet = event.packet
        if (lastPingAt > 0) {
            when (packet) {
                is S01PacketJoinGame -> {
                    lastPingAt = -1L
                    invokedCommand = false
                    startPingUpdater()
                }

                is S37PacketStatistics -> {
                    val diff = abs(System.nanoTime() - lastPingAt) / 1_000_000.0
                    lastPingAt = -1L
                    latestPing = diff
                    updateDisplay()
                    if (invokedCommand) {
                        invokedCommand = false
                        ChatUtils.chat(formatPingMessage(latestPing))
                    }
                }
            }
        }
    }


    @HandleEvent
    fun onCommandRegister(event: CommandRegistrationEvent) {
        event.register("shping") {
            description = "Check your ping"
            category = CommandCategory.USERS_ACTIVE
            callback { sendPing(true) }
        }
    }

    private fun formatPingMessage(ping: Double): String {
        val color = when {
            ping < 50 -> "2"
            ping < 100 -> "a"
            ping < 149 -> "6"
            ping < 249 -> "c"
            else -> "4"
        }
        return "§$color${round(ping * 100) / 100} §7ms"
    }


    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        autoPingerEnabled = false
    }

    fun startPingUpdater() {
        if (autoPingerEnabled) return
        autoPingerEnabled = true
        val scheduler = Executors.newScheduledThreadPool(1)
        ChatUtils.debug("Starting Ping Updater")
        scheduler.scheduleAtFixedRate({
            sendPing(false)
        }, 0, 5, TimeUnit.SECONDS)
    }

    private fun updateDisplay() {
        display = "§ePing: ${formatPingMessage(latestPing)}"
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.pingDisplayPosition.renderString(display, posLabel = "Ping Display")
    }


    private fun isEnabled() = config.pingDisplay && (LorenzUtils.inSkyBlock || OutsideSbFeature.PING_DISPLAY.isSelected())
}
