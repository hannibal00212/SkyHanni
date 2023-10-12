package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.garden.visitor.VisitorAcceptedEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorArrivalEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorLeftEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorRefusedEvent
import at.hannibal2.skyhanni.events.withAlpha
import at.hannibal2.skyhanni.test.command.CopyErrorCommand
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.editCopy
import at.hannibal2.skyhanni.utils.NEUInternalName
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack

object VisitorAPI {
    private var visitors = mapOf<String, Visitor>()
    var inVisitorInventory = false
    val config get() = SkyHanniMod.feature.garden
    private val logger = LorenzLogger("garden/visitors/api")

    fun getVisitorsMap() = visitors
    fun getVisitors() = visitors.values
    fun getVisitor(id: Int) = visitors.map { it.value }.find { it.entityId == id }

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
            LorenzUtils.debug("Found visitor from npc that is not in tab list. Adding it still.")
            visitor = visitors[name]
        }

        if (visitor != null) return visitor

        println("visitors: $visitors")
        println("name: $name")
        CopyErrorCommand.logErrorState(
                "Error finding the visitor `$name§c`. Try to reopen the inventory",
                "visitor is null! name='$name', visitors=`$visitors`"
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

    class VisitorOffer(
            val offerItem: ItemStack
    )

    class Visitor(
            val visitorName: String,
            var entityId: Int = -1,
            var nameTagEntityId: Int = -1,
            var status: VisitorStatus,
            var inSacks: Boolean = false,
            val items: MutableMap<NEUInternalName, Int> = mutableMapOf(),
            var offer: VisitorOffer? = null,
    ) {
        var lore: List<String> = emptyList()
        var allRewards = listOf<NEUInternalName>()
        var lastLore = listOf<String>()

        fun getEntity(): Entity? = Minecraft.getMinecraft().theWorld.getEntityByID(entityId)
        fun getNameTagEntity(): Entity? = Minecraft.getMinecraft().theWorld.getEntityByID(nameTagEntityId)

        fun hasReward(): VisitorReward? {
            for (internalName in allRewards) {
                val reward = VisitorReward.getByInternalName(internalName) ?: continue

                if (config.visitorRewardWarning.drops.contains(reward.ordinal)) {
                    return reward
                }
            }

            return null
        }
    }

    enum class VisitorStatus(val displayName: String, val color: Int) {
        NEW("§eNew", LorenzColor.YELLOW.toColor().withAlpha(100)),
        WAITING("Waiting", -1),
        READY("§aItems Ready", LorenzColor.GREEN.toColor().withAlpha(80)),
        ACCEPTED("§7Accepted", LorenzColor.DARK_GRAY.toColor().withAlpha(80)),
        REFUSED("§cRefused", LorenzColor.RED.toColor().withAlpha(60)),
    }
}