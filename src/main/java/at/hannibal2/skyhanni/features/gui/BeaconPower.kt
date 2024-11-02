package at.hannibal2.skyhanni.features.gui

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object BeaconPower {

    private val storage get() = ProfileStorageData.profileSpecific?.beaconPower
    private val config get() = SkyHanniMod.feature.gui

    private val group = RepoPattern.group("gui.beaconpower")

    // TODO add regex tests
    private val deactivatedPattern by group.pattern(
        "deactivated",
        "§7Beacon Deactivated §8- §cNo Power Remaining",
    )
    private val timeRemainingPattern by group.pattern(
        "time",
        "§7Power Remaining: §e(?<time>.+)",
    )
    private val boostedStatPattern by group.pattern(
        "stat",
        "§7Current Stat: (?<stat>.+)",
    )
    private val noBoostedStatPattern by group.pattern(
        "nostat",
        "TODO",
    )

    private var expiryTime: SimpleTimeMark
        get() = storage?.beaconPowerExpiryTime ?: SimpleTimeMark.farPast()
        set(value) {
            storage?.beaconPowerExpiryTime = value
        }

    private var stat: String?
        get() = storage?.boostedStat
        set(value) {
            storage?.boostedStat = value
        }

    private var display = ""

    private const val BEACON_POWER_SLOT = 22
    private const val STATS_SLOT = 23

    @SubscribeEvent
    fun onInventoryUpdate(event: InventoryUpdatedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (event.inventoryName != "Beacon") return
        val items = event.inventoryItems

        items[BEACON_POWER_SLOT]?.let { item ->
            item.getLore().forEach {
                if (deactivatedPattern.matches(it)) {
                    expiryTime = SimpleTimeMark.farPast()
                    return@let
                }
                timeRemainingPattern.matchMatcher(it) {
                    val duration = TimeUtils.getDuration(group("time"))
                    expiryTime = SimpleTimeMark.now() + duration
                    return@let
                }
            }
        }

        items[STATS_SLOT]?.let { item ->
            item.getLore().forEach {
                if (noBoostedStatPattern.matches(it)) {
                    stat = null
                    return@let
                }
                boostedStatPattern.matchMatcher(it) {
                    stat = group("stat")
                    return@let
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.beaconPowerPosition.renderString(display, posLabel = "Beacon Power")
    }

    @SubscribeEvent
    fun onSecond(event: SecondPassedEvent) {
        if (!isEnabled()) return
        display = drawDisplay()
    }

    private fun drawDisplay(): String = buildString {
        append("§eBeacon: ")
        if (expiryTime.isInPast()) {
            append("§cNot active")
        } else {
            append("§b${expiryTime.timeUntil().format(maxUnits = 2)}")
            if (config.beaconPowerStat) append(" §7(${stat ?: "§cNo stat"}§7)")
        }
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = ""
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.beaconPower && !LorenzUtils.isBingoProfile
}
