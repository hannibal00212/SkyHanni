package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.util.ChatComponentText

@SkyHanniModule
object SeaCreatureMessageShortener {

    private val config get() = SkyHanniMod.feature.fishing

    @HandleEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val original = event.chatEvent.chatComponent.formattedText
        var edited = original

        if (config.shortenFishingMessage) {
            edited = "§9You caught a ${event.seaCreature.displayName}§9!"
        }

        if (config.compactDoubleHook && event.doubleHook) {
            edited = "§e§lDOUBLE HOOK! $edited"
        }

        if (original == edited) return
        event.chatEvent.chatComponent = ChatComponentText(edited)
    }
}
