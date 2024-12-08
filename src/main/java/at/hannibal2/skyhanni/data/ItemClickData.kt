package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.EntityClickEvent
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketSentEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation

@SkyHanniModule
object ItemClickData {

    @HandleEvent
    fun onItemClickSend(event: PacketSentEvent) {
        val packet = event.packet
        val cancelled = when {
            packet is C08PacketPlayerBlockPlacement -> {
                if (packet.placedBlockDirection != 255) {
                    val position = packet.position.toLorenzVec()
                    BlockClickEvent(ClickType.RIGHT_CLICK, position, packet.stack).post()
                } else {
                    ItemClickEvent(InventoryUtils.getItemInHand(), ClickType.RIGHT_CLICK).post()
                }
            }

            packet is C07PacketPlayerDigging && packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK -> {
                val position = packet.position.toLorenzVec()
                val blockClickCancelled =
                    BlockClickEvent(ClickType.LEFT_CLICK, position, InventoryUtils.getItemInHand()).post()
                ItemClickEvent(InventoryUtils.getItemInHand(), ClickType.LEFT_CLICK).also {
                    if (blockClickCancelled) it.cancel()
                }.post()
            }

            packet is C0APacketAnimation -> {
                ItemClickEvent(InventoryUtils.getItemInHand(), ClickType.LEFT_CLICK).post()
            }

            packet is C02PacketUseEntity -> {
                val clickType = when (packet.action) {
                    C02PacketUseEntity.Action.INTERACT -> ClickType.RIGHT_CLICK
                    C02PacketUseEntity.Action.ATTACK -> ClickType.LEFT_CLICK
                    C02PacketUseEntity.Action.INTERACT_AT -> ClickType.RIGHT_CLICK
                    else -> return
                }
                val clickedEntity = packet.getEntityFromWorld(Minecraft.getMinecraft().theWorld) ?: return
                EntityClickEvent(clickType, clickedEntity, InventoryUtils.getItemInHand()).post()
            }

            else -> {
                return
            }
        }

        if (cancelled) {
            event.cancel()
        }
    }

    /* @SubscribeEvent
    fun onEntityClick(event: InputEvent) {
        if (!LorenzUtils.inSkyBlock) return

        val minecraft = Minecraft.getMinecraft()

        val attackKey = minecraft.gameSettings.keyBindAttack
        val useKey = minecraft.gameSettings.keyBindUseItem

        val clickType = when {
            attackKey.isKeyDown -> ClickType.LEFT_CLICK
            useKey.isKeyDown -> ClickType.RIGHT_CLICK
            else -> return
        }

        val clickedEntity = minecraft.pointedEntity
        if (minecraft.thePlayer == null) return
        if (clickedEntity == null) return

        EntityClickEvent(clickType, clickedEntity, InventoryUtils.getItemInHand()).postAndCatch()
    } */
}
