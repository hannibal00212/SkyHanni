package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.misc.HideArmorConfig.ModeEntry
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.EntityUtils.getArmorInventory
import at.hannibal2.skyhanni.utils.EntityUtils.hasPotionEffect
import at.hannibal2.skyhanni.utils.EntityUtils.isNPC
import at.hannibal2.skyhanni.utils.FakePlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.compat.Effects
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HideArmor {

    private val config get() = SkyHanniMod.feature.misc.hideArmor2
    private var armor = mapOf<Int, ItemStack>()

    private fun shouldHideArmor(entity: EntityLivingBase): Boolean {
        if (!LorenzUtils.inSkyBlock) return false
        if (entity !is EntityPlayer) return false
        if (entity is FakePlayer) return false
        if (entity.hasPotionEffect(Effects.invisibility)) return false
        if (entity.isNPC()) return false

        return when (config.mode) {
            ModeEntry.ALL -> true

            ModeEntry.OWN -> entity is EntityPlayerSP
            ModeEntry.OTHERS -> entity !is EntityPlayerSP

            else -> false
        }
    }

    @SubscribeEvent
    fun onRenderLivingPre(event: SkyHanniRenderEntityEvent.Pre<EntityLivingBase>) {
        val entity = event.entity
        if (!shouldHideArmor(entity)) return
        val armorInventory = entity.getArmorInventory() ?: return

        armor = buildMap {
            for ((i, stack) in armorInventory.withIndex()) {
                stack?.let {
                    if (!config.onlyHelmet || i == 3) {
                        this[i] = it.copy()
                        armorInventory[i] = null
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderLivingPost(event: SkyHanniRenderEntityEvent.Post<EntityLivingBase>) {
        val entity = event.entity
        if (!shouldHideArmor(entity)) return
        val armorInventory = entity.getArmorInventory() ?: return

        for ((index, stack) in armor) {
            armorInventory[index] = stack
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(15, "misc.hideArmor2.mode") { element ->
            ConfigUtils.migrateIntToEnum(element, ModeEntry::class.java)
        }
    }
}
