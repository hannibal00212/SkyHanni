package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.getReadableNBTDump
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getMinecraftId
import net.minecraft.item.ItemStack

object CopyItemCommand {

    fun command() {
        val itemStack = InventoryUtils.getItemInHand()
        if (itemStack == null) {
            ChatUtils.userError("No item in hand!")
            return
        }
        copyItemToClipboard(itemStack)
    }

    fun copyItemToClipboard(itemStack: ItemStack) {
        val resultList = mutableListOf<String>()
        resultList.add(itemStack.getInternalName().toString())
        resultList.add("display name: '" + itemStack.displayName.toString() + "'")
        resultList.add("minecraft id: '" + itemStack.getMinecraftId() + "'")
        resultList.add("lore:")
        for (line in itemStack.getLore()) {
            resultList.add(" '$line'")
        }
        resultList.add("")
        resultList.add("getTagCompound")
        itemStack.tagCompound?.let {
            resultList.addAll(it.getReadableNBTDump())
        } ?: resultList.add("no tag compound")

        val string = resultList.joinToString("\n")
        OSUtils.copyToClipboard(string)
        ChatUtils.chat("Item info copied into the clipboard!")
    }
}
