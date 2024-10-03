package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzColor.Companion.toLorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.block.BlockStainedGlass
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DungeonLividFinder {

    private val config get() = SkyHanniMod.feature.dungeon.lividFinder
    private val blockLocation = LorenzVec(6, 109, 43)

    var lividEntity: EntityOtherPlayerMP? = null
    private var lividArmorStand: EntityArmorStand? = null
    private var gotBlinded = false
    private var color: LorenzColor? = null

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!inDungeon()) return

        val isCurrentlyBlind = isCurrentlyBlind()
        if (!gotBlinded) {
            gotBlinded = isCurrentlyBlind
            return
        } else if (isCurrentlyBlind) return

        if (!config.enabled) return

        val dyeColor = blockLocation.getBlockStateAt().getValue(BlockStainedGlass.COLOR)
        color = dyeColor.toLorenzColor()

        val color = color ?: return
        val chatColor = color.getChatColor()

        lividArmorStand = EntityUtils.getEntities<EntityArmorStand>()
            .firstOrNull { it.name.startsWith("$chatColor﴾ $chatColor§lLivid") }

        if (event.isMod(20)) {
            if (lividArmorStand == null) {
            val amountArmorStands = EntityUtils.getEntities<EntityArmorStand>().filter { it.name.contains("Livid") }.count()
                if (amountArmorStands >= 8) {
                    ErrorManager.logErrorStateWithData(
                        "Could not find livid",
                        "could not find lividArmorStand",
                        "dyeColor" to dyeColor,
                        "color" to color,
                        "chatColor" to chatColor,
                        "amountArmorStands" to amountArmorStands,
                    )
                }
            }
        }

        val lividArmorStand = lividArmorStand ?: return

        val aabb = with(lividArmorStand) {
            AxisAlignedBB(
                posX - 0.5,
                posY - 2,
                posZ - 0.5,
                posX + 0.5,
                posY,
                posZ + 0.5
            )
        }
        val world = Minecraft.getMinecraft().theWorld
        val newLivid = world.getEntitiesWithinAABB(EntityOtherPlayerMP::class.java, aabb)
            .takeIf { it.size == 1 }?.firstOrNull() ?: return
        if (!newLivid.name.contains("Livid")) return

        lividEntity = newLivid
        RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
            newLivid,
            color.toColor().withAlpha(30)
        ) { shouldHighlight() }
    }

    private fun shouldHighlight() = getLividAlive() != null && config.enabled

    private fun getLividAlive() = lividEntity?.let {
        if (!it.isDead && it.health > 0.5) it else null
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!inDungeon()) return
        if (!config.hideWrong) return
        if (!config.enabled) return

        val entity = event.entity
        if (entity is EntityPlayerSP) return
        val livid = getLividAlive() ?: return

        if (entity != livid && entity != lividArmorStand) {
            if (entity.name.contains("Livid")) {
                event.cancel()
            }
        }
    }

    private fun isCurrentlyBlind() = if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.blindness)) {
        Minecraft.getMinecraft().thePlayer.getActivePotionEffect(Potion.blindness).duration > 10
    } else false

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!inDungeon()) return
        if (!config.enabled) return

        val livid = getLividAlive() ?: return
        val location = livid.getLorenzVec().add(-0.5, 0.0, -0.5)

        val lorenzColor = color ?: return

        event.drawDynamicText(location, lorenzColor.getChatColor() + "Livid", 1.5)

        if (location.distanceSqToPlayer() < 50) return

        val color = lorenzColor.toColor()
        event.draw3DLine(event.exactPlayerEyeLocation(), location.add(0.5, 0.0, 0.5), color, 3, true)
        event.drawWaypointFilled(location, color, beacon = false, seeThroughBlocks = true)
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lividEntity = null
        gotBlinded = false
    }

    private fun inDungeon(): Boolean {
        if (!DungeonAPI.inDungeon()) return false
        if (!DungeonAPI.inBossRoom) return false
        if (!DungeonAPI.isOneOf("F5", "M5")) return false

        return true
    }
}
