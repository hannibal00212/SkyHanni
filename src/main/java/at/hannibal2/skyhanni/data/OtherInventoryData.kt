package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils.getTitle
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object OtherInventoryData {

    private var currentInventory: Inventory? = null
    private var acceptItems = false
    private var lateEvent: InventoryUpdatedEvent? = null

    @SubscribeEvent
    fun onCloseWindow(event: GuiContainerEvent.CloseWindowEvent) {
        close(event.gui.getTitle())
    }

    fun close(title: String?, reopenSameName: Boolean = false) {
        InventoryCloseEvent(title ?: "Null", reopenSameName).postAndCatch()
        currentInventory = null
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        lateEvent?.let {
            it.postAndCatch()
            lateEvent = null
        }
    }

    @HandleEvent
    fun onInventoryDataReceiveEvent(event: PacketReceivedEvent) {
        val packet = event.packet

        if (packet is S2EPacketCloseWindow) {
            close(Minecraft.getMinecraft().currentScreen?.getTitle())
        }

        if (packet is S2DPacketOpenWindow) {
            val windowId = packet.windowId
            val title = packet.windowTitle.unformattedText
            val slotCount = packet.slotCount
            close(title, reopenSameName = title == currentInventory?.title)

            currentInventory = Inventory(windowId, title, slotCount)
            acceptItems = true
        }

        if (packet is S2FPacketSetSlot) {
            if (!acceptItems) {
                currentInventory?.let {
                    if (it.windowId != packet.func_149175_c()) return

                    val slot = packet.func_149173_d()
                    if (slot < it.slotCount) {
                        val itemStack = packet.func_149174_e()
                        if (itemStack != null) {
                            it.items[slot] = itemStack
                            lateEvent = InventoryUpdatedEvent(it)
                        }
                    }
                }
                return
            }
            currentInventory?.let {
                if (it.windowId != packet.func_149175_c()) return

                val slot = packet.func_149173_d()
                if (slot < it.slotCount) {
                    val itemStack = packet.func_149174_e()
                    if (itemStack != null) {
                        it.items[slot] = itemStack
                    }
                } else {
                    done(it)
                    return
                }

                if (it.items.size == it.slotCount) {
                    done(it)
                }
            }
        }
    }

    private fun done(inventory: Inventory) {
        InventoryFullyOpenedEvent(inventory).postAndCatch()
        inventory.fullyOpenedOnce = true
        InventoryUpdatedEvent(inventory).postAndCatch()
        acceptItems = false
    }

    class Inventory(
        val windowId: Int,
        val title: String,
        val slotCount: Int,
        val items: MutableMap<Int, ItemStack> = mutableMapOf(),
        var fullyOpenedOnce: Boolean = false,
    )
}
