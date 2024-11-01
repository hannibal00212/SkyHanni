package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToIgnoreY
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.compat.getLoadedPlayers
import at.hannibal2.skyhanni.utils.compat.getNameAsString
import at.hannibal2.skyhanni.utils.compat.getWholeInventory
import at.hannibal2.skyhanni.utils.compat.normalizeAsArray
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object EntityUtils {

    @Deprecated("Old. Instead use entity detection feature instead.")
    fun EntityLivingBase.hasNameTagWith(
        y: Int,
        contains: String,
        debugRightEntity: Boolean = false,
        inaccuracy: Double = 1.6,
        debugWrongEntity: Boolean = false,
    ): Boolean = getNameTagWith(y, contains, debugRightEntity, inaccuracy, debugWrongEntity) != null

    fun getPlayerEntities(): MutableList<EntityOtherPlayerMP> {
        val list = mutableListOf<EntityOtherPlayerMP>()
        for (entity in Minecraft.getMinecraft().theWorld?.getLoadedPlayers().orEmpty()) {
            if (!entity.isNPC() && entity is EntityOtherPlayerMP) {
                list.add(entity)
            }
        }
        return list
    }

    fun EntityLivingBase.getAllNameTagsInRadiusWith(
        contains: String,
        radius: Double = 3.0,
    ): List<EntityArmorStand> = getArmorStandsInRadius(getLorenzVec().up(3), radius).filter {
        it.getNameAsString().contains(contains)
    }

    @Deprecated("Old. Instead use entity detection feature instead.")
    fun EntityLivingBase.getNameTagWith(
        y: Int,
        contains: String,
        debugRightEntity: Boolean = false,
        inaccuracy: Double = 1.6,
        debugWrongEntity: Boolean = false,
    ): EntityArmorStand? = getAllNameTagsWith(y, contains, debugRightEntity, inaccuracy, debugWrongEntity).firstOrNull()

    @Deprecated("Old. Instead use entity detection feature instead.")
    fun EntityLivingBase.getAllNameTagsWith(
        y: Int,
        contains: String,
        debugRightEntity: Boolean = false,
        inaccuracy: Double = 1.6,
        debugWrongEntity: Boolean = false,
    ): List<EntityArmorStand> {
        val center = getLorenzVec().up(y)
        return getArmorStandsInRadius(center, inaccuracy).filter {
            val result = it.getNameAsString().contains(contains)
            if (debugWrongEntity && !result) {
                LorenzUtils.consoleLog("wrong entity in aabb: '" + it.name + "'")
            }
            if (debugRightEntity && result) {
                LorenzUtils.consoleLog("mob: " + center.printWithAccuracy(2))
                LorenzUtils.consoleLog("nametag: " + it.getLorenzVec().printWithAccuracy(2))
                LorenzUtils.consoleLog("accuracy: " + (it.getLorenzVec() - center).printWithAccuracy(3))
            }
            result
        }
    }

    private fun getArmorStandsInRadius(center: LorenzVec, radius: Double): List<EntityArmorStand> {
        val a = center.add(-radius, -radius - 3, -radius).toBlockPos()
        val b = center.add(radius, radius + 3, radius).toBlockPos()
        val alignedBB = AxisAlignedBB(a, b)
        val clazz = EntityArmorStand::class.java
        val worldObj = Minecraft.getMinecraft().theWorld ?: return emptyList()
        return worldObj.getEntitiesWithinAABB(clazz, alignedBB)
    }

    @Deprecated("Old. Instead use entity detection feature instead.")
    fun EntityLivingBase.hasBossHealth(health: Int): Boolean = this.hasMaxHealth(health, true)

    @Deprecated("Old. Instead use entity detection feature instead.")
    fun EntityLivingBase.hasMaxHealth(health: Int, boss: Boolean = false, maxHealth: Int = baseMaxHealth): Boolean {
        val derpyMultiplier = if (LorenzUtils.isDerpy) 2 else 1
        if (maxHealth == health * derpyMultiplier) return true

        if (!boss && !DungeonAPI.inDungeon()) {
            // Corrupted
            if (maxHealth == health * 3 * derpyMultiplier) return true
            // Runic
            if (maxHealth == health * 4 * derpyMultiplier) return true
            // Corrupted+Runic
            if (maxHealth == health * 12 * derpyMultiplier) return true
        }

        return false
    }

    fun EntityPlayer.getSkinTexture(): String? {
        val gameProfile = gameProfile ?: return null

        return gameProfile.properties.entries()
            .filter { it.key == "textures" }
            .map { it.value }
            .firstOrNull { it.name == "textures" }?.value
    }

    inline fun <reified T : Entity> getEntitiesNextToPlayer(radius: Double): Sequence<T> =
        getEntitiesNearby<T>(LocationUtils.playerLocation(), radius)

    inline fun <reified T : Entity> getEntitiesNearby(location: LorenzVec, radius: Double): Sequence<T> =
        getEntities<T>().filter { it.distanceTo(location) < radius }

    inline fun <reified T : Entity> getEntitiesNearbyIgnoreY(location: LorenzVec, radius: Double): Sequence<T> =
        getEntities<T>().filter { it.distanceToIgnoreY(location) < radius }

    fun EntityLivingBase.isAtFullHealth() = baseMaxHealth == health.toInt()

    // TODO: Make a utils method for just checking the helmet texture
    fun EntityArmorStand.hasSkullTexture(skin: String): Boolean {
        val inventory = this.getWholeInventory() ?: return false
        return inventory.any { it != null && it.getSkullTexture() == skin }
    }

    fun EntityPlayer.isNPC() = !isRealPlayer()

    fun EntityLivingBase.getArmorInventory(): Array<ItemStack?>? =
        if (this is EntityPlayer) inventory.armorInventory.normalizeAsArray() else null

    fun EntityEnderman.getBlockInHand(): IBlockState? = heldBlockState

    inline fun <reified R : Entity> getEntities(): Sequence<R> = getAllEntities().filterIsInstance<R>()

    private fun WorldClient.getAllEntities(): Iterable<Entity> =
        //#if MC < 1.14
        loadedEntityList
    //#else
    //$$ entitiesForRendering()
    //#endif

    fun getAllEntities(): Sequence<Entity> = Minecraft.getMinecraft().theWorld?.getAllEntities()?.let {
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread) it
        // TODO: while i am here, i want to point out that copying the entity list does not constitute proper synchronization,
        //  but *does* make crashes because of it rarer.
        else it.toMutableList()
    }?.asSequence().orEmpty()

    fun getAllTileEntities(): Sequence<TileEntity> = Minecraft.getMinecraft().theWorld?.loadedTileEntityList?.let {
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread) it else it.toMutableList()
    }?.asSequence()?.filterNotNull().orEmpty()

    fun Entity.canBeSeen(viewDistance: Number = 150.0) = getLorenzVec().up(0.5).canBeSeen(viewDistance)

    fun getEntityByID(entityId: Int): Entity? = Minecraft.getMinecraft().thePlayer?.entityWorld?.getEntityByID(entityId)

    @SubscribeEvent
    fun onEntityRenderPre(
        event:
        //#if MC < 1.14
        RenderLivingEvent.Pre<*>,
        //#else
        //$$ RenderLivingEvent.Pre<*, *>
        //#endif

    ) {
        val shEvent = SkyHanniRenderEntityEvent.Pre(event.entity, event.renderer, event.x, event.y, event.z)
        if (shEvent.postAndCatch()) {
            event.cancel()
        }
    }

    @SubscribeEvent
    fun onEntityRenderPost(
        event:
        //#if MC < 11400
        RenderLivingEvent.Post<*>,
        //#else
        //$$ RenderLivingEvent.Post<*, *>
        //#endif

    ) {
        SkyHanniRenderEntityEvent.Post(event.entity, event.renderer, event.x, event.y, event.z).postAndCatch()
    }

    //#if MC < 11400
    @SubscribeEvent
    fun onEntityRenderSpecialsPre(
        event: RenderLivingEvent.Specials.Pre<*>,
    ) {
        val shEvent = SkyHanniRenderEntityEvent.Specials.Pre(event.entity, event.renderer, event.x, event.y, event.z)
        if (shEvent.postAndCatch()) {
            event.cancel()
        }
    }

    @SubscribeEvent
    fun onEntityRenderSpecialsPost(
        event: RenderLivingEvent.Specials.Post<*>,
    ) {
        SkyHanniRenderEntityEvent.Specials.Post(event.entity, event.renderer, event.x, event.y, event.z).postAndCatch()
    }
    //#endif

    fun EntityLivingBase.isCorrupted() = baseMaxHealth == health.toInt().derpy() * 3 || isRunicAndCorrupt()
    fun EntityLivingBase.isRunic() = baseMaxHealth == health.toInt().derpy() * 4 || isRunicAndCorrupt()
    fun EntityLivingBase.isRunicAndCorrupt() = baseMaxHealth == health.toInt().derpy() * 3 * 4

    fun Entity.cleanName() = this.getNameAsString().removeColor()
}

//#if FORGE
private fun Event.cancel() {
    isCanceled = true
}
//#endif
