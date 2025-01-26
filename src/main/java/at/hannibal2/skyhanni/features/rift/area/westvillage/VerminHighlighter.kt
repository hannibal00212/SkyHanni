package at.hannibal2.skyhanni.features.rift.area.westvillage

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.wearingSkullTexture
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.SkullTextureHolder
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntitySilverfish
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object VerminHighlighter {
    private val config get() = RiftApi.config.area.westVillage.verminHighlight

    private val checkedEntities = TimeLimitedSet<Int>(1.minutes)

    private val VERMIN_FLY_TEXTURE by lazy { SkullTextureHolder.getTexture("VERMIN_FLY") }
    private val VERMIN_SPIDER_TEXTURE by lazy { SkullTextureHolder.getTexture("VERMIN_SPIDER") }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        for (entity in EntityUtils.getEntities<EntityLivingBase>()) {
            val id = entity.entityId
            if (id in checkedEntities) continue
            checkedEntities.add(id)

            if (!isVermin(entity)) continue
            val color = config.color.get().toSpecialColor().addAlpha(60)
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(entity, color) { isEnabled() }
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.color) {
            // running setEntityColorWithNoHurtTime() again
            checkedEntities.clear()
        }
    }

    private fun isVermin(entity: EntityLivingBase): Boolean = when (entity) {
        is EntityArmorStand -> entity.wearingSkullTexture(VERMIN_FLY_TEXTURE) || entity.wearingSkullTexture(VERMIN_SPIDER_TEXTURE)
        is EntitySilverfish -> entity.baseMaxHealth == 8

        else -> false
    }

    private fun hasItemInHand() = InventoryUtils.itemInHandId == "TURBOMAX_VACUUM".toInternalName()

    fun isEnabled() = RiftApi.inRift() && RiftApi.inWestVillage() && config.enabled && hasItemInHand()

}
