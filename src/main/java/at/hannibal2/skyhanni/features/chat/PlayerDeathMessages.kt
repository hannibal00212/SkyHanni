package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.player.PlayerDeathEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.misc.MarkedPlayerManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PlayerDeathMessages {

    private val lastTimePlayerSeen = mutableMapOf<String, Long>()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isHideFarDeathsEnabled()) return

        checkOtherPlayers()
    }

    @SubscribeEvent
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val name = event.name

        if (MarkedPlayerManager.config.highlightInChat &&
            !DungeonAPI.inDungeon() &&
            !LorenzUtils.inKuudraFight &&
            MarkedPlayerManager.isMarkedPlayer(name)
        ) {
            val reason = event.reason
            val color = MarkedPlayerManager.config.chatColor.getChatColor()
            ChatUtils.chat(" §c☠ $color$name §7$reason", false)
            event.chatEvent.blockedReason = "marked_player_death"
            return
        }

        val time = System.currentTimeMillis() > lastTimePlayerSeen.getOrDefault(name, 0) + 30_000
        if (isHideFarDeathsEnabled() && time) {
            event.chatEvent.blockedReason = "far_away_player_death"
        }
    }

    private fun checkOtherPlayers() {
        val entities = EntityUtils.getEntities<EntityOtherPlayerMP>()
            .filter { it.getLorenzVec().distance(LocationUtils.playerLocation()) < 25 }
        for (otherPlayer in entities) {
            lastTimePlayerSeen[otherPlayer.name] = System.currentTimeMillis()
        }
    }

    private fun isHideFarDeathsEnabled(): Boolean =
        LorenzUtils.inSkyBlock && SkyHanniMod.feature.chat.hideFarDeathMessages && !DungeonAPI.inDungeon() && !LorenzUtils.inKuudraFight
}
