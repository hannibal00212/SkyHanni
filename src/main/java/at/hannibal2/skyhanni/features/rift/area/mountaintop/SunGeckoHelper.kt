package at.hannibal2.skyhanni.features.rift.area.mountaintop

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.rift.area.mountaintop.SunGeckoConfig
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.skyblock.ScoreboardAreaChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
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
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SunGeckoHelper {
    val config: SunGeckoConfig = SkyHanniMod.feature.rift.area.mountaintop.sunGecko
    val pos: Position = config.pos
    var display: ArrayList<String> = ArrayList()
    var modifiers: MutableSet<MODIFIERS> = mutableSetOf()
    private val patternGroup = RepoPattern.group("rift.area.mountaintop.sungecko")

    /**
     * REGEX-TEST: §a﴾ §8[§7Lv20§8] §c§l§eSun Gecko§r§r §e45§f/§a250§c❤ §a﴿
     * REGEX-TEST: §a﴾ §8[§7Lv20§8] §c§l§eSun Gecko§r§r §a250§f/§a250§c❤ §a﴿
     */

    private val sunGeckoName by patternGroup.pattern(
        "name",
        "§a﴾ §8\\[§7Lv20§8] §c§l§eSun Gecko§r§r (?<healthColour>§[aec])(?<healthLeft>\\d+)§f/§a(?<totalHealth>\\d+)§c❤ §a﴿"
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
        "(?<firstHalf>§(?<firstColour>[ac])\\[.*) §e§lx(?<combo>\\d+) (?<secondHalf>§(?<lastColour>[ac]).*)]"
    )

    private var healthLeft = 250
    private var totalHealth = 250
    private var actionBarFormatted: String = ""
    private var healthColour: String = "§a"
    private var onFirstPhase = true
    private var timeSliceDuration = Duration.ZERO
    private var timeSinceLastHit = SimpleTimeMark.farPast()
    private var combo = 1

    fun reset() {
        healthLeft = 250
        totalHealth = 250
        actionBarFormatted = ""
        healthColour = "§a"
        onFirstPhase = true
        timeSliceDuration = Duration.ZERO
        timeSinceLastHit = SimpleTimeMark.farPast()
        combo = 1
    }

    fun updateDisplay() {
        display.clear()
        var displayHealthLeft = healthLeft
        var displayTotalHealth = totalHealth
        if (modifiers.contains(MODIFIERS.REVIVAL)) {
            if (onFirstPhase) displayHealthLeft += totalHealth
            displayTotalHealth *= 2
        }

        val health = "$healthColour$displayHealthLeft§f/§a$displayTotalHealth§c❤"
        display.add("§eSun Gecko $health")
        display.add("$actionBarFormatted §e§lCombo: x$combo")
        //this is just a total guess but it looks right enough
        //i think its inconsistent because of how often the action bar updates
        var expiryTime = 5.seconds + 200.milliseconds
        if (modifiers.contains(MODIFIERS.COLLECTIVE)) {
            expiryTime += (modifiers.count() * 200).milliseconds
        }
        val timeLeft = timeSinceLastHit + expiryTime
        if (timeLeft.timeUntil().inWholeMilliseconds > expiryTime.inWholeMilliseconds - 300.milliseconds.inWholeMilliseconds) {
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
    fun onGuiRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

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

        sunGeckoName.matchMatcher(nearestEntity?.name ?: "") {
            val health = group("healthLeft")?.toIntOrNull() ?: -1
            if (health > healthLeft) {
                onFirstPhase = false
                modifiers.add(MODIFIERS.REVIVAL)
            }
            healthLeft = health
            totalHealth = group("totalHealth")?.toIntOrNull() ?: 250
            healthColour = group("healthColour")?.toString() ?: "§a"
        }

        updateDisplay()

        pos.renderStrings(display, 0, "Sun Gecko Helper")
    }

    @SubscribeEvent
    fun onInventoryFullyOpened(event: InventoryUpdatedEvent) {
        if (!isEnabled()) return
        if (event.inventoryName != "Modifiers") return
        modifiers.clear()

        for (modifier in MODIFIERS.entries) {
            val slot = modifier.slot
            val item = event.inventoryItems[slot] ?: continue
            if (item.item != Item.getItemFromBlock(Blocks.stained_glass_pane)) continue
            if (item.itemDamage != 5) continue
            modifiers.add(modifier)
        }

    }

    @HandleEvent
    fun onActionBar(event: ActionBarUpdateEvent) {
        if (!isEnabled()) return
        sunGeckoActionBar.findMatcher(event.actionBar) {
            val firstHalf = groupOrNull("firstHalf") ?: return
            val secondHalf = groupOrNull("secondHalf") ?: return
            combo = groupOrNull("combo")?.toIntOrNull() ?: return
            val firstColour = groupOrNull("firstColour") ?: return
            val lastColour = groupOrNull("lastColour") ?: return

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
                //this is a hypixel bug
                //it goes from 8/8 to 9/8 to 2/8
                //the combo does not go up at 9/8
                //so i guess the overlay is wrong but whatever
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

    @HandleEvent
    fun onAreaChanged(event: ScoreboardAreaChangeEvent) {
        if (!isEnabled()) return
        reset()
    }

    fun isEnabled() = SkyHanniMod.feature.rift.area.mountaintop.sunGecko.enabled && RiftAPI.inRift() && RiftAPI.inMountainTop()

    enum class MODIFIERS(val slot: Int) {
        REVIVAL(19), /* spawns a second dude*/
        COMBO_MANIAC(20),
        TIME_SLICED(21), /* reduces combo lvl up by 1 for 30 seconds only*/
        BUFFANTICS(22),
        COLLECTIVE(23), /* increase time for combo by 0.2 per modifier */
        BRAND_NEW_DANCE(24),
        CULMINATION(25), /* reduces combo lvl up by 1*/
    }


}
