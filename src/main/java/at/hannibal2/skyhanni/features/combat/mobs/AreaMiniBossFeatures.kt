package at.hannibal2.skyhanni.features.combat.mobs

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.SlayerApi
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import at.hannibal2.skyhanni.utils.TimeUtils.format
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AreaMiniBossFeatures {

    private val config get() = SkyHanniMod.feature.combat.mobs
    private var lastSpawnTime = SimpleTimeMark.farPast()
    private var miniBossType: AreaMiniBossType? = null
    private var respawnCooldown = 11.seconds

    @HandleEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val type = AreaMiniBossType.entries.find { it.displayName == event.mob.name } ?: return
        miniBossType = type
        val time = SimpleTimeMark.now()
        val diff = time - lastSpawnTime
        if (diff in 5.seconds..20.seconds) {
            respawnCooldown = diff
        }
        lastSpawnTime = time
        if (config.areaBossHighlight) {
            event.mob.highlight(type.color.addOpacity(type.colorOpacity))
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!SlayerApi.isInAnyArea) return
        if (!config.areaBossRespawnTimer) return

        val miniBoss = miniBossType ?: return

        val time = miniBoss.getTime()
        miniBoss.spawnLocations.filter { it.distanceToPlayer() < 15 }
            .forEach { event.drawDynamicText(it, time, 1.2, ignoreBlocks = false) }
    }

    private fun AreaMiniBossType.getTime(): String {
        val spawnedSince = lastSpawnTime.passedSince()
        if (respawnCooldown <= spawnedSince) return "§c?"

        val estimatedTime = respawnCooldown - spawnedSince
        val format = estimatedTime.format(showMilliSeconds = true)
        return color.getChatColor() + format
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        miniBossType = null
    }

    // TODO move to repo
    private enum class AreaMiniBossType(
        val displayName: String,
        val color: LorenzColor,
        val colorOpacity: Int,
        vararg val spawnLocations: SkyHanniVec3d,
    ) {
        GOLDEN_GHOUL(
            "Golden Ghoul", LorenzColor.YELLOW, 127,
            SkyHanniVec3d(-99, 39, -86),
            SkyHanniVec3d(-128, 42, -138),
        ),
        OLD_WOLF(
            "Old Wolf", LorenzColor.GOLD, 60,
            SkyHanniVec3d(-248, 123, 54),
            SkyHanniVec3d(-256, 105, 75),
            SkyHanniVec3d(-268, 90, 97),
            SkyHanniVec3d(-258, 94, 75),
            SkyHanniVec3d(-225, 92, 127),
        ),
        SOUL_OF_THE_ALPHA(
            "Soul of the Alpha", LorenzColor.GOLD, 60,
            SkyHanniVec3d(-381, 56, -94),
            SkyHanniVec3d(-394, 63, -52),
            SkyHanniVec3d(-386, 50, -2),
            SkyHanniVec3d(-396, 58, 29),
        ),
        VOIDLING_EXTREMIST(
            "Voidling Extremist", LorenzColor.LIGHT_PURPLE, 127,
            SkyHanniVec3d(-591, 10, -304),
            SkyHanniVec3d(-544, 21, -301),
            SkyHanniVec3d(-593, 26, -328),
            SkyHanniVec3d(-565, 41, -307),
            SkyHanniVec3d(-573, 51, -353),
        ),
        MILLENNIA_AGED_BLAZE(
            "Millennia-Aged Blaze", LorenzColor.DARK_RED, 60,
            SkyHanniVec3d(-292, 97, -999),
            SkyHanniVec3d(-232, 77, -951),
            SkyHanniVec3d(-304, 73, -952),
            SkyHanniVec3d(-281, 82, -1010),
            SkyHanniVec3d(-342, 86, -1035),
        ),
    }
}
