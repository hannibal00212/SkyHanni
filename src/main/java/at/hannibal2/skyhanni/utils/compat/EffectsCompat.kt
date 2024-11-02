package at.hannibal2.skyhanni.utils.compat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
//#if MC > 1.12
//$$ import net.minecraft.init.MobEffects
//#endif

enum class EffectsCompat(val potion: Potion) {
    INVISIBILITY(
        //#if MC < 1.12
        Potion.invisibility
        //#else
        //$$ MobEffects.INVISIBILITY
        //#endif
    ),
    BLINDNESS(
        //#if MC < 1.12
        Potion.blindness
        //#else
        //$$ MobEffects.BLINDNESS
        //#endif
    ),
    ;

    companion object {
        fun EntityLivingBase.hasPotionEffect(effect: EffectsCompat): Boolean {
            return this.isPotionActive(effect.potion)
        }

        fun EntityLivingBase.activePotionEffect(effect: EffectsCompat): PotionEffect? {
            return this.getActivePotionEffect(effect.potion)
        }
    }
}
