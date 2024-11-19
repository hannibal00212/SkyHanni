package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.PurseChangeCause
import at.hannibal2.skyhanni.events.PurseChangeEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.million
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PurseAPI {
    private val patternGroup = RepoPattern.group("data.purse")

    /**
     * REGEX-TEST: Piggy: §6423,085,766
     * REGEX-TEST: Purse: §6423,085,776 §e(+5)
     */
    val coinsPattern by patternGroup.pattern(
        "coins",
        "(?:§.)*(?:Piggy|Purse): §6(?<coins>[\\d,.]+)(?: ?(?:§.)*\\([+-](?<earned>[\\d,.]+)\\)?|.*)?$",
    )

    /**
     * REGEX-TEST: Piggy: §6423,085,766
     */
    val piggyPattern by patternGroup.pattern(
        "piggy",
        "Piggy: (?<coins>.*)",
    )

    private var inventoryCloseTime = SimpleTimeMark.farPast()
    var currentPurse = 0.0
        private set

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inventoryCloseTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        coinsPattern.firstMatcher(event.added) {
            val newPurse = group("coins").formatDouble()
            val diff = newPurse - currentPurse
            if (diff == 0.0) return
            currentPurse = newPurse

            PurseChangeEvent(diff, currentPurse, getCause(diff)).postAndCatch()
        }
    }

    // TODO add more causes in the future (e.g. ah/bz/bank)
    private fun getCause(diff: Double): PurseChangeCause {
        if (diff > 0) {
            if (diff == 1.0) {
                return PurseChangeCause.GAIN_TALISMAN_OF_COINS
            }

            // TODO relic of coins support
            if (diff == 15.million || diff == 100.million) {
                return PurseChangeCause.GAIN_DICE_ROLL
            }

            if (Minecraft.getMinecraft().currentScreen == null) {
                if (inventoryCloseTime.passedSince() > 2.seconds) {
                    return PurseChangeCause.GAIN_MOB_KILL
                }
            }
            return PurseChangeCause.GAIN_UNKNOWN
        } else {
            if (SlayerAPI.questStartTime.passedSince() < 1.5.seconds) {
                return PurseChangeCause.LOSE_SLAYER_QUEST_STARTED
            }

            if (diff == -6_666_666.0 || diff == -666_666.0) {
                return PurseChangeCause.LOSE_DICE_ROLL_COST
            }

            return PurseChangeCause.LOSE_UNKNOWN
        }
    }

    fun getPurse(): Double = currentPurse
}
