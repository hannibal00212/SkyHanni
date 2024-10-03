package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.EntityMovementData
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CarnivalReminder {

    private val config get() = SkyHanniMod.feature.event.carnival
    private val storage get() = ProfileStorageData.profileSpecific?.carnival

    private var nextCheckTime = SimpleTimeMark.farFuture()

    private var claimedToday = false

    private var lastClaimedDay
        get() = storage?.lastClaimedDay
        set(value) {
            storage?.lastClaimedDay = value
        }

    private val repoGroup = RepoPattern.group("carnival.tickets")

    /** REGEX-TEST: §aYou claimed §r§aCarnival Ticket §r§8x25§r§a!
     */
    private val ticketClaimedPattern by repoGroup.pattern("claimed", "§aYou claimed §r§aCarnival Ticket §r§8x25§r§a!")

    /** REGEX-TEST: §e[NPC] §aCarnival Leader§f: §rYou've already claimed your §aCarnival Tickets §ffor §btoday§f, but I'm happy to answer any questions you might have.
     */
    private val alreadyClaimedPattern by repoGroup.pattern(
        "already",
        "§e\\[NPC\\] §aCarnival Leader§f: §rYou've already claimed your §aCarnival Tickets §ffor §btoday§f, but I'm happy to answer any questions you might have.",
    )

    @SubscribeEvent
    fun onSecondPassedEvent(event: SecondPassedEvent) {
        if (!isEnabled() || nextCheckTime.isInFuture()) return
        check()
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        claimedToday = false
        if (!isEnabled()) return
        nextCheckTime = 30.0.seconds.fromNow()
        checkDate()
        check()
    }

    @SubscribeEvent
    fun onLorenzChat(event: LorenzChatEvent) {
        if (!isEnabled() && !claimedToday) return
        if (!ticketClaimedPattern.matches(event.message) && !alreadyClaimedPattern.matches(event.message)) return
        claimedToday = true
        lastClaimedDay = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate()
    }

    private fun checkDate() {
        val currentDay = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate()
        val lastClaimedDay = lastClaimedDay

        claimedToday = !(lastClaimedDay == null || currentDay.isAfter(lastClaimedDay))
    }

    fun check() {
        if (claimedToday) {
            checkDate()
        } else if (!ReminderUtils.isBusy()) {
            ChatUtils.clickToActionOrDisable(
                "Carnival Tickets are ready to be claimed!",
                config::reminderDailyTickets,
                "warp to The Carnival",
            ) {
                HypixelCommands.warp("carnival")
                EntityMovementData.onNextTeleport(IslandType.HUB) {
                    IslandGraphs.pathFind(LorenzVec(-89.5, 71.0, -18.7), "§aCarnival Tickets", condition = { config.reminderDailyTickets })
                }
            }
            nextCheckTime = 5.0.minutes.fromNow()
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.reminderDailyTickets && Perk.CHIVALROUS_CARNIVAL.isActive
}
