package at.hannibal2.skyhanni.utils.compat

import net.minecraft.enchantment.Enchantment
//#if MC >= 1.12
//$$ import net.minecraft.init.Enchantments
//#endif

enum class EnchantmentsCompat(val enchantment: Enchantment) {
    PROTECTION(
        //#if MC < 1.12
        Enchantment.protection
        //#else
        //$$ Enchantments.PROTECTION
        //#endif
    ),
}
