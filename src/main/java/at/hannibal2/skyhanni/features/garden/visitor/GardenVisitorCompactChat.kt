package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object GardenVisitorCompactChat {

    private val config get() = VisitorAPI.config

    private val patternGroup = RepoPattern.group("garden.visitor.compact")

    /**
     * REGEX-TEST:     §8+§f2x §dGold Essence
     * REGEX-TEST:     §fDead Bush
     * REGEX-TEST:     §8+§52 Pelts
     * REGEX-TEST:     $8+§215 §7Garden Experience
     * REGEX-TEST:     §8+§35k §7Farming XP
     * REGEX-TEST:     §8+§311k §7Farming XP
     * REGEX-TEST:     §8+§c32 Copper
     * REGEX-TEST:     §7§aFine Flour §8x3
     * REGEX-TEST:     §7§9Turbo-Carrot I Book
     * REGEX-TEST:     §7§8+§d1,241 Gemstone Powder
     * REGEX-TEST:     §7§8+§2Crystal Hollows Pass
     */
    @Suppress("MaxLineLength")
    private val visitorRewardPattern by patternGroup.pattern(
        "visitorreward",
        "^ {4}(?:(?:§.)+\\+)?(?:(?<amountcolor>§.)(?<amount>[\\d,]+(?:\\.?(?:\\d)?k)?)x? )?(?:(?<rewardcolor>(?:§.)+)?(?<reward>.*?))(?: (?:(?:§.)?)?x(?<altamount>\\d+))?\$"
    )

    /**
     * REGEX-TEST: §6§lOFFER ACCEPTED §8with §aLibrarian §8(§a§lUNCOMMON§8)
     * REGEX-TEST: §6§lOFFER ACCEPTED §8with §6Sirius §8(§6§lLEGENDARY§8)
     * REGEX-TEST: §6§lOFFER ACCEPTED §8with §cSpaceman §8(§c§lSPECIAL§8)
     */
    private val fullyAcceptedPattern by patternGroup.pattern(
        "fullyaccepted",
        "§6§lOFFER ACCEPTED §8with (?<color>§.)?(?<name>.*) §8\\((?<rarity>.*)\\)"
    )
    private val discardRewardNamePattern by patternGroup.pattern(
        "disregardrewardname",
        "^(Copper|Farming XP|Farming Experience|Garden Experience|Bits)\$"
    )
    private val rewardsTextPattern by patternGroup.pattern(
        "rewardstext",
        "^ {2}§a§lREWARDS"
    )

    private var visitorAcceptedChat = mutableListOf<String>()
    private var visitorNameFormatted = ""
    private var rewardsList = mutableListOf<String>()

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (GardenAPI.inGarden() && config.compactRewardChat && (
                fullyAcceptedPattern.matcher(event.message.removeResets()).matches() ||
                    visitorRewardPattern.matcher(event.message.removeResets()).matches() ||
                    rewardsTextPattern.matcher(event.message.removeResets()).matches()
                )
        ) {
            handleChat(event)
        }
    }

    private fun handleChat(event: LorenzChatEvent) {
        val transformedMessage = event.message.removeResets()

        fullyAcceptedPattern.matchMatcher(transformedMessage) {
            visitorAcceptedChat = mutableListOf()
            rewardsList = mutableListOf()
            val visitorColor = groupOrNull("color") ?: "§7"
            val visitorName = group("name")
            visitorNameFormatted = "$visitorColor$visitorName"
        }

        // If visitor name has not yet been matched, we aren't looking at a visitor accept message, and can ignore this.
        if (visitorNameFormatted.isBlank()) return

        // Match rewards and transform
        visitorRewardPattern.matchMatcher(transformedMessage) {
            val rewardColor = groupOrNull("rewardcolor")
            val amountColor = groupOrNull("amountcolor")
            val amount = groupOrNull("amount")
            val altAmount = groupOrNull("altamount")
            val reward = group("reward")

            val fullTextColor = if (rewardColor.isNullOrBlank() || rewardColor == "§7") {
                if (amountColor.isNullOrBlank()) "§f"
                else amountColor
            } else rewardColor

            val amountString = if (amount != null) {
                if (discardRewardNamePattern.matcher(reward).matches()) "$amount"
                else "$amount "
            } else {
                if (altAmount == null) "" else "$altAmount "
            }

            // Don't add name for copper, farming XP, garden XP, or bits
            val rewardString = if (discardRewardNamePattern.matcher(reward).matches()) "" else reward

            rewardsList.add(
                "$fullTextColor$amountString$rewardString"
            )
        }

        compactChat(event)
    }

    private fun compactChat(event: LorenzChatEvent) {
        event.blockedReason = "compact_visitor"
        visitorAcceptedChat.add(event.message)
        if (visitorAcceptedChat.size == 3) {
            DelayedRun.runDelayed(200.milliseconds) {
                sendCompact()
            }
        }
    }

    private fun sendCompact() {
        // This prevents commission rewards, crop milestone data, etc. from triggering incorrectly
        if (visitorNameFormatted.isBlank()) return

        if (visitorAcceptedChat.isNotEmpty()) {
            ChatUtils.hoverableChat(createCompactVisitorMessage(), hover = visitorAcceptedChat, prefix = false)
        }

        this.visitorNameFormatted = ""
        this.rewardsList.clear()
    }

    private fun createCompactVisitorMessage(): String {
        val rewardsFormatted = rewardsList.joinToString(separator = "§7, ")
        return "§6§lOFFER ACCEPTED §7w/§r$visitorNameFormatted§6§l!§r $rewardsFormatted"
    }
}
