package at.hannibal2.skyhanni.features.combat.mobs

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.SlayerAPI
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AreaMiniBossFeatures {

    private val config get() = SkyHanniMod.feature.combat.mobs
    private var lastSpawnTime = SimpleTimeMark.farPast()
    private var miniBossType: AreaMiniBossType? = null
    private var respawnCooldown = 11.seconds

    @SubscribeEvent
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

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!SlayerAPI.isInAnyArea) return
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

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        miniBossType = null
    }

    // TODO move to repo
    private enum class AreaMiniBossType(
        val displayName: String,
        val color: LorenzColor,
        val colorOpacity: Int,
        vararg val spawnLocations: LorenzVec,
    ) {
        GOLDEN_GHOUL(
            "Golden Ghoul", LorenzColor.YELLOW, 127,
            LorenzVec(-99, 39, -86),
            LorenzVec(-128, 42, -138),
        ),
        OLD_WOLF(
            "Old Wolf", LorenzColor.GOLD, 60,
            LorenzVec(-248, 123, 54),
            LorenzVec(-256, 105, 75),
            LorenzVec(-268, 90, 97),
            LorenzVec(-258, 94, 75),
            LorenzVec(-225, 92, 127),
        ),
        SOUL_OF_THE_ALPHA(
            "Soul of the Alpha", LorenzColor.GOLD, 60,
            LorenzVec(-381, 56, -94),
            LorenzVec(-394, 63, -52),
            LorenzVec(-386, 50, -2),
            LorenzVec(-396, 58, 29),
        ),
        VOIDLING_EXTREMIST(
            "Voidling Extremist", LorenzColor.LIGHT_PURPLE, 127,
            LorenzVec(-591, 10, -304),
            LorenzVec(-544, 21, -301),
            LorenzVec(-593, 26, -328),
            LorenzVec(-565, 41, -307),
            LorenzVec(-573, 51, -353),
        ),
        MILLENNIA_AGED_BLAZE(
            "Millennia-Aged Blaze", LorenzColor.DARK_RED, 60,
            LorenzVec(-292, 97, -999),
            LorenzVec(-232, 77, -951),
            LorenzVec(-304, 73, -952),
            LorenzVec(-281, 82, -1010),
            LorenzVec(-342, 86, -1035),
        ),
    }
}
