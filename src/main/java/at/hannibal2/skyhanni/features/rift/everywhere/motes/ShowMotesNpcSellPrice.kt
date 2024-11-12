package at.hannibal2.skyhanni.features.rift.everywhere.motes

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.rift.motes.RiftInventoryValueConfig.NumberFormatEntry
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.features.rift.RiftAPI.motesNpcPrice
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils.chat
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils.addSelector
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ShowMotesNpcSellPrice {

    private val config get() = RiftAPI.config.motes

    private val burgerPattern by RepoPattern.pattern(
        "rift.everywhere.burger",
        ".*(?:§\\w)+You have (?:§\\w)+(?<amount>\\d) Grubber Stacks.*"
    )

    private var display = emptyList<List<Any>>()
    private val itemMap = mutableMapOf<NEUInternalName, Pair<MutableList<Int>, Double>>()
    private var inInventory = false
    private val slotList = mutableListOf<Int>()

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isInventoryValueEnabled()) return
        if (inInventory) {
            config.inventoryValue.position.renderStringsAndItems(
                display,
                itemScale = 0.7,
                posLabel = "Inventory Motes Value"
            )
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isInventoryValueEnabled()) return
        if (!event.isMod(10, 1)) return
        processItems()
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isShowPriceEnabled()) return

        val itemStack = event.itemStack

        val baseMotes = itemStack.motesNpcPrice() ?: return
        val burgerStacks = config.burgerStacks
        val burgerText = if (burgerStacks > 0) "(${burgerStacks}x≡) " else ""
        val size = itemStack.stackSize
        if (size > 1) {
            event.toolTip.add(
                "§6NPC price: $burgerText§d${baseMotes.addSeparators()} Motes " +
                    "§7($size x §d${(baseMotes / size).addSeparators()} Motes§7)"
            )
        } else {
            event.toolTip.add("§6NPC price: $burgerText§d${baseMotes.addSeparators()} Motes")
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        reset()
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        reset()
    }

    private fun reset() {
        if (!isInventoryValueEnabled()) return
        itemMap.clear()
        slotList.clear()
        inInventory = false
    }

    private fun processItems() {
        val inventoryName = InventoryUtils.openInventoryName()
        if (!inventoryName.contains("Rift Storage")) return
        val stacks = InventoryUtils.getItemsInOpenChest().map { it.slotIndex to it.stack }
        itemMap.clear()
        for ((index, stack) in stacks) {
            val itemValue = stack.motesNpcPrice() ?: continue
            val internalName = stack.getInternalName()
            if (itemMap.contains(internalName)) {
                val (oldIndex, oldValue) = itemMap[internalName] ?: return
                oldIndex.add(index)
                itemMap[internalName] = Pair(oldIndex, oldValue + itemValue)
            } else {
                itemMap[internalName] = Pair(mutableListOf(index), itemValue)
            }
        }
        inInventory = true
        update()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!RiftAPI.inRift()) return
        burgerPattern.matchMatcher(event.message) {
            config.burgerStacks = group("amount").toInt()
            chat("Set your McGrubber's burger stacks to ${group("amount")}.")
        }
    }

    private fun update() {
        display = drawDisplay()
    }

    private fun drawDisplay() = buildList<List<Any>> {
        val newDisplay = mutableListOf<List<Any>>()
        newDisplay.addAsSingletonList("§7Item Values:")
        val sorted = itemMap.toList().sortedByDescending { it.second.second }.toMap().toMutableMap()

        for ((internalName, pair) in sorted) {
            newDisplay.add(
                buildList {
                    val (index, value) = pair
                    add("  §7- ")
                    val stack = internalName.getItemStack()
                    add(stack)
                    val price = value.formatPrice()
                    val valuePer = stack.motesNpcPrice() ?: continue
                    val tips = buildList {
                        add("§6Item: ${stack.displayName}")
                        add("§6Value per: §d$valuePer Motes")
                        add("§6Total in chest: §d${(value / valuePer).toInt()}")
                        add("")
                        add("§6Total value: §d$price coins")
                    }
                    add(
                        Renderable.hoverTips(
                            "§6${stack.displayName}: §b$price",
                            tips,
                            highlightsOnHoverSlots = index,
                            stack = stack
                        )
                    )
                }
            )
        }
        val total = itemMap.values.fold(0.0) { acc, pair -> acc + pair.second }.formatPrice()
        newDisplay.addAsSingletonList("§7Total price: §b$total")
        val name = FormatType.entries[config.inventoryValue.formatType.ordinal].type // todo avoid ordinal
        newDisplay.addAsSingletonList("§7Price format: §c$name")
        newDisplay.addSelector<FormatType>(
            " ",
            getName = { type -> type.type },
            isCurrent = { it.ordinal == config.inventoryValue.formatType.ordinal }, // todo avoid ordinal
            onChange = {
                config.inventoryValue.formatType = NumberFormatEntry.entries[it.ordinal] // todo avoid ordinal
                update()
            }
        )
        return newDisplay
    }

    enum class FormatType(val type: String) {
        SHORT("Short"),
        LONG("Long")
    }

    private fun Double.formatPrice(): String = when (config.inventoryValue.formatType) {
        NumberFormatEntry.SHORT -> this.shortFormat()
        NumberFormatEntry.LONG -> this.addSeparators()
        else -> "0"
    }

    private fun isShowPriceEnabled() = RiftAPI.inRift() && config.showPrice

    private fun isInventoryValueEnabled() = RiftAPI.inRift() && config.inventoryValue.enabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(15, "rift.motes.inventoryValue.formatType") { element ->
            ConfigUtils.migrateIntToEnum(element, NumberFormatEntry::class.java)
        }
    }
}
