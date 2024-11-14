package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.event.hoppity.HoppityEggsConfig
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.hoppity.EggFoundEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.BOUGHT
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_FACTORY_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.CHOCOLATE_SHOP_MILESTONE
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.getEggType
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.HITMAN
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.SIDE_DISH
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.STRAY
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggsManager.eggFoundPattern
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

typealias RarityType = HoppityEggsConfig.CompactRarityTypes

@SkyHanniModule
object HoppityEggsCompactChat {

    private var hoppityEggChat = mutableListOf<String>()
    private var duplicate = false
    private var lastRarity = ""
    private var lastName = ""
    private var lastProfit = ""
    private var newRabbit = false
    private var lastChatMeal: HoppityEggType? = null
    private var lastDuplicateAmount: Long? = null
    var persistHitman = false
    private val config get() = ChocolateFactoryAPI.config
    private val eventConfig get() = SkyHanniMod.feature.event.hoppityEggs

    fun compactChat(event: LorenzChatEvent? = null, lastDuplicateAmount: Long? = null) {
        if (!HoppityEggsManager.config.compactChat) return
        lastDuplicateAmount?.let {
            this.lastDuplicateAmount = it
            this.duplicate = true
        }
        event?.let {
            it.blockedReason = "compact_hoppity"
            hoppityEggChat.add(it.message)
        }
        if (hoppityEggChat.size == 3) sendCompact()
    }

    private fun sendCompact() {
        if (lastChatMeal.let { HoppityEggType.resettingEntries.contains(it) } && eventConfig.sharedWaypoints) {
            DelayedRun.runDelayed(5.milliseconds) {
                clickableCompact(HoppityEggsManager.getAndDisposeWaypointOnclick())
                resetCompactData()
            }
        } else {
            ChatUtils.hoverableChat(createCompactMessage(), hover = hoppityEggChat, prefix = false)
            resetCompactData()
        }
    }

    private fun resetCompactData() {
        this.hoppityEggChat = mutableListOf()
        this.duplicate = false
        this.newRabbit = false
        this.lastRarity = ""
        this.lastName = ""
        this.lastProfit = ""
        this.lastChatMeal = null
        this.lastDuplicateAmount = null
    }

    private fun createCompactMessage(): String {
        val mealNameFormat = when (lastChatMeal) {
            BOUGHT -> "§aBought Rabbit"
            SIDE_DISH -> "§6§lSide Dish §r§6Egg"
            CHOCOLATE_SHOP_MILESTONE, CHOCOLATE_FACTORY_MILESTONE -> "§6§lMilestone Rabbit"
            STRAY -> "§aStray Rabbit"
            HITMAN -> "§cHitman Rabbit"
            else -> "${lastChatMeal?.coloredName.orEmpty()} Egg"
        }

        val rarityConfig = HoppityEggsManager.config.rarityInCompact
        return if (duplicate) {
            val format = lastDuplicateAmount?.shortFormat() ?: "?"
            val timeFormatted = lastDuplicateAmount?.let {
                ChocolateFactoryAPI.timeUntilNeed(it).format(maxUnits = 2)
            } ?: "?"

            val dupeNumberFormat = if (eventConfig.showDuplicateNumber) {
                (HoppityCollectionStats.getRabbitCount(this.lastName)).takeIf { it > 0 }?.let {
                    " §7(§b#$it§7)"
                }.orEmpty()
            } else ""

            val showDupeRarity = rarityConfig.let { it == RarityType.BOTH || it == RarityType.DUPE }
            val timeStr = if (config.showDuplicateTime) ", §a+§b$timeFormatted§7" else ""
            "$mealNameFormat! §7Duplicate ${if (showDupeRarity) "$lastRarity " else ""}" +
                "$lastName$dupeNumberFormat §7(§6+$format Chocolate§7$timeStr)"
        } else if (newRabbit) {
            val showNewRarity = rarityConfig.let { it == RarityType.BOTH || it == RarityType.NEW }
            "$mealNameFormat! §d§lNEW ${if (showNewRarity) "$lastRarity " else ""}$lastName §7($lastProfit§7)"
        } else "?"
    }

    private fun clickableCompact(onClick: () -> Unit) {
        val hover = hoppityEggChat.joinToString("\n") + " \n§eClick here to share the location of this chocolate egg with the server!"
        hoppityEggChat.clear()
        ChatUtils.clickableChat(
            createCompactMessage(),
            hover = hover,
            onClick = onClick,
            expireAt = 30.seconds.fromNow(),
            oneTimeClick = true,
            prefix = false,
        )
    }

    @HandleEvent
    fun onEggFound(event: EggFoundEvent) {
        if (!HoppityEggsManager.config.compactChat || HoppityEggType.resettingEntries.contains(event.type) || event.type == BOUGHT) return
        lastChatMeal = event.type
        hoppityEggChat.add(
            when (event.type) {
                SIDE_DISH ->
                    "§d§lHOPPITY'S HUNT §r§dYou found a §r§6§lSide Dish §r§6Egg §r§din the Chocolate Factory§r§d!"

                CHOCOLATE_FACTORY_MILESTONE ->
                    "§d§lHOPPITY'S HUNT §r§dYou claimed a §r§6§lChocolate Milestone Rabbit §r§din the Chocolate Factory§r§d!"

                CHOCOLATE_SHOP_MILESTONE ->
                    "§d§lHOPPITY'S HUNT §r§dYou claimed a §r§6§lShop Milestone Rabbit §r§din the Chocolate Factory§r§d!"

                STRAY ->
                    "§d§lHOPPITY'S HUNT §r§dYou found a §r§aStray Rabbit§r§d!"

                else ->
                    "§d§lHOPPITY'S HUNT §r§7Unknown Egg Type?"
            },
        )
        if (hoppityEggChat.size == 3) sendCompact()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        eggFoundPattern.matchMatcher(event.message) {
            resetCompactData()
            lastChatMeal = if (persistHitman) HITMAN else getEggType(event)
            compactChat(event)
        }

        HoppityEggsManager.eggBoughtPattern.matchMatcher(event.message) {
            if (group("rabbitname") == lastName) {
                lastChatMeal = BOUGHT
                compactChat(event)
            }
        }

        HoppityEggsManager.rabbitFoundPattern.matchMatcher(event.message) {
            lastName = group("name")
            lastRarity = group("rarity")
            compactChat(event)
        }

        HoppityEggsManager.newRabbitFound.matchMatcher(event.message) {
            newRabbit = true
            groupOrNull("other")?.let {
                lastProfit = it
                compactChat(event)
                return
            }
            val chocolate = groupOrNull("chocolate")
            val perSecond = group("perSecond")
            lastProfit = chocolate?.let {
                "§6+$it §7and §6+${perSecond}x c/s!"
            } ?: "§6+${perSecond}x c/s!"
            compactChat(event)
        }
    }
}
