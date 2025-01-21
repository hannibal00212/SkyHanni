package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.garden.contests.rewards.ContestRewardSet
import at.hannibal2.skyhanni.events.garden.contests.rewards.ContestRewardsClaimedEvent
import at.hannibal2.skyhanni.features.garden.AnitaMedalProfit
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object CompactJacobClaim {

    private val config get() = SkyHanniMod.feature.chat
    private val patternGroup = RepoPattern.group("chat.jacobcompact")

    // <editor-fold desc="Patterns">

    /**
     * REGEX-TEST:   §r§6§lFARMING CONTEST REWARDS CLAIMED
     */
    private val openingPattern by patternGroup.pattern(
        "opening",
        " {2}§r§6§lFARMING CONTEST REWARDS CLAIMED"
    )

    /**
     * REGEX-TEST:   §r§a§lREWARDS
     */
    private val rewardsPattern by patternGroup.pattern(
        "rewards",
        " {2}§r§a§lREWARDS"
    )

    /**
     * REGEX-TEST:     §r§7§aJacob's Ticket §8x271
     * REGEX-TEST:     §r§7§aCarnival Ticket §8x21
     */
    private val ticketPattern by patternGroup.pattern(
        "tickets",
        " {4}§r§7§a(?<type>Jacob's|Carnival) Ticket §8x(?<amount>[\\d,]+)"
    )

    /**
     * REGEX-TEST:     §r§7§81x §9Turbo-Cacti I Book
     * REGEX-TEST:     §r§7§81x §9Turbo-Pumpkin I Book
     * REGEX-TEST:     §r§7§84x §9Turbo-Wheat I Book
     * REGEX-TEST:     §r§7§86x §9Turbo-Mushrooms I Book
     * REGEX-TEST:     §r§7§81x §9Turbo-Warts I Book
     */
    private val bookPattern by patternGroup.pattern(
        "book",
        " {4}§r§7§8(?<amount>[\\d,]+)x §9Turbo-(?<crop>[^ ]+) I Book"
    )

    /**
     * REGEX-TEST:     §r§7§8+§e8 §7gold medals
     * REGEX-TEST:     §r§7§8+§e7 §7silver medals
     * REGEX-TEST:     §r§7§8+§e5 §7bronze medals
     */
    private val medalsPattern by patternGroup.pattern(
        "medals",
        " {4}§r§7§8\\+§e(?<amount>[\\d,]+) §7(?<type>[^ ]+) medals?"
    )

    /**
     * REGEX-TEST:     §r§8+§r§b2,293 Bits
     */
    private val bitsPattern by patternGroup.pattern(
        "bits",
        " {4}§r§8\\+§r§b(?<amount>[\\d,]+) Bits"
    )
    // </editor-fold>

    private var inLoop = false
    private var rewardSet = ContestRewardSet()
    private val messageSet: MutableList<String> = mutableListOf()

    private fun publishEvent() {
        ContestRewardsClaimedEvent(rewardSet, messageSet).post()
        rewardSet.reset()
        messageSet.clear()
    }

    private val shorteningMap: Map<CropType, Pair<LorenzColor, String>> = mapOf(
        CropType.CACTUS to Pair(LorenzColor.GREEN, "Ca"),
        CropType.SUGAR_CANE to Pair(LorenzColor.GREEN, "Su"),
        CropType.MELON to Pair(LorenzColor.GREEN, "Me"),

        CropType.MUSHROOM to Pair(LorenzColor.RED, "Mu"),
        CropType.NETHER_WART to Pair(LorenzColor.RED, "Ne"),

        CropType.CARROT to Pair(LorenzColor.GOLD, "Ca"),
        CropType.COCOA_BEANS to Pair(LorenzColor.GOLD, "Co"),
        CropType.POTATO to Pair(LorenzColor.GOLD, "Po"),
        CropType.PUMPKIN to Pair(LorenzColor.GOLD, "Pu"),
        CropType.WHEAT to Pair(LorenzColor.GOLD, "Wh"),
    )

    private fun LorenzChatEvent.block(reason: String) {
        messageSet.add(message)
        blockedReason = reason
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!config.compactJacobClaim) return
        val message = event.message
        var eventDelay = 300.milliseconds

        openingPattern.matchMatcher(message) {
            inLoop = true
            return event.block("compact_jacob_bulk_claim")
        }

        if (!inLoop) return

        rewardsPattern.matchMatcher(message) {
            return event.block("compact_jacob_bulk_claim")
        }

        val startingHash = rewardSet.hashCode()

        ticketPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            when (group("type")) {
                "Jacob's" -> rewardSet = rewardSet.copy(jacobTickets = amount)
                "Carnival" -> rewardSet = rewardSet.copy(carnivalTickets = amount)
            }
        }

        bookPattern.matchMatcher(message) {
            val crop = CropType.getByNameOrNull(group("crop")) ?: when (group("crop").lowercase()) {
                "cacti" -> CropType.CACTUS
                else -> return
            }
            val amount = group("amount").formatInt()
            rewardSet = rewardSet.copy(books = rewardSet.books + (crop to amount))
        }

        medalsPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            val type = group("type")
            val medalType = AnitaMedalProfit.MedalType.bySimpleNameOrNull(type) ?: return
            rewardSet = rewardSet.copy(medals = rewardSet.medals + (medalType to amount))
        }

        bitsPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            rewardSet = rewardSet.copy(bits = amount)
            eventDelay = 0.milliseconds
        }

        if (rewardSet.hashCode() != startingHash) event.block("compact_jacob_bulk_claim")

        val hashNow = rewardSet.hashCode()
        DelayedRun.runDelayed(eventDelay) {
            if (rewardSet.hashCode() == hashNow) {
                inLoop = false
                publishEvent()
            }
        }
    }

    @HandleEvent
    fun onContestRewardsClaimed(event: ContestRewardsClaimedEvent) {
        if (!config.compactJacobClaim) return
        event.sendCompact()
    }

    private fun ContestRewardsClaimedEvent.sendCompact() = ChatUtils.hoverableChat(
        listOf(
            "§6§lContest Rewards",
            getTicketsFormat(),
            getBooksFormat(),
            getMedalsFormat(),
            getBitsFormat()
        ).joinToString(separator = " §8§l| §r"),
        hover = messages,
        prefix = false
    )

    private fun ContestRewardsClaimedEvent.getTicketsFormat() = buildString {
        if (rewards.jacobTickets == 0 && rewards.carnivalTickets == 0) return@buildString
        val ticketList = listOf(
            "§a${rewards.jacobTickets} Ja".takeIf { rewards.jacobTickets > 0 }.orEmpty(),
            "§a${rewards.carnivalTickets} Ca".takeIf { rewards.carnivalTickets > 0 }.orEmpty()
        ).filterNot { it.isEmpty() }
        append("Tickets: " + ticketList.joinToString(separator = "§7, ") { it })
    }

    private fun ContestRewardsClaimedEvent.getBooksFormat() = buildString {
        val books = rewards.books.takeIf { it.isNotEmpty() } ?: return@buildString
        val bookList = books.map { (crop, amount) ->
            val (color, shortName) = shorteningMap[crop] ?: return@map ""
            "${color.getChatColor()}$amount $shortName".takeIf { amount > 0 }.orEmpty()
        }.filterNot { it.isEmpty() }.sortedBy { it.removeColor() }
        append("Books: " + bookList.joinToString(separator = "§7, ") { it })
    }

    private fun ContestRewardsClaimedEvent.getMedalsFormat() = buildString {
        val medals = rewards.medals.takeIf { it.isNotEmpty() } ?: return@buildString
        val medalList = medals.toSortedMap(compareBy { it.ordinal }).map { (medalType, amount) ->
            "${medalType.color.getChatColor()}$amount".takeIf { amount > 0 }.orEmpty()
        }.filterNot { it.isEmpty() }
        append("Medals: " + medalList.joinToString(separator = "§7, ") { it })
    }

    private fun ContestRewardsClaimedEvent.getBitsFormat() = buildString {
        if (rewards.bits == 0) return@buildString
        append("Bits: §b${rewards.bits}")
    }
}
