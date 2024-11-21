package at.hannibal2.skyhanni.features.rift.area.stillgorechateau

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.jsonobjects.repo.RiftEffigiesJson
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.RawScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object RiftBloodEffigies {

    private val config get() = RiftAPI.config.area.stillgoreChateau.bloodEffigies

    private var locations: List<LorenzVec> = emptyList()
    private var effigiesTimes = cleanMap()

    private val patternGroup = RepoPattern.group("rift.area.stillgore.effegies")
    private val effigiesTimerPattern by patternGroup.pattern(
        "respawn",
        "§eRespawn §c(?<time>.*) §7\\(or click!\\)"
    )
    val heartsPattern by patternGroup.pattern(
        "heart",
        "Effigies: (?<hearts>(?:(?:§[7c])?⧯)*)"
    )

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        effigiesTimes = cleanMap()
    }

    private fun cleanMap() = (0..5).associateWith { SimpleTimeMark.farPast() }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Rift Blood Effigies")

        if (!isEnabled()) {
            event.addIrrelevant("Not in Stillgore Château or not enabled ")
            return
        }
        event.addData {
            for ((number, duration) in effigiesTimes) {
                val time = duration.timeUntil().format()
                add("$number: $time ($duration)")
            }
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val newLocations = event.getConstant<RiftEffigiesJson>("RiftEffigies").locations
        if (newLocations.size != 6) {
            error("Invalid rift effigies size: ${newLocations.size} (expected 6)")
        }
        locations = newLocations
    }

    @SubscribeEvent
    fun onScoreboardChange(event: RawScoreboardUpdateEvent) {
        if (!isEnabled()) return

        val line = event.rawScoreboard.firstOrNull { it.startsWith("Effigies:") } ?: return
        val hearts = heartsPattern.matchMatcher(line) {
            group("hearts")
        } ?: return

        val split = hearts.split("§").drop(1)
        for ((index, s) in split.withIndex()) {
            val time = effigiesTimes[index] ?: continue

            if (time.isInPast()) {
                if (s == "7") {
                    if (time.isFarPast()) {
                        ChatUtils.chat("Effigy #${index + 1} respawned!")
                        effigiesTimes = effigiesTimes.editCopy { this[index] = SimpleTimeMark.farPast() }
                    }
                } else {
                    if (time.isFarPast()) {
                        ChatUtils.chat("Effigy #${index + 1} is broken!")
                        val endTime = SimpleTimeMark.now() + 20.minutes
                        effigiesTimes = effigiesTimes.editCopy { this[index] = endTime }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        for (entity in EntityUtils.getEntitiesNearby<EntityArmorStand>(LocationUtils.playerLocation(), 6.0)) {
            effigiesTimerPattern.matchMatcher(entity.name) {
                val nearest = locations.minByOrNull { it.distanceSq(entity.getLorenzVec()) } ?: return
                val index = locations.indexOf(nearest)

                val string = group("time")
                val time = TimeUtils.getDuration(string)
                effigiesTimes = effigiesTimes.editCopy { this[index] = SimpleTimeMark.now() + time }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        for ((index, location) in locations.withIndex()) {
            val name = "Effigy #${index + 1}"
            val duration = effigiesTimes[index] ?: continue

            if (duration.isFarPast()) {
                if (config.unknownTime) {
                    event.drawWaypointFilled(location, LorenzColor.GRAY.toColor(), seeThroughBlocks = true)
                    event.drawDynamicText(location, "§7Unknown Time ($name)", 1.5)
                    continue
                }
            } else {
                if (duration.isFarPast()) {
                    event.drawWaypointFilled(location, LorenzColor.RED.toColor(), seeThroughBlocks = true)
                    event.drawDynamicText(location, "§cBreak $name!", 1.5)
                    continue
                }

                val timeUntil = duration.timeUntil()
                if (config.respawningSoon && timeUntil <= config.respwningSoonTime.minutes) {
                    event.drawWaypointFilled(location, LorenzColor.YELLOW.toColor(), seeThroughBlocks = true)
                    val time = timeUntil.format()
                    event.drawDynamicText(location, "§e$name is respawning §b$time", 1.5)
                    continue
                }
            }

            if (location.distanceToPlayer() < 5) {
                event.drawDynamicText(location, "§7$name", 1.5)
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.enabled && RiftAPI.inStillgoreChateau()

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(9, "rift.area.stillgoreChateauConfig", "rift.area.stillgoreChateau")
    }
}
