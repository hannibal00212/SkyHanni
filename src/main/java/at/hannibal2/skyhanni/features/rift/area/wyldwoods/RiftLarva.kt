package at.hannibal2.skyhanni.features.rift.area.wyldwoods

import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.getEntities
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RiftLarva {

    private val config get() = RiftAPI.config.area.wyldWoods.larvas
    private var hasHookInHand = false

    // TODO: Move to repo
    @Suppress("MaxLineLength")
    private const val LARVA_SKULL_TEXTURE =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgzYjMwZTlkMTM1YjA1MTkwZWVhMmMzYWM2MWUyYWI1NWEyZDgxZTFhNThkYmIyNjk4M2ExNDA4MjY2NCJ9fX0="

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        checkHand()
        if (!hasHookInHand) return

        if (event.repeatSeconds(1)) {
            findLarvas()
        }
    }

    private fun checkHand() {
        hasHookInHand = InventoryUtils.getItemInHand()?.getInternalName()?.equals("LARVA_HOOK") ?: false
    }

    private fun findLarvas() {
        for (stand in getEntities<EntityArmorStand>()) {
            if (stand.hasSkullTexture(LARVA_SKULL_TEXTURE)) {
                RenderLivingEntityHelper.setEntityColor(
                    stand,
                    config.highlightColor.toChromaColor().withAlpha(1)
                ) { isEnabled() && hasHookInHand }
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.highlight
}
