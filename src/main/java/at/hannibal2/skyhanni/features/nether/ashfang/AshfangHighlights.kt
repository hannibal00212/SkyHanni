package at.hannibal2.skyhanni.features.nether.ashfang

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.events.entity.EntityLeaveWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils
import at.hannibal2.skyhanni.utils.ColorUtils.getExtendedColorCode
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawCylinderInWorld
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object AshfangHighlights {

    private val config get() = AshfangManager.config

    // TODO: Move both of these to repo
    @Suppress("MaxLineLength")
    private const val BLAZING_SOUL =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODI4N2IzOTdkYWY5NTE2YTBiZDc2ZjVmMWI3YmY5Nzk1MTVkZjNkNWQ4MzNlMDYzNWZhNjhiMzdlZTA4MjIxMiJ9fX0="
    @Suppress("MaxLineLength")
    private const val GRAVITY_ORB =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE2OWNjZjdhZDkwNGM5YTg1MmVhMmZmM2Y1YjRlMjNhZGViZjcyZWQxMmQ1ZjI0Yjc4Y2UyZDQ0YjRhMiJ9fX0="
    private val blazingSouls = mutableSetOf<EntityArmorStand>()
    private val gravityOrbs = mutableSetOf<EntityArmorStand>()
    private const val MAX_DISTANCE = 15.0

    @HandleEvent(onlyOnSkyblock = true, onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onEntityJoin(event: EntityEnterWorldEvent<EntityArmorStand>) {
        if (!AshfangManager.active) return
        val entity = event.entity
        DelayedRun.runNextTick {
            when {
                entity.hasSkullTexture(BLAZING_SOUL) -> blazingSouls += entity
                entity.hasSkullTexture(GRAVITY_ORB) -> gravityOrbs += entity
            }
        }
    }

    @HandleEvent(onlyOnSkyblock = true, onlyOnIsland = IslandType.CRIMSON_ISLE)
    fun onEntityLeave(event: EntityLeaveWorldEvent<EntityArmorStand>) {
        blazingSouls -= event.entity
        gravityOrbs -= event.entity
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!AshfangManager.active) return

        if (config.blazingSouls.enabled) {
            val color = config.blazingSouls.color.toChromaColor()
            blazingSouls.forEach {
                val location = event.exactLocation(it)
                event.drawWaypointFilled(location.add(-0.5, 1.25, -0.5), color, extraSize = -0.15)
                event.drawBlendedColorString(location, "Blazing Soul")
            }
        }

        if (config.gravityOrbs.enabled) {
            val color = config.gravityOrbs.color.toChromaColor()
            gravityOrbs.forEach {
                val location = event.exactLocation(it)
                event.drawCylinderInWorld(color, location.add(-0.5, -2.0, -0.5), 3.5f, 4.5f)
                event.drawBlendedColorString(location, "Gravity Orb")
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        blazingSouls.clear()
        gravityOrbs.clear()
    }

    private fun LorenzRenderWorldEvent.drawBlendedColorString(location: LorenzVec, text: String) {
        val distance = location.distanceToPlayer()
        if (distance < MAX_DISTANCE) {
            val colorCode = getColorCode(distance)
            drawString(location.add(y = 2.5), colorCode + text)
        }
    }

    private fun getColorCode(distance: Double): String =
        ColorUtils.blendRGB(LorenzColor.GREEN.toColor(), LorenzColor.RED.toColor(), distance / MAX_DISTANCE).getExtendedColorCode()

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "ashfang.blazingSouls", "crimsonIsle.ashfang.blazingSouls.enabled")
        event.move(2, "ashfang.blazingSoulsColor", "crimsonIsle.ashfang.blazingSouls.color")

        event.move(1, "ashfang.gravityOrbs", "ashfang.gravityOrbs.enabled")
        event.move(1, "ashfang.gravityOrbsColor", "ashfang.gravityOrbs.color")
        event.move(2, "ashfang.gravityOrbs", "crimsonIsle.ashfang.gravityOrbs")
    }
}
