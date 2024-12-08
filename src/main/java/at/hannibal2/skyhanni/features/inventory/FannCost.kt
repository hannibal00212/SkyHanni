package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.insertLineAfter
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern
import kotlin.time.DurationUnit

@SkyHanniModule
object FannCost {

    private val config get() = SkyHanniMod.feature.inventory.fannCost
    private val showCoins get() = config.coinsPerXP
    private val showBits get() = config.xpPerBit
    private var trainingMode: TrainingMode = TrainingMode.DAY_COUNT

    private val patternGroup = RepoPattern.group("fann.inventory")

    /**
     * REGEX-TEST: Training Slot 1
     * REGEX-TEST: Training Slot 2
     * REGEX-TEST: Training Slot 3
     */
    private val trainingSlotInventoryPattern by patternGroup.pattern(
        "training",
        "Training Slot [1-3]",
    )

    /**
     * REGEX-TEST: §aBegin Training
     */
    private val anvilPattern by patternGroup.pattern(
        "anvil",
        "§aBegin Training",
    )

    /**
     * REGEX-TEST: Will earn a total of 1,000,000 EXP.
     * REGEX-TEST: Will earn a total of 2 EXP
     * REGEX-TEST: Will earn a total of 1,000 EXP
     */
    private val expEarnedPattern by patternGroup.pattern(
        "exp.total",
        "Will earn a total of (?<expEarned>.*) EXP\\.?",
    )

    /**
     * REGEX-TEST: EXP Per Day: 1,000
     * REGEX-TEST: EXP Per Day: 1,230,000 (+3.4%)
     * REGEX-TEST: EXP Per Day: 1,623,000 (+9.1%)
     * REGEX-TEST: EXP Per Day: 1
     */
    private val dailyExpPattern by patternGroup.pattern(
        "exp.daily",
        "EXP Per Day: (?<expDaily>\\d[\\d,]*)",
    )

    /**
     * REGEX-TEST: Will take: 1d 0h 0m 0s
     * REGEX-TEST: Will take: 3d 11h 10m 35s
     * REGEX-TEST: Will take: 0d 0h 0m 1s
     * REGEX-TEST: Will take: 493d 19h 49m 59s
     */
    private val durationPattern by patternGroup.pattern(
        "training.duration.pattern",
        "Will take: (?<time>.*)",
    )

    /**
     * REGEX-TEST: 6,749,742 Coins
     * REGEX-TEST: 13,492,398.8 Coins
     * REGEX-TEST: 1,000,000.3 Coins (1% off)
     * REGEX-TEST: 12,345,678 Coins (5% off)
     */
    private val coinsPattern by patternGroup.pattern(
        "coin",
        "(?<coin>\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?|\\d+\\.?\\d*) Coins(?: \\([1-5]% off\\))?",
    )

    /**
     * REGEX-TEST: 5,024.3 Bits
     * REGEX-TEST: 1,000 Bits
     * REGEX-TEST: 139 Bits
     */
    private val bitsPattern by patternGroup.pattern(
        "bits",
        "(?<bit>\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?|\\d+\\.?\\d*) Bits",
    )

    /**
     * REGEX-TEST: User Input
     */
    private val userInputPattern by patternGroup.pattern(
        "slot24.name.input",
        "User Input",
    )

    /**
     * REGEX-TEST: Type: Free
     * REGEX-TEST: Type: Light
     * REGEX-TEST: Type: Moderate
     * REGEX-TEST: Type: Expert
     * REGEX-TEST: Type: Ultra
     * REGEX-TEST: Type: Turbo!
     */
    private val trainingTypePattern by patternGroup.pattern(
        "training.type",
        "Type: (?<type>.*)",
    )

    @SubscribeEvent
    fun onFannAnvilTooltip(event: LorenzToolTipEvent) {
        if (!trainingSlotInventoryPattern.matches(InventoryUtils.openInventoryName())) return
        if (!anvilPattern.matches(event.itemStack.displayName)) return
        val tooltip = event.toolTip

        val trainingType = tooltip.getTrainingType() ?: return
        if (trainingType == TrainingType.FREE) return
        if (!showCoins || !showBits) return

        when (trainingMode) {
            TrainingMode.DAY_COUNT -> {
                val totalExp = tooltip.getExpEarned() ?: return
                val coinPerExp = tooltip.getCoins() / totalExp
                val xpPerBit = totalExp / tooltip.getBits()

                tooltip.insertLineAfter(coinsPattern, "§6 ➜ Coins/XP: ${coinPerExp.roundTo(2)}")
                tooltip.insertLineAfter(bitsPattern, "§b ➜ XP/Bit: ${xpPerBit.roundTo(2)}")
            }

            TrainingMode.UNTIL_LEVEL -> {
                val dailyExp = tooltip.getDailyExp() ?: return
                val duration = tooltip.getDuration() ?: return
                val totalExp = dailyExp * duration
                val coinPerExp = tooltip.getCoins() / totalExp
                val xpPerBit = totalExp / tooltip.getBits()
                tooltip.insertLineAfter(coinsPattern, "§6 ➜ Coins/XP: ${coinPerExp.roundTo(2)}")
                tooltip.insertLineAfter(bitsPattern, "§b ➜ XP/Bit: ${xpPerBit.roundTo(2)}")

            }
        }
    }

    @SubscribeEvent
    fun onInventoryUpdate(event: InventoryUpdatedEvent) {
        if (!trainingSlotInventoryPattern.matches(event.inventoryName.removeColor())) return
        val slot24 = event.inventoryItems[24] ?: return

        val name = slot24.displayName.removeColor()
        if (userInputPattern.matches(name)) {
            trainingMode = TrainingMode.DAY_COUNT
        } else {
            trainingMode = TrainingMode.UNTIL_LEVEL
        }

    }


    private fun <T> Pattern.read(lore: List<String>, name: String, func: (String) -> T): T? {
        for (line in lore) {
            val linePlain = line.removeColor()
            this.matchMatcher(linePlain) {
                group(name)?.let { return func(it) }
            }
        }
        return null
    }

    private fun List<String>.getCoins(): Double {
        return coinsPattern.read(this, "coin") { it.formatDouble() } ?: 0.0
    }

    // In case of Bits not found, return 1 so the division is not by zero
    private fun List<String>.getBits(): Double {
        return bitsPattern.read(this, "bit") { it.formatDouble() } ?: 1.0
    }

    private fun List<String>.getExpEarned(): Double? {
        return expEarnedPattern.read(this, "expEarned") { it.formatDouble() }
    }

    private fun List<String>.getDailyExp(): Double? {
        return dailyExpPattern.read(this, "expDaily") { it.formatDouble() }
    }

    private fun List<String>.getTrainingType(): TrainingType? {
        return trainingTypePattern.read(this, "type") { typestr ->
            TrainingType.entries.firstOrNull { it.type == typestr }
        }
    }

    private fun List<String>.getDuration(): Double? {
        return durationPattern.read(this, "time") {
            TimeUtils.getDuration(it).toDouble(DurationUnit.DAYS)
        }
    }

    private enum class TrainingMode {
        DAY_COUNT,
        UNTIL_LEVEL,
    }

    private enum class TrainingType(val type: String) {
        FREE("Free"),
        LIGHT("Light"),
        MODERATE("Moderate"),
        EXPERT("Expert"),
        ULTRA("Ultra"),
        TURBO("Turbo!"),
    }
}
