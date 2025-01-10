package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.LorenzColor

@SkyHanniModule
object HighlightDungeonDeathmite {

    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        if (!DungeonAPI.inDungeon()) return
        if (!SkyHanniMod.feature.dungeon.highlightDeathmites) return
        if (event.mob.name == "Deathmite") event.mob.highlight(LorenzColor.DARK_RED.toColor().addAlpha(20))
    }
}
