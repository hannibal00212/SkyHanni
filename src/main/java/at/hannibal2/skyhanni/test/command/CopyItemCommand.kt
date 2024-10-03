package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getMinecraftId
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object CopyItemCommand {

    fun command() {
        val itemStack = InventoryUtils.getItemInHand()
        if (itemStack == null) {
            ChatUtils.userError("No item in hand!")
            return
        }
        copyItemToClipboard(itemStack)
    }

    private fun recurseTag(compound: NBTTagCompound, text: String, list: MutableList<String>) {
        for (s in compound.keySet) {
            if (s == "Lore") continue
            val tag = compound.getTag(s)

            if (tag !is NBTTagCompound) {
                list.add("$text$s: $tag")
            } else {
                val element = compound.getCompoundTag(s)
                list.add("$text$s:")
                recurseTag(element, "$text  ", list)
            }
        }
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
        if (itemStack.hasTagCompound()) {
            val tagCompound = itemStack.tagCompound
            recurseTag(tagCompound, "  ", resultList)
        }

        val string = resultList.joinToString("\n")
        OSUtils.copyToClipboard(string)
        ChatUtils.chat("Item info copied into the clipboard!")
    }
}
