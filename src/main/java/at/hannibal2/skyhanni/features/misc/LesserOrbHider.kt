package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.entity.EntityEquipmentChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.SkullTextureHolder
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes

@SkyHanniModule
object LesserOrbHider {

    private val config get() = SkyHanniMod.feature.misc
    private val enabled = config.lesserOrbHider
    private val hiddenEntities = CollectionUtils.weakReferenceList<EntityArmorStand>()

    private val LESSER_TEXTURE by lazy { SkullTextureHolder.getTexture("LESSER_ORB") }

    @HandleEvent(onlyOnSkyblock = true)
    fun onArmorChange(event: EntityEquipmentChangeEvent<EntityArmorStand>) {
        val entity = event.entity
        val itemStack = event.newItemStack ?: return

        if (event.isHand && itemStack.getSkullTexture() == LESSER_TEXTURE) {
            hiddenEntities.add(entity)
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onCheckRender(event: CheckRenderEntityEvent<EntityArmorStand>) {
        if (!enabled) return

        if (event.entity in hiddenEntities) {
            event.cancel()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!enabled) return
        if (event.type != EnumParticleTypes.REDSTONE) return

        for (armorStand in hiddenEntities) {
            val distance = armorStand.distanceTo(event.location)
            if (distance < 4) {
                event.cancel()
            }
        }
    }
}
