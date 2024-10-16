package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.HypixelJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AutoJoinSkyblock {
    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        if (!SkyHanniMod.feature.misc.autoJoinSkyblock) return

        DelayedRun.runDelayed(1.seconds) {
            HypixelCommands.skyblock()
        }
    }
}
