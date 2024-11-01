package at.hannibal2.skyhanni.features.slayer.enderman

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.EntityUtils.getBlockInHand
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkullTextureHolder
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object EndermanSlayerFeatures {

    private val config get() = SkyHanniMod.feature.slayer.endermen
    private val beaconConfig get() = config.beacon
    private val endermenWithBeacons = mutableListOf<EntityEnderman>()
    private val flyingBeacons = mutableSetOf<EntityArmorStand>()
    private val nukekubiSkulls = mutableSetOf<EntityArmorStand>()
    private var sittingBeacon = mapOf<LorenzVec, SimpleTimeMark>()
    private val logger = LorenzLogger("slayer/enderman")

    private val NUKEKUBI_SKULL_TEXTURE by lazy { SkullTextureHolder.getTexture("MOB_NUKEKUBI") }

    @HandleEvent(onlyOnIsland = IslandType.THE_END)
    fun onCheckRender(event: CheckRenderEntityEvent<Entity>) {
        val entity = event.entity
        if (entity in endermenWithBeacons || entity in flyingBeacons) return

        if (entity is EntityEnderman && showBeacon() && hasBeaconInHand(entity) && entity.canBeSeen(15.0)) {
            endermenWithBeacons.add(entity)
            logger.log("Added enderman with beacon at ${entity.getLorenzVec()}")
        }

        if (entity is EntityArmorStand) {
            if (showBeacon()) {
                val stack = entity.inventory[4] ?: return
                if (stack.name == "Beacon" && entity.canBeSeen(15.0)) {
                    flyingBeacons.add(entity)
                    RenderLivingEntityHelper.setEntityColor(
                        entity,
                        beaconConfig.beaconColor.toChromaColor().withAlpha(1),
                    ) {
                        beaconConfig.highlightBeacon
                    }
                    if (beaconConfig.showWarning) {
                        LorenzUtils.sendTitle("§4Beacon", 2.seconds)
                    }
                    logger.log("Added flying beacons at ${entity.getLorenzVec()}")
                }
            }

            if (config.highlightNukekebi &&
                entity.inventory.any {
                    it?.getSkullTexture() == NUKEKUBI_SKULL_TEXTURE
                } && entity !in nukekubiSkulls
            ) {
                nukekubiSkulls.add(entity)
                RenderLivingEntityHelper.setEntityColor(
                    entity,
                    LorenzColor.GOLD.toColor().withAlpha(1),
                ) { config.highlightNukekebi }
                logger.log("Added Nukekubi skulls at ${entity.getLorenzVec()}")
            }
        }
    }

    private fun hasBeaconInHand(enderman: EntityEnderman) = enderman.getBlockInHand()?.block == Blocks.beacon

    private fun canSee(b: LorenzVec) = b.canBeSeen(15.0)

    private fun showBeacon() = beaconConfig.highlightBeacon || beaconConfig.showWarning || beaconConfig.showLine

    @SubscribeEvent
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!IslandType.THE_END.isInIsland()) return

        if (beaconConfig.highlightBeacon) {
            endermenWithBeacons.removeIf { it.isDead || !hasBeaconInHand(it) }

            for (location in endermenWithBeacons.map { it.getLorenzVec().add(-0.5, 0.2, -0.5) }) {
                event.drawColor(location, beaconConfig.beaconColor.toChromaColor(), alpha = 0.5f)
            }
        }

        drawSittingBeacon(event)
        drawFlyingBeacon(event)
        drawNukekubiSkulls(event)
    }

    private fun drawNukekubiSkulls(event: LorenzRenderWorldEvent) {
        for (skull in nukekubiSkulls) {
            if (skull.isDead) continue
            if (config.highlightNukekebi) {
                event.drawDynamicText(
                    skull.getLorenzVec().add(-0.5, 1.5, -0.5),
                    "§6Nukekubi Skull",
                    1.6,
                    ignoreBlocks = false,
                    maxDistance = 20,
                )
            }
            if (config.drawLineToNukekebi) {
                val skullLocation = event.exactLocation(skull)
                if (skullLocation.distanceToPlayer() > 20) continue
                if (!skullLocation.canBeSeen()) continue
                event.drawLineToEye(
                    skullLocation.up(),
                    LorenzColor.GOLD.toColor(),
                    3,
                    true
                )
            }
        }
    }

    private fun drawFlyingBeacon(event: LorenzRenderWorldEvent) {
        for (beacon in flyingBeacons) {
            if (beacon.isDead) continue
            if (beaconConfig.highlightBeacon) {
                val beaconLocation = event.exactLocation(beacon)
                event.drawDynamicText(beaconLocation.add(y = 1), "§4Beacon", 1.8)
            }

            if (beaconConfig.showLine) {
                val beaconLocation = event.exactLocation(beacon)
                event.drawLineToEye(
                    beaconLocation.add(0.5, 1.0, 0.5),
                    beaconConfig.lineColor.toChromaColor(),
                    beaconConfig.lineWidth,
                    true,
                )
            }
        }
    }

    private fun drawSittingBeacon(event: LorenzRenderWorldEvent) {
        for ((location, time) in sittingBeacon) {
            if (location.distanceToPlayer() > 20) continue
            if (beaconConfig.showLine) {
                event.drawLineToEye(
                    location.add(0.5, 1.0, 0.5),
                    beaconConfig.lineColor.toChromaColor(),
                    beaconConfig.lineWidth,
                    true,
                )
            }

            if (beaconConfig.highlightBeacon) {
                val duration = 5.seconds - time.passedSince()
                val durationFormat = duration.format(showMilliSeconds = true)
                event.drawColor(location, beaconConfig.beaconColor.toChromaColor(), alpha = 1f)
                event.drawWaypointFilled(location, beaconConfig.beaconColor.toChromaColor(), true, true)
                event.drawDynamicText(location.add(y = 1), "§4Beacon §b$durationFormat", 1.8)
            }
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!IslandType.THE_END.isInIsland()) return

        nukekubiSkulls.removeAll {
            if (it.isDead) {
                RenderLivingEntityHelper.removeEntityColor(it)
            }
            it.isDead
        }
        flyingBeacons.removeAll {
            if (it.isDead) {
                RenderLivingEntityHelper.removeEntityColor(it)
            }
            it.isDead
        }

        // Removing the beacon if It's still there after 7 seconds.
        // This is just a workaround for the cases where the ServerBlockChangeEvent don't detect the beacon despawn info.
        val toRemove = sittingBeacon.filter { it.value.passedSince() > 7.seconds }
        if (toRemove.isNotEmpty()) {
            sittingBeacon = sittingBeacon.editCopy {
                toRemove.keys.forEach { remove(it) }
            }
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!IslandType.THE_END.isInIsland()) return
        if (!showBeacon()) return

        val location = event.location
        if (event.new == "beacon") {
            val armorStand = flyingBeacons.find { location.distance(it.getLorenzVec()) < 3 }
            if (armorStand != null) {
                flyingBeacons.remove(armorStand)
                RenderLivingEntityHelper.removeEntityColor(armorStand)
                sittingBeacon = sittingBeacon.editCopy { this[location] = SimpleTimeMark.now() }
                logger.log("Replaced flying beacon with sitting beacon at $location")
            }
        } else {
            if (location in sittingBeacon) {
                logger.log("Removed sitting beacon $location")
                sittingBeacon = sittingBeacon.editCopy { remove(location) }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        endermenWithBeacons.clear()
        flyingBeacons.clear()
        nukekubiSkulls.clear()
        sittingBeacon = emptyMap()
        logger.log("Reset everything (world change)")
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(
            3,
            "slayer.endermanBeaconConfig.highlightBeacon",
            "slayer.endermen.endermanBeaconConfig.highlightBeacon",
        )
        event.move(3, "slayer.endermanBeaconConfig.beaconColor", "slayer.endermen.endermanBeaconConfig.beaconColor")
        event.move(3, "slayer.endermanBeaconConfig.showWarning", "slayer.endermen.endermanBeaconConfig.showWarning")
        event.move(3, "slayer.endermanBeaconConfig.showLine", "slayer.endermen.endermanBeaconConfig.showLine")
        event.move(3, "slayer.endermanBeaconConfig.lneColor", "slayer.endermen.endermanBeaconConfig.lineColor")
        event.move(3, "slayer.endermanBeaconConfig.lineWidth", "slayer.endermen.endermanBeaconConfig.lineWidth")
        event.move(3, "slayer.endermanHighlightNukekebi", "slayer.endermen.highlightNukekebi")
        event.move(9, "slayer.enderman.endermanBeaconConfig", "slayer.endermen.beacon")
    }
}
