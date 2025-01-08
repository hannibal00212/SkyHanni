package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TerracottaPhase {

    private val config get() = SkyHanniMod.feature.dungeon.terracottaPhase

    private var inTerracottaPhase = false

    private val repoGroup = RepoPattern.group("dungeon.terracotta")
    private val terracottaStartPattern by repoGroup.pattern(
        "start",
        "§c\\[BOSS] Sadan§r§f: So you made it all the way here... Now you wish to defy me\\? Sadan\\?!",
    )
    private val terracottaEndPattern by repoGroup.pattern(
        "end",
        "§c\\[BOSS] Sadan§r§f: ENOUGH!",
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (terracottaStartPattern.matches(event.message)) {
            inTerracottaPhase = true
        } else if (terracottaEndPattern.matches(event.message)) {
            inTerracottaPhase = false
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        if (isActive() && config.hideDamageSplash && DamageIndicatorManager.isDamageSplash(event.entity)) {
            event.cancel()
        }
    }

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (isActive() && config.hideParticles) {
            event.cancel()
        }
    }

    private fun isActive() = inTerracottaPhase && isEnabled()

    private fun isEnabled() = DungeonAPI.inBossRoom && DungeonAPI.getCurrentBoss() == DungeonFloor.F6
}
