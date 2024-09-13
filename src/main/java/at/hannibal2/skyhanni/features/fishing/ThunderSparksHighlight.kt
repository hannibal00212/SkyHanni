package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SpecialColor
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyHanniModule
object ThunderSparksHighlight {

    private val config get() = SkyHanniMod.feature.fishing.thunderSpark
    private const val TEXTURE =
        "ewogICJ0aW1lc3RhbXAiIDogMTY0MzUwNDM3MjI1NiwKICAicHJvZmlsZUlkIiA6ICI2MzMyMDgwZTY3YTI0Y2MxYjE3ZGJhNzZmM2MwMGYxZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUZWFtSHlkcmEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2IzMzI4ZDNlOWQ3MTA0MjAzMjI1NTViMTcyMzkzMDdmMTIyNzBhZGY4MWJmNjNhZmM1MGZhYTA0YjVjMDZlMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
    private val sparks = mutableListOf<EntityArmorStand>()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        EntityUtils.getEntities<EntityArmorStand>().filter {
            it !in sparks && it.hasSkullTexture(TEXTURE)
        }.forEach { sparks.add(it) }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        val special = config.color
        val color = Color(SpecialColor.specialToChromaRGB(special), true)

        val playerLocation = LocationUtils.playerLocation()
        for (spark in sparks) {
            if (spark.isDead) continue
            val sparkLocation = spark.getLorenzVec()
            val block = sparkLocation.getBlockAt()
            val seeThroughBlocks =
                sparkLocation.distanceToPlayer() < 6 && (block == Blocks.flowing_lava || block == Blocks.lava)
            event.drawWaypointFilled(
                sparkLocation.add(-0.5, 0.0, -0.5), color, extraSize = -0.25, seeThroughBlocks = seeThroughBlocks
            )
            if (sparkLocation.distance(playerLocation) < 10) {
                event.drawString(sparkLocation.add(y = 1.5), "Thunder Spark", seeThroughBlocks = seeThroughBlocks)
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        sparks.clear()
    }

    private fun isEnabled() =
        (IslandType.CRIMSON_ISLE.isInIsland() || LorenzUtils.isStrandedProfile) && config.highlight

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "fishing.thunderSparkHighlight", "fishing.thunderSpark.highlight")
        event.move(3, "fishing.thunderSparkColor", "fishing.thunderSpark.color")
    }
}
