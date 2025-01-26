package at.hannibal2.skyhanni.features.event.jerry

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.entity.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.LorenzColor
import net.minecraft.entity.passive.EntityVillager

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

    @HandleEvent(onlyOnSkyblock = true)
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        if (!config.highlightJerries) return

        val entity = event.entity
        val maxHealth = event.maxHealth

        if (entity is EntityVillager && maxHealth in 3..6) {
            val color = listOfLorenzColors[maxHealth].toColor().addAlpha(20)
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(entity, color) { config.highlightJerries }
        }
    }
}
