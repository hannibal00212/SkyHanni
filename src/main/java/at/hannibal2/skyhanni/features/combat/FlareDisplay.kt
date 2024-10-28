package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.combat.FlareConfig
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColorInt
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawSphereInWorld
import at.hannibal2.skyhanni.utils.RenderUtils.drawSphereWireframeInWorld
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object FlareDisplay {

    private val config get() = SkyHanniMod.feature.combat.flare
    private var display = emptyList<Renderable>()
    private var flares = mutableListOf<Flare>()

    private var activeWarning = false

    class Flare(val type: FlareType, val entity: EntityArmorStand, val location: LorenzVec = entity.getLorenzVec())

    private val MAX_FLARE_TIME = 3.minutes

    // TODO: Move to repo
    @Suppress("MaxLineLength")
    private val flareSkins = mapOf(
        "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzMwNjIyMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJlMmJmNmMxZWMzMzAyNDc5MjdiYTYzNDc5ZTU4NzJhYzY2YjA2OTAzYzg2YzgyYjUyZGFjOWYxYzk3MTQ1OCIKICAgIH0KICB9Cn0="
            to FlareType.WARNING,
        "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzMyNjQzMiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQyYmY5ODY0NzIwZDg3ZmQwNmI4NGVmYTgwYjc5NWM0OGVkNTM5YjE2NTIzYzNiMWYxOTkwYjQwYzAwM2Y2YiIKICAgIH0KICB9Cn0="
            to FlareType.ALERT,
        "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY4NzM0NzQ4OSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwNjJjYzk4ZWJkYTcyYTZhNGI4OTc4M2FkY2VmMjgxNWI0ODNhMDFkNzNlYTg3YjNkZjc2MDcyYTg5ZDEzYiIKICAgIH0KICB9Cn0="
            to FlareType.SOS,
    )

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (config.displayType == FlareConfig.DisplayType.WORLD) return
        config.position.renderRenderables(display, posLabel = "Flare Timer")
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (config.displayType == FlareConfig.DisplayType.GUI) return

        for (flare in flares) {
            val location = flare.location.add(-0.5, 0.0, -0.5)
            val name = flare.type.displayName
            val time = "§b${getRemainingTime(flare).format()}"
            event.drawDynamicText(location, name, 1.5, ignoreBlocks = false)
            event.drawDynamicText(location, time, 1.5, yOff = 10f, ignoreBlocks = false)
        }
    }

    @SubscribeEvent
    fun onSecondsPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        flares.removeIf { !it.entity.isEntityAlive }
        for (entity in EntityUtils.getAllEntities().filterIsInstance<EntityArmorStand>()) {
            if (!entity.canBeSeen()) continue
            if (entity.ticksExisted.ticks > MAX_FLARE_TIME) continue
            if (isAlreadyKnownFlare(entity)) continue
            getFlareTypeForTexture(entity)?.let {
                flares.add(Flare(it, entity))
            }
            activeWarning = false
        }
        var newDisplay: List<Renderable>? = null
        for (type in FlareType.entries) {
            val flare = getFlareForType(type) ?: continue
            val remainingTime = getRemainingTime(flare)

            val name = type.displayName
            if (newDisplay == null) {
                newDisplay = buildList {
                    val displayTime = if (remainingTime.isNegative()) "§eSoon" else "§b${remainingTime.format()}"
                    add(Renderable.string("$name: $displayTime"))
                    if (config.showManaBuff) {
                        type.manaBuff?.let {
                            add(Renderable.string(" §b$it §7mana regen"))
                        }
                    }
                }
            }
            if (remainingTime !in 0.seconds..config.warnWhenAboutToExpire.seconds) continue
            activeWarning = true
            val message = "$name §eexpires in: §b${remainingTime.inWholeSeconds}s"
            when (config.alertType) {
                FlareConfig.AlertType.CHAT -> {
                    ChatUtils.chat(message)
                }

                FlareConfig.AlertType.TITLE -> {
                    LorenzUtils.sendTitle(message, 1.seconds)
                }

                FlareConfig.AlertType.CHAT_TITLE -> {
                    ChatUtils.chat(message)
                    LorenzUtils.sendTitle(message, 1.seconds)
                }

                else -> {}
            }
            if (config.expireSound) {
                SoundUtils.playPlingSound()
            }
        }
        display = newDisplay.orEmpty()
    }

    private fun getRemainingTime(flare: Flare): Duration {
        val entity = flare.entity
        val aliveTime = entity.ticksExisted.ticks
        val remainingTime = (MAX_FLARE_TIME - aliveTime)
        return remainingTime
    }

    private fun getFlareForType(type: FlareType): Flare? = flares.firstOrNull { it.type == type }

    private fun getFlareTypeForTexture(entity: EntityArmorStand): FlareType? =
        flareSkins.entries.firstOrNull { entity.hasSkullTexture(it.key) }?.value

    private fun isAlreadyKnownFlare(entity: EntityArmorStand): Boolean =
        flares.any { it.entity.entityId == entity.entityId }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        flares.clear()
        display = emptyList()
    }

    @SubscribeEvent
    fun onRender(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (config.outlineType == FlareConfig.OutlineType.NONE) return

        for (flare in flares) {
            val entity = flare.entity
            val location = flare.location

            val color = when (flare.type) {
                FlareType.WARNING -> config.warningColor
                FlareType.ALERT -> config.alertColor
                FlareType.SOS -> config.sosColor
            }.toChromaColor()

            when (config.outlineType) {
                FlareConfig.OutlineType.FILLED -> {
                    event.drawSphereInWorld(color, location, 40f)
                }

                FlareConfig.OutlineType.WIREFRAME -> {
                    event.drawSphereWireframeInWorld(color, location, 40f)
                }

                FlareConfig.OutlineType.CIRCLE -> {
                    RenderUtils.drawCircle(entity, event.partialTicks, 40.0, color)
                }

                else -> {}
            }
        }
    }

    @SubscribeEvent
    fun onRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled() || !config.flashScreen || !activeWarning) return
        val minecraft = Minecraft.getMinecraft()
        val alpha = ((2 + sin(System.currentTimeMillis().toDouble() / 1000)) * 255 / 4).toInt().coerceIn(0..255)
        Gui.drawRect(
            0,
            0,
            minecraft.displayWidth,
            minecraft.displayHeight,
            (alpha shl 24) or (config.flashColor.toChromaColorInt() and 0xFFFFFF),
        )
        GlStateManager.color(1F, 1F, 1F, 1F)
    }

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
        if (!isEnabled()) return
        if (!config.hideParticles) return

        val location = event.location
        val distance = flares.minOfOrNull { it.location.distance(location) } ?: return
        if (distance < 2.5) {
            if (event.type == EnumParticleTypes.FLAME) {
                event.cancel()
            }
        }
    }

    enum class FlareType(val displayName: String, val manaBuff: String?) {
        SOS("§5SOS Flare", "+125%"),
        ALERT("§9Alert Flare", "+50%"),
        WARNING("§aWarning Flare", null),
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
