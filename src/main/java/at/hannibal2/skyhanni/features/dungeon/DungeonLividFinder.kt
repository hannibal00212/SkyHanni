package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzColor.Companion.toLorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.MobUtils.mob
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBoxNea
import at.hannibal2.skyhanni.utils.RenderUtils.drawLineToEye
import at.hannibal2.skyhanni.utils.RenderUtils.exactBoundingBox
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.compat.EffectsCompat
import at.hannibal2.skyhanni.utils.compat.EffectsCompat.Companion.activePotionEffect
import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DungeonLividFinder {

    private val config get() = SkyHanniMod.feature.dungeon.lividFinder
    private val blockLocation = LorenzVec(6, 109, 43)

    private val isBlind by RecalculatingValue(2.ticks, ::isCurrentlyBlind)

    var livid: Mob? = null
        private set
    private var lividArmorStandId: Int? = null

    val lividEntityOrArmorstand: Entity?
        get() = livid?.baseEntity ?: lividArmorStandId?.let { EntityUtils.getEntityByID(it) }

    private var fakeLivids = mutableSetOf<Mob>()

    private var color: LorenzColor? = null

    @SubscribeEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        if (!inLividBossRoom()) return
        val mob = event.mob
        if (mob.name != "Livid" && mob.name != "Real Livid") return
        if (mob.baseEntity !is EntityOtherPlayerMP) return

        val lividColor = color ?: return
        if (mob.isLividColor(lividColor)) {
            livid = mob
            // When the real livid dies at the same time as a fake livid, Hypixel despawns the player entity,
            // and makes it impossible to get the mob of the real livid again.
            lividArmorStandId = mob.armorStand?.entityId
            if (config.enabled) mob.highlight(lividColor.toColor())
        } else {
            fakeLivids += mob
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!inLividBossRoom()) return
        val block = blockLocation.getBlockStateAt()
        if (block.block != Blocks.wool) return
        color = block.getValue(BlockStainedGlass.COLOR).toLorenzColor()
    }

    @SubscribeEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn.SkyblockMob) {
        when (event.mob) {
            livid -> livid = null
            in fakeLivids -> fakeLivids -= event.mob
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        color = null
        lividArmorStandId = null
    }

    @HandleEvent(onlyOnIsland = IslandType.CATACOMBS)
    fun onCheckRender(event: CheckRenderEntityEvent<Entity>) {
        if (!inLividBossRoom() || !config.hideWrong) return
        if (livid == null && lividArmorStandId == null) return // in case livid detection fails, don't hide anything
        if (event.entity.mob in fakeLivids) event.cancel()
    }

    private fun isCurrentlyBlind() = (Minecraft.getMinecraft().thePlayer?.activePotionEffect(EffectsCompat.BLINDNESS)?.duration ?: 0) > 10

    private fun Mob.isLividColor(color: LorenzColor): Boolean {
        val chatColor = color.getChatColor()
        return armorStand?.name?.startsWith("$chatColor﴾ $chatColor§lLivid") ?: false
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!inLividBossRoom() || !config.enabled) return
        if (isBlind) return

        val entity = lividEntityOrArmorstand ?: return
        val lorenzColor = color ?: return

        val location = event.exactLocation(entity)
        val boundingBox = event.exactBoundingBox(entity)

        event.drawDynamicText(location, lorenzColor.getChatColor() + "Livid", 1.5)

        val color = lorenzColor.toColor()
        event.drawFilledBoundingBoxNea(boundingBox, color, 0.5f)

        if (location.distanceSqToPlayer() > 50) {
            event.drawLineToEye(location.add(x = 0.5, z = 0.5), color, 3, true)
        }
    }

    private fun inLividBossRoom() = DungeonAPI.inBossRoom && DungeonAPI.getCurrentBoss() == DungeonFloor.F5
}
