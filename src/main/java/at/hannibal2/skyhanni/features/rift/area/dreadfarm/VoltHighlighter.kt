package at.hannibal2.skyhanni.features.rift.area.dreadfarm

import at.hannibal2.skyhanni.events.EntityEquipmentChangeEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.EntityUtils.getEntities
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object VoltHighlighter {

    private val config get() = RiftAPI.config.area.dreadfarm.voltCrux

    private const val LIGHTNING_DISTANCE = 7F
    private const val ARMOR_SLOT_HEAD = 3
    private val CHARGE_TIME = 12.seconds
    private var chargingSince = mapOf<Entity, SimpleTimeMark>()

    @SubscribeEvent
    fun onArmorChange(event: EntityEquipmentChangeEvent) {
        if (!RiftAPI.inRift() || !config.voltWarning) return
        val player = Minecraft.getMinecraft().thePlayer ?: return
        if (event.isHead && getVoltState(event.entity) == VoltState.DOING_LIGHTNING &&
            event.entity.positionVector.squareDistanceTo(player.positionVector) <= LIGHTNING_DISTANCE * LIGHTNING_DISTANCE
        ) {
            chargingSince = chargingSince.editCopy {
                this[event.entity] = SimpleTimeMark.now()
            }
        }
    }

    @SubscribeEvent
    fun onRender(event: LorenzRenderWorldEvent) {
        if (!RiftAPI.inRift() || !(config.voltRange || config.voltMoodMeter)) return
        for (entity in getEntities<EntityLivingBase>()) {
            val state = getVoltState(entity)
            if (state == VoltState.NO_VOLT) continue

            if (config.voltMoodMeter)
                RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                    entity,
                    when (state) {
                        VoltState.FRIENDLY -> 0x8000FF00.toInt()
                        VoltState.DOING_LIGHTNING -> 0x800000FF.toInt()
                        VoltState.HOSTILE -> 0x80FF0000.toInt()
                        else -> 0
                    },
                ) { config.voltMoodMeter }
            if (state == VoltState.DOING_LIGHTNING && config.voltRange) {
                RenderUtils.drawCylinderInWorld(
                    config.voltColour.toChromaColor(),
                    entity.posX,
                    entity.posY - 4f,
                    entity.posZ,
                    radius = LIGHTNING_DISTANCE,
                    partialTicks = event.partialTicks,
                    height = 20F,
                )
                val dischargingSince = chargingSince.getOrDefault(entity, SimpleTimeMark.farPast())
                val dischargeTimeLeft = CHARGE_TIME - dischargingSince.passedSince()
                if (dischargeTimeLeft > Duration.ZERO) {
                    event.drawDynamicText(
                        event.exactLocation(entity).up(2.5),
                        "§eLightning: ${dischargeTimeLeft.format(showMilliSeconds = true)}",
                        2.5,
                    )
                }
            }
        }
    }

    enum class VoltState {
        NO_VOLT,
        FRIENDLY,
        HOSTILE,
        DOING_LIGHTNING,
    }

    @Suppress("MaxLineLength")
    private fun getVoltState(itemStack: ItemStack): VoltState {
        return when (itemStack.getSkullTexture()) {
            // TODO: Move these textures to the repo
            "ewogICJ0aW1lc3RhbXAiIDogMTY3Mzg4MzU3MjAzNSwKICAicHJvZmlsZUlkIiA6ICI0MDU4NDhjMmJjNTE0ZDhkOThkOTJkMGIwYzhiZDQ0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJMb3ZlT3dPIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2UxYjFiZmI1MzZiNjQxNmIyNmEyODNkMmQ4YWQ0YjE3NzFiYmU1Yjc2ODk2ZTI3MjdkNWU4MzNiYzg5NDk4MmQiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==" -> {
                VoltState.DOING_LIGHTNING
            }

            "ewogICJ0aW1lc3RhbXAiIDogMTY3MzYzNzQ1OTAwOCwKICAicHJvZmlsZUlkIiA6ICJmMTA0NzMxZjljYTU0NmI0OTkzNjM4NTlkZWY5N2NjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6aWFkODciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2FlYTYyNTFlNThlM2QyMDU1MmEwMzVkNDI0NTYxZWFlZTA4M2ZlYWNkMWU2Y2IzYzJhNWNmOTQ1Y2U2ZDc2ZSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9" -> {
                VoltState.FRIENDLY
            }

            "ewogICJ0aW1lc3RhbXAiIDogMTY3Mzg4MzEwNjAwMywKICAicHJvZmlsZUlkIiA6ICI5NTE3OTkxNjljYzE0MGY1OGM2MmRjOGZmZTU3NjBiZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJZdWFyaWciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGY5ZWRlOTcwZDQwYzViMjQ1Y2JkNjUxMzQ5ZWUxNjZmNjk1ZDI1MDM0NWY4ZjBlNjNmY2IxMGNmYjVhMmI3OCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9" -> {
                VoltState.HOSTILE
            }

            else -> VoltState.NO_VOLT
        }
    }

    private fun getVoltState(entity: Entity): VoltState {
        if (entity !is EntityArmorStand) return VoltState.NO_VOLT
        val helmet = entity.getCurrentArmor(ARMOR_SLOT_HEAD) ?: return VoltState.NO_VOLT
        return getVoltState(helmet)
    }
}
