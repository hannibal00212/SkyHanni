package at.hannibal2.skyhanni.features.skillprogress

import at.hannibal2.skyhanni.api.SkillAPI
import at.hannibal2.skyhanni.api.SkillAPI.activeSkill
import at.hannibal2.skyhanni.api.SkillAPI.defaultSkillCap
import at.hannibal2.skyhanni.api.SkillAPI.exactLevelingMap
import at.hannibal2.skyhanni.api.SkillAPI.levelingMap
import at.hannibal2.skyhanni.utils.Quad
import com.google.common.base.Splitter

object SkillUtil {

    val SPACE_SPLITTER = Splitter.on("  ").omitEmptyStrings().trimResults()
    const val XP_NEEDED_FOR_60 = 111_672_425L
    const val XP_NEEDED_FOR_50 = 55_172_425L

    fun getSkillInfo(skill: SkillType): SkillAPI.SkillInfo? {
        return SkillAPI.storage?.get(skill)
    }

    fun xpRequiredForLevel(desiredLevel: Int): Long {
        var totalXp = 0L
        val maxLevel = 60

        if (desiredLevel <= maxLevel) {
            for (level in 1..desiredLevel) {
                totalXp += levelingMap[level]?.toLong() ?: 0L
            }
        } else {
            val xpNeeded = XP_NEEDED_FOR_60

            totalXp += xpNeeded

            var level = 60
            var xpForNext = 7000000L + 600000L
            var slope = 600000L

            while (level < desiredLevel) {
                totalXp += xpForNext
                level++
                xpForNext += slope

                if (level % 10 == 0) slope *= 2
            }
        }

        return totalXp
    }

    fun getLevelExact(neededXp: Long): Int {
        return exactLevelingMap.getOrDefault(neededXp.toInt(), defaultSkillCap[activeSkill?.lowercaseName] ?: 60)
    }

    fun calculateLevelXp(level: Int): Double {
        return SkillAPI.levelArray.asSequence().take(level + 1).sumOf { it.toDouble() }
    }

    fun calculateSkillLevel(currentXp: Long, maxSkillCap: Int): Quad<Int, Long, Long, Long> {
        var xpCurrent = currentXp
        var level = 0
        val maxLevel = maxSkillCap.coerceAtMost(60)

        while (level < maxLevel && xpCurrent >= (levelingMap[level + 1]?.toLong() ?: Long.MAX_VALUE)) {
            val xpForNextLevel = levelingMap[level + 1]?.toLong() ?: Long.MAX_VALUE
            xpCurrent -= xpForNextLevel
            level++
        }

        var xpForNext = levelingMap[level + 1]?.toLong() ?: 0L
        var overflowXp = 0L

        if (level >= maxLevel) {
            val xpNeeded = if (maxSkillCap == 50) XP_NEEDED_FOR_50 else XP_NEEDED_FOR_60

            if (currentXp >= xpNeeded) {
                overflowXp = currentXp - xpNeeded

                xpCurrent = overflowXp
                var slope = 300000L
                var xpForCurr = 4000000L + slope

                while (xpCurrent >= xpForCurr && level < 60) {
                    level++
                    xpCurrent -= xpForCurr
                    xpForCurr += slope
                    if (level % 10 == 0) slope *= 2
                }

                if (level >= 60) {
                    slope = 600000L
                    xpForCurr = 7000000L + slope
                    while (xpCurrent >= xpForCurr) {
                        level++
                        xpCurrent -= xpForCurr
                        xpForCurr += slope
                        if (level % 10 == 0) slope *= 2
                    }
                }

                xpForNext = xpForCurr
            }
        }

        return Quad(level, xpCurrent, xpForNext, overflowXp)
    }

}
