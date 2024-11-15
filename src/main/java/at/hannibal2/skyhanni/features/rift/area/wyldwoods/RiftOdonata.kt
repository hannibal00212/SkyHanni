package at.hannibal2.skyhanni.features.rift.area.wyldwoods

import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.EntityUtils.getEntities
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.SkullTextureHolder
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RiftOdonata {

    private val config get() = RiftAPI.config.area.wyldWoods.odonata
    private var hasBottleInHand = false

    private val ODONATA_SKULL_TEXTURE by lazy { SkullTextureHolder.getTexture("MOB_ODONATA") }
    private val emptyBottle by lazy { "EMPTY_ODONATA_BOTTLE".toInternalName() }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        checkHand()
        if (!hasBottleInHand) return

        if (event.repeatSeconds(1)) {
            findOdonatas()
        }
    }

    private fun checkHand() {
        hasBottleInHand = InventoryUtils.getItemInHand()?.getInternalName() == emptyBottle
    }

    private fun findOdonatas() {
        for (stand in getEntities<EntityArmorStand>()) {
            if (stand.hasSkullTexture(ODONATA_SKULL_TEXTURE)) {
                RenderLivingEntityHelper.setEntityColor(
                    stand,
                    config.highlightColor.toChromaColor().addAlpha(1),
                ) { isEnabled() && hasBottleInHand }
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.highlight
}
