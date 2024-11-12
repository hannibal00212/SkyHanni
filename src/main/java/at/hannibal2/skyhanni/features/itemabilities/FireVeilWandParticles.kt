package at.hannibal2.skyhanni.features.itemabilities

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.itemability.FireVeilWandConfig.DisplayEntry
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.features.nether.ashfang.AshfangFreezeCooldown
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object FireVeilWandParticles {

    private val config get() = SkyHanniMod.feature.inventory.itemAbilities.fireVeilWands
    private val item by lazy { "FIRE_VEIL_WAND".toInternalName() }

    private var lastClick = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (config.display == DisplayEntry.PARTICLES) return
        if (lastClick.passedSince() > 5.5.seconds) return
        if (event.type == EnumParticleTypes.FLAME && event.speed == 0.55f) {
            event.cancel()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onItemClick(event: ItemClickEvent) {
        if (event.clickType != ClickType.RIGHT_CLICK) return
        val internalName = event.itemInHand?.getInternalName()

        if (AshfangFreezeCooldown.isCurrentlyFrozen()) return

        if (internalName == item) {
            lastClick = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (config.display != DisplayEntry.LINE) return
        if (lastClick.passedSince() > 5.5.seconds) return

        val color = config.displayColor.toChromaColor()
        RenderUtils.drawCircle(Minecraft.getMinecraft().thePlayer, event.partialTicks, 3.5, color)
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "itemAbilities.fireVeilWandDisplayColor", "itemAbilities.fireVeilWands.displayColor")
        event.move(3, "itemAbilities.fireVeilWandDisplay", "itemAbilities.fireVeilWands.display")

        event.transform(15, "itemAbilities.fireVeilWands.display") { element ->
            ConfigUtils.migrateIntToEnum(element, DisplayEntry::class.java)
        }
    }
}
