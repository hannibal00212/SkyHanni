package at.hannibal2.skyhanni.features.rift.area.mountaintop

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.rift.area.mountaintop.SunGeckoConfig
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.skyblock.ScoreboardAreaChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SunGeckoHelper {
    val config: SunGeckoConfig get() = SkyHanniMod.feature.rift.area.mountaintop.sunGecko
    val pos: Position get() = config.pos
    var display: ArrayList<String> = ArrayList()
    var modifiers: MutableSet<MODIFIERS> = mutableSetOf()
    private val patternGroup = RepoPattern.group("rift.area.mountaintop.sungecko")

    /**
     * REGEX-TEST: §a﴾ §8[§7Lv20§8] §c§l§eSun Gecko§r§r §e45§f/§a250§c❤ §a﴿
     * REGEX-TEST: §a﴾ §8[§7Lv20§8] §c§l§eSun Gecko§r§r §a250§f/§a250§c❤ §a﴿
     */
    private val sunGeckoName by patternGroup.pattern(
        "name",
        "§a﴾ §8\\[§7Lv20§8] §c§l§eSun Gecko§r§r (?<healthColor>§[aec])(?<healthLeft>\\d+)§f/§a(?<totalHealth>\\d+)§c❤ §a﴿"
    )

    /**
     * REGEX-TEST: §a[⬛⬛⬛⬜§c⬜ §e§lx2 §c⬜⬜⬜⬜⬜]
     * REGEX-TEST: §c[⬜⬜⬜⬜⬜ §e§lx1 §c⬜⬜⬜⬜⬜]
     * REGEX-TEST: §a[⬛⬛⬛⬛§c⬛ §e§lx1 §c⬛⬜⬜⬜⬜]
     * REGEX-TEST: §a[⬛⬛⬛⬛⬛ §e§lx1 §a⬛§c⬛⬛⬜⬜]
     * REGEX-TEST: §a[⬛⬛⬛⬛⬛ §e§lx1 §c§c⬛⬛⬜⬜⬜]
     * REGEX-TEST: §a[⬛⬛⬛§c⬛⬛ §e§lx2 §c⬛⬛⬛⬜⬜]
     */
    private val sunGeckoActionBar by patternGroup.pattern(
        "actionbar",
        "(?<firstHalf>§[ac]\\[.*) §e§lx(?<combo>\\d+) (?<secondHalf>§[ac].*)]"
    )

    private var healthLeft = 250
    private var totalHealth = 250
    private var actionBarFormatted: String = ""
    private var healthColor: String = "§a"
    private var onFirstPhase = true
    private var timeSliceDuration = Duration.ZERO
    private var timeSinceLastHit = SimpleTimeMark.farPast()
    private var combo = 1
    private var scanningChat = false
    private var inTimeChamber = false

    fun reset() {
        healthLeft = 250
        totalHealth = 250
        actionBarFormatted = ""
        healthColor = "§a"
        onFirstPhase = true
        timeSliceDuration = Duration.ZERO
        timeSinceLastHit = SimpleTimeMark.farPast()
        combo = 1
        scanningChat = false
        inTimeChamber = false
    }

    fun updateDisplay() {
        display.clear()
        var displayHealthLeft = healthLeft
        var displayTotalHealth = totalHealth
        if (modifiers.contains(MODIFIERS.REVIVAL)) {
            if (onFirstPhase) displayHealthLeft += totalHealth
            displayTotalHealth *= 2
        }

        val health = "$healthColor$displayHealthLeft§f/§a$displayTotalHealth§c❤"
        display.add("§eSun Gecko $health")
        display.add("$actionBarFormatted §e§lCombo: x$combo")

        //this is just a total guess but it looks right enough
        //i think its inconsistent because of how often the action bar updates
        var expiryTime = 5.seconds + 200.milliseconds
        if (modifiers.contains(MODIFIERS.COLLECTIVE)) {
            expiryTime += (modifiers.count() * 200).milliseconds
        }
        val timeLeft = timeSinceLastHit + expiryTime
        if (timeLeft.timeUntil().inWholeMilliseconds > expiryTime.inWholeMilliseconds - 800.milliseconds.inWholeMilliseconds ||
            timeLeft.isInPast()) {
            display.add("§aCombo Timer: ${expiryTime.format()}/${expiryTime.format()}")
        } else {
            display.add("§aCombo Timer: ${timeLeft.timeUntil().format(showMilliSeconds = true)}/${expiryTime.format()}")
        }

        if (config.showModifiers) {
            display.add("§6Modifiers:")
            for (modifier in modifiers) {
                display.add("§e${modifier.name.allLettersFirstUppercase()}")
            }
        }
    }

    @SubscribeEvent
    fun onMobSpawn(event: MobEvent.Spawn) {
        if (!event.mob.name.contains("Sun Gecko")) return
        if (event.mob.name.contains("?") && config.highlightFakeBoss) {
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                event.mob.baseEntity,
                Color.RED.addAlpha(80),
            ) { config.highlightFakeBoss }
        } else if (config.highlightRealBoss) {
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                event.mob.baseEntity,
                Color.GREEN.addAlpha(80),
            ) { config.highlightRealBoss }
        }

    }


    @SubscribeEvent
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!inTimeChamber) return

        var nearestEntityDistance = 100.0
        var nearestEntity: Entity? = null
        for (entity in EntityUtils.getAllEntities()) {
            sunGeckoName.matchMatcher(entity.name) {
                if (entity.distanceToPlayer() < nearestEntityDistance) {
                    nearestEntityDistance = entity.distanceToPlayer()
                    nearestEntity = entity
                }
            }
        }

        if (nearestEntity == null) return

        sunGeckoName.matchMatcher(nearestEntity?.name.orEmpty()) {
            val health = group("healthLeft")?.toIntOrNull() ?: -1
            if (health > healthLeft) {
                onFirstPhase = false
                modifiers.add(MODIFIERS.REVIVAL)
            }
            healthLeft = health
            totalHealth = group("totalHealth")?.toIntOrNull() ?: 250
            healthColor = group("healthColor")?.toString() ?: "§a"
        }

        updateDisplay()

        pos.renderStrings(display, 0, "Sun Gecko Helper")
    }

    @HandleEvent
    fun onActionBar(event: ActionBarUpdateEvent) {
        if (!isEnabled()) return
        sunGeckoActionBar.findMatcher(event.actionBar) {
            val firstHalf = groupOrNull("firstHalf") ?: return
            val secondHalf = groupOrNull("secondHalf") ?: return
            combo = groupOrNull("combo")?.toIntOrNull() ?: return

            val firstHalfFull = firstHalf.count { it == '⬛' }
            val secondHalfFull = secondHalf.count { it == '⬛' }
            var comboHitCount = firstHalfFull + secondHalfFull
            var totalHits = 10

            if (!group().contains("§c")) {
                timeSinceLastHit = SimpleTimeMark.now()
            }

            if (modifiers.contains(MODIFIERS.CULMINATION)) {
                totalHits -= 1
            }
            if (modifiers.contains(MODIFIERS.TIME_SLICED)) {
                if (timeSliceDuration.inWholeSeconds > 150) {
                    totalHits -= 1
                }
            }

            if (comboHitCount == 9 && totalHits == 8) {

                // this is a hypixel bug
                // it goes from 8/8 to 9/8 to 2/8
                // the combo does not go up at 9/8
                // so i guess the overlay is wrong but whatever
                comboHitCount = 1
            }
            actionBarFormatted = "§a$comboHitCount/$totalHits"
        }
    }

    @HandleEvent
    fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
        if (!isEnabled()) return
        for (line in event.full) {
            if (line.startsWith(" Big damage in: §d")) {
                modifiers.add(MODIFIERS.TIME_SLICED)
                timeSliceDuration = TimeUtils.getDuration(line.replace(" Big damage in: §d", ""))
            }
        }
    }


    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (event.message == "§f                           §r§c§lACTIVE MODIFIERS!") {
            scanningChat = true
        } else if (event.message == "§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬") {
            scanningChat = false
        }

        if (scanningChat) {
            for (modifier in MODIFIERS.entries) {
                if (event.message.contains(modifier.name.allLettersFirstUppercase())) {
                    modifiers.add(modifier)
                }
            }
        }
    }

    @HandleEvent
    fun onAreaChanged(event: ScoreboardAreaChangeEvent) {
        if (!isEnabled()) return
        reset()
        if (event.area == "Time Chamber") {
            inTimeChamber = true
        }
    }

    fun isEnabled() = config.enabled && RiftAPI.inRift() && RiftAPI.inMountainTop()

    enum class MODIFIERS {
        REVIVAL, // spawns a second dude
        COMBO_MANIC,
        TIME_SLICED, // reduces combo lvl up by 1 for 30 seconds only
        BUFFANTICS,
        COLLECTIVE, // increase time for combo by 0.2 per modifier
        BRAND_NEW_DANCE,
        CULMINATION, // reduces combo lvl up by 1
    }


}
