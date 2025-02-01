package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiEditSign
import at.hannibal2.skyhanni.mixins.transformers.gui.AccessorGuiContainer
import at.hannibal2.skyhanni.utils.StringUtils.capAtMinecraftLength
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ChatComponentText

object McScreen {

    var screen: GuiScreen?
        get() = McClient.minecraft.currentScreen
        set(value) = McClient.minecraft.displayGuiScreen(value)

    val width: Int
        get() = ScaledResolution(McClient.minecraft).scaledWidth

    val height: Int
        get() = ScaledResolution(McClient.minecraft).scaledHeight

    val isOpen get() = screen != null
    val GuiScreen.isSign get() = this is GuiEditSign
    val isSignOpen get() = screen is GuiEditSign
    val GuiScreen.isChest get() = this is GuiChest
    val isChestOpen get() = screen is GuiChest
    val GuiScreen.isInventory get() = this is GuiInventory
    val isInventoryOpen get() = screen is GuiInventory
    val GuiScreen.isChat get() = this is GuiChat
    val isChatOpen get() = screen is GuiChat

    val asChest get() = screen as? GuiChest
    val asContainer get() = screen as? GuiContainer
    val asSign get() = screen as? GuiEditSign

    val GuiChest.name get() = (inventorySlots as ContainerChest).lowerChestInventory.displayName.unformattedText.trim()

    val GuiContainer.left get() = (this as AccessorGuiContainer).guiLeft
    val GuiContainer.top get() = (this as AccessorGuiContainer).guiTop

    val GuiEditSign.text get() = (this as? AccessorGuiEditSign)?.tileSign?.signText?.map { it.unformattedText }

    fun GuiScreen.setTextIntoSign(text: String, line: Int = 0) {
        if (this !is AccessorGuiEditSign) return
        this.tileSign.signText[line] = ChatComponentText(text)
    }

    fun GuiScreen.addTextIntoSign(addedText: String) {
        if (this !is AccessorGuiEditSign) return
        val lines = this.tileSign.signText
        val index = this.editLine
        val text = lines[index].unformattedText + addedText
        lines[index] = ChatComponentText(text.capAtMinecraftLength(91))
    }

    fun GuiContainer.sendSlotClick(slot: Int, button: Int, mode: Int) {
        McClient.minecraft.playerController.windowClick(
            inventorySlots.windowId,
            slot,
            button,
            mode,
            McPlayer.player,
        )
    }
}
