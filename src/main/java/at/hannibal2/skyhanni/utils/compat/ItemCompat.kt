package at.hannibal2.skyhanni.utils.compat

import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack

fun ItemStack.getTooltipCompat(advanced: Boolean): MutableList<String> {
    //#if MC < 1.12
    return this.getTooltip(Minecraft.getMinecraft().thePlayer, advanced)
    //#elseif MC < 1.16
    //$$ return this.getTooltip(Minecraft.getMinecraft().player) { advanced }
    //#else
    //$$ return this.getTooltipLines(Minecraft.getInstance().player) { advanced }.map { it.getFormattedTextCompat() }.toMutableList()
    //#endif
}
