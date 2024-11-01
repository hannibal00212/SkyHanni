package at.hannibal2.skyhanni.features.combat.mobs

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils.getBlockInHand
import at.hannibal2.skyhanni.utils.EntityUtils.highlight
import at.hannibal2.skyhanni.utils.EntityUtils.isCorrupted
import at.hannibal2.skyhanni.utils.EntityUtils.matchesHealth
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object MobHighlight {

    private val config get() = SkyHanniMod.feature.combat.mobs
    private var arachne: Mob? = null

    @SubscribeEvent
    fun onEntityHealthUpdate(event: EntityHealthUpdateEvent) {
        if (!LorenzUtils.inSkyBlock || !config.corruptedMobHighlight) return
        if (event.entity.isCorrupted()) event.entity.highlight(LorenzColor.DARK_PURPLE)
    }

    // Mob detection isn't used here to allow for highlighting Zealots from further away.
    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityMaxHealthUpdateEvent) {
        if (!LorenzUtils.inSkyBlock || !IslandType.THE_END.isInIsland()) return
        val entity = event.entity as? EntityEnderman ?: return

        val heldBlock = entity.getBlockInHand()?.block

        val color = when {
            heldBlock == Blocks.end_portal_frame && config.specialZealotHighlighter -> LorenzColor.DARK_RED
            heldBlock == Blocks.ender_chest && config.chestZealotHighlighter -> LorenzColor.GREEN
            entity.isZealotOrBruiser() && config.zealotBruiserHighlighter -> LorenzColor.DARK_AQUA
            else -> return
        }

        entity.highlight(color)
    }

    @SubscribeEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        val mob = event.mob
        val name = mob.name

        val color = when {
            name == "Boss Corleone" && config.corleoneHighlighter -> LorenzColor.DARK_PURPLE

            name == "Arachne's Keeper" && config.arachneKeeperHighlight -> LorenzColor.DARK_BLUE
            name == "Arachne's Brood" && config.arachneBossHighlighter -> LorenzColor.GOLD
            name == "Arachne" && config.arachneBossHighlighter -> {
                arachne = mob
                LorenzColor.RED
            }
            // mob.isRunic && config.runicMobHighlighter -> LorenzColor.LIGHT_PURPLE

            else -> return
        }

        mob.highlight(color)
    }

    @SubscribeEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn.SkyblockMob) {
        if (arachne == event.mob) arachne = null
    }

    @SubscribeEvent
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!LorenzUtils.inSkyBlock || !config.lineToArachne) return

        val arachne = arachne?.baseEntity ?: return

        if (arachne.distanceToPlayer() > 10) return

        event.drawLineToEye(
            arachne.getLorenzVec().up(),
            LorenzColor.RED.toColor(),
            config.lineToArachneWidth,
            true,
        )
    }

    private fun EntityEnderman.isZealotOrBruiser() = matchesHealth(13_000) || matchesHealth(65_000)
}
