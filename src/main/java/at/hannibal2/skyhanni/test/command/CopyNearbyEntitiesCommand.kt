package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.data.mob.MobFilter.isDisplayNPC
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.data.mob.MobFilter.isSkyBlockMob
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.EntityUtils.getBlockInHand
import at.hannibal2.skyhanni.utils.EntityUtils.getSkinTexture
import at.hannibal2.skyhanni.utils.EntityUtils.isNPC
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

        val resultList = mutableListOf<String>()
        var counter = 0

        for (entity in EntityUtils.getAllEntities().sortedBy { it.entityId }) {
            val position = entity.position
            val vec = position.toLorenzVec()
            val distance = start.distance(vec)
            val mob = MobData.entityToMob[entity]
            if (distance < searchRadius) {
                val simpleName = entity.javaClass.simpleName
                resultList.add("entity: $simpleName")
                val displayName = entity.displayName
                resultList.add("name: '" + entity.name + "'")
                if (entity is EntityArmorStand) resultList.add("cleanName: '" + entity.cleanName() + "'")
                resultList.add("displayName: '${displayName.formattedText}'")
                resultList.add("entityId: ${entity.entityId}")
                resultList.add("Type of Mob: ${getType(entity, mob)}")
                resultList.add("uuid version: ${entity.uniqueID.version()} (${entity.uniqueID})")
                resultList.add("location data:")
                resultList.add("-  vec: $vec")
                resultList.add("-  distance: $distance")

                val rotationYaw = entity.rotationYaw
                val rotationPitch = entity.rotationPitch
                resultList.add("-  rotationYaw: $rotationYaw")
                resultList.add("-  rotationPitch: $rotationPitch")

                val firstPassenger = entity.getFirstPassenger()
                resultList.add("firstPassenger: $firstPassenger")
                val ridingEntity = entity.ridingEntity
                resultList.add("ridingEntity: $ridingEntity")

                if (entity.isInvisible) {
                    resultList.add("Invisible: true")
                }

                if (entity is EntityLivingBase) {
                    resultList.add("EntityLivingBase:")
                    val baseMaxHealth = entity.baseMaxHealth
                    val health = entity.health.toInt()
                    resultList.add("-  baseMaxHealth: $baseMaxHealth")
                    resultList.add("-  health: $health")
                }

                if (entity is EntityPlayer) {
                    val inventory = entity.inventory
                    if (inventory != null) {
                        resultList.add("armor:")
                        for ((i, itemStack) in inventory.armorInventory.withIndex()) {
                            val name = itemStack?.name ?: "null"
                            resultList.add("-  at: $i: $name")
                        }
                    }
                }

                when (entity) {
                    is EntityArmorStand -> {
                        resultList.add("EntityArmorStand:")
                        val headRotation = entity.headRotation.toLorenzVec()
                        val bodyRotation = entity.bodyRotation.toLorenzVec()
                        resultList.add("-  headRotation: $headRotation")
                        resultList.add("-  bodyRotation: $bodyRotation")

                        resultList.add("-  inventory:")
                        for ((id, stack) in entity.inventory.withIndex()) {
                            resultList.add("-  id $id ($stack)")
                            printItemStackData(stack, resultList)
                        }
                    }

                    is EntityEnderman -> {
                        resultList.add("EntityEnderman:")
                        val heldBlockState = entity.getBlockInHand()
                        resultList.add("-  heldBlockState: $heldBlockState")
                        if (heldBlockState != null) {
                            val block = heldBlockState.block
                            resultList.add("-  block: $block")
                        }
                    }

                    is EntityMagmaCube -> {
                        resultList.add("EntityMagmaCube:")
                        val squishFactor = entity.squishFactor
                        val slimeSize = entity.slimeSize
                        resultList.add("-  factor: $squishFactor")
                        resultList.add("-  slimeSize: $slimeSize")
                    }

                    is EntityItem -> {
                        resultList.add("EntityItem:")
                        val stack = entity.entityItem
                        val stackName = stack.name
                        val stackDisplayName = stack.displayName
                        val cleanName = stack.cleanName()
                        val itemEnchanted = stack.isEnchanted()
                        val itemDamage = stack.itemDamage
                        val stackSize = stack.stackSize
                        val maxStackSize = stack.maxStackSize
                        resultList.add("-  name: '$stackName'")
                        resultList.add("-  stackDisplayName: '$stackDisplayName'")
                        resultList.add("-  cleanName: '$cleanName'")
                        resultList.add("-  itemEnchanted: '$itemEnchanted'")
                        resultList.add("-  itemDamage: '$itemDamage'")
                        resultList.add("-  stackSize: '$stackSize'")
                        resultList.add("-  maxStackSize: '$maxStackSize'")
                    }

                    is EntityOtherPlayerMP -> {
                        resultList.add("EntityOtherPlayerMP:")

                        val skinTexture = entity.getSkinTexture()
                        resultList.add("-  skin texture: $skinTexture")
                    }

                    is EntityCreeper -> {
                        resultList.add("EntityCreeper:")
                        val creeperState = entity.creeperState
                        val ignite = entity.hasIgnited()
                        val powered = entity.powered
                        resultList.add("-  creeperState: '$creeperState'")
                        resultList.add("-  ignite: '$ignite'")
                        resultList.add("-  powered: '$powered'")
                    }

                    is EntityWither -> {
                        resultList.add("EntityWither:")
                        val invulTime = entity.invulTime
                        resultList.add("-  invulTime: '$invulTime'")
                    }
                }
                if (mob != null && mob.mobType != Mob.Type.PLAYER) {
                    resultList.add("MobInfo: ")
                    resultList.addAll(getMobInfo(mob).map { "-  $it" })
                }
                resultList.add("")
                resultList.add("")
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

    private fun printItemStackData(stack: ItemStack?, resultList: MutableList<String>) {
        if (stack != null) {
            val skullTexture = stack.getSkullTexture()
            if (skullTexture != null) {
                resultList.add("-     skullTexture:")
                resultList.add("-     $skullTexture")
            }
            val cleanName = stack.cleanName()
            val stackName = stack.name
            val type = stack.javaClass.name
            resultList.add("-     name: '$stackName'")
            resultList.add("-     cleanName: '$cleanName'")
            resultList.add("-     type: $type")
        }
    }

    private fun getType(entity: Entity, mob: Mob?) = buildString {
        if (entity is EntityLivingBase && entity.isDisplayNPC()) append("DisplayNPC, ")
        if (entity is EntityPlayer && entity.isNPC()) append("NPC, ")
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
