package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.now
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Matcher
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HoppityEggsManager {

    val config get() = SkyHanniMod.feature.event.hoppityEggs

    /**
     * REGEX-TEST: §d§lHOPPITY'S HUNT §r§dYou found a §r§9Chocolate Lunch Egg §r§don a ledge next to the stairs up§r§d!
     * REGEX-TEST: §d§lHOPPITY'S HUNT §r§dYou found a §r§aChocolate Dinner Egg §r§dbehind Emissary Sisko§r§d!
     * REGEX-TEST: §d§lHOPPITY'S HUNT §r§dYou found a §r§9Chocolate Lunch Egg §r§dnear the Diamond Essence Shop§r§d!
     */
    val eggFoundPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.found",
        "§d§lHOPPITY'S HUNT §r§dYou found a §r§.Chocolate (?<meal>\\w+) Egg §r§d(?<note>.*)§r§d!",
    )

    /**
     * REGEX-TEST: §aYou bought §r§9Casanova §r§afor §r§6970,000 Coins§r§a!
     * REGEX-TEST: §aYou bought §r§fHeidie §r§afor §r§6194,000 Coins§r§a!
     */
    val eggBoughtPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.bought",
        "§aYou bought §r(?<rabbitname>.*?) §r§afor §r§6((\\d|,)*) Coins§r§a!",
    )

    /**
     * REGEX-TEST: §D§LHOPPITY'S HUNT §7You found §fArnie §7(§F§LCOMMON§7)!
     * REGEX-TEST: §D§LHOPPITY'S HUNT §7You found §aPenelope §7(§A§LUNCOMMON§7)!
     * REGEX-TEST: §D§LHOPPITY'S HUNT §7You found §6Solomon §7(§6§LLEGENDARY§7)!
     */
    val rabbitFoundPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.found",
        "§D§LHOPPITY'S HUNT §7You found (?<name>.*) §7\\((?<rarity>.*)§7\\)!",
    )

    /**
     * REGEX-TEST: §d§lNEW RABBIT! §6+2 Chocolate §7and §6+0.003x Chocolate §7per second!
     * REGEX-TEST: §d§lNEW RABBIT! §6+0.02x Chocolate §7per second!
     * REGEX-TEST: §d§lNEW RABBIT! §7Your §dTime Tower §7charge time is now §a7h§7!
     */
    val newRabbitFound by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.found.new",
        "§d§lNEW RABBIT! (?:((§6\\+(?<chocolate>.*) Chocolate §7and )?§6\\+(?<perSecond>.*)x Chocolate §7per second!)|(?<other>.*))",
    )

    val duplicateRabbitFound by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.duplicate",
        "§7§lDUPLICATE RABBIT! §6\\+(?<amount>[\\d,]+) Chocolate",
    )

    private val noEggsLeftPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.noneleft",
        "§cThere are no hidden Chocolate Rabbit Eggs nearby! Try again later!",
    )
    private val eggSpawnedPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.spawned",
        "§d§lHOPPITY'S HUNT §r§dA §r§.Chocolate (?<meal>\\w+) Egg §r§dhas appeared!",
    )
    private val eggAlreadyCollectedPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.alreadycollected",
        "§cYou have already collected this Chocolate (?<meal>\\w+) Egg§r§c! Try again when it respawns!",
    )
    private val hoppityEventNotOn by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.notevent",
        "§cThis only works during Hoppity's Hunt!",
    )

    private var lastMeal: HoppityEggType? = null
    private var lastNote: String? = null

    // has claimed all eggs at least once
    private var warningActive = false
    private var lastWarnTime = SimpleTimeMark.farPast()

    private var latestWaypointOnclick: () -> Unit = {}

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastMeal = null
        lastNote = null
    }

    @HandleEvent
    fun onEggFound(event: EggFoundEvent) {
        if (!HoppityAPI.isHoppityEvent() || !event.type.isResetting) return
        HoppityEggLocations.saveNearestEgg()
        event.type.markClaimed()
        lastMeal = event.type
        lastNote = event.note
    }

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onRabbitFound(event: RabbitFoundEvent) {
        DelayedRun.runDelayed(1.seconds) {
            HoppityCollectionStats.incrementRabbitCount(event.rabbitName)
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        hoppityEventNotOn.matchMatcher(event.message) {
            val currentYear = SkyBlockTime.now().year

            if (config.timeInChat) {
                val timeUntil = SkyBlockTime(currentYear + 1).asTimeMark().timeUntil()
                ChatUtils.chat("§eHoppity's Hunt is not active. The next Hoppity's Hunt is in §b${timeUntil.format()}§e.")
                event.blockedReason = "hoppity_egg"
            }
            return
        }

        if (!HoppityAPI.isHoppityEvent()) return

        noEggsLeftPattern.matchMatcher(event.message) {
            HoppityEggType.allFound()

            if (config.timeInChat) {
                val nextEgg = HoppityEggType.resettingEntries.minByOrNull { it.timeUntil() } ?: return
                ChatUtils.chat("§eNext egg available in §b${nextEgg.timeUntil().format()}§e.")
                event.blockedReason = "hoppity_egg"
            }
            return
        }

        eggAlreadyCollectedPattern.matchMatcher(event.message) {
            getEggType(event).markClaimed()
            if (config.timeInChat) {
                val nextEgg = HoppityEggType.resettingEntries.minByOrNull { it.timeUntil() } ?: return
                ChatUtils.chat("§eNext egg available in §b${nextEgg.timeUntil().format()}§e.")
                event.blockedReason = "hoppity_egg"
            }
            return
        }

        eggSpawnedPattern.matchMatcher(event.message) {
            getEggType(event).markSpawned()
            return
        }
    }

    internal fun Matcher.getEggType(event: LorenzChatEvent): HoppityEggType =
        HoppityEggType.getMealByName(group("meal")) ?: run {
            ErrorManager.skyHanniError(
                "Unknown meal: ${group("meal")}",
                "message" to event.message,
            )
        }

    fun getAndDisposeWaypointOnclick(): () -> Unit {
        val onClick = latestWaypointOnclick
        latestWaypointOnclick = {}
        return onClick
    }

    fun shareWaypointPrompt() {
        if (!config.sharedWaypoints) return
        val meal = lastMeal ?: return
        val note = lastNote ?: return
        lastMeal = null
        lastNote = null

        val currentLocation = LocationUtils.playerLocation()
        DelayedRun.runNextTick {
            latestWaypointOnclick = { HoppityEggsShared.shareNearbyEggLocation(currentLocation, meal, note) }
            if (config.compactChat) return@runNextTick
            ChatUtils.clickableChat(
                "Click here to share the location of this chocolate egg with the server!",
                onClick = latestWaypointOnclick,
                "§eClick to share!",
                expireAt = 30.seconds.fromNow(),
                oneTimeClick = true,
            )
            latestWaypointOnclick = {}
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isActive()) return
        HoppityEggType.checkClaimed()
        checkWarn()
    }

    private fun checkWarn() {
        val allEggsRemaining = HoppityEggType.allEggsRemaining()
        if (!warningActive) {
            warningActive = !allEggsRemaining
        }

        if (warningActive && allEggsRemaining) {
            warn()
        }
    }

    private fun warn() {
        if (!config.warnUnclaimedEggs) return
        if (ReminderUtils.isBusy() && !config.warnWhileBusy) return
        if (lastWarnTime.passedSince() < 1.minutes) return

        lastWarnTime = now()
        val amount = HoppityEggType.resettingEntries.size
        val message = "All $amount Hoppity Eggs are ready to be found!"
        if (config.warpUnclaimedEggs) {
            val (action, actionName) = if (LorenzUtils.inSkyBlock) {
                { HypixelCommands.warp(config.warpDestination) } to "warp to ${config.warpDestination}".trim()
            } else {
                { HypixelCommands.skyblock() } to "join /skyblock!"
            }
            ChatUtils.clickToActionOrDisable(
                message,
                config::warpUnclaimedEggs,
                actionName = actionName,
                action = action,
            )
        } else ChatUtils.chat(message)
        LorenzUtils.sendTitle("§e$amount Hoppity Eggs!", 5.seconds)
        SoundUtils.repeatSound(100, 10, SoundUtils.plingSound)
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(
            44,
            "event.chocolateFactory.highlightHoppityShop",
            "event.chocolateFactory.hoppityEggs.highlightHoppityShop",
        )
        event.move(44, "event.chocolateFactory.hoppityEggs", "event.hoppityEggs")
        event.move(50, "event.hoppityEggs.showDuringContest", "event.hoppityEggs.showWhileBusy")
        event.move(62, "event.hoppityEggs.uniquesWarpMenu", "event.hoppityEggs.warpMenu.enabled")
        event.move(62, "event.hoppityEggs.uniquesWarpMenuHideMax", "event.hoppityEggs.warpMenu.hideWhenMaxed")
    }

    fun isActive() = (LorenzUtils.inSkyBlock || (LorenzUtils.onHypixel && config.showOutsideSkyblock)) &&
        HoppityAPI.isHoppityEvent()
}
