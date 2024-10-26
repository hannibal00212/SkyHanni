package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object JoinCrystalHollows {

    private var lastWrongPassTime = 0L

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        val message = event.message
        if (message == "§cYou do not have an active Crystal Hollows pass!") {
            lastWrongPassTime = System.currentTimeMillis()
            if (!IslandType.DWARVEN_MINES.isInIsland()) {
                ChatUtils.clickableChat(
                    "Click here to warp to Dwarven Mines!",
                    onClick = {
                        HypixelCommands.warp("mines")
                    }, "§eClick to run /warp mines!"
                )
            } else {
                ChatUtils.chat("Buy a §2Crystal Hollows Pass §efrom §5Gwendolyn")
            }
        }
        if (message == "§e[NPC] §5Gwendolyn§f: §rGreat! Now hop on into the Minecart and I'll get you on your way!" && inTime()) {
            ChatUtils.clickableChat("Click here to warp to Crystal Hollows!", onClick = {
                HypixelCommands.warp("ch")
            }, "§eClick to run /warp ch!")
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (!isEnabled()) return

        if (event.newIsland == IslandType.DWARVEN_MINES && inTime()) {
            ChatUtils.chat("Buy a §2Crystal Hollows Pass §efrom §5Gwendolyn§e!")
        }
        if (event.newIsland == IslandType.CRYSTAL_HOLLOWS) {
            lastWrongPassTime = 0
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!IslandType.DWARVEN_MINES.isInIsland()) return
        if (!isEnabled()) return

        if (inTime()) {
            val location = LorenzVec(88, 198, -99)
            event.drawWaypointFilled(location, LorenzColor.YELLOW.toColor())
            event.drawDynamicText(location, "§eBuy Crystal Hollows Pass", 1.3)
        }
    }

    private fun inTime() = lastWrongPassTime + 1000 * 60 * 2 > System.currentTimeMillis()

    fun isEnabled() = SkyHanniMod.feature.misc.crystalHollowsJoin
}
