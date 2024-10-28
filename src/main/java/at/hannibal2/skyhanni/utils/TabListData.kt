package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.TablistFooterUpdateEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.mixins.hooks.tabListGuard
import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiPlayerTabOverlay
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils.conditionalTransform
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.time.Duration.Companion.seconds
//#if MC<1.12
import net.minecraft.world.WorldSettings
//#else
//$$ import net.minecraft.world.GameType
//#endif

@SkyHanniModule
object TabListData {
    private var tablistCache = emptyList<String>()
    private var debugCache: List<String>? = null

    private var header = ""
    private var footer = ""

    var fullyLoaded = false

    // TODO replace with TabListUpdateEvent
    @Deprecated("replace with TabListUpdateEvent")
    fun getTabList() = debugCache ?: tablistCache
    fun getHeader() = header
    fun getFooter() = footer

    fun toggleDebug() {
        if (debugCache != null) {
            ChatUtils.chat("Disabled tab list debug.")
            debugCache = null
            return
        }
        SkyHanniMod.coroutineScope.launch {
            val clipboard = OSUtils.readFromClipboard() ?: return@launch
            debugCache = clipboard.lines()
            ChatUtils.chat("Enabled tab list debug with your clipboard.")
        }
    }

    fun copyCommand(args: Array<String>) {
        if (debugCache != null) {
            ChatUtils.clickableChat(
                "Tab list debug is enabled!",
                onClick = { toggleDebug() },
                "§eClick to disable!"
            )
            return
        }

        val resultList = mutableListOf<String>()
        val noColor = args.size == 1 && args[0] == "true"
        for (line in getTabList()) {
            val tabListLine = line.transformIf({ noColor }) { removeColor() }
            if (tabListLine != "") resultList.add("'$tabListLine'")
        }

        val tabHeader = header.conditionalTransform(noColor, { this.removeColor() }, { this })
        val tabFooter = footer.conditionalTransform(noColor, { this.removeColor() }, { this })

        val widgets = TabWidget.entries.filter { it.isActive }
            .joinToString("\n") { "\n${it.name} : \n${it.lines.joinToString("\n")}" }
        val string =
            "Header:\n\n$tabHeader\n\nBody:\n\n${resultList.joinToString("\n")}\n\nFooter:\n\n$tabFooter\n\nWidgets:$widgets"

        OSUtils.copyToClipboard(string)
        ChatUtils.chat("Tab list copied into the clipboard!")
    }

    private val playerOrdering = Ordering.from(PlayerComparator())

    @SideOnly(Side.CLIENT)
    internal class PlayerComparator : Comparator<NetworkPlayerInfo> {

        override fun compare(o1: NetworkPlayerInfo, o2: NetworkPlayerInfo): Int {
            val team1 = o1.playerTeam
            val team2 = o2.playerTeam
            return ComparisonChain.start().compareTrueFirst(
                //#if MC<1.12
                o1.gameType != WorldSettings.GameType.SPECTATOR,
                o2.gameType != WorldSettings.GameType.SPECTATOR
                //#else
                //$$ o1.gameType != GameType.SPECTATOR,
                //$$ o2.gameType != GameType.SPECTATOR
                //#endif
            )
                .compare(
                    if (team1 != null) team1.registeredName else "",
                    if (team2 != null) team2.registeredName else ""
                )
                .compare(o1.gameProfile.name, o2.gameProfile.name).result()
        }
    }

    private fun readTabList(): List<String>? {
        val thePlayer = Minecraft.getMinecraft()?.thePlayer ?: return null
        val players = playerOrdering.sortedCopy(thePlayer.sendQueue.playerInfoMap)
        val result = mutableListOf<String>()
        tabListGuard = true
        for (info in players) {
            val name = Minecraft.getMinecraft().ingameGUI.tabList.getPlayerName(info)
            result.add(LorenzUtils.stripVanillaMessage(name))
        }
        tabListGuard = false
        return result.dropLast(1)
    }

    var dirty = false

    @HandleEvent(receiveCancelled = true)
    fun onPacketReceive(event: PacketReceivedEvent) {
        if (event.packet is S38PacketPlayerListItem) {
            dirty = true
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!dirty) return
        dirty = false

        val tabList = readTabList() ?: return
        if (tablistCache != tabList) {
            tablistCache = tabList
            TabListUpdateEvent(getTabList()).postAndCatch()
            if (!LorenzUtils.onHypixel) {
                workaroundDelayedTabListUpdateAgain()
            }
        }

        val tabListOverlay = Minecraft.getMinecraft().ingameGUI.tabList as AccessorGuiPlayerTabOverlay
        header = tabListOverlay.header_skyhanni?.formattedText.orEmpty()

        val tabFooter = tabListOverlay.footer_skyhanni?.formattedText.orEmpty()
        if (tabFooter != footer && tabFooter != "") {
            TablistFooterUpdateEvent(tabFooter).postAndCatch()
        }
        footer = tabFooter
    }

    private fun workaroundDelayedTabListUpdateAgain() {
        DelayedRun.runDelayed(2.seconds) {
            if (LorenzUtils.onHypixel) {
                println("workaroundDelayedTabListUpdateAgain")
                TabListUpdateEvent(getTabList()).postAndCatch()
            }
        }
    }
}
