package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.ASCENSION_ROPE
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.JUNGLE_HEART
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.OIL_BARREL
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.PICKONIMBUS
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.PREHISTORIC_EGG
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.ROBOT_PARTS
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.SLUDGE_JUICE
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.TREASURITE
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.WISHING_COMPASS
import at.hannibal2.skyhanni.config.features.chat.PowderMiningFilterConfig.SimplePowderMiningRewardTypes.YOGGIE
import at.hannibal2.skyhanni.config.features.chat.PowderMiningGemstoneFilterConfig.GemstoneFilterEntry
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

@SkyHanniModule
object PowderMiningChatFilter {

    private val config get() = SkyHanniMod.feature.chat.filterType.powderMiningFilter
    private val gemstoneConfig get() = config.gemstoneFilterConfig

    val patternGroup = RepoPattern.group("filter.powdermining")

    // TODO rename to "openedRewards" ?
    private var unclosedRewards = false

    /**
     * REGEX-TEST: §aYou uncovered a treasure chest!
     */
    private val uncoverChestPattern by patternGroup.pattern(
        "warning.chestuncover",
        "§aYou uncovered a treasure chest!",
    )

    /**
     * REGEX-TEST: §6You have successfully picked the lock on this chest!
     */
    private val successfulPickPattern by patternGroup.pattern(
        "warning.successpick",
        "§6You have successfully picked the lock on this chest!",
    )

    /**
     * REGEX-TEST: §cThis chest has already been looted.
     */
    private val alreadyLootedPattern by patternGroup.pattern(
        "warning.alreadylooted",
        "§cThis chest has already been looted\\.",
    )

    /**
     * REGEX-TEST: §cYou need a tool with a §r§aBreaking Power §r§cof §r§66§r§c to mine Ruby Gemstone Block§r§c! Speak to §r§dFragilis §r§cby the entrance to the Crystal Hollows to learn more!
     */
    private val breakingPowerPattern by patternGroup.pattern(
        "warning.breakingpower",
        "§cYou need a tool with a §r§aBreaking Power §r§cof (?:§.)*\\d+§r§c to mine (Ruby|Amethyst|Jade|Amber|Sapphire|Topaz) Gemstone Block§r§c!.+",
    )

    /**
     * REGEX-TEST: §e§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
     * REGEX-TEST: §d§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
     */
    private val chestWrapperPattern by patternGroup.pattern(
        "powder.chestwrapper",
        "^§[ed]§l▬{64}\$",
    )

    /**
     * REGEX-TEST:   §r§6§lCHEST LOCKPICKED
     */
    private val lockPickedPattern by patternGroup.pattern(
        "powder.picked",
        ".*§r§6§lCHEST LOCKPICKED.*",
    )

    /**
     * REGEX-TEST:   §r§5§lLOOT CHEST COLLECTED
     */
    private val lootChestCollectedPattern by patternGroup.pattern(
        "lootchest.collected",
        ".*§r§5§lLOOT CHEST COLLECTED.*",
    )

    /**
     * REGEX-TEST:   §r§a§lREWARDS
     */
    private val rewardHeaderPattern by patternGroup.pattern(
        "reward.header",
        ".*§r§[af]§lREWARDS.*",
    )

    /**
     * REGEX-TEST:    §r§a§r§aGreen Goblin Egg
     * REGEX-TEST:    §r§9Goblin Egg
     * REGEX-TEST:    §r§dDiamond Essence
     * REGEX-TEST:    §r§dGold Essence
     * REGEX-TEST:    §r§dGold Essence §r§8x3
     * REGEX-TEST:    §r§dGemstone Powder §r§8x537
     * REGEX-TEST:    §r§dDiamond Essence §r§8x2
     * REGEX-TEST:    §r§2Mithril Powder §r§8x153
     * REGEX-TEST:    §r§5Treasurite
     * REGEX-TEST:    §r§f⸕ Rough Amber Gemstone §r§8x24
     * REGEX-TEST:    §r§f❤ Rough Ruby Gemstone §r§8x24
     * REGEX-TEST:    §r§f❈ Rough Amethyst Gemstone §r§8x24
     * REGEX-TEST:    §r§9§r§eYellow Goblin Egg
     * REGEX-TEST:    §r§a⸕ Flawed Amber Gemstone
     * REGEX-TEST:    §r§aWishing Compass §r§8x3
     * REGEX-TEST:    §r§a⸕ Flawed Amber Gemstone §r§8x2
     */
    val genericMiningRewardMessage by patternGroup.pattern(
        "reward.generic",
        " {4}(?<reward>§.+?[^§]*)(?: §r§8x(?<amount>[\\d,]+))?\$",
    )

    /**
     * REGEX-TEST: §r§2Mithril Powder §r§8x153
     * REGEX-TEST: §r§dGemstone Powder §r§8x537
     */
    private val powderRewardPattern by patternGroup.pattern(
        "reward.powder",
        "§r§[d2](?:Gemstone|Mithril) Powder( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§dGold Essence
     * REGEX-TEST: §r§dGold Essence §r§8x3
     * REGEX-TEST: §r§dDiamond Essence §r§8x2
     * REGEX-TEST: §r§dDiamond Essence
     */
    private val essenceRewardPattern by patternGroup.pattern(
        "reward.essence",
        "§r§d(?:Gold|Diamond) Essence( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§9Ascension Rope
     */
    private val ascensionRopeRewardPattern by patternGroup.pattern(
        "reward.ascensionrope",
        "§r§9Ascension Rope( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§aWishing Compass
     */
    private val wishingCompassRewardPattern by patternGroup.pattern(
        "reward.wishingcompass",
        "§r§aWishing Compass( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§aOil Barrel
     */
    private val oilBarrelRewardPattern by patternGroup.pattern(
        "reward.oilbarrel",
        "§r§aOil Barrel( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§fPrehistoric Egg
     */
    private val prehistoricEggPattern by patternGroup.pattern(
        "reward.prehistoricegg",
        "§r§fPrehistoric Egg( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§5Pickonimbus 2000
     */
    private val pickonimbusPattern by patternGroup.pattern(
        "reward.pickonimbus",
        "§r§5Pickonimbus 2000( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§6Jungle Heart
     */
    private val jungleHeartPattern by patternGroup.pattern(
        "reward.jungleheart",
        "§r§6Jungle Heart( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§aSludge Juice
     */
    private val sludgeJuicePattern by patternGroup.pattern(
        "reward.sludgejuice",
        "§r§aSludge Juice( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§aYoggie
     */
    private val yoggiePattern by patternGroup.pattern(
        "reward.yoggie",
        "§r§aYoggie( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§9FTX 3070
     * REGEX-TEST: §r§9Synthetic Heart
     * REGEX-TEST: §r§9Control Switch
     * REGEX-TEST: §r§9Robotron Reflector
     * REGEX-TEST: §r§9Electron Transmitter
     * REGEX-TEST: §r§9Superlite Motor
     */
    private val robotPartsPattern by patternGroup.pattern(
        "reward.robotparts",
        "§r§9(?:FTX 3070|Synthetic Heart|Control Switch|Robotron Reflector|Electron Transmitter|Superlite Motor)( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§5Treasurite
     */
    private val treasuritePattern by patternGroup.pattern(
        "reward.treasurite",
        "§r§5Treasurite( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§9§r§cRed Goblin Egg
     * REGEX-TEST: §r§9§r§3Blue Goblin Egg
     * REGEX-TEST: §r§9Goblin Egg
     * REGEX-TEST: §r§9Goblin Egg §r§8x2
     * REGEX-TEST: §r§a§r§aGreen Goblin Egg
     * REGEX-TEST: §r§9§r§eYellow Goblin Egg
     */
    private val goblinEggPattern by patternGroup.pattern(
        "reward.goblineggs",
        "(?:§.)*(?<color>[a-zA-Z]+)? ?Goblin Egg( §r§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §r§f❈ Rough Amethyst Gemstone §r§8x24
     * REGEX-TEST: §r§a❈ Flawed Amethyst Gemstone §r§8x4
     * REGEX-TEST: §r§9⸕ Fine Amber Gemstone
     * REGEX-TEST: §r§5⸕ Flawless Amber Gemstone
     * REGEX-TEST: §r§f❁ Rough Jasper Gemstone §r§8x24
     * REGEX-TEST: §r§a❁ Flawed Jasper Gemstone
     */
    @Suppress("MaxLineLength")
    private val gemstonePattern by patternGroup.pattern(
        "reward.gemstone",
        "§r§[fa9][❤❈☘⸕✎✧❁] (?<tier>Rough|Flawed|Fine|Flawless) (?<gem>Ruby|Amethyst|Jade|Amber|Sapphire|Topaz|Jasper) Gemstone( §r§8x(?<amount>[\\d,]+))?",
    )

    @Suppress("CyclomaticComplexMethod")
    fun block(message: String): String? {
        // Generic "you uncovered a chest" message
        if (uncoverChestPattern.matches(message)) return "powder_mining_chest"
        if (successfulPickPattern.matches(message)) return "powder_mining_picked"
        if (alreadyLootedPattern.matches(message)) return "powder_mining_dupe"
        // Breaking power warning
        if (breakingPowerPattern.matches(message) && gemstoneConfig.strongerToolMessages) return "stronger_tool"
        // Closing or opening a reward 'loop' with the spam of ▬
        if (chestWrapperPattern.matches(message)) {
            unclosedRewards = !unclosedRewards
            return "reward_wrapper"
        }

        if (!unclosedRewards) return null
        if (StringUtils.isEmpty(message)) return "powder_mining_empty"
        if (lockPickedPattern.matches(message)) return "powder_chest_lockpicked"
        if (lootChestCollectedPattern.matches(message)) return "loot_chest_opened"
        if (rewardHeaderPattern.matches((message))) return "powder_reward_header"

        // All powder and loot chest rewards start with 4 spaces
        // To simplify regex statements, this check is done outside
        val ssMessage = message.takeIf { it.startsWith("    ") }?.substring(4) ?: return null

        // Powder
        powderRewardPattern.matchMatcher(ssMessage) {
            if (config.powderFilterThreshold == 60000) return "powder_mining_powder"
            val amountStr = groupOrNull("amount") ?: "1"
            if (amountStr.isNotEmpty() && config.powderFilterThreshold > 0) {
                val amountParsed = amountStr.replace(",", "").toInt()
                return if (amountParsed < config.powderFilterThreshold) "powder_mining_powder"
                else "no_filter"
            }
        }

        // Essence
        essenceRewardPattern.matchMatcher(ssMessage) {
            if (config.essenceFilterThreshold == 20) return "powder_mining_essence"
            val amountStr = groupOrNull("amount") ?: "1"
            if (amountStr.isNotEmpty() && config.essenceFilterThreshold > 0) {
                val amountParsed = amountStr.toInt()
                return if (amountParsed < config.essenceFilterThreshold) "powder_mining_essence"
                else "no_filter"
            }
        }

        blockSimpleRewards(ssMessage)?.let { return it }
        blockGoblinEggs(ssMessage)?.let { return it }
        blockGemstones(ssMessage)?.let { return it }

        // Fallback default
        return null
    }

    private var rewardPatterns: Map<Pair<Pattern, PowderMiningFilterConfig.SimplePowderMiningRewardTypes>, String> =
        emptyMap()

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRepoReload(event: RepositoryReloadEvent) {
        rewardPatterns = mapOf(
            ascensionRopeRewardPattern to ASCENSION_ROPE to "powder_mining_ascension_rope",
            wishingCompassRewardPattern to WISHING_COMPASS to "powder_mining_wishing_compass",
            oilBarrelRewardPattern to OIL_BARREL to "powder_mining_oil_barrel",
            prehistoricEggPattern to PREHISTORIC_EGG to "powder_mining_prehistoric_egg",
            pickonimbusPattern to PICKONIMBUS to "powder_mining_pickonimbus",
            jungleHeartPattern to JUNGLE_HEART to "powder_mining_jungle_heart",
            sludgeJuicePattern to SLUDGE_JUICE to "powder_mining_sludge_juice",
            yoggiePattern to YOGGIE to "powder_mining_yoggie",
            robotPartsPattern to ROBOT_PARTS to "powder_mining_robot_parts",
            treasuritePattern to TREASURITE to "powder_mining_treasurite",
        )
    }

    private fun blockSimpleRewards(ssMessage: String): String? {
        for ((patternToReward, returnReason) in rewardPatterns) {
            if (patternToReward.first.matches(ssMessage)) {
                return if (config.simplePowderMiningTypes.contains(patternToReward.second)) returnReason
                else "no_filter"
            }
        }
        return null
    }

    private fun blockGoblinEggs(ssMessage: String): String? {
        goblinEggPattern.matchMatcher(ssMessage) {
            if (config.goblinEggs == PowderMiningFilterConfig.GoblinEggFilterEntry.SHOW_ALL) return "no_filter"
            if (config.goblinEggs == PowderMiningFilterConfig.GoblinEggFilterEntry.HIDE_ALL) return "powder_mining_goblin_eggs"

            return when (val colorStr = groupOrNull("color")?.lowercase()) {
                // 'Colorless', base goblin eggs will never be shown in this code path
                null -> "powder_mining_goblin_eggs"
                "green" -> if (config.goblinEggs > PowderMiningFilterConfig.GoblinEggFilterEntry.GREEN_UP) {
                    "powder_mining_goblin_eggs"
                } else "no_filter"

                "yellow" -> if (config.goblinEggs > PowderMiningFilterConfig.GoblinEggFilterEntry.YELLOW_UP) {
                    "powder_mining_goblin_eggs"
                } else "no_filter"

                "red" -> if (config.goblinEggs > PowderMiningFilterConfig.GoblinEggFilterEntry.RED_UP) {
                    "powder_mining_goblin_eggs"
                } else "no_filter"
                // BLUE_ONLY enum not explicitly used in comparison, as the only
                // case that will block it is HIDE_ALL, which is covered above
                "blue" -> "no_filter"
                else -> {
                    ErrorManager.logErrorWithData(
                        NoSuchElementException(),
                        "Unknown Goblin Egg color detected in Powder Mining Filter: '$colorStr' - please report this in the Discord!",
                        noStackTrace = true,
                    )
                    "no_filter"
                }
            }
        }
        return null
    }

    private fun blockGemstones(ssMessage: String): String? {
        gemstonePattern.matchMatcher(ssMessage) {
            val gemStr = groupOrNull("gem")?.lowercase() ?: return null
            val tierStr = groupOrNull("tier")?.lowercase() ?: return null

            val gemSpecificFilterEntry = when (gemStr) {
                "ruby" -> gemstoneConfig.rubyGemstones
                "sapphire" -> gemstoneConfig.sapphireGemstones
                "amber" -> gemstoneConfig.amberGemstones
                "amethyst" -> gemstoneConfig.amethystGemstones
                "jade" -> gemstoneConfig.jadeGemstones
                "topaz" -> gemstoneConfig.topazGemstones
                "jasper" -> gemstoneConfig.jasperGemstones
                // Failure case that should never be reached
                else -> return "no_filter"
            }

            if (gemSpecificFilterEntry == GemstoneFilterEntry.HIDE_ALL) return "powder_mining_gemstones"

            return when (tierStr) {
                // Never allowed through, except for in SHOW_ALL,
                // which is handled above
                "rough" -> "powder_mining_gemstones"
                "flawed" -> if (gemSpecificFilterEntry > GemstoneFilterEntry.FLAWED_UP) {
                    "powder_mining_gemstones"
                } else "no_filter"

                "fine" -> if (gemSpecificFilterEntry > GemstoneFilterEntry.FINE_UP) {
                    "powder_mining_gemstones"
                } else "no_filter"
                // FLAWLESS_ONLY enum not explicitly used in comparison, as the only
                // case that will block it is HIDE_ALL, which is covered above
                "flawless" -> "no_filter"
                // This should not be reachable
                else -> "no_filter"
            }
        }
        return null
    }
}
