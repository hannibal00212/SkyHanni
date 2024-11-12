package at.hannibal2.skyhanni.features.event.jerry

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import net.minecraft.entity.passive.EntityVillager
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HighlightJerries {

    private val config get() = SkyHanniMod.feature.event.jerry

    // RED RED WHITE LIGHT_PURPLE are fallbacks in case Hypixel admins do a little trolling
    private val listOfLorenzColors = listOf(
        LorenzColor.RED,
        LorenzColor.RED,
        LorenzColor.WHITE,
        LorenzColor.GREEN,
        LorenzColor.BLUE,
        LorenzColor.DARK_PURPLE,
        LorenzColor.GOLD,
        LorenzColor.LIGHT_PURPLE,
    )

    @SubscribeEvent
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.highlightJerries) return

        val entity = event.entity
        val maxHealth = event.maxHealth

        if (entity is EntityVillager && maxHealth in 3..6) {
            val color = listOfLorenzColors[maxHealth].toColor().addAlpha(20)
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(entity, color) { config.highlightJerries }
        }
    }
}
