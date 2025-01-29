package at.hannibal2.skyhanni.features.slayer.blaze

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.BossHealthChangeEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.combat.damageindicator.DamageIndicatorManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BlazeSlayerFirePitsWarning {

    private val config get() = SkyHanniMod.feature.slayer.blazes

    private var lastFirePitsWarning = SimpleTimeMark.farPast()

    private fun fireFirePits() {
        LorenzUtils.sendTitle("§cFire Pits!", 2.seconds)
        lastFirePitsWarning = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(10)) return

        if (lastFirePitsWarning.passedSince() < 2.seconds) {
            SoundUtils.createSound("random.orb", 0.8f).playSound()
        }
    }

    @HandleEvent
    fun onBossHealthChange(event: BossHealthChangeEvent) {
        if (!isEnabled()) return
        val entityData = event.entityData

        val health = event.health
        val maxHealth = event.maxHealth
        val lastHealth = event.lastHealth

        val percentHealth = maxHealth * 0.33
        if (health < percentHealth && lastHealth > percentHealth) {
            when (entityData.bossType) {
                BossType.SLAYER_BLAZE_3,
                BossType.SLAYER_BLAZE_4,
                -> {
                    fireFirePits()
                }

                else -> {}
            }
        }
    }

    private fun isEnabled() =
        LorenzUtils.inSkyBlock && config.firePitsWarning && DamageIndicatorManager.isBossSpawned(
            BossType.SLAYER_BLAZE_3,
            BossType.SLAYER_BLAZE_4,
            BossType.SLAYER_BLAZE_QUAZII_3,
            BossType.SLAYER_BLAZE_QUAZII_4,
            BossType.SLAYER_BLAZE_TYPHOEUS_3,
            BossType.SLAYER_BLAZE_TYPHOEUS_4,
        )

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "slayer.firePitsWarning", "slayer.blazes.firePitsWarning")
    }
}
