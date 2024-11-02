package at.hannibal2.skyhanni.features.event.winter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ReindrakeWarpHelper {

    private val config get() = SkyHanniMod.feature.event.winter

    private val patternGroup = RepoPattern.group("event.winter.reindrakewarphelper")

    /**
     * REGEX-TEST: §c§lWOAH! §cA §4Reindrake §cwas summoned from the depths!
     * REGEX-TEST: §c§lWOAH! §r§cA §r§4Reindrake §r§cwas summoned from the depths!
     */
    private val spawnPattern by patternGroup.pattern(
        "spawn.message",
        "§c§lWOAH! (?:§r)?§cA (?:§r)?§4Reindrake (?:§r)?§cwas summoned from the depths!",
    )

    @SubscribeEvent
    fun onMessage(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (!spawnPattern.matches(event.message)) return
        ChatUtils.clickToActionOrDisable(
            "A Reindrake was detected. Click to warp to the Winter Island spawn!",
            config::reindrakeWarpHelper,
            actionName = "warp to winter island spawn",
            action = { HypixelCommands.warp("winter") }
        )
    }

    fun isEnabled() = IslandType.WINTER.isInIsland() && config.reindrakeWarpHelper
}
