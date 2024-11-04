package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import net.minecraft.entity.passive.EntityHorse
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object RiftHorsezookaHider {

    private val HORSEZOOKA by lazy { "HORSEZOOKA".asInternalName() }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!RiftAPI.inRift()) return
        if (!SkyHanniMod.feature.rift.horsezookaHider) return

        if (event.entity is EntityHorse && InventoryUtils.itemInHandId == HORSEZOOKA) {
            event.cancel()
        }
    }
}
