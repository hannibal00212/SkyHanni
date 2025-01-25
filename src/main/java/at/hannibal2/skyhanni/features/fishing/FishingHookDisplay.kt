package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.events.fishing.FishingBobberCastEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object FishingHookDisplay {

    private val config get() = SkyHanniMod.feature.fishing.fishingHookDisplay
    private var armorStand: EntityArmorStand? = null
    private val potentialArmorStands = mutableListOf<EntityArmorStand>()
    private val pattern = "§e§l(\\d+(\\.\\d+)?)".toPattern()

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        reset()
    }

    @HandleEvent
    fun onBobberThrow(event: FishingBobberCastEvent) {
        reset()
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return

        if (armorStand == null) {
            val filter = potentialArmorStands.filter { it.hasCustomName() && it.hasCorrectName() }
            if (filter.size == 1) {
                armorStand = filter[0]
            }
        }
    }

    private fun reset() {
        potentialArmorStands.clear()
        armorStand = null
    }

    @HandleEvent
    fun onJoinWorld(event: EntityEnterWorldEvent<EntityArmorStand>) {
        if (!isEnabled()) return
        potentialArmorStands.add(event.entity)
    }

    @HandleEvent
    fun onCheckRender(event: CheckRenderEntityEvent<EntityArmorStand>) {
        if (!isEnabled()) return
        if (!config.hideArmorStand) return

        if (event.entity == armorStand) {
            event.cancel()
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        val armorStand = armorStand ?: return
        if (armorStand.isDead) {
            reset()
            return
        }
        if (!armorStand.hasCustomName()) return
        val alertText = if (armorStand.name == "§c§l!!!") config.customAlertText.replace("&", "§") else armorStand.name

        config.position.renderString(alertText, posLabel = "Fishing Hook Display")
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(72, "fishing.fishingHookDisplay.position", Position::migrate)
    }

    private fun EntityArmorStand.hasCorrectName(): Boolean {
        if (name == "§c§l!!!") {
            return true
        }
        return pattern.matcher(name).matches()
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled && FishingApi.holdingRod
}
