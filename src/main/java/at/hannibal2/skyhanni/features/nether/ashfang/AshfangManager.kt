package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.crimsonisle.ashfang.AshfangConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.isAtFullHealth
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.MobUtils.mob
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AshfangManager {

    val config: AshfangConfig get() = SkyHanniMod.feature.crimsonIsle.ashfang

    private val ashfangMobs = mutableSetOf<Mob>()
    var ashfang: Mob? = null
        private set
    var lastSpawnTime = SimpleTimeMark.farPast()
        private set

    val active get() = ashfang != null

    @SubscribeEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        if (!IslandType.CRIMSON_ISLE.isInIsland()) return
        val mob = event.mob
        val color = when (mob.name) {
            "Ashfang Follower" -> LorenzColor.DARK_GRAY
            "Ashfang Underling" -> LorenzColor.RED
            "Ashfang Acolyte" -> LorenzColor.BLUE
            "Ashfang" -> {
                ashfang = mob
                return
            }

            else -> return
        }
        ashfangMobs += mob
        if (config.highlightBlazes) mob.highlight(color.toColor().addAlpha(40))
    }

    @SubscribeEvent
    fun onMobFirstSeen(event: MobEvent.FirstSeen.SkyblockMob) {
        if (!IslandType.CRIMSON_ISLE.isInIsland()) return
        if (!event.mob.name.contains("Ashfang ")) return
        if (lastSpawnTime.passedSince() < 10.seconds) return
        lastSpawnTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn.SkyblockMob) {
        val mob = event.mob
        ashfangMobs -= mob
        if (ashfang == mob) {
            ashfang = null
            if (mob.isInRender()) lastSpawnTime = SimpleTimeMark.farPast()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        if (!active || !config.hide.fullNames) return
        val mob = event.entity.mob ?: return
        if (mob !in ashfangMobs) return
        if (mob.baseEntity.isAtFullHealth()) event.cancel()
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastSpawnTime = SimpleTimeMark.farPast()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "ashfang.nextResetCooldown", "crimsonIsle.ashfang.nextResetCooldown")
        event.move(2, "ashfang.highlightBlazes", "crimsonIsle.ashfang.highlightBlazes")
        event.move(2, "ashfang.hideNames", "crimsonIsle.ashfang.hide.fullNames")
    }
}
