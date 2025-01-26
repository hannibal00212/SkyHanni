package at.hannibal2.skyhanni.features.misc.teleportpad

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object TeleportPadCompactName {
    private val patternGroup = RepoPattern.group("misc.teleportpad")

    /**
     * REGEX-TEST: §a✦ §aWarp To §9Blue
     */
    private val namePattern by patternGroup.pattern(
        "name",
        "§.✦ §aWarp To (?<name>.*)"
    )

    /**
     * REGEX-TEST: §c✦ §cNo Destination
     */
    private val noNamePattern by patternGroup.pattern(
        "noname",
        "§.✦ §cNo Destination"
    )

    @HandleEvent(priority = HandleEvent.HIGH, onlyOnIsland = IslandType.PRIVATE_ISLAND)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        if (!SkyHanniMod.feature.misc.teleportPad.compactName) return
        val entity = event.entity

        val name = entity.name

        noNamePattern.matchMatcher(name) {
            event.cancel()
        }

        namePattern.matchMatcher(name) {
            entity.customNameTag = group("name")
        }
    }
}
