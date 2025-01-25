package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.GardenConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemBlink
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.seconds

private typealias Type = GardenConfig.BurrowingSporesNotificationType

@SkyHanniModule
object GardenBurrowingSporesNotifier {

    private val config get() = GardenApi.config
    private val patternGroup = RepoPattern.group("garden.burrowingspores")
    private val sporeDropMessage by patternGroup.pattern(
        "drop",
        "§6§lVERY RARE CROP! §r§f§r§9Burrowing Spores\\.",
    )

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onChat(event: SkyHanniChatEvent) {
        val selected = config.burrowingSporesNotificationType
        val titleEnabled = selected in listOf(Type.TITLE, Type.BOTH)
        val blinkEnabled = selected in listOf(Type.BLINK, Type.BOTH)
        if (!titleEnabled && !blinkEnabled) return
        if (!sporeDropMessage.matches(event.message)) return

        if (titleEnabled) LorenzUtils.sendTitle("§9Burrowing Spores!", 5.seconds)
        if (blinkEnabled) ItemBlink.setBlink(NeuItems.getItemStackOrNull("BURROWING_SPORES"), 5_000)
    }
}
