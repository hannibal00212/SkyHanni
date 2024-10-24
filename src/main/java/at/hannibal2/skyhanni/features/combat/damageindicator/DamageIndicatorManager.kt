package at.hannibal2.skyhanni.features.combat.damageindicator

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory
import at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.NameVisibility
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.BossHealthChangeEvent
import at.hannibal2.skyhanni.events.DamageIndicatorDeathEvent
import at.hannibal2.skyhanni.events.DamageIndicatorDetectedEvent
import at.hannibal2.skyhanni.events.DamageIndicatorFinalBossEvent
import at.hannibal2.skyhanni.events.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.slayer.blaze.HellionShield
import at.hannibal2.skyhanni.features.slayer.blaze.HellionShieldHelper.setHellionShield
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.CollectionUtils.put
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.canBeSeen
import at.hannibal2.skyhanni.utils.EntityUtils.getNameTagWith
import at.hannibal2.skyhanni.utils.EntityUtils.hasNameTagWith
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.getLorenzVec
import com.google.gson.JsonArray
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityWolf
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.UUID
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
// TODO cut class into smaller pieces
@Suppress("LargeClass")
object DamageIndicatorManager {

    private var mobFinder: MobFinder? = null
    private val maxHealth = mutableMapOf<UUID, Long>()
    private val config get() = SkyHanniMod.feature.combat.damageIndicator

    private val enderSlayerHitsNumberPattern = ".* §[5fd]§l(?<hits>\\d+) Hits?".toPattern()

    private var data = mapOf<UUID, EntityData>()
    private val damagePattern = "[✧✯]?(\\d+[⚔+✧❤♞☄✷ﬗ✯]*)".toPattern()

    fun isBoss(entity: EntityLivingBase) = data.values.any { it.entity == entity }

    fun isDamageSplash(entity: EntityLivingBase): Boolean {
        if (entity.ticksExisted > 300 || entity !is EntityArmorStand) return false
        if (!entity.hasCustomName()) return false
        if (entity.isDead) return false
        val name = entity.customNameTag.removeColor().replace(",", "")

        return damagePattern.matcher(name).matches()
    }

    fun isBossSpawned(type: BossType) = data.entries.any { it.value.bossType == type }

    fun isBossSpawned(vararg types: BossType) = types.any { isBossSpawned(it) }

    fun getDistanceTo(vararg types: BossType): Double {
        val playerLocation = LocationUtils.playerLocation()
        return data.values.filter { it.bossType in types }
            .map { it.entity.getLorenzVec().distance(playerLocation) }
            .let { list ->
                if (list.isEmpty()) Double.MAX_VALUE else list.minOf { it }
            }
    }

    fun getNearestDistanceTo(location: LorenzVec): Double {
        return data.values
            .map { it.entity.getLorenzVec() }
            .minOfOrNull { it.distance(location) } ?: Double.MAX_VALUE
    }

    fun removeDamageIndicator(type: BossType) {
        data = data.editCopy {
            values.removeIf { it.bossType == type }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        mobFinder = MobFinder()
        data = emptyMap()
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChat(event: LorenzChatEvent) {
        mobFinder?.handleChat(event.message)
    }

    @SubscribeEvent
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        GlStateManager.disableDepth()
        GlStateManager.disableCull()

        // TODO config to define between 100ms and 5 sec
        val filter = data.filter {
            val waitForRemoval = if (it.value.dead && !noDeathDisplay(it.value.bossType)) 4_000 else 100
            (System.currentTimeMillis() > it.value.timeLastTick + waitForRemoval) || (it.value.dead && noDeathDisplay(it.value.bossType))
        }
        if (filter.isNotEmpty()) {
            data = data.editCopy {
                for (entry in filter) {
                    remove(entry.key)
                }
            }
        }

        val sizeHealth: Double
        val sizeNameAbove: Double
        val sizeBossName: Double
        val sizeFinalResults: Double
        val smallestDistanceVew: Double
        val thirdPersonView = Minecraft.getMinecraft().gameSettings.thirdPersonView
        // 0 == normal
        // 1 == f3 behind
        // 2 == selfie
        if (thirdPersonView == 1) {
            sizeHealth = 2.8
            sizeNameAbove = 2.2
            sizeBossName = 2.4
            sizeFinalResults = 1.8

            smallestDistanceVew = 10.0
        } else {
            sizeHealth = 1.9
            sizeNameAbove = 1.8
            sizeBossName = 2.1
            sizeFinalResults = 1.4

            smallestDistanceVew = 6.0
        }

        for (data in data.values) {

            // TODO test end stone protector in hole? - maybe change eye pos
//            data.ignoreBlocks =
//                data.bossType == BossType.END_ENDSTONE_PROTECTOR && Minecraft.getMinecraft().thePlayer.isSneaking

            if (!data.ignoreBlocks && !data.entity.canBeSeen(70.0)) continue
            if (!data.isConfigEnabled()) continue

            val entity = data.entity

            var healthText = data.healthText
            val delayedStart = data.delayedStart
            delayedStart?.let {
                if (!it.isInPast()) {
                    val delay = it.timeUntil()
                    healthText = formatDelay(delay)
                }
            }

            val location = if (data.dead && data.deathLocation != null) {
                data.deathLocation!!
            } else {
                val loc = entity.getLorenzVec()
                if (data.dead) data.deathLocation = loc
                loc
            }.add(-0.5, 0.0, -0.5)


            event.drawDynamicText(location, healthText, sizeHealth, smallestDistanceVew = smallestDistanceVew)

            if (data.nameAbove.isNotEmpty()) {
                event.drawDynamicText(
                    location,
                    data.nameAbove,
                    sizeNameAbove,
                    -18f,
                    smallestDistanceVew = smallestDistanceVew,
                )
            }

            var bossName = when (config.bossName) {
                NameVisibility.HIDDEN -> ""
                NameVisibility.FULL_NAME -> data.bossType.fullName
                NameVisibility.SHORT_NAME -> data.bossType.shortName
                else -> data.bossType.fullName
            }

            if (data.namePrefix.isNotEmpty()) {
                bossName = data.namePrefix + bossName
            }
            if (data.nameSuffix.isNotEmpty()) {
                bossName += data.nameSuffix
            }
            event.drawDynamicText(location, bossName, sizeBossName, -9f, smallestDistanceVew = smallestDistanceVew)

            if (config.showDamageOverTime) {
                var diff = 13f
                val currentDamage = data.damageCounter.currentDamage
                val currentHealing = data.damageCounter.currentHealing
                if (currentDamage != 0L || currentHealing != 0L) {
                    val formatDamage = "§c" + currentDamage.shortFormat()
                    val formatHealing = "§a+" + currentHealing.shortFormat()
                    val finalResult = if (currentHealing == 0L) {
                        formatDamage
                    } else if (currentDamage == 0L) {
                        formatHealing
                    } else {
                        "$formatDamage §7/ $formatHealing"
                    }
                    event.drawDynamicText(
                        location,
                        finalResult,
                        sizeFinalResults,
                        diff,
                        smallestDistanceVew = smallestDistanceVew,
                    )
                    diff += 9f
                }
                for (damage in data.damageCounter.oldDamages) {
                    val formatDamage = "§c" + damage.damage.shortFormat() + "/s"
                    val formatHealing = "§a+" + damage.healing.shortFormat() + "/s"
                    val finalResult = if (damage.healing == 0L) {
                        formatDamage
                    } else if (damage.damage == 0L) {
                        formatHealing
                    } else {
                        "$formatDamage §7/ $formatHealing"
                    }
                    event.drawDynamicText(
                        location,
                        finalResult,
                        sizeFinalResults,
                        diff,
                        smallestDistanceVew = smallestDistanceVew,
                    )
                    diff += 9f
                }
            }
        }
        GlStateManager.enableDepth()
        GlStateManager.enableCull()
    }

    private fun EntityData.isConfigEnabled() = bossType.bossTypeToggle in config.bossesToShow

    private fun noDeathDisplay(bossType: BossType): Boolean {
        return when (bossType) {
            BossType.SLAYER_BLAZE_TYPHOEUS_1,
            BossType.SLAYER_BLAZE_TYPHOEUS_2,
            BossType.SLAYER_BLAZE_TYPHOEUS_3,
            BossType.SLAYER_BLAZE_TYPHOEUS_4,
            BossType.SLAYER_BLAZE_QUAZII_1,
            BossType.SLAYER_BLAZE_QUAZII_2,
            BossType.SLAYER_BLAZE_QUAZII_3,
            BossType.SLAYER_BLAZE_QUAZII_4,

            // TODO f3/m3 4 guardians, f2/m2 4 boss room fighters
            -> true

            else -> false
        }
    }

    private fun tickDamage(damageCounter: DamageCounter) {
        val now = System.currentTimeMillis()
        if (damageCounter.currentDamage != 0L || damageCounter.currentHealing != 0L) {
            if (damageCounter.firstTick == 0L) {
                damageCounter.firstTick = now
            }

            if (now > damageCounter.firstTick + 1_000) {
                damageCounter.oldDamages.add(
                    0, OldDamage(now, damageCounter.currentDamage, damageCounter.currentHealing),
                )
                damageCounter.firstTick = 0L
                damageCounter.currentDamage = 0
                damageCounter.currentHealing = 0
            }
        }
        damageCounter.oldDamages.removeIf { now > it.time + 5_000 }
    }

    private fun formatDelay(delay: Duration): String {
        val color = when {
            delay < 1.seconds -> LorenzColor.DARK_PURPLE
            delay < 3.seconds -> LorenzColor.LIGHT_PURPLE

            else -> LorenzColor.WHITE
        }
        val format = delay.format(showMilliSeconds = true)
        return color.getChatColor() + format
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        data = data.editCopy {
            EntityUtils.getEntities<EntityLivingBase>()
                .mapNotNull(::checkEntity)
                .forEach { this put it }
        }
    }

    private fun checkEntity(entity: EntityLivingBase): Pair<UUID, EntityData>? {
        try {
            val entityData = grabData(entity) ?: return null
            if (DungeonAPI.inDungeon()) {
                checkFinalBoss(entityData.finalDungeonBoss, entity.entityId)
            }

            val health = entity.health.toLong()
            val maxHealth: Long
            val biggestHealth = getMaxHealthFor(entity)
            if (biggestHealth == 0L) {
                val currentMaxHealth = entity.baseMaxHealth.toLong()
                maxHealth = max(currentMaxHealth, health)
                setMaxHealth(entity, maxHealth)
            } else {
                maxHealth = biggestHealth
            }

            entityData.namePrefix = ""
            entityData.nameSuffix = ""
            entityData.nameAbove = ""
            val customHealthText = if (health == 0L) {
                entityData.dead = true
                if (entityData.bossType.showDeathTime && config.timeToKillSlayer) {
                    entityData.nameAbove = entityData.timeToKill
                }
                "§cDead"
            } else {
                getCustomHealth(entityData, health, entity, maxHealth) ?: return null
            }

            data[entity.uniqueID]?.let {
                val lastHealth = it.lastHealth
                checkDamage(entityData, health, lastHealth)
                tickDamage(entityData.damageCounter)

                BossHealthChangeEvent(entityData, lastHealth, health, maxHealth).postAndCatch()
            }
            entityData.lastHealth = health

            if (customHealthText.isNotEmpty()) {
                entityData.healthText = customHealthText
            } else {
                val color = NumberUtil.percentageColor(health, maxHealth)
                entityData.healthText = color.getChatColor() + health.shortFormat()
            }
            entityData.timeLastTick = System.currentTimeMillis()
            return entity.uniqueID to entityData
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(
                e, "Error checking damage indicator entity",
                "entity" to entity,
            )
            return null
        }
    }

    private fun getCustomHealth(
        entityData: EntityData,
        health: Long,
        entity: EntityLivingBase,
        maxHealth: Long,
    ): String? {

        when (entityData.bossType) {
            BossType.DUNGEON_F4_THORN -> {
                val thorn = checkThorn(health, maxHealth)
                if (thorn == null) {
                    val floor = DungeonAPI.dungeonFloor
                    ErrorManager.logErrorStateWithData(
                        "Could not detect thorn",
                        "checkThorn returns null",
                        "health" to health,
                        "maxHealth" to maxHealth,
                        "floor" to floor,
                    )
                }
                return thorn
            }

            BossType.SLAYER_ENDERMAN_1,
            BossType.SLAYER_ENDERMAN_2,
            BossType.SLAYER_ENDERMAN_3,
            BossType.SLAYER_ENDERMAN_4,
            -> return checkEnderSlayer(entity as EntityEnderman, entityData, health.toInt(), maxHealth.toInt())

            BossType.SLAYER_BLOODFIEND_1,
            BossType.SLAYER_BLOODFIEND_2,
            BossType.SLAYER_BLOODFIEND_3,
            BossType.SLAYER_BLOODFIEND_4,
            -> return checkVampireSlayer(entity as EntityOtherPlayerMP, entityData, health.toInt(), maxHealth.toInt())

            BossType.SLAYER_BLAZE_1,
            BossType.SLAYER_BLAZE_2,
            BossType.SLAYER_BLAZE_3,
            BossType.SLAYER_BLAZE_4,
            BossType.SLAYER_BLAZE_QUAZII_2,
            BossType.SLAYER_BLAZE_QUAZII_3,
            BossType.SLAYER_BLAZE_QUAZII_4,
            BossType.SLAYER_BLAZE_TYPHOEUS_2,
            BossType.SLAYER_BLAZE_TYPHOEUS_3,
            BossType.SLAYER_BLAZE_TYPHOEUS_4,
            -> return checkBlazeSlayer(entity as EntityLiving, entityData, health.toInt(), maxHealth.toInt())

            BossType.NETHER_MAGMA_BOSS -> return checkMagmaCube(
                entity as EntityMagmaCube,
                entityData,
                health.toInt(),
                maxHealth.toInt(),
            )

            BossType.SLAYER_ZOMBIE_5 -> {
                if ((entity as EntityZombie).hasNameTagWith(3, "§fBoom!")) {
                    // TODO fix
//                    val ticksAlive = entity.ticksExisted % (20 * 5)
//                    val remainingTicks = (5 * 20).toLong() - ticksAlive
//                    val format = formatDelay(remainingTicks * 50)
//                    entityData.nameSuffix = " §f§lBOOM - $format"
                    entityData.nameSuffix = " §f§lBOOM!"
                }
            }

            BossType.SLAYER_WOLF_3,
            BossType.SLAYER_WOLF_4,
            -> {
                if ((entity as EntityWolf).hasNameTagWith(2, "§bCalling the pups!")) {
                    return "Pups!"
                }
            }

            BossType.NETHER_BARBARIAN_DUKE,
            -> {
                val location = entity.getLorenzVec()
                entityData.ignoreBlocks = location.y == 117.0 && location.distanceToPlayer() < 15
            }

            else -> return ""
        }
        return ""
    }

    private fun checkBlazeSlayer(entity: EntityLiving, entityData: EntityData, health: Int, maxHealth: Int): String {
        var found = false
        for (shield in HellionShield.entries) {
            val armorStand = entity.getNameTagWith(3, shield.name)
            if (armorStand != null) {
                val number = armorStand.name.split(" ♨")[1].substring(0, 1)
                entity.setHellionShield(shield)
                entityData.nameAbove = shield.formattedName + " $number"
                found = true
                break
            }
        }
        if (!found) {
            entity.setHellionShield(null)
        }

        if (!SkyHanniMod.feature.slayer.blazes.phaseDisplay) return ""

        var calcHealth = health
        val calcMaxHealth: Int
        entityData.namePrefix = when (entityData.bossType) {
            BossType.SLAYER_BLAZE_1,
            BossType.SLAYER_BLAZE_2,
            -> {
                val step = maxHealth / 2
                calcMaxHealth = step
                if (health > step) {
                    calcHealth -= step
                    "§c1/2 "
                } else {
                    calcHealth = health
                    "§a2/2 "
                }
            }

            BossType.SLAYER_BLAZE_3,
            BossType.SLAYER_BLAZE_4,
            -> {
                val step = maxHealth / 3
                calcMaxHealth = step
                if (health > step * 2) {
                    calcHealth -= step * 2
                    "§c1/3 "
                } else if (health > step) {
                    calcHealth -= step
                    "§e2/3 "
                } else {
                    calcHealth = health
                    "§a3/3 "
                }
            }

            else -> return ""
        }

        return NumberUtil.percentageColor(
            calcHealth.toLong(), calcMaxHealth.toLong(),
        ).getChatColor() + calcHealth.shortFormat()
    }

    private fun checkMagmaCube(
        entity: EntityMagmaCube,
        entityData: EntityData,
        health: Int,
        maxHealth: Int,
    ): String? {
        val slimeSize = entity.slimeSize
        entityData.namePrefix = when (slimeSize) {
            24 -> "§c1/6"
            22 -> "§e2/6"
            20 -> "§e3/6"
            18 -> "§e4/6"
            16 -> "§e5/6"
            else -> {
                val color = NumberUtil.percentageColor(health.toLong(), 10_000_000)
                entityData.namePrefix = "§a6/6"
                return color.getChatColor() + health.shortFormat()
            }
        } + " §f"

        // hide while in the middle
//        val position = entity.getLorenzVec()
        // TODO other logic or something
//        entityData.healthLineHidden = position.x == -368.0 && position.z == -804.0

        var calcHealth = -1
        for (line in ScoreboardData.sidebarLinesRaw) {
            if (line.contains("▎")) {
                val color: String
                if (line.startsWith("§7")) {
                    color = "§7"
                } else if (line.startsWith("§e")) {
                    color = "§e"
                } else if (line.startsWith("§6") || line.startsWith("§a") || line.startsWith("§c")) {
                    calcHealth = 0
                    break
                } else {
                    ErrorManager.logErrorStateWithData(
                        "Unknown magma boss health sidebar format",
                        "Damage Indicator could not find magma boss bar data",
                        "line" to line,
                        "ScoreboardData.sidebarLinesRaw" to ScoreboardData.sidebarLinesRaw,
                        "calcHealth" to calcHealth,
                        "slimeSize" to slimeSize,
                        "entity" to entity,
                        "entityData" to entityData,
                    )
                    break
                }

                val text = line.replace("\uD83C\uDF81" + color, "")
                val max = 25.0
                val length = text.split("§e", "§7")[1].length
                val missing = (health.toDouble() / max) * length
                calcHealth = (health - missing).toInt()
            }
        }
        if (calcHealth == -1) return null

        val color = NumberUtil.percentageColor(calcHealth.toLong(), maxHealth.toLong())
        return color.getChatColor() + calcHealth.shortFormat()
    }

    private fun checkEnderSlayer(
        entity: EntityEnderman,
        entityData: EntityData,
        health: Int,
        maxHealth: Int,
    ): String? {
        var calcHealth = health
        val calcMaxHealth: Int
        entityData.namePrefix = when (entityData.bossType) {
            BossType.SLAYER_ENDERMAN_1,
            BossType.SLAYER_ENDERMAN_2,
            BossType.SLAYER_ENDERMAN_3,
            -> {
                val step = maxHealth / 3
                calcMaxHealth = step
                if (health > step * 2) {
                    calcHealth -= step * 2
                    "§c1/3 "
                } else if (health > step) {
                    calcHealth -= step
                    "§e2/3 "
                } else {
                    calcHealth = health
                    "§a3/3 "
                }
            }

            BossType.SLAYER_ENDERMAN_4 -> {
                val step = maxHealth / 6
                calcMaxHealth = step
                if (health > step * 5) {
                    calcHealth -= step * 5
                    "§c1/6 "
                } else if (health > step * 4) {
                    calcHealth -= step * 4
                    "§e2/6 "
                } else if (health > step * 3) {
                    calcHealth -= step * 3
                    "§e3/6 "
                } else if (health > step * 2) {
                    calcHealth -= step * 2
                    "§e4/6 "
                } else if (health > step) {
                    calcHealth -= step
                    "§e5/6 "
                } else {
                    calcHealth = health
                    "§a6/6 "
                }
            }

            else -> return null
        }
        var result = NumberUtil.percentageColor(
            calcHealth.toLong(), calcMaxHealth.toLong(),
        ).getChatColor() + calcHealth.shortFormat()

        if (!SkyHanniMod.feature.slayer.endermen.phaseDisplay) {
            result = ""
            entityData.namePrefix = ""
        }

        // Hit phase
        var hitPhaseText: String? = null
        val armorStandHits = entity.getNameTagWith(3, " Hit")
        if (armorStandHits != null) {
            val maxHits = when (entityData.bossType) {
                BossType.SLAYER_ENDERMAN_1 -> 15
                BossType.SLAYER_ENDERMAN_2 -> 30
                BossType.SLAYER_ENDERMAN_3 -> 60
                BossType.SLAYER_ENDERMAN_4 -> 100
                else -> 100
            }
            val hits = enderSlayerHitsNumberPattern.matchMatcher(armorStandHits.name) {
                group("hits").toInt()
            } ?: error("No hits number found in ender slayer name '${armorStandHits.name}'")

            hitPhaseText = NumberUtil.percentageColor(hits.toLong(), maxHits.toLong()).getChatColor() + "$hits Hits"
        }

        val ridingEntity = entity.ridingEntity
        // Laser phase
        if (config.enderSlayer.laserPhaseTimer && ridingEntity != null) {
            val totalTimeAlive = 8.2.seconds

            val ticksAlive = ridingEntity.ticksExisted.ticks
            val remainingTime = totalTimeAlive - ticksAlive
            val formatDelay = formatDelay(remainingTime)
            if (config.enderSlayer.showHealthDuringLaser || hitPhaseText != null) {
                entityData.nameSuffix = " §f$formatDelay"
            } else {
                return formatDelay
            }
        }
        hitPhaseText?.let {
            return it
        }

        return result
    }

    private fun checkVampireSlayer(
        entity: EntityOtherPlayerMP,
        entityData: EntityData,
        health: Int,
        maxHealth: Int,
    ): String {
        val config = config.vampireSlayer

        if (config.percentage) {
            val percentage = LorenzUtils.formatPercentage(health.toDouble() / maxHealth)
            entityData.nameSuffix = " §e$percentage"
        }

        if (config.maniaCircles) {
            entity.ridingEntity?.let {
                val existed = it.ticksExisted
                if (existed > 40) {
                    val end = (20 * 26) - existed
                    val time = end.toDouble() / 20
                    entityData.nameAbove = "Mania Circles: §b${time.roundTo(1)}s"
                    return ""
                }
            }
        }

        if (config.hpTillSteak) {
            val rest = maxHealth * 0.2
            val showHealth = health - rest
            if (showHealth < 300) {
                entityData.nameAbove = if (showHealth > 0) {
                    "§cHP till Steak: ${showHealth.addSeparators()}"
                } else "§cSteak!"
            }
        }

        return ""
    }

    private fun checkThorn(realHealth: Long, realMaxHealth: Long): String? {
        val maxHealth: Int
        val health = if (DungeonAPI.isOneOf("F4")) {
            maxHealth = 4

            if (realMaxHealth == 300_000L) {
                // no derpy
                when {
                    realHealth == 1L -> 0
                    realHealth <= 66_000 -> 1
                    realHealth <= 144_000 -> 2
                    realHealth <= 222_000 -> 3
                    realHealth <= 300_000 -> 4

                    else -> return null
                }
            } else {
                // derpy
                when {
                    realHealth == 1L -> 0
                    realHealth <= 132_000 -> 1
                    realHealth <= 288_000 -> 2
                    realHealth <= 444_000 -> 3
                    realHealth <= 600_000 -> 4

                    else -> return null
                }
            }
        } else if (DungeonAPI.isOneOf("M4")) {
            maxHealth = 6

            if (realMaxHealth == 900_000L) {
                // no derpy
                when {
                    realHealth == 1L -> 0
                    realHealth <= 135_000 -> 1
                    realHealth <= 288_000 -> 2
                    realHealth <= 441_000 -> 3
                    realHealth <= 594_000 -> 4
                    realHealth <= 747_000 -> 5
                    realHealth <= 900_000L -> 6

                    else -> return null
                }
            } else {
                // derpy
                when {
                    realHealth == 1L -> 0
                    realHealth <= 270_000 -> 1
                    realHealth <= 576_000 -> 2
                    realHealth <= 882_000 -> 3
                    realHealth <= 1_188_000 -> 4
                    realHealth <= 1_494_000 -> 5
                    realHealth <= 1_800_000 -> 6

                    else -> return null
                }
            }
        } else {
            ErrorManager.logErrorStateWithData(
                "Thorn in wrong floor detected",
                "Invalid floor for thorn",
                "dungeonFloor" to DungeonAPI.dungeonFloor,
            )
            return null
        }
        val color = NumberUtil.percentageColor(health.toLong(), maxHealth.toLong())
        return color.getChatColor() + health + "/" + maxHealth
    }

    private fun checkDamage(entityData: EntityData, health: Long, lastHealth: Long) {
        val damage = lastHealth - health
        val healing = health - lastHealth
        if (damage > 0 && entityData.bossType != BossType.DUMMY) {
            val damageCounter = entityData.damageCounter
            damageCounter.currentDamage += damage
        }
        if (healing > 0) {
            // Hide auto heal every 10 ticks (with rounding errors)
            if ((healing == 15_000L || healing == 15_001L) && entityData.bossType == BossType.SLAYER_ZOMBIE_5) return

            val damageCounter = entityData.damageCounter
            damageCounter.currentHealing += healing
        }
    }

    private fun grabData(entity: EntityLivingBase): EntityData? {
        if (data.contains(entity.uniqueID)) return data[entity.uniqueID]

        val entityResult = mobFinder?.tryAdd(entity) ?: return null

        val entityData = EntityData(
            entity,
            entityResult.ignoreBlocks,
            entityResult.delayedStart?.asTimeMark(),
            entityResult.finalDungeonBoss,
            entityResult.bossType,
            foundTime = SimpleTimeMark.now(),
        )
        DamageIndicatorDetectedEvent(entityData).postAndCatch()
        return entityData
    }

    private fun checkFinalBoss(finalBoss: Boolean, id: Int) {
        if (finalBoss) {
            DamageIndicatorFinalBossEvent(id).postAndCatch()
        }
    }

    private fun setMaxHealth(entity: EntityLivingBase, currentMaxHealth: Long) {
        maxHealth[entity.uniqueID!!] = currentMaxHealth
    }

    private fun getMaxHealthFor(entity: EntityLivingBase): Long {
        return maxHealth.getOrDefault(entity.uniqueID!!, 0L)
    }

    @HandleEvent
    fun onEntityJoin(event: EntityEnterWorldEvent<Entity>) {
        mobFinder?.handleNewEntity(event.entity)
    }

    private val dummyDamageCache = mutableListOf<UUID>()

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityLivingBase>) {
        val entity = event.entity

        val entityData = data.values.find {
            val distance = it.entity.getLorenzVec().distance(entity.getLorenzVec())
            distance < 4.5
        }

        if (isDamageSplash(entity)) {
            val name = entity.customNameTag.removeColor().replace(",", "")

            if (entityData != null) {
                if (config.hideDamageSplash) {
                    event.cancel()
                }
                if (entityData.bossType == BossType.DUMMY) {
                    val uuid = entity.uniqueID
                    if (dummyDamageCache.contains(uuid)) return
                    dummyDamageCache.add(uuid)
                    val dmg = name.toCharArray().filter { Character.isDigit(it) }.joinToString("").toLong()
                    entityData.damageCounter.currentDamage += dmg
                }
            }
        } else {
            if (entityData != null && isEnabled() && config.hideVanillaNametag && entityData.isConfigEnabled()) {
                val name = entity.name
                if (name.contains("Plasmaflux")) return
                if (name.contains("Overflux")) return
                if (name.contains("Mana Flux")) return
                if (name.contains("Radiant")) return
                event.cancel()
            }
        }
    }

    @SubscribeEvent
    fun onEntityHealthUpdate(event: EntityHealthUpdateEvent) {
        val data = data[event.entity.uniqueID] ?: return
        if (event.health <= 1) {
            if (!data.firstDeath) {
                data.firstDeath = true
                DamageIndicatorDeathEvent(event.entity, data).postAndCatch()
            }
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "damageIndicator", "combat.damageIndicator")
        event.move(3, "slayer.endermanPhaseDisplay", "slayer.endermen.phaseDisplay")
        event.move(3, "slayer.blazePhaseDisplay", "slayer.blazes.phaseDisplay")
        event.transform(11, "combat.damageIndicator.bossesToShow") { element ->
            ConfigUtils.migrateIntArrayListToEnumArrayList(element, BossCategory::class.java)
        }

        event.transform(15, "combat.damageIndicator.bossName") { element ->
            ConfigUtils.migrateIntToEnum(element, NameVisibility::class.java)
        }
        event.transform(23, "combat.damageIndicator.bossesToShow") { element ->
            val result = JsonArray()
            for (bossType in element as JsonArray) {
                if (bossType.asString == "DUNGEON_ALL") continue
                result.add(bossType)
            }

            result
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
