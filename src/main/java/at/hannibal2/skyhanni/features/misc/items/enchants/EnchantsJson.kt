package at.hannibal2.skyhanni.features.misc.items.enchants

import at.hannibal2.skyhanni.features.misc.items.enchants.EnchantParser.enchantmentExclusivePattern
import at.hannibal2.skyhanni.features.misc.items.enchants.EnchantParser.enchantmentPattern
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class EnchantsJson {
    @Expose
    @SerializedName("NORMAL")
    var normal: HashMap<String, Enchant.Normal> = hashMapOf()

    @Expose
    @SerializedName("ULTIMATE")
    var ultimate: HashMap<String, Enchant.Ultimate> = hashMapOf()

    @Expose
    @SerializedName("STACKING")
    var stacking: HashMap<String, Enchant.Stacking> = hashMapOf()

    fun getFromLore(passedLoreName: String): Enchant {
        val loreName = passedLoreName.lowercase()
        var enchant: Enchant? = normal[loreName]
        if (enchant == null) enchant = ultimate[loreName]
        if (enchant == null) enchant = stacking[loreName]
        if (enchant == null) enchant = Enchant.Dummy(passedLoreName)
        return enchant
    }

    fun containsEnchantment(enchants: Map<String, Int>, line: String): Boolean {
        val exclusiveMatch = enchantmentExclusivePattern.matcher(line)
        if (!exclusiveMatch.find()) return false // This is the case that the line is not exclusively enchants

        val matcher = enchantmentPattern.matcher(line)
        while (matcher.find()) {
            val enchant = this.getFromLore(matcher.group("enchant"))
            if (enchants.isNotEmpty()) {
                if (enchants.containsKey(enchant.nbtName)) return true
            } else {
                if (normal.containsKey(enchant.loreName.lowercase()) ||
                    ultimate.containsKey(enchant.loreName.lowercase()) ||
                    stacking.containsKey(enchant.loreName.lowercase())
                )
                    return true
            }
        }
        return false
    }

    fun hasEnchantData() = normal.isNotEmpty() && ultimate.isNotEmpty() && stacking.isNotEmpty()
}
