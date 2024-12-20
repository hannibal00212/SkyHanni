package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.crash.CrashReport
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CrashOnDeath {
    private val config get() = SkyHanniMod.feature.misc

    /**
     * REGEX-TEST: §c ☠ §r§7You were killed by §r§4§lMagma Boss§r§7§r§7.
     */
    private val pattern by RepoPattern.pattern(
        "ownplayer.death.chat",
        "§c ☠ §r§7You (?<reason>.+)",
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        if (pattern.matches(event.message)) {
            Minecraft.getMinecraft().crashed(CrashReport("Not Reading", Throwable("Don't toggle all the Options")))
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.crashOnDeath
}
