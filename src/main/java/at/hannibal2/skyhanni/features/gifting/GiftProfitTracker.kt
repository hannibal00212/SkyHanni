package at.hannibal2.skyhanni.features.gifting

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.gifting.UniqueGiftingOpportunitiesFeatures.isHoldingGift
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.CollectionUtils.sumAllValues
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.ItemTrackerData
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GiftProfitTracker {
    val config get() = SkyHanniMod.feature.event.gifting.giftProfitTracker
    val patternGroup = RepoPattern.group("misc.gifting")

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §f§lCOMMON!
     * REGEX-TEST: §9§lRARE!
     * REGEX-TEST: §e§lSWEET!
     * REGEX-TEST: §c§lSANTA TIER!
     * REGEX-TEST: §c§lPARTY TIER!
     */
    private val giftRewardRarityPattern by patternGroup.pattern(
        "reward.rarity",
        "§.§l(?<rarity>COMMON|RARE|SWEET|SANTA|PARTY)(?: TIER)?!.*"
    )

    /**
     * REGEX-TEST: §f§lCOMMON! §r§3+500 Enchanting XP §r§egift with §r§b[MVP§r§d+§r§b] paysley§r§f§r§e!
     * REGEX-TEST: §f§lCOMMON! §r§3+500 Combat XP §r§egift with §r§b[MVP§r§f+§r§b] m640§r§f§r§e!
     * REGEX-TEST: §f§lCOMMON! §r§3+500 Enchanting XP §r§egift with §r§7CreationV3§r§7§r§e!
     */
    val xpGainedPattern by patternGroup.pattern(
        "reward.skillxp",
        "§.§l.*! §r§.\\+(?<amount>[\\d,]+) (?<skill>[\\w ]+) XP §r§egift with §r.*"
    )

    /**
     * REGEX-TEST: §9§lRARE! §r§6+5,000 coins §r§egift with §r§b[MVP§r§d+§r§b] kizzazz§r§f§r§e!
     * REGEX-TEST: §f§lCOMMON! §r§6+5,000 coins §r§egift with §r§a[VIP] Deato_Wez§r§f§r§e!
     * REGEX-TEST: §9§lRARE! §r§6+20,000 coins §r§egift with §r§a[VIP§r§6+§r§a] Grazma§r§f§r§e!
     * REGEX-TEST: §e§lSWEET! §r§6+100,000 coins §r§egift with §r§a[VIP] Destrudot§r§f§r§e!
     */
    val coinsGainedPattern by patternGroup.pattern(
        "reward.coins",
        "§.§l.*! §r§.\\+(?<amount>[\\d,]+) coins §r§egift with §r.*"
    )

    /**
     * REGEX-TEST: §5§lEXTRA! §d+5 North Stars
     * REGEX-TEST: §5§lEXTRA! §d+4 North Stars
     * REGEX-TEST: §5§lEXTRA! §d+1 North Star
     */
    val northStarsPattern by patternGroup.pattern(
        "reward.northstars",
        "§5§lEXTRA! §d\\+(?<amount>[\\d,]+) North Stars?"
    )

    /**
     * REGEX-TEST: §9§lRARE! §r§aForaging XP Boost III Potion §r§egift with §r§b[MVP§r§f+§r§b] m640§r§f§r§e!
     * REGEX-TEST: §9§lRARE! §r§aFarming XP Boost III Potion §r§egift with §r§7gay_player§r§7§r§e!
     * REGEX-TEST: §9§lRARE! §r§aEnchanting XP Boost III Potion §r§egift with §r§7cfitz24§r§7§r§e!
     */
    val boostPotionPattern by patternGroup.pattern(
        "reward.boostpotion",
        "§.§l.*! §r§.(?<skill>[\\w ]+) XP Boost (?<tier>[IVXLCDM]+) Potion §r§egift with §r.*"
    )

    /**
     * REGEX-TEST: §9§lRARE! §r§9Scavenger IV §r§egift with ...
     * REGEX-TEST: §9§lRARE! §r§9Looting IV §r§egift with ...
     * REGEX-TEST: §9§lRARE! §r§9Luck VI §r§egift with ...
     */
    val enchantmentBookPattern by patternGroup.pattern(
        "reward.enchantmentbook",
        "§9§lRARE! §r§9(?<enchantment>.+) (?<tier>[IVXLCDM]+) §r§egift with .*"
    )

    /**
     * REGEX-TEST: §e§lSWEET! §r§5Snow Suit Helmet §r§egift with §r§b[MVP§r§4+§r§b] FearNotMyName§r§f§r§e!
     * REGEX-TEST: §9§lRARE! §r§f◆ Ice Rune §r§egift with §r§b[MVP§r§2+§r§b] TravisScotties§r§f§r§e!
     * REGEX-TEST: §e§lSWEET! §r§5Snow Suit Chestplate §r§egift with §r§7Sanstin21§r§7§r§e!
     * REGEX-TEST: §c§lSANTA TIER! §r§6Cryopowder Shard §r§egift with §r§7MicrosoftDotInc§r§7§r§e!
     */
    val genericRewardPattern by patternGroup.pattern(
        "reward.generic",
        "§.§l.*! §r§.(?<item>.+) §r§egift with §r.*"
    )

    // Patterns to remove from chat - kept here to centralize more specific patterns away from ChatUtils
    val spamPatterns = listOf(
        "§cThis player is playing a profile mode which doesn't allow gifting!",
        "§cAn error occurred!",
        "§cCan't place gifts this close to spawn!",
        "§cYou cannot place a gift so close to an NPC!",
        "§eClick a player to gift them! §r§cThis isn't a player!",
        ".*§r§cdisconnected, gift refunded!",
        "§cThis gift is for .*, sorry!"
    ).map { it.toPattern() }
    // </editor-fold>

    private val tracker = SkyHanniItemTracker("Gift Tracker", { Data() }, { it.giftProfitTracker }) {
        drawDisplay(it)
    }

    class Data : ItemTrackerData() {
        override fun resetItems() {
            giftsUsed.clear()
            rarityRewardTypesGained.clear()
            northStarsGained = 0
            skillXpGained.clear()
        }

        override fun getDescription(timesGained: Long): List<String> {
            val totalRewards = rarityRewardTypesGained.sumAllValues().toLong().takeIf { it > 0 } ?: 1
            val percentage = timesGained.toDouble() / totalRewards
            val dropRate = LorenzUtils.formatPercentage(percentage.coerceAtMost(1.0))
            return listOf(
                "§7Dropped §e${timesGained.addSeparators()} §7times.",
                "§7Your drop rate: §c$dropRate.",
            )
        }

        override fun getCoinName(item: TrackedItem) = "§6Gift Coins"

        override fun getCoinDescription(item: TrackedItem): List<String> {
            val giftCoinsFormat = item.totalAmount.shortFormat()
            return listOf(
                "§7Coins occasionally drop from gifts.",
                "§7You got §6$giftCoinsFormat coins §7that way.",
            )
        }

        @Expose
        var giftsUsed: MutableMap<GiftType, Long> = mutableMapOf()

        @Expose
        var rarityRewardTypesGained: MutableMap<GiftRewardRarityType, Long> = mutableMapOf()

        @Expose
        var northStarsGained: Long = 0

        @Expose
        var skillXpGained: MutableMap<SkillType, Long> = mutableMapOf()
    }

    enum class GiftType(
        val displayName: String,
    ) {
        WHITE("§fWhite Gift"),
        GREEN("§aGreen Gift"),
        RED("§9§cRed Gift"),
        PARTY("§aParty Gift"),
        ;

        fun toInternalName() = "${name}_GIFT".toInternalName()

        companion object {
            fun byUserInput(name: String) = entries.firstOrNull { it.name.equals(name, true) }
        }
    }

    enum class GiftRewardRarityType(
        val displayName: String,
    ) {
        COMMON("§f§lCOMMON"),
        RARE("§9§lRARE"),
        SWEET("§e§lSWEET"),
        SANTA("§c§lSANTA TIER"),
        PARTY("§c§lPARTY TIER"),
        ;

        override fun toString() = displayName

        companion object {
            fun getByNameOrNull(name: String) = entries.firstOrNull {
                it.name.uppercase() == name.uppercase()
            }
        }
    }

    private val boostPotionCache = mutableMapOf<Pair<SkillType, Int>, NEUInternalName>()
    private fun getBoostPotion(skill: SkillType, tier: Int) = boostPotionCache.getOrPut(skill to tier) {
        "POTION_${skill.name.uppercase()}_XP_BOOST;$tier".toInternalName()
    }

    private const val ADD_GIFT_USAGE = "§eUsage:\n§6/shaddusedgifts §e<§6giftType§7: white,red,green§e> <§6amount§e>\n" +
        "§eExample: §6/shaddusedgifts white 10\n§eIf no amount is specified, 1 is assumed."

    private fun tryAddUsedGift(args: Array<String>): String {
        if (args.isEmpty()) return ADD_GIFT_USAGE
        val giftName = args[0]
        val gift = GiftType.byUserInput(giftName) ?: return ADD_GIFT_USAGE
        val amountArg = args.getOrNull(1) ?: "1"
        val amount = amountArg.toLongOrNull() ?: return "§cInvalid amount (§4${args[1]}§c) specified.\n$ADD_GIFT_USAGE"
        tracker.modify {
            it.giftsUsed.addOrPut(gift, amount)
        }
        val pluralization = if (amount == 1L) "" else "s"
        return "§aAdded §2${amount.addSeparators()}§8x §7${gift.displayName}$pluralization §ato used gifts."
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shaddusedgifts") {
            description = "Add used gifts to the gift profit tracker."
            category = CommandCategory.USERS_ACTIVE
            callback {
                ChatUtils.chat(tryAddUsedGift(it))
            }
        }
        event.register("shresetgifttracker") {
            description = "Reset the gift profit tracker."
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        tracker.renderDisplay(config.position)
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        northStarsPattern.matchMatcher(event.message) {
            val amount = group("amount").formatInt()
            tracker.modify {
                it.northStarsGained += amount
            }
            return // Don't continue to other patterns
        }

        giftRewardRarityPattern.matchMatcher(event.message) {
            val rewardRarity = GiftRewardRarityType.getByNameOrNull(group("rarity")) ?: return
            tracker.modify {
                it.rarityRewardTypesGained.addOrPut(rewardRarity, 1)
            }
        } ?: return // All gift messages should start with a rarity, if not, ignore the message

        xpGainedPattern.matchMatcher(event.message) {
            val skill = SkillType.getByNameOrNull(group("skill")) ?: return
            val amount = group("amount").formatLong()
            tracker.modify {
                it.skillXpGained.addOrPut(skill, amount)
            }
            return // Don't continue to other patterns
        }

        coinsGainedPattern.matchMatcher(event.message) {
            val amount = group("amount").formatInt()
            tracker.addCoins(amount, false)
            return // Don't continue to other patterns
        }

        boostPotionPattern.matchMatcher(event.message) {
            val skill = SkillType.getByNameOrNull(group("skill")) ?: return
            val tier = group("tier").romanToDecimal()
            val item = getBoostPotion(skill, tier)
            tracker.addItem(item, 1, false)
            return // Don't continue to other patterns
        }

        enchantmentBookPattern.matchMatcher(event.message) {
            val enchantment = group("enchantment")
            val tier = group("tier").romanToDecimal()
            val item = "${enchantment.uppercase()};$tier".toInternalName()
            tracker.addItem(item, 1, false)
            return // Don't continue to other patterns
        }

        genericRewardPattern.matchMatcher(event.message) {
            val (itemName, amount) = when (group("item")) {
                "◆ Ice Rune" -> "ICE_RUNE;1" to 1
                else -> ItemUtils.readItemAmount(group("item")) ?: return
            }
            NEUInternalName.fromItemNameOrNull(itemName)?.let { item ->
                tracker.addItem(item, amount, false)
            }
        }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§e§lGift Profit Tracker")
        var profit = tracker.drawItems(data, { true }, this)

        val giftsUsed = data.giftsUsed
        val applicableGifts = giftsUsed.filter { it.value > 0 }
        var totalGiftCost = 0.0
        val giftCostStrings = buildList {
            applicableGifts.forEach { (gift, count) ->
                val item = gift.toInternalName()
                val price = item.getPrice()
                val totalPrice = price * count
                if (totalPrice > 0) {
                    profit -= totalPrice
                    totalGiftCost += totalPrice
                    add("§7${count}x ${gift.displayName}§7: §c-${totalPrice.shortFormat()}")
                }
            }
        }

        // Add loss due to used gifts
        giftsUsed.sumAllValues().takeIf { it > 0 }?.let {
            val specificGiftFormat = if (applicableGifts.count() == 1) applicableGifts.keys.first().displayName else "§eGifts"
            val giftFormat = "§7${it.addSeparators()}x $specificGiftFormat§7: §c-${totalGiftCost.shortFormat()}"
            add(
                if (applicableGifts.count() == 1) Renderable.string(giftFormat).toSearchable()
                else Renderable.hoverTips(
                    giftFormat,
                    giftCostStrings,
                ).toSearchable(),
            )
        }

        // North star gains
        data.northStarsGained.takeIf { it > 0 }?.let {
            val northStarsFormat = it.shortFormat()
            add(
                Renderable.hoverTips(
                    "§d$northStarsFormat §5North Stars§7",
                    listOf("§7You gained §d${it.addSeparators()} §5North Stars."),
                ).toSearchable(),
            )
        }

        // Skill XP gains
        data.skillXpGained.sumAllValues().takeIf { it > 0 }?.let { sumXpGained ->
            val applicableSkills = data.skillXpGained.filter { it.value > 0 }
            val skillHoverTips = buildList {
                applicableSkills.forEach { (skill, xp) ->
                    add("§7${xp.addSeparators()} §3${skill.displayName} XP")
                }
            }.toMutableList()
            if (applicableSkills.size > 1) {
                skillHoverTips.add("§7You gained §e${sumXpGained.addSeparators()} §7total skill XP.")
            }
            add(
                Renderable.hoverTips(
                    "§7${sumXpGained.shortFormat()} §3Skill XP",
                    skillHoverTips,
                ).toSearchable(),
            )
        }

        // Breakdown of rewards by rarity
        val totalRewards = data.rarityRewardTypesGained.sumAllValues().toLong()
        val applicableRarities = data.rarityRewardTypesGained.filter { it.value > 0 }
        val rewardHoverTips = buildList {
            applicableRarities.forEach { (rarity, count) ->
                add("§7${count.addSeparators()}x ${rarity.displayName}§7")
            }
        }
        totalRewards.takeIf { it > 0 }?.let {
            add(
                Renderable.hoverTips(
                    "§eTotal Rewards§7: ${it.shortFormat()}",
                    rewardHoverTips,
                ).toSearchable(),
            )
        }

        add(tracker.addTotalProfit(profit, totalRewards, "gift"))
        tracker.addPriceFromButton(this)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled && (!config.holdingGift || isHoldingGift())
}
