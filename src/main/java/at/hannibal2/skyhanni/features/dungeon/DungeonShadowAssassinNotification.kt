package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.mixins.transformers.AccessorWorldBorderPacket
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.SoundUtils
import net.minecraft.network.play.server.S44PacketWorldBorder
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object DungeonShadowAssassinNotification {

    private val config get() = SkyHanniMod.feature.dungeon

    @HandleEvent(onlyOnIsland = IslandType.CATACOMBS)
    fun onWorldBorderChange(event: PacketReceivedEvent) {
        if (!isEnabled()) return
        if (DungeonAPI.dungeonFloor?.contains("3") == true && DungeonAPI.inBossRoom) return

        val packet = event.packet as? AccessorWorldBorderPacket ?: return
        val action = packet.action
        val warningTime = packet.warningTime

        if (action == S44PacketWorldBorder.Action.INITIALIZE && warningTime == 10000) {
            TitleManager.sendTitle("§cShadow Assassin Jumping!", 2.seconds, 3.6, 7.0f)
            SoundUtils.playBeepSound()
        }
    }

    private fun isEnabled() = config.shadowAssassinJumpNotifier
}
