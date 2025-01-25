package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.data.mob.MobFilter.isDisplayNpc
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.data.mob.MobFilter.isSkyBlockMob
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.EntityUtils.getBlockInHand
import at.hannibal2.skyhanni.utils.EntityUtils.getSkinTexture
import at.hannibal2.skyhanni.utils.EntityUtils.isNpc
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.isEnchanted
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.compat.getFirstPassenger
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

object CopyNearbyEntitiesCommand {

    fun command(args: Array<String>) {
        var searchRadius = 10
        if (args.size == 1) {
            searchRadius = args[0].toInt()
        }

        val start = LocationUtils.playerLocation()

        var counter = 0

        val resultList = buildList {
            for (entity in EntityUtils.getAllEntities().sortedBy { it.entityId }) {
                val position = entity.position
                val vec = position.toLorenzVec()
                val distance = start.distance(vec)
                val mob = MobData.entityToMob[entity]
                if (distance >= searchRadius) continue

                val simpleName = entity.javaClass.simpleName
                add("entity: $simpleName")
                val displayName = entity.displayName
                add("name: '" + entity.name + "'")
                if (entity is EntityArmorStand) add("cleanName: '" + entity.cleanName() + "'")
                add("displayName: '${displayName.formattedText}'")
                add("entityId: ${entity.entityId}")
                add("Type of Mob: ${getType(entity, mob)}")
                add("uuid version: ${entity.uniqueID.version()} (${entity.uniqueID})")
                add("location data:")
                add("-  vec: $vec")
                add("-  distance: $distance")

                val rotationYaw = entity.rotationYaw
                val rotationPitch = entity.rotationPitch
                add("-  rotationYaw: $rotationYaw")
                add("-  rotationPitch: $rotationPitch")

                val firstPassenger = entity.getFirstPassenger()
                add("firstPassenger: $firstPassenger")
                val ridingEntity = entity.ridingEntity
                add("ridingEntity: $ridingEntity")

                if (entity.isInvisible) {
                    add("Invisible: true")
                }

                if (entity is EntityLivingBase) {
                    add("EntityLivingBase:")
                    val baseMaxHealth = entity.baseMaxHealth
                    val health = entity.health.toInt()
                    add("-  baseMaxHealth: $baseMaxHealth")
                    add("-  health: $health")
                }

                if (entity is EntityPlayer) {
                    val inventory = entity.inventory
                    if (inventory != null) {
                        add("armor:")
                        for ((i, itemStack) in inventory.armorInventory.withIndex()) {
                            val name = itemStack?.name ?: "null"
                            add("-  at: $i: $name")
                        }
                    }
                }

                when (entity) {
                    is EntityArmorStand -> addArmorStand(entity)
                    is EntityEnderman -> addEnderman(entity)
                    is EntityMagmaCube -> addMagmaCube(entity)
                    is EntityItem -> addItem(entity)
                    is EntityOtherPlayerMP -> addOtherPlayer(entity)
                    is EntityCreeper -> addCreeper(entity)
                    is EntityWither -> addWither(entity)
                }
                if (mob != null && mob.mobType != Mob.Type.PLAYER) {
                    add("MobInfo: ")
                    addAll(getMobInfo(mob).map { "-  $it" })
                }
                add("")
                add("")
                counter++
            }
        }

        if (counter != 0) {
            val string = resultList.joinToString("\n")
            OSUtils.copyToClipboard(string)
            ChatUtils.chat("$counter entities copied into the clipboard!")
        } else {
            ChatUtils.chat("No entities found in a search radius of $searchRadius!")
        }
    }

    private fun MutableList<String>.addArmorStand(entity: EntityArmorStand) {
        add("EntityArmorStand:")
        val headRotation = entity.headRotation.toLorenzVec()
        val bodyRotation = entity.bodyRotation.toLorenzVec()
        add("-  headRotation: $headRotation")
        add("-  bodyRotation: $bodyRotation")

        add("-  inventory:")
        for ((id, stack) in entity.inventory.withIndex()) {
            add("-  id $id ($stack)")
            printItemStackData(stack)
        }
    }

    private fun MutableList<String>.addEnderman(entity: EntityEnderman) {
        add("EntityEnderman:")
        val heldBlockState = entity.getBlockInHand()
        add("-  heldBlockState: $heldBlockState")
        if (heldBlockState != null) {
            val block = heldBlockState.block
            add("-  block: $block")
        }
    }

    private fun MutableList<String>.addMagmaCube(entity: EntityMagmaCube) {
        add("EntityMagmaCube:")
        val squishFactor = entity.squishFactor
        val slimeSize = entity.slimeSize
        add("-  factor: $squishFactor")
        add("-  slimeSize: $slimeSize")
    }

    private fun MutableList<String>.addItem(entity: EntityItem) {
        add("EntityItem:")
        val stack = entity.entityItem
        val stackName = stack.name
        val stackDisplayName = stack.displayName
        val cleanName = stack.cleanName()
        val itemEnchanted = stack.isEnchanted()
        val itemDamage = stack.itemDamage
        val stackSize = stack.stackSize
        val maxStackSize = stack.maxStackSize
        val skullTexture = stack.getSkullTexture()
        add("-  name: '$stackName'")
        add("-  stackDisplayName: '$stackDisplayName'")
        add("-  cleanName: '$cleanName'")
        add("-  itemEnchanted: '$itemEnchanted'")
        add("-  itemDamage: '$itemDamage'")
        add("-  stackSize: '$stackSize'")
        add("-  maxStackSize: '$maxStackSize'")
        skullTexture?.let { add("-  skullTexture: '$it'") }
    }

    private fun MutableList<String>.addOtherPlayer(entity: EntityOtherPlayerMP) {
        add("EntityOtherPlayerMP:")

        val skinTexture = entity.getSkinTexture()
        add("-  skin texture: $skinTexture")
    }

    private fun MutableList<String>.addCreeper(entity: EntityCreeper) {
        add("EntityCreeper:")
        val creeperState = entity.creeperState
        val ignite = entity.hasIgnited()
        val powered = entity.powered
        add("-  creeperState: '$creeperState'")
        add("-  ignite: '$ignite'")
        add("-  powered: '$powered'")
    }

    private fun MutableList<String>.addWither(entity: EntityWither) {
        add("EntityWither:")
        val invulTime = entity.invulTime
        val isArmored = entity.isArmored
        add("-  invulTime: '$invulTime'")
        add("-  armored: '$isArmored'")
    }

    private fun MutableList<String>.printItemStackData(stack: ItemStack?) {
        if (stack != null) {
            val skullTexture = stack.getSkullTexture()
            if (skullTexture != null) {
                add("-     skullTexture:")
                add("-     $skullTexture")
            }
            val cleanName = stack.cleanName()
            val stackName = stack.name
            val type = stack.javaClass.name
            add("-     name: '$stackName'")
            add("-     cleanName: '$cleanName'")
            add("-     type: $type")
        }
    }

    private fun getType(entity: Entity, mob: Mob?) = buildString {
        if (entity is EntityLivingBase && entity.isDisplayNpc()) append("DisplayNPC, ")
        if (entity is EntityPlayer && entity.isNpc()) append("NPC, ")
        if (entity is EntityPlayer && entity.isRealPlayer()) append("RealPlayer, ")
        if (mob?.mobType == Mob.Type.SUMMON) append("Summon, ")
        if (entity.isSkyBlockMob()) {
            append("SkyblockMob(")

            if (mob == null) {
                append(if (entity.distanceToPlayer() > MobData.DETECTION_RANGE) "Not in Range" else "None")
                append(")")
            } else {
                append(mob.mobType.name)
                if (mob.baseEntity == entity) append("/Base")
                append(")\"")
                append(mob.name)
                append("\"")
            }
            append(", ")
        }

        if (isNotEmpty()) {
            delete(length - 2, length) // Remove the last ", "
        } else {
            append("NONE")
        }
    }

    fun getMobInfo(mob: Mob) = buildList<String> {
        add("Name: ${mob.name}")
        add("Type: ${mob.mobType}")
        add("Base Entity: ${mob.baseEntity.asString()}")
        add("ArmorStand: ${mob.armorStand?.asString()}")
        if (mob.extraEntities.isNotEmpty()) {
            add("Extra Entities")
            addAll(mob.extraEntities.map { "  " + it.asString() })
        }
        if (mob.hologram1Delegate.isInitialized()) {
            add("Hologram1: ${mob.hologram1?.asString()}")
        }
        if (mob.hologram2Delegate.isInitialized()) {
            add("Hologram2: ${mob.hologram2?.asString()}")
        }
        if (mob.owner != null) {
            add("Owner: ${mob.owner.ownerName}")
        }
        add("Level or Tier: ${mob.levelOrTier.takeIf { it != -1 }}")
        if (mob.mobType == Mob.Type.DUNGEON) {
            add("Is Starred: ${mob.hasStar}")
            add("Attribute: ${mob.attribute ?: "NONE"}")
        }
        if (mob.boundingBox != mob.baseEntity.entityBoundingBox) {
            add("Bounding Box: ${mob.boundingBox}")
        }
    }

    private fun EntityLivingBase.asString() =
        this.entityId.toString() + " - " + this.javaClass.simpleName + " \"" + this.name + "\""
}
