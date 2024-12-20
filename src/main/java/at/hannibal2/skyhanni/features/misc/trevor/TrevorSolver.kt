package at.hannibal2.skyhanni.features.misc.trevor

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import kotlin.time.Duration.Companion.seconds

object TrevorSolver {

    private val animalHealths = setOf(100, 200, 500, 1000, 2000, 5000, 10000, 30000)

    var currentMob: TrevorMob? = null
    private var maxHeight: Double = 0.0
    private var minHeight: Double = 0.0
    private var foundID = -1
    var mobCoordinates = LorenzVec(0.0, 0.0, 0.0)
    var mobLocation = TrapperMobArea.NONE
    var averageHeight = (minHeight + maxHeight) / 2

    fun findMobHeight(height: Int, above: Boolean) {
        val playerPosition = LocationUtils.playerLocation().roundTo(2)
        val mobHeight = if (above) playerPosition.y + height else playerPosition.y - height
        if (maxHeight == 0.0) {

            maxHeight = mobHeight + 2.5
            minHeight = mobHeight - 2.5
        } else {
            if (mobHeight + 2.5 in minHeight..maxHeight) {
                maxHeight = mobHeight + 2.5
            } else if (mobHeight - 2.5 in minHeight..maxHeight) {
                minHeight = mobHeight - 2.5
            } else {
                maxHeight = mobHeight + 2.5
                minHeight = mobHeight - 2.5
            }
        }
        averageHeight = (minHeight + maxHeight) / 2
    }

    fun findMob() {
        Minecraft.getMinecraft().theWorld ?: return
        for (entity in EntityUtils.getAllEntities()) {
            if (entity is EntityOtherPlayerMP) continue
            val name = entity.name
            val isTrevor = MobData.entityToMob[entity]?.let { it.name != name && isTrevorMob(it) } ?: false
            val entityHealth = if (entity is EntityLivingBase) entity.baseMaxHealth.derpy() else 0
            currentMob = TrevorMob.entries.firstOrNull { it.mobName.contains(name) }
            if ((animalHealths.any { it == entityHealth } && currentMob != null) || isTrevor) {
                if (foundID == entity.entityId) {
                    val dist = entity.position.toLorenzVec().distanceToPlayer()
                    val isOasisMob = currentMob == TrevorMob.RABBIT || currentMob == TrevorMob.SHEEP
                    if (isOasisMob && mobLocation == TrapperMobArea.OASIS && !isTrevor) return
                    val canSee = entity.canBeSeen() && dist < currentMob!!.renderDistance
                    if (canSee) {
                        if (mobLocation != TrapperMobArea.FOUND) {
                            LorenzUtils.sendTitle("§2Saw ${currentMob!!.mobName}!", 3.seconds)
                        }
                        mobLocation = TrapperMobArea.FOUND
                        mobCoordinates = entity.position.toLorenzVec()
                    }
                } else {
                    foundID = entity.entityId
                }
                return
            }
        }
        if (foundID != -1) {
            mobCoordinates = LorenzVec(0.0, 0.0, 0.0)
            foundID = -1
        }
    }

    private fun isTrevorMob(mob: Mob): Boolean =
        TrevorTracker.TrapperMobRarity.entries.any { mob.name.startsWith(it.formattedName + " ", ignoreCase = true) }

    fun resetLocation() {
        maxHeight = 0.0
        minHeight = 0.0
        averageHeight = (minHeight + maxHeight) / 2
        foundID = -1
        mobCoordinates = LorenzVec(0.0, 0.0, 0.0)
    }
}

