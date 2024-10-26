package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.PurseChangeCause
import at.hannibal2.skyhanni.events.PurseChangeEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.million
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PurseAPI {

    private val storage get() = ProfileStorageData.profileSpecific

    private val patternGroup = RepoPattern.group("data.purse")
    val coinsPattern by patternGroup.pattern(
        "coins",
        "(?:§.)*(?:Piggy|Purse): §6(?<coins>[\\d,.]+)(?: ?(?:§.)*\\([+-](?<earned>[\\d,.]+)\\)?|.*)?$",
    )
    val piggyPattern by patternGroup.pattern(
        "piggy",
        "Piggy: (?<coins>.*)",
    )

    private var inventoryCloseTime = SimpleTimeMark.farPast()
    var currentPurse: Long
        get() = storage?.purse ?: 0
        private set(value) {
            storage?.purse = value
        }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inventoryCloseTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        coinsPattern.firstMatcher(event.added) {
            val newPurse = group("coins").formatLong()
            val diff = (newPurse - currentPurse).toInt()
            if (diff == 0) return
            currentPurse = newPurse

            PurseChangeEvent(diff, currentPurse, getCause(diff)).postAndCatch()
        }
    }

    // TODO add more causes in the future (e.g. ah/bz/bank)
    private fun getCause(diff: Int): PurseChangeCause {
        return if (diff > 0) {
            when (diff) {
                1 -> PurseChangeCause.GAIN_TALISMAN_OF_COINS
                // TODO relic of coins support
                15.million, 100.million -> PurseChangeCause.GAIN_DICE_ROLL
                else -> {
                    if (Minecraft.getMinecraft().currentScreen == null && inventoryCloseTime.passedSince() > 2.seconds) {
                        PurseChangeCause.GAIN_MOB_KILL
                    } else PurseChangeCause.GAIN_UNKNOWN
                }
            }
        } else {
            when {
                SlayerAPI.questStartTime.passedSince() < 1.5.seconds -> PurseChangeCause.LOSE_SLAYER_QUEST_STARTED
                diff == -6_666_666 || diff == -666_666 -> PurseChangeCause.LOSE_DICE_ROLL_COST
                else -> PurseChangeCause.LOSE_UNKNOWN
            }
        }
    }

    @Deprecated("", ReplaceWith("PurseAPI.currentPurse.toDouble()"))
    fun getPurse(): Double = currentPurse.toDouble()
}
