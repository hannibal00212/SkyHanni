package at.hannibal2.skyhanni.features.combat.damageindicator

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.dungeon.DungeonLividFinder
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.pests.PestType
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.hasBossHealth
import at.hannibal2.skyhanni.utils.EntityUtils.hasMaxHealth
import at.hannibal2.skyhanni.utils.EntityUtils.hasNameTagWith
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import at.hannibal2.skyhanni.utils.LorenzUtils.ignoreDerpy
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityGuardian
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntityMagmaCube
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySilverfish
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityWolf
import java.util.UUID

class MobFinder {

    // F1
    private var floor1bonzo1 = false
    private var floor1bonzo1SpawnTime = 0L
    private var floor1bonzo2 = false
    private var floor1bonzo2SpawnTime = 0L

    // F2
    private var floor2summons1 = false
    private var floor2summons1SpawnTime = 0L
    private var floor2summonsDiedOnce = mutableListOf<EntityOtherPlayerMP>()
    private var floor2secondPhase = false
    private var floor2secondPhaseSpawnTime = 0L

    // F3
    private var floor3GuardianShield = false
    private var floor3GuardianShieldSpawnTime = 0L
    private var guardians = mutableListOf<EntityGuardian>()
    private var floor3Professor = false
    private var floor3ProfessorSpawnTime = 0L
    private var floor3ProfessorGuardianPrepare = false
    private var floor3ProfessorGuardianPrepareSpawnTime = 0L
    private var floor3ProfessorGuardian = false
    private var floor3ProfessorGuardianEntity: EntityGuardian? = null

    // F5
    private var floor5lividEntity: EntityOtherPlayerMP? = null
    private var floor5lividEntitySpawnTime = 0L
    private val correctLividPattern = "§c\\[BOSS] (.*) Livid§r§f: Impossible! How did you figure out which one I was\\?!".toPattern()

    // F6
    private var floor6Giants = false
    private var floor6GiantsSpawnTime = 0L
    private var floor6GiantsSeparateDelay = mutableMapOf<UUID, Pair<Long, BossType>>()
    private var floor6Sadan = false
    private var floor6SadanSpawnTime = 0L

    internal fun tryAdd(entity: EntityLivingBase) = when {
        DungeonAPI.inDungeon() -> tryAddDungeon(entity)
        RiftAPI.inRift() -> tryAddRift(entity)
        GardenAPI.inGarden() -> tryAddGarden(entity)
        else -> {
            if (entity is EntityLiving && entity.hasNameTagWith(2, "Dummy §a10M§c❤")) {
                EntityResult(bossType = BossType.DUMMY)
            } else {
                when (entity) {
                    /*
                     * Note that the order does matter here.
                     * For example, if you put EntityZombie before EntityPigZombie,
                     * EntityPigZombie will never be reached because EntityPigZombie extends EntityZombie.
                     * Please take this into consideration if you are to modify this.
                     */
                    is EntityOtherPlayerMP -> tryAddEntityOtherPlayerMP(entity)
                    is EntityIronGolem -> tryAddEntityIronGolem(entity)
                    is EntityPigZombie -> tryAddEntityPigZombie(entity)
                    is EntityMagmaCube -> tryAddEntityMagmaCube(entity)
                    is EntityEnderman -> tryAddEntityEnderman(entity)
                    is EntitySkeleton -> tryAddEntitySkeleton(entity)
                    is EntityGuardian -> tryAddEntityGuardian(entity)
                    is EntityZombie -> tryAddEntityZombie(entity)
                    is EntityWither -> tryAddEntityWither(entity)
                    is EntityDragon -> tryAddEntityDragon(entity)
                    is EntitySpider -> tryAddEntitySpider(entity)
                    is EntityHorse -> tryAddEntityHorse(entity)
                    is EntityBlaze -> tryAddEntityBlaze(entity)
                    is EntityWolf -> tryAddEntityWolf(entity)
                    else -> null
                }
            }
        }
    }

    private fun tryAddGarden(entity: EntityLivingBase): EntityResult? {
        if (entity is EntitySilverfish || entity is EntityBat) {
            return tryAddGardenPest(entity)
        }

        return null
    }

    private fun tryAddGardenPest(entity: EntityLivingBase): EntityResult? {
        if (!GardenAPI.inGarden()) return null

        return PestType.entries.firstOrNull { entity.hasNameTagWith(3, it.displayName) }
            ?.let { EntityResult(bossType = it.damageIndicatorBoss) }
    }

    private fun tryAddDungeon(entity: EntityLivingBase) = when {
        DungeonAPI.isOneOf("F1", "M1") -> tryAddDungeonF1(entity)
        DungeonAPI.isOneOf("F2", "M2") -> tryAddDungeonF2(entity)
        DungeonAPI.isOneOf("F3", "M3") -> tryAddDungeonF3(entity)
        DungeonAPI.isOneOf("F4", "M4") -> tryAddDungeonF4(entity)
        DungeonAPI.isOneOf("F5", "M5") -> tryAddDungeonF5(entity)
        DungeonAPI.isOneOf("F6", "M6") -> tryAddDungeonF6(entity)
        else -> null
    }

    private fun tryAddDungeonF1(entity: EntityLivingBase) = when {
        floor1bonzo1 && entity is EntityOtherPlayerMP && entity.name == "Bonzo " -> {
            EntityResult(floor1bonzo1SpawnTime, bossType = BossType.DUNGEON_F1_BONZO_FIRST)
        }

        floor1bonzo2 && entity is EntityOtherPlayerMP && entity.name == "Bonzo " -> {
            EntityResult(floor1bonzo2SpawnTime, bossType = BossType.DUNGEON_F1_BONZO_SECOND, finalDungeonBoss = true)
        }

        else -> null
    }

    private fun tryAddDungeonF2(entity: EntityLivingBase): EntityResult? {
        if (entity.name == "Summon " && entity is EntityOtherPlayerMP) {
            if (floor2summons1 && !floor2summonsDiedOnce.contains(entity)) {
                if (entity.health.toInt() != 0) {
                    return EntityResult(floor2summons1SpawnTime, bossType = BossType.DUNGEON_F2_SUMMON)
                }
                floor2summonsDiedOnce.add(entity)
            }
            if (floor2secondPhase) {
                return EntityResult(floor2secondPhaseSpawnTime, bossType = BossType.DUNGEON_F2_SUMMON)
            }
        }

        if (floor2secondPhase && entity is EntityOtherPlayerMP) {
            // TODO only show scarf after (all/at least x) summons are dead?
            if (entity.name == "Scarf ") {
                return EntityResult(
                    floor2secondPhaseSpawnTime,
                    finalDungeonBoss = true,
                    bossType = BossType.DUNGEON_F2_SCARF,
                )
            }
        }
        return null
    }

    private fun tryAddDungeonF3(entity: EntityLivingBase): EntityResult? {
        if (entity is EntityGuardian && floor3GuardianShield) {
            if (guardians.size == 4) {
                calcGuardiansTotalHealth()
            } else {
                findGuardians()
            }
            if (guardians.contains(entity)) {
                return EntityResult(floor3GuardianShieldSpawnTime, true, bossType = BossType.DUNGEON_F3_GUARDIAN)
            }
        }

        if (floor3Professor && entity is EntityOtherPlayerMP && entity.name == "The Professor") {
            return EntityResult(
                floor3ProfessorSpawnTime,
                floor3ProfessorSpawnTime + 1_000 > System.currentTimeMillis(),
                bossType = BossType.DUNGEON_F3_PROFESSOR_1,
            )
        }
        if (floor3ProfessorGuardianPrepare && entity is EntityOtherPlayerMP && entity.name == "The Professor") {
            return EntityResult(
                floor3ProfessorGuardianPrepareSpawnTime,
                true,
                bossType = BossType.DUNGEON_F3_PROFESSOR_2,
            )
        }

        if (entity is EntityGuardian && floor3ProfessorGuardian && entity == floor3ProfessorGuardianEntity) {
            return EntityResult(finalDungeonBoss = true, bossType = BossType.DUNGEON_F3_PROFESSOR_2)
        }
        return null
    }

    private fun tryAddDungeonF4(entity: EntityLivingBase): EntityResult? {
        if (entity is EntityGhast) {
            return EntityResult(
                bossType = BossType.DUNGEON_F4_THORN,
                ignoreBlocks = true,
                finalDungeonBoss = true,
            )
        }
        return null
    }

    private fun tryAddDungeonF5(entity: EntityLivingBase): EntityResult? {
        if (entity is EntityOtherPlayerMP && entity == DungeonLividFinder.lividEntity) {
            return EntityResult(
                bossType = BossType.DUNGEON_F5,
                ignoreBlocks = true,
                finalDungeonBoss = true,
            )
        }
        return null
    }

    private fun tryAddDungeonF6(entity: EntityLivingBase): EntityResult? {
        if (entity !is EntityGiantZombie || entity.isInvisible) return null
        if (floor6Giants && entity.posY > 68) {
            val (extraDelay, bossType) = checkExtraF6GiantsDelay(entity)
            return EntityResult(
                floor6GiantsSpawnTime + extraDelay,
                floor6GiantsSpawnTime + extraDelay + 1_000 > System.currentTimeMillis(),
                bossType = bossType,
            )
        }

        if (floor6Sadan) {
            return EntityResult(floor6SadanSpawnTime, finalDungeonBoss = true, bossType = BossType.DUNGEON_F6_SADAN)
        }
        return null
    }

    private fun tryAddRift(entity: EntityLivingBase): EntityResult? {
        if (entity is EntityOtherPlayerMP) {
            if (entity.name == "Leech Supreme") {
                return EntityResult(bossType = BossType.LEECH_SUPREME)
            }

            if (entity.name == "Bloodfiend ") {
                // there is no derpy in rift
                val hp = entity.baseMaxHealth.ignoreDerpy()
                when {
                    entity.hasMaxHealth(625, true, hp) -> return EntityResult(bossType = BossType.SLAYER_BLOODFIEND_1)
                    entity.hasMaxHealth(1_100, true, hp) -> return EntityResult(bossType = BossType.SLAYER_BLOODFIEND_2)
                    entity.hasMaxHealth(1_800, true, hp) -> return EntityResult(bossType = BossType.SLAYER_BLOODFIEND_3)
                    entity.hasMaxHealth(2_400, true, hp) -> return EntityResult(bossType = BossType.SLAYER_BLOODFIEND_4)
                    entity.hasMaxHealth(3_000, true, hp) -> return EntityResult(bossType = BossType.SLAYER_BLOODFIEND_5)
                }
            }
        }
        if (entity is EntitySlime && entity.baseMaxHealth == 1_000) {
            return EntityResult(bossType = BossType.BACTE)
        }
        return null
    }

    private fun tryAddEntityBlaze(entity: EntityLivingBase) = when {
        entity.name != "Dinnerbone" &&
            entity.hasNameTagWith(2, "§e﴾ §8[§7Lv200§8] §l§8§lAshfang§r ") &&
            entity.hasMaxHealth(
                50_000_000,
                true,
            ) -> {
            EntityResult(bossType = BossType.NETHER_ASHFANG)
        }

        entity.hasNameTagWith(2, "§c☠ §bInferno Demonlord ") -> {
            when {
                entity.hasBossHealth(2_500_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_1)
                entity.hasBossHealth(10_000_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_2)
                entity.hasBossHealth(45_000_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_3)
                entity.hasBossHealth(150_000_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_4)
                else -> null
            }
        }

        else -> null
    }

    private fun tryAddEntitySkeleton(entity: EntityLivingBase) = when {
        entity.hasNameTagWith(2, "§c☠ §3ⓆⓊⒶⓏⒾⒾ ") -> {
            when {
                entity.hasBossHealth(10_000_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_QUAZII_4)
                entity.hasBossHealth(5_000_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_QUAZII_3)
                entity.hasBossHealth(1_750_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_QUAZII_2)
                entity.hasBossHealth(500_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_QUAZII_1)
                else -> null
            }
        }

        entity.hasNameTagWith(5, "§e﴾ §8[§7Lv200§8] §l§8§lBladesoul§r ") -> {
            EntityResult(bossType = BossType.NETHER_BLADESOUL)
        }

        else -> null
    }

    private fun tryAddEntityOtherPlayerMP(entity: EntityLivingBase) = when {
        entity.name == "Mage Outlaw" -> EntityResult(bossType = BossType.NETHER_MAGE_OUTLAW)
        entity.name == "DukeBarb " &&
            entity.getLorenzVec()
                .distanceToPlayer() < 30 -> EntityResult(bossType = BossType.NETHER_BARBARIAN_DUKE)

        entity.name == "Minos Inquisitor" -> EntityResult(bossType = BossType.MINOS_INQUISITOR)
        entity.name == "Minos Champion" -> EntityResult(bossType = BossType.MINOS_CHAMPION)
        entity.name == "Minotaur " -> EntityResult(bossType = BossType.MINOTAUR)

        else -> null
    }

    private fun tryAddEntityWither(entity: EntityLivingBase) = when {
        entity.hasNameTagWith(4, "§8[§7Lv100§8] §c§5Vanquisher§r ") -> {
            EntityResult(bossType = BossType.NETHER_VANQUISHER)
        }

        else -> null
    }

    private fun tryAddEntityEnderman(entity: EntityLivingBase): EntityResult? {
        if (!entity.hasNameTagWith(3, "§c☠ §bVoidgloom Seraph ")) return null

        return when {
            entity.hasMaxHealth(300_000, true) -> EntityResult(bossType = BossType.SLAYER_ENDERMAN_1)
            entity.hasMaxHealth(12_000_000, true) -> EntityResult(bossType = BossType.SLAYER_ENDERMAN_2)
            entity.hasMaxHealth(50_000_000, true) -> EntityResult(bossType = BossType.SLAYER_ENDERMAN_3)
            entity.hasMaxHealth(210_000_000, true) -> EntityResult(bossType = BossType.SLAYER_ENDERMAN_4)
            else -> null
        }
    }

    // TODO testing and use sidebar data
    @Suppress("UnusedParameter")
    private fun tryAddEntityDragon(entity: EntityLivingBase) = when {
        IslandType.THE_END.isInIsland() -> EntityResult(bossType = BossType.END_ENDER_DRAGON)
        IslandType.WINTER.isInIsland() -> EntityResult(bossType = BossType.WINTER_REINDRAKE)

        else -> null
    }

    private fun tryAddEntityIronGolem(entity: EntityLivingBase) = when {
        entity.hasNameTagWith(3, "§e﴾ §8[§7Lv100§8] §lEndstone Protector§r ") -> {
            EntityResult(bossType = BossType.END_ENDSTONE_PROTECTOR)
        }

        entity.hasMaxHealth(1_500_000) -> {
            EntityResult(bossType = BossType.GAIA_CONSTRUCT)
        }

        entity.hasMaxHealth(100_000_000) -> {
            EntityResult(bossType = BossType.LORD_JAWBUS)
        }

        else -> null
    }

    private fun tryAddEntityZombie(entity: EntityLivingBase) = when {
        entity.hasNameTagWith(2, "§c☠ §bRevenant Horror") -> {
            when {
                entity.hasMaxHealth(500, true) -> EntityResult(bossType = BossType.SLAYER_ZOMBIE_1)
                entity.hasMaxHealth(20_000, true) -> EntityResult(bossType = BossType.SLAYER_ZOMBIE_2)
                entity.hasMaxHealth(400_000, true) -> EntityResult(bossType = BossType.SLAYER_ZOMBIE_3)
                entity.hasMaxHealth(1_500_000, true) -> EntityResult(bossType = BossType.SLAYER_ZOMBIE_4)

                else -> null
            }
        }

        entity.hasNameTagWith(2, "§c☠ §fAtoned Horror ") && entity.hasMaxHealth(10_000_000, true) -> {
            EntityResult(bossType = BossType.SLAYER_ZOMBIE_5)
        }

        else -> null
    }

    private fun tryAddEntityMagmaCube(entity: EntityLivingBase) = when {
        entity.hasNameTagWith(15, "§e﴾ §8[§7Lv500§8] §l§4§lMagma Boss§r ") && entity.hasMaxHealth(200_000_000, true) -> {
            EntityResult(bossType = BossType.NETHER_MAGMA_BOSS, ignoreBlocks = true)
        }

        else -> null
    }

    private fun tryAddEntityHorse(entity: EntityLivingBase) = when {
        entity.hasNameTagWith(15, "§8[§7Lv100§8] §c§6Headless Horseman§r ") && entity.hasMaxHealth(3_000_000, true) -> {
            EntityResult(bossType = BossType.HUB_HEADLESS_HORSEMAN)
        }

        else -> null
    }

    private fun tryAddEntityPigZombie(entity: EntityLivingBase) = if (entity.hasNameTagWith(2, "§c☠ §6ⓉⓎⓅⒽⓄⒺⓊⓈ ")) {
        when {
            entity.hasBossHealth(10_000_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_TYPHOEUS_4)
            entity.hasBossHealth(5_000_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_TYPHOEUS_3)
            entity.hasBossHealth(1_750_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_TYPHOEUS_2)
            entity.hasBossHealth(500_000) -> EntityResult(bossType = BossType.SLAYER_BLAZE_TYPHOEUS_1)
            else -> null
        }
    } else null

    private fun tryAddEntitySpider(entity: EntityLivingBase): EntityResult? {
        if (entity.hasNameTagWith(1, "§5☠ §4Tarantula Broodfather ")) {
            when {
                entity.hasMaxHealth(740, true) -> return EntityResult(bossType = BossType.SLAYER_SPIDER_1)
                entity.hasMaxHealth(30_000, true) -> return EntityResult(bossType = BossType.SLAYER_SPIDER_2)
                entity.hasMaxHealth(900_000, true) -> return EntityResult(bossType = BossType.SLAYER_SPIDER_3)
                entity.hasMaxHealth(2_400_000, true) -> return EntityResult(bossType = BossType.SLAYER_SPIDER_4)
            }
        }
        if (entity.hasNameTagWith(1, "[§7Lv12§8] §4Broodmother")) {
            if (entity.hasMaxHealth(6000)) {
                return EntityResult(bossType = BossType.BROODMOTHER)
            }
        }
        checkArachne(entity as EntitySpider)?.let { return it }
        return null
    }

    private fun checkArachne(entity: EntitySpider): EntityResult? {
        if (entity.hasNameTagWith(1, "[§7Lv300§8] §cArachne") || entity.hasNameTagWith(1, "[§7Lv300§8] §lArachne")) {
            val maxHealth = entity.baseMaxHealth
            // Ignore the minis
            if (maxHealth == 12 || maxHealth.derpy() == 4000) return null
            return EntityResult(bossType = BossType.ARACHNE_SMALL)
        }
        if (entity.hasNameTagWith(1, "[§7Lv500§8] §cArachne") || entity.hasNameTagWith(1, "[§7Lv500§8] §lArachne")) {
            val maxHealth = entity.baseMaxHealth
            if (maxHealth == 12 || maxHealth.derpy() == 20_000) return null
            return EntityResult(bossType = BossType.ARACHNE_BIG)
        }

        return null
    }

    private fun tryAddEntityWolf(entity: EntityLivingBase) = if (entity.hasNameTagWith(1, "§c☠ §fSven Packmaster ")) {
        when {
            entity.hasMaxHealth(2_000, true) -> EntityResult(bossType = BossType.SLAYER_WOLF_1)
            entity.hasMaxHealth(40_000, true) -> EntityResult(bossType = BossType.SLAYER_WOLF_2)
            entity.hasMaxHealth(750_000, true) -> EntityResult(bossType = BossType.SLAYER_WOLF_3)
            entity.hasMaxHealth(2_000_000, true) -> EntityResult(bossType = BossType.SLAYER_WOLF_4)
            else -> null
        }
    } else null

    private fun tryAddEntityGuardian(entity: EntityLivingBase) = if (entity.hasMaxHealth(35_000_000)) {
        EntityResult(bossType = BossType.THUNDER)
    } else null

    private fun checkExtraF6GiantsDelay(entity: EntityGiantZombie): Pair<Long, BossType> {
        val uuid = entity.uniqueID

        if (floor6GiantsSeparateDelay.contains(uuid)) {
            return floor6GiantsSeparateDelay[uuid]!!
        }

        val middle = LorenzVec(-8, 0, 56)

        val loc = entity.getLorenzVec()

        var pos = 0

        val type: BossType
        if (loc.x > middle.x && loc.z > middle.z) {
            // first
            pos = 2
            type = BossType.DUNGEON_F6_GIANT_3
        } else if (loc.x > middle.x && loc.z < middle.z) {
            // second
            pos = 3
            type = BossType.DUNGEON_F6_GIANT_4
        } else if (loc.x < middle.x && loc.z < middle.z) {
            // third
            pos = 0
            type = BossType.DUNGEON_F6_GIANT_1
        } else if (loc.x < middle.x && loc.z > middle.z) {
            // fourth
            pos = 1
            type = BossType.DUNGEON_F6_GIANT_2
        } else {
            pos = 0
            type = BossType.DUNGEON_F6_GIANT_1
        }

        val extraDelay = 900L * pos
        val pair = Pair(extraDelay, type)
        floor6GiantsSeparateDelay[uuid] = pair

        return pair
    }

    fun handleChat(message: String) {
        if (!DungeonAPI.inDungeon()) return
        when (message) {
            // F1
            "§c[BOSS] Bonzo§r§f: Gratz for making it this far, but I'm basically unbeatable." -> {
                floor1bonzo1 = true
                floor1bonzo1SpawnTime = System.currentTimeMillis() + 11_250
            }

            "§c[BOSS] Bonzo§r§f: Oh noes, you got me.. what ever will I do?!" -> {
                floor1bonzo1 = false
            }

            "§c[BOSS] Bonzo§r§f: Oh I'm dead!" -> {
                floor1bonzo2 = true
                floor1bonzo2SpawnTime = System.currentTimeMillis() + 4_200
            }

            "§c[BOSS] Bonzo§r§f: Alright, maybe I'm just weak after all.." -> {
                floor1bonzo2 = false
            }

            // F2
            "§c[BOSS] Scarf§r§f: ARISE, MY CREATIONS!" -> {
                floor2summons1 = true
                floor2summons1SpawnTime = System.currentTimeMillis() + 3_500
            }

            "§c[BOSS] Scarf§r§f: Those toys are not strong enough I see." -> {
                floor2summons1 = false
            }

            "§c[BOSS] Scarf§r§f: Don't get too excited though." -> {
                floor2secondPhase = true
                floor2secondPhaseSpawnTime = System.currentTimeMillis() + 6_300
            }

            "§c[BOSS] Scarf§r§f: Whatever..." -> {
                floor2secondPhase = false
            }

            // F3
            "§c[BOSS] The Professor§r§f: I was burdened with terrible news recently..." -> {
                floor3GuardianShield = true
                floor3GuardianShieldSpawnTime = System.currentTimeMillis() + 15_400
            }

            "§c[BOSS] The Professor§r§f: Oh? You found my Guardians' one weakness?" -> {
                floor3GuardianShield = false
                DamageIndicatorManager.removeDamageIndicator(BossType.DUNGEON_F3_GUARDIAN)
                floor3Professor = true
                floor3ProfessorSpawnTime = System.currentTimeMillis() + 10_300
            }

            "§c[BOSS] The Professor§r§f: I see. You have forced me to use my ultimate technique." -> {
                floor3Professor = false

                floor3ProfessorGuardianPrepare = true
                floor3ProfessorGuardianPrepareSpawnTime = System.currentTimeMillis() + 10_500
            }

            "§c[BOSS] The Professor§r§f: The process is irreversible, but I'll be stronger than a Wither now!" -> {
                floor3ProfessorGuardian = true
            }

            "§c[BOSS] The Professor§r§f: What?! My Guardian power is unbeatable!" -> {
                floor3ProfessorGuardian = false
            }

            // F5
            "§c[BOSS] Livid§r§f: This Orb you see, is Thorn, or what is left of him." -> {
                floor5lividEntity = DungeonLividFinder.lividEntity
                floor5lividEntitySpawnTime = System.currentTimeMillis() + 13_000
            }

            // F6
            "§c[BOSS] Sadan§r§f: ENOUGH!" -> {
                floor6Giants = true
                floor6GiantsSpawnTime = System.currentTimeMillis() + 7_400
            }

            "§c[BOSS] Sadan§r§f: You did it. I understand now, you have earned my respect." -> {
                floor6Giants = false
                floor6Sadan = true
                floor6SadanSpawnTime = System.currentTimeMillis() + 32_500
            }

            "§c[BOSS] Sadan§r§f: NOOOOOOOOO!!! THIS IS IMPOSSIBLE!!" -> {
                floor6Sadan = false
            }
        }

        correctLividPattern.matchMatcher(message) {
            floor5lividEntity = null
        }
    }

    fun handleNewEntity(entity: Entity) {
        if (DungeonAPI.inDungeon() && floor3ProfessorGuardian && entity is EntityGuardian && floor3ProfessorGuardianEntity == null) {
            floor3ProfessorGuardianEntity = entity
            floor3ProfessorGuardianPrepare = false
        }
    }

    private fun findGuardians() {
        guardians.clear()

        for (entity in EntityUtils.getEntities<EntityGuardian>()) {
            // F3
            if (entity.hasMaxHealth(1_000_000, true) || entity.hasMaxHealth(1_200_000, true)) {
                guardians.add(entity)
            }

            // M3
            if (entity.hasMaxHealth(120_000_000, true) || entity.hasMaxHealth(240_000_000, true)) {
                guardians.add(entity)
            }
            // M3 Reinforced Guardian
            if (entity.hasMaxHealth(140_000_000, true) || entity.hasMaxHealth(280_000_000, true)) {
                guardians.add(entity)
            }
        }
    }

    private fun calcGuardiansTotalHealth() {
        var totalHealth = 0
        for (guardian in guardians) {
            totalHealth += guardian.health.toInt()
        }
        if (totalHealth == 0) {
            floor3GuardianShield = false
            guardians.clear()
        }
    }
}
