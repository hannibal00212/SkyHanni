package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object LastServers {

    private val config get() = SkyHanniMod.feature.misc.lastServers
    private var lastServerId: String? = null
    private val lastServers = mutableMapOf<String, SimpleTimeMark>()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        val id = HypixelData.serverId ?: return
        // Update the time of the current server if the player is still on the same server.
        // This is necessary because the player can be on the same server for a long time.
        // And if the player leaves the server and joins it again, it still warns the player.
        if (lastServerId == id) {
            lastServers[id] = SimpleTimeMark.now()
            return
        }

        lastServers.entries.removeIf { it.value.passedSince() > config.warnTime.seconds }
        lastServers[id]?.passedSince()?.let {
            ChatUtils.chat("§7You were already on this server §b${it.format()}§7 ago.")
        }
        ChatUtils.debug("Adding $id to last servers.")
        lastServerId = id
        lastServers[id] = SimpleTimeMark.now()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
