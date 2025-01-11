package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.entity.EntityLivingBase

@SkyHanniModule
object DungeonBossHideDamageSplash {

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        if (!DungeonAPI.inDungeon()) return
        if (!SkyHanniMod.feature.dungeon.damageSplashBoss) return
        if (!DungeonAPI.inBossRoom) return

        if (DamageIndicatorManager.isDamageSplash(event.entity)) {
            event.cancel()
        }
    }
}
