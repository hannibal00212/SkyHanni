package at.hannibal2.skyhanni.data.mob

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.MobData.MobResult
import at.hannibal2.skyhanni.data.mob.MobData.MobResult.Companion.makeMobResult
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.takeWhileInclusive
import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.EntityUtils.isNPC
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.LorenzUtils.derpy
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.MobUtils
import at.hannibal2.skyhanni.utils.MobUtils.isDefaultValue
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityCaveSpider
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityGiantZombie
import net.minecraft.entity.monster.EntityGuardian
import net.minecraft.entity.monster.EntitySnowman
import net.minecraft.entity.monster.EntityWitch
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntityChicken
import net.minecraft.entity.passive.EntityCow
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityMooshroom
import net.minecraft.entity.passive.EntityPig
import net.minecraft.entity.passive.EntityRabbit
import net.minecraft.entity.passive.EntitySheep
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer

@Suppress("RegExpRedundantEscape")
@SkyHanniModule
object MobFilter {

    private val repoGroup = RepoPattern.group("mob.detection")

    /** REGEX-TEST: Wither Husk 500M❤ */
    val mobNameFilter by repoGroup.pattern(
        "filter.basic",
        "(?:\\[\\w+(?<level>\\d+)\\] )?(?<corrupted>.Corrupted )?(?<name>[^ᛤ]*)(?: ᛤ)? [\\dBMk.,❤]+",
    )
    val slayerNameFilter by repoGroup.pattern("filter.slayer", "^. (?<name>.*) (?<tier>[IV]+) \\d+.*")

    /** REGEX-TEST: ﴾ Storm ﴿
     *  REGEX-TEST: ﴾ [Lv200] aMage Outlawa 70M/70M❤ ﴿
     *  REGEX-TEST: ﴾ [Lv500] Magma Boss █████████████████████████ ﴿
     *  REGEX-TEST: ﴾ [Lv200] Bladesoul 50M/50M❤ ﴿
     *  REGEX-TEST: ﴾ [Lv300] Arachne 20,000/20,000❤ ﴿
     *  REGEX-TEST: ﴾ [Lv500] Arachne 100k/100k❤ ﴿
     *  REGEX-TEST: ﴾ [Lv200] Barbarian Duke X 70M/70M❤ ﴿
     *  REGEX-TEST: ﴾ [Lv100] Endstone Protector 4.6M/5M❤ ﴿
     *  REGEX-TEST: ﴾ [Lv400] Thunder 29M/35M❤ ﴿
     *  */
    val bossMobNameFilter by repoGroup.pattern(
        "filter.boss",
        "^. (?:\\[Lv(?<level>\\d+)\\] )?(?<name>[^ᛤ\n]*?)(?: ᛤ)?(?: [\\d\\/BMk.,❤]+| █+)? .$",
    )
    val dungeonNameFilter by repoGroup.pattern(
        "filter.dungeon",
        "^(?:(?<star>✯)\\s)?(?:(?<attribute>${DungeonAttribute.toRegexLine})\\s)?(?:\\[[\\w\\d]+\\]\\s)?(?<name>[^ᛤ]+)(?: ᛤ)?\\s[^\\s]+$",
    )
    val summonFilter by repoGroup.pattern(
        "filter.summon",
        "^(?<owner>\\w+)'s (?<name>.*) \\d+.*",
    )
    val dojoFilter by repoGroup.pattern(
        "filter.dojo",
        "^(?:(?<points>\\d+) pts|(?<empty>\\w+))$",
    )
    val jerryPattern by repoGroup.pattern(
        "jerry",
        "(?:\\[\\w+(?<level>\\d+)\\] )?(?<owner>\\w+)'s (?<name>\\w+ Jerry) \\d+ Hits",
    )
    val petCareNamePattern by repoGroup.pattern(
        "pattern.petcare",
        "^\\[\\w+ (?<level>\\d+)\\] (?<name>.*)",
    )
    val wokeSleepingGolemPattern by repoGroup.pattern(
        "pattern.dungeon.woke.golem",
        "(?:§c§lWoke|§5§lSleeping) Golem§r",
    )
    val jerryMagmaCubePattern by repoGroup.pattern(
        "pattern.jerry.magma.cube",
        "§c(?:Cubie|Maggie|Cubert|Cübe|Cubette|Magmalene|Lucky 7|8ball|Mega Cube|Super Cube)(?: ᛤ)? §a\\d+§8\\/§a\\d+§c❤",
    )
    val summonOwnerPattern by repoGroup.pattern(
        "pattern.summon.owner",
        ".*Spawned by: (?<name>.*).*",
    )
    val heavyPearlPattern by repoGroup.pattern(
        "pattern.heavypearl.collect",
        "§.§lCOLLECT!",
    )

    /**
     * REGEX-TEST: §8[§7Lv1§8] §5Horse
     * REGEX-TEST: §8[§7Lv52§8] §eArmadillo
     * REGEX-TEST: §8[§7Lv12§8] §eSkeleton Horse
     * REGEX-TEST: §8[§7Lv49§8] §ePig
     * REGEX-TEST: §8[§7Lv64§8] §eRat
     */
    val illegalEntitiesPattern by repoGroup.pattern(
        "pattern.pet.entities",
        "^§8\\[§7Lv\\d+§8] §.(?<name>Horse|Armadillo|Skeleton Horse|Pig|Rat)$",
    )

    // TODO: Move all of these to repo
    @Suppress("MaxLineLength")
    internal const val RAT_SKULL =
        "ewogICJ0aW1lc3RhbXAiIDogMTYxODQxOTcwMTc1MywKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJCdUlJZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThhYmI0NzFkYjBhYjc4NzAzMDExOTc5ZGM4YjQwNzk4YTk0MWYzYTRkZWMzZWM2MWNiZWVjMmFmOGNmZmU4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="
    @Suppress("MaxLineLength")
    private const val HELLWISP_TENTACLE_SKULL =
        "ewogICJ0aW1lc3RhbXAiIDogMTY0OTM4MzAyMTQxNiwKICAicHJvZmlsZUlkIiA6ICIzYjgwOTg1YWU4ODY0ZWZlYjA3ODg2MmZkOTRhMTVkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLaWVyYW5fVmF4aWxpYW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI3MDQ2Mzg0OTM2MzhiODVjMzhkZDYzZmZkYmUyMjJmZTUzY2ZkNmE1MDk3NzI4NzU2MTE5MzdhZTViNWUyMiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
    @Suppress("MaxLineLength")
    private const val RIFT_EYE_SKULL1 =
        "ewogICJ0aW1lc3RhbXAiIDogMTY0ODA5MTkzNTcyMiwKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI2YmRlNDUwNDljN2I3ZDM0NjA1ZDgwNmEwNjgyOWI2Zjk1NWI4NTZhNTk5MWZkMzNlN2VhYmNlNDRjMDgzNCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
    @Suppress("MaxLineLength")
    private const val RIFT_EYE_SKULL2 =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdkYjE5MjNkMDNjNGVmNGU5ZjZlODcyYzVhNmFkMjU3OGIxYWZmMmIyODFmYmMzZmZhNzQ2NmM4MjVmYjkifX19"
    @Suppress("MaxLineLength")
    internal const val NPC_TURD_SKULL =
        "ewogICJ0aW1lc3RhbXAiIDogMTYzOTUxMjYxNzc5MywKICAicHJvZmlsZUlkIiA6ICIwZjczMDA3NjEyNGU0NGM3YWYxMTE1NDY5YzQ5OTY3OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJPcmVfTWluZXIxMjMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjM2MzBkOWIwMjA4OGVhMTkyNGE4NzIyNDJhYmM3NWI2MjYyYzJhY2E5MmFlY2Y4NzE0YTU3YTQxZWVhMGI5ZCIKICAgIH0KICB9Cn0="

    const val MINION_MOB_PREFIX = "Minion Mob "

    enum class DungeonAttribute {
        Flaming,
        Stormy,
        Speedy,
        Fortified,
        Healthy,
        Healing,
        Boomer,
        Golden,
        Stealth,
        ;

        companion object {

            val toRegexLine = DungeonAttribute.entries.joinToString("|") { it.name }
        }
    }

    private val extraDisplayNPCByName = setOf(
        "Guy ", // Guy NPC (but only as visitor)
        "vswiblxdxg", // Mayor Cole
        "anrrtqytsl", // Weaponsmith
    )

    private val displayNPCCompressedNamePattern by repoGroup.pattern("displaynpc.name", "[a-z0-9]{10}")

    private fun displayNPCNameCheck(name: String) = name.startsWith('§') ||
        displayNPCCompressedNamePattern.matches(name) ||
        extraDisplayNPCByName.contains(name)

    private val listOfClickArmorStand = setOf(
        "§e§lCLICK",
        "§6§lSEASONAL SKINS",
        "§e§lGATE KEEPER",
        "§e§lBLACKSMITH",
        "§e§lSHOP",
        "§e§lTREASURES",
    )

    fun Entity.isSkyBlockMob(): Boolean = when {
        this !is EntityLivingBase -> false
        this is EntityArmorStand -> false
        this is EntityPlayer && this.isRealPlayer() -> false
        this.isDisplayNPC() -> false
        this is EntityWither && this.entityId < 0 -> false
        else -> true
    }

    fun EntityPlayer.isRealPlayer() = uniqueID?.let { it.version() == 4 } ?: false

    fun EntityLivingBase.isDisplayNPC() =
        (this is EntityPlayer && isNPC() && displayNPCNameCheck(this.name)) ||
            (this is EntityVillager && this.maxHealth == 20.0f) || // Villager NPCs in the Village
            (this is EntityWitch && this.entityId <= 500) || // Alchemist NPC
            (this is EntityCow && this.entityId <= 500) || // Shania NPC (in Rift and Outside)
            (this is EntitySnowman && this.entityId <= 500) // Sherry NPC (in Jerry Island)

    fun createDisplayNPC(entity: EntityLivingBase): Boolean {
        val clickArmorStand = MobUtils.getArmorStandByRangeAll(entity, 1.5).firstOrNull { armorStand ->
            listOfClickArmorStand.contains(armorStand.name)
        } ?: return false
        val armorStand = MobUtils.getArmorStand(clickArmorStand, -1) ?: return false
        MobEvent.Spawn.DisplayNPC(MobFactories.displayNPC(entity, armorStand, clickArmorStand)).postAndCatch()
        return true
    }

    /** baseEntity must have passed the .isSkyBlockMob() function */
    internal fun createSkyblockEntity(baseEntity: EntityLivingBase): MobResult {
        val nextEntity = MobUtils.getNextEntity(baseEntity, 1) as? EntityLivingBase

        exceptions(baseEntity, nextEntity)?.let { return it }

        // Check if Late Stack
        nextEntity?.let {
            MobData.entityToMob[it]?.apply { internalAddEntity(baseEntity) }?.also { return MobResult.illegal }
        }

        // Stack up the mob
        var caughtSkyblockMob: Mob? = null
        val extraEntityList = generateSequence(nextEntity) {
            MobUtils.getNextEntity(it, 1) as? EntityLivingBase
        }.takeWhileInclusive { entity ->
            !(entity is EntityArmorStand && !entity.isDefaultValue()) && MobData.entityToMob[entity]?.also {
                caughtSkyblockMob = it
            }?.run { false } ?: true
        }.toList()
        stackedMobsException(baseEntity, extraEntityList)?.let { return it }

        // If Late Stack add all entities
        caughtSkyblockMob?.apply { internalAddEntity(extraEntityList.dropLast(1)) }?.also { return MobResult.illegal }

        val armorStand = extraEntityList.lastOrNull() as? EntityArmorStand ?: return MobResult.notYetFound

        if (armorStand.isDefaultValue()) return MobResult.notYetFound
        return createSkyblockMob(baseEntity, armorStand, extraEntityList.dropLast(1))?.let { MobResult.found(it) }
            ?: MobResult.notYetFound
    }

    private fun createSkyblockMob(
        baseEntity: EntityLivingBase,
        armorStand: EntityArmorStand,
        extraEntityList: List<EntityLivingBase>,
    ): Mob? =
        MobFactories.summon(baseEntity, armorStand, extraEntityList)
            ?: MobFactories.slayer(baseEntity, armorStand, extraEntityList)
            ?: MobFactories.boss(baseEntity, armorStand, extraEntityList)
            ?: if (DungeonAPI.inDungeon()) MobFactories.dungeon(
                baseEntity,
                armorStand,
                extraEntityList,
            ) else (
                MobFactories.basic(baseEntity, armorStand, extraEntityList)
                    ?: MobFactories.dojo(baseEntity, armorStand)
                )

    private fun noArmorStandMobs(baseEntity: EntityLivingBase): MobResult? = when {
        baseEntity is EntityBat -> createBat(baseEntity)

        baseEntity.isFarmMob() -> createFarmMobs(baseEntity)?.let { MobResult.found(it) }
        baseEntity is EntityDragon -> when (LorenzUtils.skyBlockIsland) {
            IslandType.CATACOMBS -> (8..16).map { MobUtils.getArmorStand(baseEntity, it) }
                .makeMobResult {
                    MobFactories.boss(baseEntity, it.first(), it.drop(1))
                }

            else -> MobResult.found(MobFactories.basic(baseEntity, baseEntity.cleanName()))
        }

        baseEntity is EntityGiantZombie && baseEntity.name == "Dinnerbone" -> MobResult.found(
            MobFactories.projectile(
                baseEntity,
                "Giant Sword",
            ),
        ) // Will false trigger if there is another Dinnerbone Giant
        baseEntity is EntityCaveSpider -> MobUtils.getArmorStand(baseEntity, -1)
            ?.takeIf { summonOwnerPattern.matches(it.cleanName()) }?.let {
                MobData.entityToMob[MobUtils.getNextEntity(baseEntity, -4)]?.internalAddEntity(baseEntity)
                    ?.let { MobResult.illegal }
            }

        baseEntity is EntityWither && baseEntity.invulTime == 800 -> MobResult.found(
            MobFactories.special(
                baseEntity,
                "Mini Wither",
            ),
        )

        baseEntity is EntityOtherPlayerMP && baseEntity.name == "Decoy " -> MobResult.found(
            MobFactories.special(
                baseEntity,
                "Decoy",
            ),
        )

        else -> null
    }

    private fun exceptions(baseEntity: EntityLivingBase, nextEntity: EntityLivingBase?): MobResult? {
        noArmorStandMobs(baseEntity)?.also { return it }
        val armorStand = nextEntity as? EntityArmorStand
        IslandExceptions.islandSpecificExceptions(baseEntity, armorStand, nextEntity)?.also { return it }

        if (armorStand == null) return null
        armorStandOnlyMobs(baseEntity, armorStand)?.also { return it }
        jerryPattern.matchMatcher(armorStand.cleanName()) {
            val level = this.group("level")?.toInt() ?: -1
            val owner = this.group("owner") ?: return@matchMatcher
            val name = this.group("name") ?: return@matchMatcher
            return MobResult.found(
                Mob(
                    baseEntity,
                    Mob.Type.BASIC,
                    armorStand,
                    name = name,
                    ownerName = owner,
                    levelOrTier = level,
                ),
            )
        }
        return when {
            (baseEntity is EntityPig || baseEntity is EntityHorse) && illegalEntitiesPattern.matches(armorStand.name) -> MobResult.illegal
            baseEntity is EntityGuardian && armorStand.cleanName()
                .matches("^\\d+".toRegex()) -> MobResult.illegal // Wierd Sea Guardian Ability
            else -> null
        }
    }

    private fun stackedMobsException(
        baseEntity: EntityLivingBase,
        extraEntityList: List<EntityLivingBase>,
    ): MobResult? =
        if (DungeonAPI.inDungeon()) {
            when {
                (baseEntity is EntityEnderman || baseEntity is EntityGiantZombie) &&
                    extraEntityList.lastOrNull()?.name == "§e﴾ §c§lLivid§r§r §a7M§c❤ §e﴿" -> MobResult.illegal // Livid Start Animation
                else -> null
            }
        } else when (LorenzUtils.skyBlockIsland) {
            IslandType.CRIMSON_ISLE -> when {
                else -> null
            }

            else -> null
        }

    private fun armorStandOnlyMobs(baseEntity: EntityLivingBase, armorStand: EntityArmorStand): MobResult? {
        if (baseEntity !is EntityZombie) return null
        when {
            illegalEntitiesPattern.matches(armorStand.name) -> return MobResult.illegal
            baseEntity.riddenByEntity is EntityPlayer && MobUtils.getArmorStand(baseEntity, 2)?.inventory?.get(4)
                ?.getSkullTexture() == RAT_SKULL -> return MobResult.illegal // Rat Morph
        }
        when (armorStand.inventory?.get(4)?.getSkullTexture()) {
            HELLWISP_TENTACLE_SKULL -> return MobResult.illegal // Hellwisp Tentacle
            RIFT_EYE_SKULL1 -> return MobResult.found(MobFactories.special(baseEntity, "Rift Teleport Eye", armorStand))
            RIFT_EYE_SKULL2 -> return MobResult.found(MobFactories.special(baseEntity, "Rift Teleport Eye", armorStand))
        }
        return null
    }

    fun EntityLivingBase.isFarmMob() =
        this is EntityAnimal && this.baseMaxHealth.derpy()
            .let { it == 50 || it == 20 || it == 130 } && LorenzUtils.skyBlockIsland != IslandType.PRIVATE_ISLAND

    private fun createFarmMobs(baseEntity: EntityLivingBase): Mob? = when (baseEntity) {
        is EntityMooshroom -> MobFactories.basic(baseEntity, "Farm Mooshroom")
        is EntityCow -> MobFactories.basic(baseEntity, "Farm Cow")
        is EntityPig -> MobFactories.basic(baseEntity, "Farm Pig")
        is EntityChicken -> MobFactories.basic(baseEntity, "Farm Chicken")
        is EntityRabbit -> MobFactories.basic(baseEntity, "Farm Rabbit")
        is EntitySheep -> MobFactories.basic(baseEntity, "Farm Sheep")
        else -> null
    }

    private fun createBat(baseEntity: EntityLivingBase): MobResult? = when (baseEntity.baseMaxHealth.derpy()) {
        5_000_000 -> MobResult.found(MobFactories.basic(baseEntity, "Cinderbat"))
        75_000 -> MobResult.found(MobFactories.basic(baseEntity, "Thorn Bat"))
        600 -> if (IslandType.GARDEN.isInIsland()) null else MobResult.notYetFound
        100 -> MobResult.found(
            MobFactories.basic(
                baseEntity,
                when {
                    DungeonAPI.inDungeon() -> "Dungeon Secret Bat"
                    IslandType.PRIVATE_ISLAND.isInIsland() -> "Private Island Bat"
                    else -> "Mega Bat"
                },
            ),
        )

        20 -> MobResult.found(MobFactories.projectile(baseEntity, "Vampire Mask Bat"))
        // 6 -> MobFactories.projectile(baseEntity, "Spirit Scepter Bat") // moved to Packet Event because 6 is default Health of Bats
        5 -> MobResult.found(MobFactories.special(baseEntity, "Bat Pinata"))
        else -> MobResult.notYetFound
    }

    internal fun EntityArmorStand?.makeMobResult(mob: (EntityArmorStand) -> Mob?) =
        this?.let { armor ->
            mob.invoke(armor)?.let { MobResult.found(it) } ?: MobResult.somethingWentWrong
        } ?: MobResult.notYetFound
}
