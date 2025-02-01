package at.hannibal2.skyhanni.features.rift.area.wyldwoods

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.addAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.getEntities
import at.hannibal2.skyhanni.utils.EntityUtils.wearingSkullTexture
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.SkullTextureHolder
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object RiftLarva {

    private val config get() = RiftApi.config.area.wyldWoods.larvas
    private var hasHookInHand = false

    private val LARVA_SKULL_TEXTURE by lazy { SkullTextureHolder.getTexture("RIFT_LARVA") }

    private val LARVA_HOOK = "LARVA_HOOK".toInternalName()

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        checkHand()
        if (!hasHookInHand) return

        findLarvas()
    }

    private fun checkHand() {
        hasHookInHand = InventoryUtils.getItemInHand()?.getInternalName() == LARVA_HOOK
    }

    private fun findLarvas() {
        for (stand in getEntities<EntityArmorStand>()) {
            if (stand.wearingSkullTexture(LARVA_SKULL_TEXTURE)) {
                RenderLivingEntityHelper.setEntityColor(
                    stand,
                    config.highlightColor.toSpecialColor().addAlpha(1),
                ) { isEnabled() && hasHookInHand }
            }
        }
    }

    fun isEnabled() = RiftApi.inRift() && config.highlight
}
