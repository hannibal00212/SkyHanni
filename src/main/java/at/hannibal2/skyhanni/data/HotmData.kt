package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.HotmAPI
import at.hannibal2.skyhanni.api.HotmAPI.MayhemPerk
import at.hannibal2.skyhanni.api.HotmAPI.SkymallPerk
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.jsonobjects.local.HotmTree
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.indexOfFirstMatch
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.pow

private fun calculateCoreOfTheMountainLoot(level: Int): Map<HotmReward, Double> = buildMap {
    for (i in 1..level) {
        when (i) {
            1, 5, 7 -> addOrPut(HotmReward.EXTRA_TOKENS, 1.0)
            2 -> addOrPut(HotmReward.ABILITY_LEVEL, 1.0)
            3 -> addOrPut(HotmReward.EXTRA_COMMISSION_SLOTS, 1.0)
            4 -> addOrPut(HotmReward.MORE_BASE_MITHRIL_POWER, 1.0)
            6 -> addOrPut(HotmReward.MORE_BASE_GEMSTONE_POWER, 2.0)
            8 -> addOrPut(HotmReward.MORE_BASE_GLACITE_POWER, 3.0)
            9 -> addOrPut(HotmReward.MINESHAFT_CHANCE, 10.0)
            10 -> addOrPut(HotmReward.EXTRA_TOKENS, 2.0)
        }
    }
}

enum class HotmData(
    val guiName: String,
    val maxLevel: Int,
    val costFun: (Int) -> (Double?),
    val rewardFun: (Int) -> (Map<HotmReward, Double>),
) {

    MINING_SPEED(
        "Mining Speed",
        50,
        { level -> (level + 1.0).pow(3.0) },
        { level -> mapOf(HotmReward.MINING_SPEED to level * 20.0) },
    ),
    MINING_FORTUNE(
        "Mining Fortune",
        50,
        { level -> (level + 1.0).pow(3.05) },
        { level -> mapOf(HotmReward.MINING_FORTUNE to level * 2.0) },
    ),
    TITANIUM_INSANIUM(
        "Titanium Insanium",
        50,
        { level -> (level + 1.0).pow(3.1) },
        { level -> mapOf(HotmReward.TITANIUM_CHANCE to 2.0 + (level * 0.1)) },
    ),
    LUCK_OF_THE_CAVE(
        "Luck of the Cave",
        45,
        { level -> (level + 1.0).pow(3.07) },
        { level -> mapOf(HotmReward.EXTRA_CHANCE_TRIGGER_RARE_OCCURRENCES to 5.0 + level) },
    ),
    EFFICIENT_MINER(
        "Efficient Miner",
        100,
        { level -> (level + 1.0).pow(2.6) },
        { level -> mapOf(HotmReward.MINING_SPREAD to 3.0 * level) },
    ),
    QUICK_FORGE(
        "Quick Forge",
        20,
        { level -> (level + 1.0).pow(3.2) },
        { level -> mapOf(HotmReward.FORGE_TIME_DECREASE to if (level >= 20) 30.0 else 10.0 + (level * 0.5)) },
    ),
    OLD_SCHOOL(
        "Old-School",
        20,
        { level -> (level + 1.0).pow(4.0) },
        { level -> mapOf(HotmReward.ORE_FORTUNE to level * 5.0) },
    ),
    PROFESSIONAL(
        "Professional",
        140,
        { level -> (level + 1.0).pow(2.3) },
        { level -> mapOf(HotmReward.MINING_SPEED to 50.0 + (level * 5.0)) },
    ),
    MOLE(
        "Mole",
        200,
        { level -> (level + 1.0).pow(2.17883) },
        { level -> mapOf(HotmReward.MINING_SPREAD to 50.0 + ((level - 1) * (350 / 199))) },
    ),
    GEM_LOVER(
        "Gem Lover",
        20,
        { level -> (level + 1.0).pow(4.0) },
        { level -> mapOf(HotmReward.GEMSTONE_FORTUNE to 20.0 + (level * 4.0)) },
    ),
    SEASONED_MINEMAN(
        "Seasoned Mineman",
        100,
        { level -> (level + 1.0).pow(2.3) },
        { level -> mapOf(HotmReward.MINING_WISDOM to 5.0 + (level * 0.1)) },
    ),
    FORTUNATE_MINEMAN(
        "Fortunate Mineman",
        50,
        { level -> (level + 1.0).pow(3.2) },
        { level -> mapOf(HotmReward.MINING_FORTUNE to level * 3.0) },
    ),
    BLOCKHEAD(
        "Blockhead",
        20,
        { level -> (level + 1.0).pow(4.0) },
        { level -> mapOf(HotmReward.BLOCK_FORTUNE to level * 5.0) },
    ),
    KEEP_IT_COOL(
        "Keep It Cool",
        50,
        { level -> (level + 1.0).pow(3.07) },
        { level -> mapOf(HotmReward.HEAT_RESISTANCE to level * 0.4) },
    ),

    LONESOME_MINER(
        "Lonesome Miner",
        45,
        { level -> (level + 1.0).pow(3.07) },
        { level -> mapOf(HotmReward.COMBAT_STAT_BOOST to 5.0 + ((level - 1.0) * 0.5)) },
    ),
    GREAT_EXPLORER(
        "Great Explorer",
        20,
        { level -> (level + 1.0).pow(4.0) },
        { level ->
            mapOf(
                HotmReward.CHANCE_OF_TREASURE_CHEST to (0.2 * (0.2 + 0.04 * (level - 1.0))),
                HotmReward.LOCKS_OF_TREASURE_CHEST to 1 + level * 0.2,
            )
        },
    ),

    POWDER_BUFF(
        "Powder Buff",
        50,
        { level -> (level + 1.0).pow(3.2) },
        { level ->
            mapOf(
                HotmReward.MORE_MITHRIL_POWER to level.toDouble(),
                HotmReward.MORE_GEMSTONE_POWER to level.toDouble(),
            )
        },
    ),
    SPEEDY_MINEMAN(
        "Speedy Mineman",
        50,
        { level -> (level + 1.0).pow(3.2) },
        { level -> mapOf(HotmReward.MINING_SPEED to level * 40.0) },
    ),




    SUBTERRANEAN_FISHER(
        "Subterranean Fisher",
        40,
        { level -> (level + 1.0).pow(3.07) },
        { level ->
            mapOf(
                HotmReward.FISHING_SPEED to 5 + (level * 0.5),
                HotmReward.SEA_CREATURE_CHANCE to 1 + (level * 0.1),
            )
        },

    ),


    // Static

    SKY_MALL("Sky Mall", 1, { null }, { emptyMap() }),
    PRECISION_MINING("Precision Mining", 1, { null }, { mapOf(HotmReward.MINING_SPEED_BOOST to 30.0) }),
    FRONT_LOADED(
        "Front Loaded",
        1,
        { null },
        {
            mapOf(
                HotmReward.MINING_SPEED to 250.0,
                HotmReward.GEMSTONE_FORTUNE to 150.0,
                HotmReward.MORE_GEMSTONE_POWER to 200.0,
            )
        },
    ),
    DAILY_GRIND("Daily Grind", 1, { null }, { emptyMap() }),
    DAILY_POWDER("Daily Powder", 1, { null }, { emptyMap() }),
    // Abilities

    PICKOBULUS(
        "Pickobulus",
        3,
        { null },
        { level ->
            mapOf(
                HotmReward.ABILITY_RADIUS to 3.0,
                HotmReward.ABILITY_COOLDOWN to 60.0 - 10.0 * (level - 1),
            )
        },
    ),
    MINING_SPEED_BOOST(
        "Mining Speed Boost",
        3,
        { null },
        { level ->
            mapOf(
                HotmReward.MINING_SPEED_BOOST to 200.0 + 50.0 * (level - 1),
                HotmReward.ABILITY_DURATION to 10.0 + 5 * (level - 1),
                HotmReward.ABILITY_COOLDOWN to 120.0,
            )
        },
    ),
    MANIAC_MINER(
        "Maniac Miner",
        3,
        { null },
        { level ->
            mapOf(
                HotmReward.ABILITY_DURATION to 20.0 + level * 5.0,
                HotmReward.ABILITY_COOLDOWN to 60.0,
                HotmReward.BREAKING_POWER to 1.0,
            )
        },
    ),

    SHEER_FORCE(
        "Sheer Force",
        3,
        { null },
        { level ->
            mapOf(
                HotmReward.ABILITY_DURATION to 20.0 + 5 * (level - 1),
                HotmReward.MINING_SPREAD to 200.0,
            )
        },
    ),

    ANOMALOUS_DESIRE(
        "Anomalous Desire",
        3,
        { null },
        { level ->
            mapOf(
                HotmReward.EXTRA_CHANCE_TRIGGER_RARE_OCCURRENCES to 30.0 + (level - 1) * 10.0,
                HotmReward.ABILITY_COOLDOWN to 120.0 - (level - 1) * 10.0,
                HotmReward.ABILITY_DURATION to 30.0,
            )
        },
    ),

    CORE_OF_THE_MOUNTAIN(
        "Core of the Mountain", 10, { null },
        { level -> calculateCoreOfTheMountainLoot(level) },
    ),

    // Mining V3

    NO_STONE_UNTURNED(
        "No Stone Unturned",
        50,
        { level -> (level + 1.0).pow(3.05) },
        { level -> mapOf(HotmReward.UNKNOWN to 0.5 * level) },
    ),

    STRONG_ARM(
        "Strong Arm",
        100,
        { level -> (level + 1.0).pow(2.3) },
        { level -> mapOf(HotmReward.MINING_SPEED to 5.0 * level) },
    ),
    STEADY_HAND(
        "Steady Hand",
        100,
        { level -> (level + 1.0).pow(2.6) },
        { level -> mapOf(HotmReward.GEMSTONE_SPREAD to 0.1 * level) },
    ),
    WARM_HEART(
        "Warm Heart",
        50,
        { level -> (level + 1.0).pow(3.1) },
        { level -> mapOf(HotmReward.COLD_RESISTANCE to 0.4 * level) },
    ),
    SURVEYOR(
        "Surveyor",
        20,
        { level -> (level + 1.0).pow(4.0) },
        { level -> mapOf(HotmReward.MINESHAFT_CHANCE to 0.75 * level) },
    ),
    METAL_HEAD(
        "Metal Head",
        20,
        { level -> (level + 1.0).pow(4.0) },
        { level -> mapOf(HotmReward.DWARVEN_METAL_FORTUNE to 5.0 * level) },
    ),
    RAGS_TO_RICHES(
        "Rags to Riches",
        50,
        { level -> (level + 1.0).pow(3.05) },
        { level -> mapOf(HotmReward.MINING_FORTUNE to 4.0 * level) },
    ),
    EAGER_ADVENTURER(
        "Eager Adventurer",
        100,
        { level -> (level + 1.0).pow(2.3) },
        { level -> mapOf(HotmReward.MINING_SPEED to 4.0 * level) },
    ),
    CRYSTALLINE(
        "Crystalline",
        50,
        { level -> (level + 1.0).pow(3.3) },
        { level -> mapOf(HotmReward.UNKNOWN to 0.5 * level) },
    ),
    GIFTS_FROM_THE_DEPARTED(
        "Gifts from the Departed",
        100,
        { level -> (level + 1.0).pow(2.45) },
        { level -> mapOf(HotmReward.UNKNOWN to 0.2 * level) },
    ),
    MINING_MASTER(
        "Mining Master",
        10,
        { level -> (level + 7.0).pow(5.0) },
        { level -> mapOf(HotmReward.PRISTINE to 0.1 * level) },
    ),
    DEAD_MANS_CHEST(
        "Dead Man's Chest",
        50,
        { level -> (level + 1.0).pow(3.2) },
        { level -> mapOf(HotmReward.UNKNOWN to 1.0 * level) },
    ),
    VANGUARD_SEEKER(
        "Vanguard Seeker",
        50,
        { level -> (level + 1.0).pow(3.1) },
        { level -> mapOf(HotmReward.UNKNOWN to 1.0 * level) },
    ),

    MINESHAFT_MAYHEM("Mineshaft Mayhem", 1, { null }, { emptyMap() }),
    GEMSTONE_INFUSION("Gemstone Infusion", 1, { null }, { emptyMap() }),
    MINERS_BLESSING("Miner's Blessing", 1, { null }, { mapOf(HotmReward.MAGIC_FIND to 30.0) }),
    ;

    private val guiNamePattern by patternGroup.pattern("perk.name.${name.lowercase().replace("_", "")}", "§.$guiName")

    val printName get() = name.allLettersFirstUppercase()

    /** Level which are actually paid with powder (does exclude [blueEgg])*/
    var rawLevel: Int
        get() = storage?.perks?.get(this.name)?.level ?: 0
        private set(value) {
            storage?.perks?.computeIfAbsent(this.name) { HotmTree.HotmPerk() }?.level = value
        }

    /** Level for which the effect that is present (considers [enabled] and [blueEgg])*/
    val activeLevel: Int
        get() = if (enabled) effectiveLevel else 0

    /** Level that considering [blueEgg]*/
    val effectiveLevel: Int get() = storage?.perks?.get(this.name)?.level?.plus(blueEgg()) ?: 0

    val isMaxLevel: Boolean
        get() = effectiveLevel >= maxLevel // >= to account for +1 from Blue Cheese

    private fun blueEgg() = if (this != CORE_OF_THE_MOUNTAIN && maxLevel != 1 && HotmAPI.isBlueEggActive) 1 else 0

    var enabled: Boolean
        get() = storage?.perks?.get(this.name)?.enabled ?: false
        private set(value) {
            storage?.perks?.computeIfAbsent(this.name) { HotmTree.HotmPerk() }?.enabled = value
        }

    var isUnlocked: Boolean
        get() = storage?.perks?.get(this.name)?.isUnlocked ?: false
        private set(value) {
            storage?.perks?.computeIfAbsent(this.name) { HotmTree.HotmPerk() }?.isUnlocked = value
        }

    var slot: Slot? = null
        private set

    fun getLevelUpCost() = costFun(rawLevel)

    fun getReward() = if (enabled) rewardFun(activeLevel) else emptyMap()

    fun calculateTotalCost(desiredLevel: Int) = (2..desiredLevel).sumOf { level -> costFun(level)?.toInt() ?: 0 }

    val totalCostMaxLevel = calculateTotalCost(maxLevel)

    // TODO move all object functions into hotm api?
    @SkyHanniModule
    companion object {

        val storage get() = ProfileStorageData.profileSpecific?.mining?.hotmTree

        val abilities =
            listOf(PICKOBULUS, MINING_SPEED_BOOST, MANIAC_MINER, GEMSTONE_INFUSION, ANOMALOUS_DESIRE, SHEER_FORCE)

        private val inventoryPattern by patternGroup.pattern(
            "inventory",
            "Heart of the Mountain",
        )

        /**
         * REGEX-TEST: §5§o§7Level 1§8/50 §7(§b0 §l0%§7):skull:
         * REGEX-TEST: §7Level 1§8/50
         */
        private val levelPattern by patternGroup.pattern(
            "perk.level",
            "(?:§.)*§(?<color>.)Level (?<level>\\d+).*",
        )

        private val notUnlockedPattern by patternGroup.pattern(
            "perk.notunlocked",
            "(§.)*Requires.*|.*Mountain!|(§.)*Click to unlock!|",
        )

        private val enabledPattern by patternGroup.pattern(
            "perk.enable",
            "§a§lENABLED|(§.)*SELECTED",
        )
        @Suppress("UnusedPrivateProperty")
        private val disabledPattern by patternGroup.pattern(
            "perk.disabled",
            "§c§lDISABLED|§7§eClick to select!",
        ) // unused for now since the assumption is when enabled isn't found, it is disabled,
        // but the value might be useful in the future or for debugging

        val perkCostPattern by patternGroup.pattern(
            "perk.cost",
            "(?:§.)*§7Cost",
        )

        private val resetChatPattern by patternGroup.pattern(
            "reset.chat",
            "§aReset your §r§5Heart of the Mountain§r§a! Your Perks and Abilities have been reset.",
        )

        private val heartItemPattern by patternGroup.pattern(
            "inventory.heart",
            "§5Heart of the Mountain",
        )
        private val resetItemPattern by patternGroup.pattern(
            "inventory.reset",
            "§cReset Heart of the Mountain",
        )

        private val heartTokensPattern by patternGroup.pattern(
            "inventory.heart.token",
            "§7Token of the Mountain: §5(?<token>\\d+)",
        )

        private val resetTokensPattern by patternGroup.pattern(
            "inventory.reset.token",
            "\\s+§8- §5(?<token>\\d+) Token of the Mountain",
        )

        private val skymallPattern by patternGroup.pattern(
            "skymall",
            "(?:§eNew buff§r§r§r: §r§f|§8 ■ §7)(?<perk>.*)",
        )

        private val mayhemChatPattern by patternGroup.pattern(
            "mayhem",
            "§b§lMAYHEM! §r§7(?<perk>.*)",
        )

        /**
         * REGEX-TEST:  Mithril: §r§299,918
         * REGEX-TEST:  Gemstone: §r§d37,670
         */
        private val powderPattern by patternGroup.pattern(
            "widget.powder",
            "\\s*(?<type>\\w+): (?:§.)+(?<amount>[\\d,.]+)"
        )

        var inInventory = false

        var tokens: Int
            get() = ProfileStorageData.profileSpecific?.mining?.tokens ?: 0
            private set(value) {
                ProfileStorageData.profileSpecific?.mining?.tokens = value
            }

        var availableTokens: Int
            get() = ProfileStorageData.profileSpecific?.mining?.availableTokens ?: 0
            private set(value) {
                ProfileStorageData.profileSpecific?.mining?.availableTokens = value
            }

        var heartItem: Slot? = null

        init {
            entries.forEach { it.guiNamePattern }
            HotmAPI.PowderType.entries.forEach {
                it.heartPattern
                it.resetPattern
            }
            HotmAPI.SkymallPerk.entries.forEach {
                it.chatPattern
                it.itemPattern
            }
            HotmAPI.MayhemPerk.entries.forEach {
                it.chatPattern
            }
            for (level in 0..CORE_OF_THE_MOUNTAIN.maxLevel) {
                val map = mutableMapOf<HotmReward, Double>()
                if (level >= 1) map.addOrPut(HotmReward.EXTRA_TOKENS, 1.0)
                if (level >= 2) map.addOrPut(HotmReward.ABILITY_LEVEL, 1.0)
                if (level >= 3) map.addOrPut(HotmReward.EXTRA_COMMISSION_SLOTS, 1.0)
                if (level >= 4) map.addOrPut(HotmReward.MORE_BASE_MITHRIL_POWER, 1.0)
                if (level >= 5) map.addOrPut(HotmReward.EXTRA_TOKENS, 1.0)
                if (level >= 6) map.addOrPut(HotmReward.MORE_BASE_GEMSTONE_POWER, 2.0)
                if (level >= 7) map.addOrPut(HotmReward.EXTRA_TOKENS, 1.0)
                if (level >= 8) map.addOrPut(HotmReward.MORE_BASE_GLACITE_POWER, 3.0)
                if (level >= 9) map.addOrPut(HotmReward.MINESHAFT_CHANCE, 10.0)
                if (level >= 10) map.addOrPut(HotmReward.EXTRA_TOKENS, 2.0)

                coreOfTheMountainPerks[level] = map
            }
        }

        fun getPerkByNameOrNull(name: String): HotmData? = entries.find { it.guiName == name }

        private fun resetTree() = entries.forEach {
            it.rawLevel = 0
            it.enabled = false
            it.isUnlocked = false
            HotmAPI.PowderType.entries.forEach { it.setCurrent(it.getTotal()) }
            availableTokens = tokens
        }

        private fun Slot.parse() {
            val item = this.stack ?: return

            if (this.handlePowder()) return

            val entry = entries.firstOrNull { it.guiNamePattern.matches(item.name) } ?: return
            entry.slot = this

            val lore = item.getLore().takeIf { it.isNotEmpty() } ?: return

            if (entry != CORE_OF_THE_MOUNTAIN && notUnlockedPattern.matches(lore.last())) {
                entry.rawLevel = 0
                entry.enabled = false
                entry.isUnlocked = false
                return
            }

            entry.isUnlocked = true

            entry.rawLevel = levelPattern.matchMatcher(lore.first()) {
                group("level").toInt().transformIf({ group("color") == "b" }, { this.minus(1) })
            } ?: entry.maxLevel

            // raw level to ignore the blue egg buff
            if (entry.rawLevel > entry.maxLevel) {
                ErrorManager.skyHanniError(
                    "Hotm Perk '${entry.name}' over max level",
                    "name" to entry.name,
                    "activeLevel" to entry.activeLevel,
                    "maxLevel" to entry.maxLevel,
                )
            }

            if (entry == CORE_OF_THE_MOUNTAIN) {
                entry.enabled = entry.rawLevel != 0
                return
            }
            entry.enabled = lore.any { enabledPattern.matches(it) }

            if (entry == SKY_MALL) handleSkyMall(lore)
        }

        private fun Slot.handlePowder(): Boolean {
            val item = this.stack ?: return false

            val isHeartItem = when {
                heartItemPattern.matches(item.name) -> true
                resetItemPattern.matches(item.name) -> false
                else -> return false
            }

            if (isHeartItem) { // Reset on the heart Item to remove duplication
                tokens = 0
                availableTokens = 0
                HotmAPI.PowderType.entries.forEach { it.reset() }
                heartItem = this
            }

            val lore = item.getLore()

            val tokenPattern = if (isHeartItem) heartTokensPattern else resetTokensPattern

            lore@ for (line in lore) {

                HotmAPI.PowderType.entries.forEach {
                    it.pattern(isHeartItem).matchMatcher(line) {
                        val powder = group("powder").replace(",", "").toLong()
                        if (isHeartItem) {
                            it.setCurrent(powder)
                        }
                        it.addTotal(powder)
                        continue@lore
                    }
                }

                tokenPattern.matchMatcher(line) {
                    val token = group("token").toInt()
                    if (isHeartItem) {
                        availableTokens = token
                    }
                    tokens += token
                    continue@lore
                }
            }
            return true
        }

        private val skyMallCurrentEffect by patternGroup.pattern(
            "skymall.current",
            "§aYour Current Effect",
        )

        private fun handleSkyMall(lore: List<String>) {
            if (!SKY_MALL.enabled || !SKY_MALL.isUnlocked) HotmAPI.skymall = null
            else {
                val index = skyMallCurrentEffect.indexOfFirstMatch(lore) ?: run {
                    ErrorManager.logErrorStateWithData(
                        "Could not read the skymall effect from the hotm tree",
                        "skyMallCurrentEffect didn't match",
                        "lore" to lore,
                    )
                    return
                }
                skymallPattern.matchMatcher(lore[index + 1]) {
                    val perk = group("perk")
                    HotmAPI.skymall = SkymallPerk.entries.firstOrNull { it.itemPattern.matches(perk) } ?: run {
                        ErrorManager.logErrorStateWithData(
                            "Could not read the skymall effect from the hotm tree",
                            "no itemPattern matched",
                            "lore" to lore,
                            "perk" to perk,
                        )
                        null
                    }
                }
            }
        }

        @SubscribeEvent
        fun onScoreboardUpdate(event: ScoreboardUpdateEvent) {
            if (!LorenzUtils.inSkyBlock) return

            ScoreboardPattern.powderPattern.firstMatcher(event.added) {
                val type = HotmAPI.PowderType.entries.firstOrNull { it.displayName == group("type") } ?: return
                val amount = group("amount").formatLong()
                val difference = amount - type.getCurrent()

                if (difference > 0) {
                    type.gain(difference)
                }
            }
        }

        @SubscribeEvent
        fun onInventoryClose(event: InventoryCloseEvent) {
            if (!inInventory) return
            inInventory = false
            entries.forEach { it.slot = null }
            heartItem = null
        }

        @SubscribeEvent
        fun onInventoryFullyOpen(event: InventoryFullyOpenedEvent) {
            if (!LorenzUtils.inSkyBlock) return
            inInventory = inventoryPattern.matches(event.inventoryName)
            if (!inInventory) return
            DelayedRun.runNextTick {
                InventoryUtils.getItemsInOpenChest().forEach { it.parse() }
                abilities.filter { it.isUnlocked }.forEach {
                    it.rawLevel = if (CORE_OF_THE_MOUNTAIN.rawLevel >= 1) 2 else 1
                }
            }
        }

        @SubscribeEvent
        fun onWidgetUpdate(event: WidgetUpdateEvent) {
            if (!event.isWidget(TabWidget.POWDER)) return
            event.lines.forEach {
                powderPattern.matchMatcher(it) {
                    val type = HotmAPI.PowderType.entries.firstOrNull { it.displayName == group("type") } ?: return
                    val amount = group("amount").replace(",", "").toLong()
                    val difference = amount - type.getCurrent()

                    if (difference > 0) {
                        type.gain(difference)
                    }
                }
            }
        }

        @SubscribeEvent
        fun onChat(event: LorenzChatEvent) {
            if (!LorenzUtils.inSkyBlock) return
            if (resetChatPattern.matches(event.message)) {
                resetTree()
                return
            }
            skymallPattern.matchMatcher(event.message) {
                val perk = group("perk")
                HotmAPI.skymall = SkymallPerk.entries.firstOrNull { it.chatPattern.matches(perk) } ?: run {
                    ErrorManager.logErrorStateWithData(
                        "Could not read the skymall effect from chat",
                        "no chatPattern matched",
                        "chat" to event.message,
                        "perk" to perk,
                    )
                    null
                }
                ChatUtils.debug("setting skymall to ${HotmAPI.skymall}")
                return
            }
            DelayedRun.runNextTick {
                mayhemChatPattern.matchMatcher(event.message) {
                    val perk = group("perk")
                    HotmAPI.mineshaftMayhem = MayhemPerk.entries.firstOrNull { it.chatPattern.matches(perk) } ?: run {
                        ErrorManager.logErrorStateWithData(
                            "Could not read the mayhem effect from chat",
                            "no chatPattern matched",
                            "chat" to event.message,
                            "perk" to perk,
                        )
                        null
                    }
                    ChatUtils.debug("setting mineshaftMayhem to ${HotmAPI.mineshaftMayhem}")
                }
            }
        }

        @SubscribeEvent
        fun onWorldSwitch(event: IslandChangeEvent) {
            if (HotmAPI.mineshaftMayhem == null) return
            HotmAPI.mineshaftMayhem = null
            ChatUtils.debug("resetting mineshaftMayhem")
        }

        @SubscribeEvent
        fun onProfileSwitch(event: ProfileJoinEvent) {
            HotmAPI.PowderType.entries.forEach {
                if (it.getStorage() == null) {
                    ProfileStorageData.profileSpecific?.mining?.powder?.put(
                        it,
                        ProfileSpecificStorage.MiningConfig.PowderStorage(),
                    )
                }
            }
        }

        @SubscribeEvent
        fun onDebug(event: DebugDataCollectEvent) {
            event.title("HotM")
            event.addIrrelevant {
                add("Tokens : $availableTokens/$tokens")
                HotmAPI.PowderType.entries.forEach {
                    add("${it.displayName} Powder: ${it.getCurrent()}/${it.getTotal()}")
                }
                add("Ability: ${HotmAPI.activeMiningAbility?.printName}")
                add("Blue Egg: ${HotmAPI.isBlueEggActive}")
                add("SkyMall: ${HotmAPI.skymall}")
                add("Mineshaft Mayhem: ${HotmAPI.mineshaftMayhem}")
            }
            event.title("HotM - Tree")
            event.addIrrelevant(
                entries.filter { it.isUnlocked }.map {
                    "${if (it.enabled) "✔" else "✖"} ${it.printName}: ${it.activeLevel}"
                },
            )
        }
    }
}

private val coreOfTheMountainPerks = mutableMapOf<Int, Map<HotmReward, Double>>()

private val patternGroup = RepoPattern.group("mining.hotm")

enum class HotmReward {
    MINING_SPEED,
    MINING_FORTUNE,
    MINING_WISDOM,
    FORGE_TIME_DECREASE,
    TITANIUM_CHANCE,
    MORE_BASE_MITHRIL_POWER,
    MORE_BASE_GEMSTONE_POWER,
    MORE_BASE_GLACITE_POWER,
    MORE_MITHRIL_POWER,
    MORE_GEMSTONE_POWER,
    COMBAT_STAT_BOOST,
    CHANCE_OF_TREASURE_CHEST,
    LOCKS_OF_TREASURE_CHEST,
    EXTRA_CHANCE_TRIGGER_RARE_OCCURRENCES,
    MINING_SPEED_BOOST,
    ABILITY_DURATION,
    ABILITY_RADIUS,
    ABILITY_COOLDOWN,
    ABILITY_LEVEL,
    MINESHAFT_CHANCE,
    EXTRA_TOKENS,
    EXTRA_COMMISSION_SLOTS,
    UNKNOWN,
    COLD_RESISTANCE,
    MINING_SPREAD,
    GEMSTONE_SPREAD,
    ORE_FORTUNE,
    BLOCK_FORTUNE,
    GEMSTONE_FORTUNE,
    DWARVEN_METAL_FORTUNE,
    HEAT_RESISTANCE,
    MAGIC_FIND,
    PRISTINE,
    FISHING_SPEED,
    SEA_CREATURE_CHANCE,
    BREAKING_POWER,
}
