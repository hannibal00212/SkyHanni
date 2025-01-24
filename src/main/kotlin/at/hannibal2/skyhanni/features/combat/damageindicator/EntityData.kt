package at.hannibal2.skyhanni.features.combat.damageindicator

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.entity.EntityLivingBase

class EntityData(
    val entity: EntityLivingBase,
    var ignoreBlocks: Boolean,
    var delayedStart: SimpleTimeMark?,
    val finalDungeonBoss: Boolean,
    val bossType: BossType,
    val damageCounter: DamageCounter = DamageCounter(),
    val foundTime: SimpleTimeMark,

    var lastHealth: Long = 0L,
    var healthText: String = "",
    var timeLastTick: Long = 0,
    var namePrefix: String = "",
    var nameSuffix: String = "",
    var nameAbove: String = "",
    var dead: Boolean = false,
    var firstDeath: Boolean = false, // TODO this defines if hp is very low, replace dead with this later
    var deathLocation: LorenzVec? = null,
) {

    val timeToKill by lazy {
        "§e" + foundTime.passedSince().format(TimeUnit.SECOND, showMilliSeconds = true)
    }
}
