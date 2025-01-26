package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object TerracottaPhase {

    private val config get() = SkyHanniMod.feature.dungeon.terracottaPhase

    private var inTerracottaPhase = false

    private val patternGroup = RepoPattern.group("dungeon.terracotta")
    private val terracottaStartPattern by patternGroup.pattern(
        "start",
        "§c\\[BOSS] Sadan§r§f: So you made it all the way here... Now you wish to defy me\\? Sadan\\?!",
    )
    private val terracottaEndPattern by patternGroup.pattern(
        "end",
        "§c\\[BOSS] Sadan§r§f: ENOUGH!",
    )

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (terracottaStartPattern.matches(event.message)) {
            inTerracottaPhase = true
        } else if (terracottaEndPattern.matches(event.message)) {
            inTerracottaPhase = false
        }
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        if (isActive() && config.hideDamageSplash && DamageIndicatorManager.isDamageSplash(event.entity)) {
            event.cancel()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (isActive() && config.hideParticles) {
            event.cancel()
        }
    }

    private fun isActive() = inTerracottaPhase && isEnabled()

    private fun isEnabled() = DungeonApi.inBossRoom && DungeonApi.getCurrentBoss() == DungeonFloor.F6
}
