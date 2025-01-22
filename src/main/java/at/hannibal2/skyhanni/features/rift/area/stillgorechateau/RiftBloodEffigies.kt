package at.hannibal2.skyhanni.features.rift.area.stillgorechateau

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.jsonobjects.repo.RiftEffigiesJson
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.RawScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.RenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object RiftBloodEffigies {

    enum class EffigyState {
        UNKNOWN,
        NOT_BROKEN,
        BROKEN,
    }

    private data class Effigy(
        var state: EffigyState = EffigyState.UNKNOWN,
        var respawnTime: SimpleTimeMark = SimpleTimeMark.farPast(),
    ) {
        fun reset() {
            state = EffigyState.UNKNOWN
            respawnTime = SimpleTimeMark.farPast()
        }
    }

    private val config get() = RiftAPI.config.area.stillgoreChateau.bloodEffigies

    private var locations: List<LorenzVec> = emptyList()
    private val effigies = (0..5).associateWith { Effigy() }

    private val patternGroup = RepoPattern.group("rift.area.stillgore.effegies")

    /**
     * REGEX-TEST: §eRespawn §c14m59s §7(or click!)
     * REGEX-TEST: §eRespawn §c1s §7(or click!)
     */
    private val effigiesTimerPattern by patternGroup.pattern(
        "respawn",
        "§eRespawn §c(?<time>.*) §7\\(or click!\\)",
    )

    /**
     * REGEX-TEST: §eBreak it!
     */
    private val effigiesBreakPattern by patternGroup.pattern(
        "break",
        "§eBreak it!",
    )

    /**
     * REGEX-TEST: Effigies: §c⧯§c⧯§c⧯§c⧯§c⧯§c⧯
     * REGEX-TEST: Effigies: §c⧯§c⧯§c⧯§c⧯§c⧯§7⧯
     */
    val heartsPattern by patternGroup.pattern(
        "heart",
        "Effigies: (?<hearts>(?:(?:§[7c])?⧯)*)",
    )

    private fun getIndex(entity: EntityArmorStand): Int? =
        locations.minByOrNull { it.distanceSq(entity.getLorenzVec()) }?.let { locations.indexOf(it) }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        effigies.values.forEach { it.reset() }
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Rift Blood Effigies")

        if (!isEnabled()) {
            event.addIrrelevant("Not in Stillgore Château or not enabled ")
            return
        }
        event.addData {
            for ((i, effigy) in effigies) {
                val time = effigy.respawnTime.timeUntil().format()
                add("${i + 1}: ${effigy.state} - $time (${effigy.respawnTime})")
            }
        }
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val newLocations = event.getConstant<RiftEffigiesJson>("RiftEffigies").locations
        if (newLocations.size != 6) {
            error("Invalid Rift effigies size: ${newLocations.size} (expected 6)")
        }
        locations = newLocations
    }

    @HandleEvent
    fun onRawScoreboardChange(event: RawScoreboardUpdateEvent) {
        if (!isEnabled()) return

        val line = event.rawScoreboard.firstOrNull { it.startsWith("Effigies:") } ?: return
        ChatUtils.debug("Effigies line: $line")
        val hearts = heartsPattern.matchMatcher(line) {
            group("hearts")
        } ?: return

        val split = hearts.split("§").drop(1)
        for ((index, s) in split.withIndex()) {
            val effigy = effigies[index] ?: continue

            val oldState = effigy.state
            effigy.state = when (s[0]) {
                '7' -> EffigyState.NOT_BROKEN
                'c' -> EffigyState.BROKEN
                else -> error("Unable to determine Rift effigy state from color code: $s")
            }

            if (oldState == EffigyState.BROKEN && effigy.state == EffigyState.NOT_BROKEN) {
                ChatUtils.chat("Effigy #${index + 1} respawned!")
            } else if (oldState == EffigyState.NOT_BROKEN && effigy.state == EffigyState.BROKEN) {
                ChatUtils.chat("Effigy #${index + 1} broken!")
                effigies[index]?.respawnTime = SimpleTimeMark.now() + 20.minutes
            }
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        for (entity in EntityUtils.getEntitiesNearby<EntityArmorStand>(LocationUtils.playerLocation(), 15.0)) {
            effigiesTimerPattern.matchMatcher(entity.name) {
                val index = getIndex(entity) ?: continue
                val time = TimeUtils.getDuration(group("time"))
                effigies[index]?.let {
                    it.state = EffigyState.BROKEN
                    it.respawnTime = SimpleTimeMark.now() + time
                }
                continue
            }

            if (effigiesBreakPattern.matches(entity.name)) {
                val index = getIndex(entity) ?: continue
                effigies[index]?.state = EffigyState.NOT_BROKEN
                continue
            }
        }
    }

    @HandleEvent
    fun onRenderWorld(event: RenderWorldEvent) {
        if (!isEnabled()) return

        for ((index, location) in locations.withIndex()) {
            val name = "Effigy #${index + 1}"
            val effigy = effigies[index] ?: continue

            when (effigy.state) {
                EffigyState.BROKEN -> {
                    if (effigy.respawnTime.isFarPast()) {
                        if (config.unknownTime) {
                            event.drawWaypointFilled(location, LorenzColor.GRAY.toColor(), seeThroughBlocks = true)
                            event.drawDynamicText(location, "§7Unknown Time ($name)", 1.5)
                            continue
                        }
                    } else if (config.respawningSoon && effigy.respawnTime.timeUntil() < config.respwningSoonTime.minutes) {
                        event.drawWaypointFilled(location, LorenzColor.YELLOW.toColor(), seeThroughBlocks = true)
                        val time = effigy.respawnTime.timeUntil().format()
                        event.drawDynamicText(location, "§e$name respawning in §b$time", 1.5)
                        continue
                    }
                }

                EffigyState.NOT_BROKEN -> {
                    event.drawWaypointFilled(location, LorenzColor.RED.toColor(), seeThroughBlocks = true)
                    event.drawDynamicText(location, "§cBreak $name!", 1.5)
                    continue
                }

                EffigyState.UNKNOWN -> {
                    if (config.unknownTime) {
                        event.drawWaypointFilled(location, LorenzColor.GRAY.toColor(), seeThroughBlocks = true)
                        event.drawDynamicText(location, "§7Unknown State ($name)", 1.5)
                        continue
                    }
                }
            }

            if (location.distanceToPlayer() <= 15) {
                event.drawDynamicText(location, "§7$name", 1.5)
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.enabled && RiftAPI.inStillgoreChateau()

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(9, "rift.area.stillgoreChateauConfig", "rift.area.stillgoreChateau")
    }
}
