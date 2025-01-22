package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.InventoryHashUpdatedEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object OtherInventoryData {

    private var currentInventory: Inventory? = null
    private var acceptItems = false
    private val lateEvents: MutableSet<InventoryOpenEvent> = mutableSetOf()

    @HandleEvent
    fun onCloseWindow(event: GuiContainerEvent.CloseWindowEvent) {
        close()
    }

    fun close(reopenSameName: Boolean = false) {
        InventoryCloseEvent(reopenSameName).post()
        currentInventory = null
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        lateEvents.forEach { it.post() }
        lateEvents.clear()
    }

    @HandleEvent
    fun onInventoryDataReceiveEvent(event: PacketReceivedEvent) {
        val packet = event.packet

        if (packet is S2EPacketCloseWindow) {
            close()
        }

        if (packet is S2DPacketOpenWindow) {
            val windowId = packet.windowId
            val title = packet.windowTitle.unformattedText
            val slotCount = packet.slotCount
            close(reopenSameName = title == currentInventory?.title)

            currentInventory = Inventory(windowId, title, slotCount)
            acceptItems = true
        }

        if (packet is S2FPacketSetSlot) {
            val inventory = currentInventory?.takeIf { it.windowId == packet.func_149175_c() } ?: return

            val slot = packet.func_149173_d()
            val itemStack = packet.func_149174_e()

            if (slot < inventory.slotCount) {
                if (!acceptItems) {
                    itemStack?.let {
                        inventory.items[slot] = it
                        lateEvents.add(InventoryUpdatedEvent(inventory))
                        recheckHash(inventory)
                    }
                    return
                }

                itemStack?.let { inventory.items[slot] = it }

                if (inventory.items.size == inventory.slotCount) done(inventory)
            } else if (acceptItems) done(inventory)
        }
    }

    private fun recheckHash(inventory: Inventory) {
        if (inventory.itemHash == inventory.items.hashCode()) return
        lateEvents.add(InventoryHashUpdatedEvent(inventory))
    }

    private fun done(inventory: Inventory) {
        InventoryFullyOpenedEvent(inventory).post()
        inventory.fullyOpenedOnce = true
        InventoryUpdatedEvent(inventory).post()
        acceptItems = false
    }

    class Inventory(
        val windowId: Int,
        val title: String,
        val slotCount: Int,
        val items: MutableMap<Int, ItemStack> = mutableMapOf(),
        val itemHash: Int = items.hashCode(),
        var fullyOpenedOnce: Boolean = false,
    )
}
