package at.hannibal2.skyhanni.features.misc.items.enchants

import at.hannibal2.skyhanni.utils.NumberUtil.toRoman
import net.minecraft.item.ItemStack

class FormattedEnchant(
    private val enchant: Enchant,
    private val level: Int,
    stacking: String,
    private val isRoman: Boolean,
) : Comparable<FormattedEnchant> {
    private val stacking: String = stacking
        get() = "§8$field"
    private val loreDescription: MutableList<String> = mutableListOf()

    fun addLore(lineOfLore: String) = loreDescription.add(lineOfLore)

    fun getLore() = loreDescription

    override fun compareTo(other: FormattedEnchant) = this.enchant.compareTo(other.enchant)

    fun getFormattedString(itemStack: ItemStack?): String {
        val builder = StringBuilder()
        builder.append(enchant.getFormattedName(level, itemStack)).append(" ").append(if (isRoman) level.toRoman() else level)

        return if (!stacking.contains("empty")) builder.append(stacking).toString() else builder.toString()
    }

    override fun toString(): String {
        return "FormattedEnchant[enchant: " + enchant.nbtName + ", level: " + level + ", isRoman: " + isRoman + "]"
    }
}
