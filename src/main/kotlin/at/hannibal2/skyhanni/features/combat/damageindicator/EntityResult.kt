package at.hannibal2.skyhanni.features.combat.damageindicator

class EntityResult(
    val delayedStart: Long? = null,
    val ignoreBlocks: Boolean = false,
    val finalDungeonBoss: Boolean = false,
    val bossType: BossType,
)
