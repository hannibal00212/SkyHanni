package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.events.CollectionUpdateEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CollectionAPI {
    private val patternGroup = RepoPattern.group("data.collection.api")
    private val counterPattern by patternGroup.pattern(
        "counter",
        ".* §e(?<amount>.*)§6/.*"
    )
    private val singleCounterPattern by patternGroup.pattern(
        "singlecounter",
        "§7Total Collected: §e(?<amount>.*)"
    )
    private val collectionTier0Pattern by patternGroup.pattern(
        "tierzero",
        "§7Progress to .* I: .*"
    )

    val collectionValue = mutableMapOf<NEUInternalName, Long>()

    // TODO repo
    private val incorrectCollectionNames = mapOf(
        "Mushroom" to "RED_MUSHROOM".toInternalName()
    )

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        collectionValue.clear()
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        val inventoryName = event.inventoryName
        if (inventoryName.endsWith(" Collection")) {
            val stack = event.inventoryItems[4] ?: return
            stack.getLore().matchFirst(singleCounterPattern) {
                val counter = group("amount").formatLong()
                val name = inventoryName.split(" ").dropLast(1).joinToString(" ")
                val internalName = incorrectCollectionNames[name] ?: NEUInternalName.fromItemName(name)
                collectionValue[internalName] = counter
            }
            CollectionUpdateEvent.post()
        }

        if (inventoryName.endsWith(" Collections")) {
            if (inventoryName == "Boss Collections") return

            for ((_, stack) in event.inventoryItems) {
                var name = stack.name.removeColor()
                if (name.contains("Collections")) continue

                val lore = stack.getLore()
                if (!lore.any { it.contains("Click to view!") }) continue

                if (!isCollectionTier0(lore)) {
                    name = name.split(" ").dropLast(1).joinToString(" ")
                }

                val internalName = incorrectCollectionNames[name] ?: NEUInternalName.fromItemName(name)
                lore.matchFirst(counterPattern) {
                    val counter = group("amount").formatLong()
                    collectionValue[internalName] = counter
                }
            }
            CollectionUpdateEvent.post()
        }
    }

    @SubscribeEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (event.source == ItemAddManager.Source.COMMAND) return
        val internalName = event.internalName
        val amount = NEUItems.getPrimitiveMultiplier(internalName).amount
        if (amount > 1) return

        // TODO add support for replenish (higher collection than actual items in inv)
        if (internalName.getItemStackOrNull() == null) {
            ChatUtils.debug("CollectionAPI.addFromInventory: item is null for '$internalName'")
            return
        }
        collectionValue.addOrPut(internalName, event.amount.toLong())
    }

    fun isCollectionTier0(lore: List<String>) = lore.any { collectionTier0Pattern.matches(it) }
    fun getCollectionCounter(internalName: NEUInternalName): Long? = collectionValue[internalName]
}
