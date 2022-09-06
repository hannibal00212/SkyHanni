package at.hannibal2.skyhanni.test.command

import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.toLorenzVec
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityMagmaCube

object CopyNearbyEntitiesCommand {

    fun command(args: Array<String>) {
        var searchRadius = 10
        if (args.size == 1) {
            searchRadius = args[0].toInt()
        }

        val minecraft = Minecraft.getMinecraft()
        val start = LocationUtils.playerLocation()
        val world = minecraft.theWorld

        val resultList = mutableListOf<String>()
        var counter = 0

        for (entity in world.loadedEntityList) {
            val position = entity.position
            val vec = position.toLorenzVec()
            val distance = start.distance(vec)
            if (distance < searchRadius) {
                val simpleName = entity.javaClass.simpleName
                resultList.add("entity: $simpleName")
                val displayName = entity.displayName
                resultList.add("name: '" + entity.name + "'")
                resultList.add("displayName: '${displayName.formattedText}'")
                resultList.add("location data:")
                resultList.add("-  vec: $vec")
                resultList.add("-  distance: $distance")

                val rotationYaw = entity.rotationYaw
                val rotationPitch = entity.rotationPitch
                resultList.add("-  rotationYaw: $rotationYaw")
                resultList.add("-  rotationPitch: $rotationPitch")

                val riddenByEntity = entity.riddenByEntity
                resultList.add("riddenByEntity: $riddenByEntity")
                val ridingEntity = entity.ridingEntity
                resultList.add("ridingEntity: $ridingEntity")


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
                            if (stack != null) {
                                val skullTexture = stack.getSkullTexture()
                                if (skullTexture != null) {
                                    resultList.add("-     skullTexture:")
                                    resultList.add("-     $skullTexture")
                                }
                                val cleanName = stack.cleanName()
                                val type = stack.javaClass.name
                                resultList.add("-     cleanName: $cleanName")
                                resultList.add("-     type: $type")
                            }
                        }
                    }

                    is EntityEnderman -> {
                        resultList.add("EntityEnderman:")
                        val heldItem = entity.heldItem
                        resultList.add("-  heldItem: $heldItem")
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
                        val itemEnchanted = stack.isItemEnchanted
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
                }
                if (entity is EntityLivingBase) {
                    resultList.add("EntityLivingBase:")
                    val baseMaxHealth = entity.baseMaxHealth.toInt()
                    val health = entity.health.toInt()
                    resultList.add("-  baseMaxHealth: $baseMaxHealth")
                    resultList.add("-  health: $health")
                }
                resultList.add("")
                resultList.add("")
                counter++
            }
        }

        if (counter != 0) {
            val string = resultList.joinToString("\n")
            OSUtils.copyToClipboard(string)
            LorenzUtils.chat("§e[SkyHanni] $counter entities copied into the clipboard!")
        } else {
            LorenzUtils.chat("§e[SkyHanni] No entities found in a search radius of $searchRadius!")
        }
    }
}