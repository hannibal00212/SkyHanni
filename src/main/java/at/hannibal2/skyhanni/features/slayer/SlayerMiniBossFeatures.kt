package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SlayerMiniBossFeatures {

    private val config get() = SkyHanniMod.feature.slayer
    private var miniBosses = mutableSetOf<Mob>()

    @SubscribeEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val mob = event.mob
        if (!SlayerMiniBossType.isMiniboss(mob.name)) return
        miniBosses += mob
        if (config.slayerMinibossHighlight) mob.highlight(LorenzColor.AQUA.toColor())
    }

    @SubscribeEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn.SkyblockMob) {
        miniBosses -= event.mob
    }

    @SubscribeEvent
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!SlayerAPI.isInAnyArea) return
        if (!config.slayerMinibossLine) return
        for (mob in miniBosses) {
            if (!mob.baseEntity.canBeSeen(10)) continue
            event.drawLineToEye(
                mob.baseEntity.getLorenzVec().up(),
                LorenzColor.AQUA.toColor(),
                config.slayerMinibossLineWidth,
                true,
            )
        }
    }

    enum class SlayerMiniBossType(vararg names: String) {
        REVENANT("Revenant Sycophant", "Revenant Champion", "Deformed Revenant", "Atoned Champion", "Atoned Revenant"),
        TARANTULA("Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula"),
        SVEN("Pack Enforcer", "Sven Follower", "Sven Alpha"),
        VOIDLING("Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac"),
        INFERNAL("Flare Demon", "Kindleheart Demon", "Burningsoul Demon"),
        ;

        val names = names.toSet()

        companion object {
            fun isMiniboss(name: String) = entries.any { name in it.names }
        }
    }
}
