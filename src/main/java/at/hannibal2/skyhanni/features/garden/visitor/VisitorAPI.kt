package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.events.garden.visitor.VisitorAcceptedEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorArrivalEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorLeftEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorRefusedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.isInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import java.awt.Color

@SkyHanniModule
object VisitorAPI {

    private var visitors = mapOf<String, Visitor>()
    var inInventory = false
    var lastClickedNpc = 0
    val config get() = GardenAPI.config.visitors
    private val logger = LorenzLogger("garden/visitors/api")

    const val INFO_SLOT = 13
    const val ACCEPT_SLOT = 29
    const val REFUSE_SLOT = 33

    val patternGroup = RepoPattern.group("garden.visitor.api")
    val visitorCountPattern by patternGroup.pattern(
        "visitor.count",
        "§b§lVisitors: §r§f\\((?<info>.*)\\)",
    )
    private val visitorNamePattern by patternGroup.pattern(
        "visitor.name",
        " (?:§.)+(?<name>§.[^§]+).*",
    )

    fun getVisitorsMap() = visitors
    fun getVisitors() = visitors.values
    fun getVisitor(id: Int) = visitors.map { it.value }.find { it.entityId == id }
    fun getVisitor(name: String) = visitors[name]

    fun reset() {
        visitors = emptyMap()
    }

    fun changeStatus(visitor: Visitor, newStatus: VisitorStatus, reason: String) {
        val old = visitor.status
        if (old == newStatus) return
        visitor.status = newStatus
        logger.log("Visitor status change for '${visitor.visitorName}': $old -> $newStatus ($reason)")

        when (newStatus) {
            VisitorStatus.ACCEPTED -> {
                VisitorAcceptedEvent(visitor).postAndCatch()
            }

            VisitorStatus.REFUSED -> {
                VisitorRefusedEvent(visitor).postAndCatch()
            }

            else -> {}
        }
    }

    fun getOrCreateVisitor(name: String): Visitor? {
        var visitor = visitors[name]
        if (visitor == null) {
            // workaround if the tab list has not yet updated when opening the visitor
            addVisitor(name)
            ChatUtils.debug("Found visitor from npc that is not in tab list. Adding it still.")
            visitor = visitors[name]
        }

        if (visitor != null) return visitor

        ErrorManager.logErrorStateWithData(
            "Error finding the visitor `$name§c`. Try to reopen the inventory",
            "Visitor is null while opening visitor inventory",
            "name" to name,
            "visitors" to visitors,
        )
        return null
    }

    fun removeVisitor(name: String): Boolean {
        if (!visitors.containsKey(name)) return false
        val visitor = visitors[name] ?: return false
        visitors = visitors.editCopy { remove(name) }
        VisitorLeftEvent(visitor).postAndCatch()
        return true
    }

    fun addVisitor(name: String): Boolean {
        if (visitors.containsKey(name)) return false
        val visitor = Visitor(name, status = VisitorStatus.NEW)
        visitors = visitors.editCopy { this[name] = visitor }
        VisitorArrivalEvent(visitor).postAndCatch()
        return true
    }

    fun fromHypixelName(line: String): String {
        var name = line.trim().replace("§r", "").trim()
        if (!name.contains("§")) {
            name = "§f$name"
        }
        return name
    }

    fun isVisitorInfo(lore: List<String>): Boolean {
        if (lore.size != 4) return false
        return lore[3].startsWith("§7Offers Accepted: §a")
    }

    class VisitorOffer(
        val offerItem: ItemStack,
    )

    class Visitor(
        val visitorName: String,
        var entityId: Int = -1,
        var nameTagEntityId: Int = -1,
        var status: VisitorStatus,
        val shoppingList: MutableMap<NEUInternalName, Int> = mutableMapOf(),
        var offer: VisitorOffer? = null,
    ) {
        var offersAccepted: Int? = null
        var pricePerCopper: Int? = null
        var totalPrice: Double? = null
        var totalReward: Double? = null
        var lore: List<String> = emptyList()
        var allRewards = listOf<NEUInternalName>()
        var lastLore = listOf<String>()
        var blockedLore = listOf<String>()
        var blockReason: VisitorBlockReason? = null

        fun getEntity() = EntityUtils.getEntityByID(entityId)
        fun getNameTagEntity() = EntityUtils.getEntityByID(nameTagEntityId)

        fun hasReward(): VisitorReward? {
            for (internalName in allRewards) {
                val reward = VisitorReward.getByInternalName(internalName) ?: continue
                if (reward in config.rewardWarning.drops) {
                    return reward
                }
            }

            return null
        }
    }

    enum class VisitorStatus(val displayName: String, val color: Color?) {
        NEW("§eNew", LorenzColor.YELLOW.toColor().addAlpha(100)),
        WAITING("Waiting", null),
        READY("§aItems Ready", LorenzColor.GREEN.toColor().addAlpha(80)),
        ACCEPTED("§7Accepted", LorenzColor.DARK_GRAY.toColor().addAlpha(80)),
        REFUSED("§cRefused", LorenzColor.RED.toColor().addAlpha(60)),
    }

    fun visitorsInTabList(tabList: List<String>): List<String> {
        var visitorCount = 0
        var found = false
        var visitorsRemaining = 0

        val visitorsInTab = mutableListOf<String>()
        loop@ for (line in tabList) {
            visitorCountPattern.matchMatcher(line) {
                found = true
                val countInfo = group("info")
                if (countInfo.isInt()) {
                    visitorCount = countInfo.toInt()
                } else if (countInfo == "§r§c§lQueue Full!§r§f") visitorCount = 5

                visitorsRemaining = visitorCount
                continue@loop
            }

            if (!found) continue
            if (visitorsRemaining <= 0) {
                found = false
                continue
            }

            visitorNamePattern.matchMatcher(line) {
                visitorsInTab.add(group("name").trim())
            }

            visitorsRemaining--
        }
        return visitorsInTab
    }

    fun Visitor.blockReason(): VisitorBlockReason? = with(config.rewardWarning) {
        val pricePerCopper = pricePerCopper ?: error("pricePerCopper is null")
        val totalPrice = totalPrice ?: error("totalPrice is null")
        val totalReward = totalReward ?: error("totalReward is null")
        val loss = totalPrice - totalReward
        return when {
            preventRefusing && hasReward() != null -> VisitorBlockReason.RARE_REWARD
            preventRefusingNew && offersAccepted == 0 -> VisitorBlockReason.NEVER_ACCEPTED
            preventRefusingCopper && pricePerCopper <= coinsPerCopperPrice -> VisitorBlockReason.CHEAP_COPPER
            preventAcceptingCopper && pricePerCopper > coinsPerCopperPrice -> VisitorBlockReason.EXPENSIVE_COPPER
            preventRefusingLowLoss && loss <= coinsLossThreshold -> VisitorBlockReason.LOW_LOSS
            preventAcceptingHighLoss && loss > coinsLossThreshold -> VisitorBlockReason.HIGH_LOSS

            else -> null
        }
    }

    enum class VisitorBlockReason(val description: String, val blockRefusing: Boolean) {
        NEVER_ACCEPTED("§cNever accepted", true),
        RARE_REWARD("§aRare visitor reward found", true),
        CHEAP_COPPER("§aCheap copper", true),
        EXPENSIVE_COPPER("§cExpensive copper", false),
        LOW_LOSS("§aLow Loss", true),
        HIGH_LOSS("§cHigh Loss", false)
    }
}
