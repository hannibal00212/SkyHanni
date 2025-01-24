package at.hannibal2.skyhanni.data.mob

import at.hannibal2.skyhanni.utils.EntityUtils.cleanName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand

object MobFactories {
    fun slayer(
        baseEntity: EntityLivingBase,
        armorStand: EntityArmorStand,
        extraEntityList: List<EntityLivingBase>
    ): Mob? =
        MobFilter.slayerNameFilter.matchMatcher(armorStand.cleanName()) {
            Mob(
                baseEntity = baseEntity,
                mobType = Mob.Type.SLAYER,
                armorStand = armorStand,
                name = this.group("name"),
                additionalEntities = extraEntityList,
                levelOrTier = this.group("tier").romanToDecimal()
            )
        }

    fun boss(
        baseEntity: EntityLivingBase,
        armorStand: EntityArmorStand,
        extraEntityList: List<EntityLivingBase> = emptyList(),
        overriddenName: String? = null
    ): Mob? =
        MobFilter.bossMobNameFilter.matchMatcher(armorStand.cleanName()) {
            Mob(
                baseEntity = baseEntity,
                mobType = Mob.Type.BOSS,
                armorStand = armorStand,
                name = overriddenName ?: this.group("name"),
                levelOrTier = group("level")?.takeIf { it.isNotEmpty() }?.toInt() ?: -1,
                additionalEntities = extraEntityList
            )
        }

    fun dungeon(
        baseEntity: EntityLivingBase,
        armorStand: EntityArmorStand,
        extraEntityList: List<EntityLivingBase> = emptyList()
    ): Mob? =
        MobFilter.dungeonNameFilter.matchMatcher(armorStand.cleanName()) {
            Mob(
                baseEntity = baseEntity,
                mobType = Mob.Type.DUNGEON,
                armorStand = armorStand,
                name = this.group("name"),
                additionalEntities = extraEntityList,
                hasStar = this.group("star")?.isNotEmpty() ?: false,
                attribute = this.group("attribute")?.takeIf { it.isNotEmpty() }
                    ?.let {
                        LorenzUtils.enumValueOfOrNull<MobFilter.DungeonAttribute>(it)
                    }
            )
        }

    fun basic(
        baseEntity: EntityLivingBase,
        armorStand: EntityArmorStand,
        extraEntityList: List<EntityLivingBase>? = null
    ): Mob? =
        MobFilter.mobNameFilter.findMatcher(armorStand.cleanName()) {
            Mob(
                baseEntity = baseEntity,
                mobType = Mob.Type.BASIC,
                armorStand = armorStand,
                name = this.group("name").removeCorruptedSuffix(
                    this.group("corrupted")?.isNotEmpty() ?: false
                ),
                additionalEntities = extraEntityList,
                levelOrTier = this.group("level")?.takeIf { it.isNotEmpty() }
                    ?.toInt() ?: -1
            )
        }

    fun basic(baseEntity: EntityLivingBase, name: String) =
        Mob(baseEntity = baseEntity, mobType = Mob.Type.BASIC, name = name)

    fun summon(
        baseEntity: EntityLivingBase,
        armorStand: EntityArmorStand,
        extraEntityList: List<EntityLivingBase>
    ): Mob? =
        MobFilter.summonFilter.matchMatcher(armorStand.cleanName()) {
            Mob(
                baseEntity = baseEntity,
                mobType = Mob.Type.SUMMON,
                armorStand = armorStand,
                name = this.group("name"),
                additionalEntities = extraEntityList,
                ownerName = this.group("owner")
            )
        }

    fun displayNPC(baseEntity: EntityLivingBase, armorStand: EntityArmorStand, clickArmorStand: EntityArmorStand): Mob =
        Mob(
            baseEntity = baseEntity,
            mobType = Mob.Type.DISPLAY_NPC,
            armorStand = armorStand,
            name = armorStand.cleanName(),
            additionalEntities = listOf(clickArmorStand)
        )

    fun player(baseEntity: EntityLivingBase): Mob = Mob(baseEntity, Mob.Type.PLAYER, name = baseEntity.name)
    fun projectile(baseEntity: EntityLivingBase, name: String): Mob =
        Mob(baseEntity = baseEntity, mobType = Mob.Type.PROJECTILE, name = name)

    fun special(baseEntity: EntityLivingBase, name: String, armorStand: EntityArmorStand? = null) =
        Mob(baseEntity = baseEntity, mobType = Mob.Type.SPECIAL, armorStand = armorStand, name = name)

    private fun String.removeCorruptedSuffix(case: Boolean) = if (case) this.dropLast(1) else this
    fun dojo(baseEntity: EntityLivingBase, armorStand: EntityArmorStand): Mob? =
        MobFilter.dojoFilter.matchMatcher(armorStand.cleanName()) {
            Mob(
                baseEntity = baseEntity,
                mobType = Mob.Type.SPECIAL,
                armorStand = armorStand,
                name = if (this.group("points")
                        ?.isNotEmpty() == true
                ) "Points: " + this.group("points") else this.group("empty").toString()
            )
        }

    fun minionMob(baseEntity: EntityLivingBase) =
        Mob(baseEntity, Mob.Type.SPECIAL, name = MobFilter.MINION_MOB_PREFIX + baseEntity.cleanName())

}
