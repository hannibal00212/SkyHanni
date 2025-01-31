package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LorenzVec
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.tileentity.TileEntity
import java.util.*

typealias BlockState = IBlockState
typealias BlockEntity = TileEntity
typealias Entity = net.minecraft.entity.Entity

@Suppress("McWorldRule")
object McWorld {

    val world: WorldClient? get() = Minecraft.getMinecraft().theWorld
    val hasWorld: Boolean get() = world != null

    // Block Related Functions
    fun getBlock(pos: LorenzVec): Block = getBlockState(pos).block
    fun getBlockState(pos: LorenzVec): BlockState = world!!.getBlockState(pos.toBlockPos())
    fun getBlockEntity(pos: LorenzVec): BlockEntity? = world?.getTileEntity(pos.toBlockPos())
    fun isBlockLoaded(pos: LorenzVec): Boolean = world?.chunkProvider?.provideChunk(pos.toBlockPos())?.isLoaded == true

    fun LorenzVec.getBlockAt(): Block = getBlock(this)
    fun LorenzVec.getBlockStateAt(): BlockState = getBlockState(this)
    fun LorenzVec.getBlockEntityAt(): BlockEntity? = getBlockEntity(this)
    fun LorenzVec.isBlockLoadedAt(): Boolean = isBlockLoaded(this)

    inline fun <T : Comparable<T>, reified P : IProperty<T>> BlockState.checkProperty(name: String, value: (T) -> Boolean): Boolean {
        val property = block.blockState.properties.find { it.name == name } ?: return false
        return if (property is P) value(getValue(property)) else false
    }

    // Player Entity Related Functions
    val players: Iterable<Player> get() =
        world?.playerEntities?.filter { it.isRealPlayer() && it is EntityOtherPlayerMP }.orEmpty()

    fun getPlayer(id: UUID): Player? = world?.getPlayerEntityByUUID(id)

    // Entity Related Functions
    val entities: List<Entity> get() = world?.loadedEntityList?.let {
        if (McClient.isCalledFromMainThread) it else it.toMutableList()
    }?.filterNotNull().orEmpty()

    fun getEntity(id: Int): Entity? = world?.getEntityByID(id)

    inline fun <reified R : Entity> getEntitiesOf(): List<R> = entities.filterIsInstance<R>()

    inline fun <reified T : Entity> getEntitiesNear(entity: Entity, radius: Double): List<T> =
        getEntitiesOf<T>().filter { it.distanceTo(entity) < radius }

    inline fun <reified T : Entity> getEntitiesNear(pos: LorenzVec, radius: Double): List<T> =
        getEntitiesOf<T>().filter { it.distanceTo(pos) < radius }

    inline fun <reified T : Entity> getEntitiesNearPlayer(radius: Double): List<T> =
        getEntitiesOf<T>().filter { it.distanceTo(McPlayer.pos) < radius }

    // TODO: BoundingBox data class
    // inline fun <reified T : Entity> getEntitiesInBox(box: BoundingBox): List<T> =
    //    world?.getEntitiesWithinAABB(T::class.java, box.toAABB()) ?: emptyList()

    // Scoreboard Related Functions
    fun getScoreboard() = world?.scoreboard

    fun getObjective(slot: Int) = getScoreboard()?.getObjectiveInDisplaySlot(slot)
}
