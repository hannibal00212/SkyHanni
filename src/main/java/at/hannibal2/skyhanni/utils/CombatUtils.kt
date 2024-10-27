package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostCounter
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostData
import at.hannibal2.skyhanni.mixins.transformers.MixinXPInformation
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpUtils
import io.github.moulberry.notenoughupdates.util.XPInformation

object CombatUtils {

    private var lastTotalXp = -1f
    private val xpGainQueue = mutableListOf<Float>()
    private var xpGainTimer = 0
    var lastUpdate: Long = -1
    private var skillInfo: XPInformation.SkillInfo? = null
    private var skillInfoLast: XPInformation.SkillInfo? = null
    private const val SKILL_TYPE = "Combat"
    var xpGainHourLast = -1f
    var xpGainHour = -1f
    var isKilling = false
    private var lastTotalKill = -1
    private val killGainQueue = mutableListOf<Int>()
    var lastKillUpdate: Long = -1
    var killGainHourLast = -1
    var killGainHour = -1
    private var gainTimer = 0

    // Todo: Why do we have two isKilling variables?
    @Suppress("ObjectPropertyNaming")
    var _isKilling = false

    private fun getSkillInfo(xpInformation: XPInformation.SkillInfo?): Float {
        return try {
            val a = xpInformation as? MixinXPInformation
            a!!.getTotalXp().toFloat()
        } catch (e: Error) {
            val xpInfo = xpInformation ?: return -1f
            xpInfo.totalXp
        }
    }

    /**
     * Taken from NotEnoughUpdates
     */
    fun calculateXP() {
        lastUpdate = System.currentTimeMillis()
        xpGainHourLast = xpGainHour
        skillInfoLast = skillInfo
        skillInfo = XPInformation.getInstance().getSkillInfo(SKILL_TYPE) ?: return
        val totalXp = getSkillInfo(skillInfo)
        if (lastTotalXp > 0) {
            val delta: Float = totalXp - lastTotalXp

            when {
                delta > 0 && delta < 1000 -> {
                    xpGainTimer = GhostCounter.config.pauseTimer
                    xpGainQueue.add(0, delta)
                    calculateXPHour()
                }

                xpGainTimer > 0 -> {
                    xpGainTimer--
                    xpGainQueue.add(0, 0f)
                    calculateXPHour()
                }

                delta <= 0 -> {
                    isKilling = false
                }
            }
        }
        lastTotalXp = totalXp
    }

    private fun calculateXPHour() {
        while (xpGainQueue.size > 30) {
            xpGainQueue.removeLast()
        }
        var totalGain = 0f
        for (f in xpGainQueue) totalGain += f
        xpGainHour = totalGain * (60 * 60) / xpGainQueue.size
        isKilling = true
    }

    fun calculateETA() {
        lastKillUpdate = System.currentTimeMillis()
        killGainHourLast = killGainHour

        GhostCounter.storage?.bestiaryNextLevel?.toInt()?.let { nextLevel ->
            GhostCounter.storage?.bestiaryCurrentKill?.toInt()?.let { kill ->
                val sum = GhostData.bestiaryData.filterKeys { it <= nextLevel - 1 }.values.sum()
                val cKill = sum + kill
                val totalKill = if (GhostCounter.config.showMax) GhostCounter.bestiaryCurrentKill else cKill

                if (lastTotalKill > 0) {
                    val delta: Int = totalKill - lastTotalKill
                    if (delta in 1..10 || gainTimer > 0) {
                        gainTimer = maxOf(gainTimer - 1, 0)
                        killGainQueue.add(0, delta)
                        while (killGainQueue.size > 30) {
                            killGainQueue.removeLast()
                        }

                        val totalGain = killGainQueue.sum()
                        killGainHour = totalGain * 3600 / killGainQueue.size
                        _isKilling = true
                    } else if (delta <= 0) {
                        _isKilling = false
                    }
                }
                lastTotalKill = totalKill
            }
        }
    }

    /**
     * Taken from NotEnoughUpdates
     */
    fun interp(now: Float, last: Float, lastUpdate: Long): Float {
        var interp = now
        if (last >= 0 && last != now) {
            var factor = (System.currentTimeMillis() - lastUpdate) / 1000f
            factor = LerpUtils.clampZeroOne(factor)
            interp = last + (now - last) * factor
        }
        return interp
    }
}
