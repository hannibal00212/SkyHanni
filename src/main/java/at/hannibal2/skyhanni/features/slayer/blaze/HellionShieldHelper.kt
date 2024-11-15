package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.entity.EntityLiving
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HellionShieldHelper {

    val hellionShieldMobs = mutableMapOf<EntityLiving, HellionShield>()

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "slayer.blazeColoredMobs", "slayer.blazes.hellion.coloredMobs")
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        hellionShieldMobs.clear()
    }

    fun EntityLiving.setHellionShield(shield: HellionShield?) {
        if (shield != null) {
            hellionShieldMobs[this] = shield
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                this,
                shield.color.toColor().addAlpha(80),
            ) { LorenzUtils.inSkyBlock && SkyHanniMod.feature.slayer.blazes.hellion.coloredMobs }
        } else {
            hellionShieldMobs.remove(this)
            RenderLivingEntityHelper.removeCustomRender(this)
        }
    }
}
