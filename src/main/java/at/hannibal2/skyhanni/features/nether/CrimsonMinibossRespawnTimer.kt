package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.nether.CrimsonMinibossRespawnTimer.MiniBoss.Companion.isSpawned
import at.hannibal2.skyhanni.features.nether.CrimsonMinibossRespawnTimer.MiniBoss.Companion.isSpawningSoon
import at.hannibal2.skyhanni.features.nether.CrimsonMinibossRespawnTimer.MiniBoss.Companion.isTimerKnown
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.isInside
import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.tileentity.TileEntityBeacon
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CrimsonMinibossRespawnTimer {

    private val config get() = SkyHanniMod.feature.crimsonIsle

    private val patternGroup = RepoPattern.group("crimson.miniboss")

    /**
     * REGEX-TEST: §c§lBEWARE - Bladesoul Is Spawning.
     */
    private val spawnPattern by patternGroup.pattern(
        "spawn",
        "§c§lBEWARE - (?<name>.+) Is Spawning\\.",
    )

    /**
     * REGEX-TEST: §f                            §r§6§lBLADESOUL DOWN!
     */
    private val downPattern by patternGroup.pattern(
        "down",
        "§f\\s*§r§6§l(?<name>.+) DOWN!",
    )

    private var currentAreaBoss: MiniBoss? = null

    private var display: Renderable? = null

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        val message = event.message
        downPattern.matchMatcher(message) {
            val miniBoss = MiniBoss.fromName(group("name")) ?: return
            miniBoss.nextSpawnTime = SimpleTimeMark.now() + 2.minutes
            miniBoss.spawned = false
            miniBoss.possibleSpawnTime = null
            miniBoss.foundBeacon = null
            update()
            return
        }
        spawnPattern.matchMatcher(message) {
            val miniBoss = MiniBoss.fromName(group("name")) ?: return
            miniBoss.spawned = true
            miniBoss.possibleSpawnTime = null
            miniBoss.foundBeacon = null
            update()
            return
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        val renderable = display ?: drawDisplay()
        config.minibossTimerPosition.renderRenderable(renderable, posLabel = "Miniboss Timer")
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        updateArea()
        update()
    }

    private fun updateArea() {
        MiniBoss.entries.forEach {
            if (it.lastSeenArea.passedSince() > 2.minutes) {
                it.nextSpawnTime = null
                it.possibleSpawnTime = null
                it.foundBeacon = null
                it.spawned = null
            }
        }
        currentAreaBoss = MiniBoss.entries.firstOrNull {
            it.area.isPlayerInside()
        }
        val now = SimpleTimeMark.now()
        currentAreaBoss?.lastSeenArea = now
        val boss = currentAreaBoss ?: return
        if (boss.isTimerKnown()) return

        val isBossInArea = MobData.skyblockMobs.filter {
            it.name == boss.displayName
        }.any { boss.area.isInside(it.baseEntity.position.toLorenzVec()) }
        if (isBossInArea) {
            boss.spawned = true
            boss.foundBeacon = null
            boss.possibleSpawnTime = null
            return
        }
        boss.spawned = false

        val isThereBeacon = EntityUtils.getAllTileEntities().filter { it is TileEntityBeacon }.any {
            boss.area.isInside(it.pos.toLorenzVec())
        }
        if (boss.foundBeacon == true && !isThereBeacon) {
            boss.foundBeacon = false
            boss.possibleSpawnTime = null
            boss.nextSpawnTime = now + 1.minutes
            return
        }
        if (boss.possibleSpawnTime != null) return
        if (isThereBeacon && boss.foundBeacon == null) {
            boss.foundBeacon = true
            boss.possibleSpawnTime = now + 1.minutes to now + 2.minutes
            return
        }
        if (!isThereBeacon && boss.foundBeacon == null) {
            boss.foundBeacon = false
            boss.possibleSpawnTime = now to now + 1.minutes
        }
    }

    private fun update() {
        display = drawDisplay()
    }

    private fun drawDisplay(): Renderable {
        val lines = MiniBoss.entries.map {
            val timer = it.nextSpawnTime
            val possibleTimer = it.possibleSpawnTime
            Renderable.string(
                buildString {
                    append("§b${it.displayName}: ")
                    if (it.isSpawned()) append("§aSPAWNED!")
                    else when {
                        it.isSpawningSoon() -> append("§6Soon!")
                        it.isTimerKnown() -> append("§e${timer?.timeUntil()?.format()}")
                        possibleTimer != null -> {
                            val (start, end) = possibleTimer
                            if (start.timeUntil().isNegative()) append("§e~Now - ")
                            else append("§e~${start.timeUntil().format()} - ")
                            if (end.timeUntil().isNegative()) append("§eNow")
                            else append("§e${end.timeUntil().format()}")
                        }
                        else -> append("§cUnknown")
                    }
                },
            )
        }
        return Renderable.verticalContainer(lines)
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        MiniBoss.entries.forEach {
            it.nextSpawnTime = null
            it.possibleSpawnTime = null
            it.foundBeacon = null
            it.spawned = null
            it.lastSeenArea = SimpleTimeMark.farPast()
        }
        currentAreaBoss = null
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Crimson Isle Miniboss")
        event.addIrrelevant {
            if (!isEnabled()) {
                add("Feature is Disabled")
                return@addIrrelevant
            }
            add("Current Area Boss: ${currentAreaBoss?.displayName}")
            MiniBoss.entries.forEach {
                add("")
                add(it.displayName)
                add("   Timer ${it.nextSpawnTime?.timeUntil()?.format()}")
                add(
                    "   Possible Timer ${it.possibleSpawnTime?.first?.timeUntil()?.format()} - " + "${
                        it.possibleSpawnTime?.second?.timeUntil()?.format()
                    }",
                )
                add("   Found Beacon ${it.foundBeacon}")
                add("   Spawned ${it.spawned}")
                add("   Last Seen Area ${it.lastSeenArea.passedSince().format()}")
            }
        }
    }

    enum class MiniBoss(
        val displayName: String,
        val area: AxisAlignedBB,
        var nextSpawnTime: SimpleTimeMark? = null,
        var possibleSpawnTime: Pair<SimpleTimeMark, SimpleTimeMark>? = null,
        var foundBeacon: Boolean? = null,
        var spawned: Boolean? = null,
        var lastSeenArea: SimpleTimeMark = SimpleTimeMark.farPast(),
    ) {
        BLADESOUL(
            "Bladesoul",
            LorenzVec(-330, 80, -486).axisAlignedTo(LorenzVec(-257, 107, -545)),
        ),
        MAGE_OUTLAW(
            "Mage Outlaw",
            LorenzVec(-200, 98, -843).axisAlignedTo(LorenzVec(-162, 116, -878)),
        ),
        BARBARIAN_DUKE_X(
            "Barbarian Duke X",
            LorenzVec(-550, 101, -890).axisAlignedTo(LorenzVec(-522, 131, -918)),
        ),
        ASHFANG(
            "Ashfang",
            LorenzVec(-462, 155, -1035).axisAlignedTo(LorenzVec(-507, 131, -955)),
        ),
        MAGMA_BOSS(
            "Magma Boss",
            LorenzVec(-318, 59, -751).axisAlignedTo(LorenzVec(-442, 90, -851)),
        ),
        ;

        override fun toString() = displayName

        companion object {
            fun fromName(spawnName: String) = entries.firstOrNull {
                it.displayName.removeColor().lowercase() == spawnName.lowercase()
            }

            fun MiniBoss.isTimerKnown(): Boolean {
                val timer = nextSpawnTime ?: return false
                return timer.passedSince() < 2.minutes + 5.seconds
            }

            fun MiniBoss.isSpawningSoon(): Boolean {
                if (spawned == true) return false
                val timer = nextSpawnTime ?: return false
                return timer.passedSince() in 0.seconds..10.seconds
            }

            fun MiniBoss.isSpawned(): Boolean {
                if (spawned == true) return true
                val timer = nextSpawnTime ?: return false
                return (timer.passedSince() - 2.minutes) in 0.seconds..20.seconds
            }
        }
    }

    private fun isEnabled() = IslandType.CRIMSON_ISLE.isInIsland() && config.minibossRespawnTimer

}
