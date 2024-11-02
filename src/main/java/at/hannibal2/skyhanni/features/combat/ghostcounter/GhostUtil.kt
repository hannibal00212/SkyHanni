package at.hannibal2.skyhanni.features.combat.ghostcounter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import java.io.FileReader

object GhostUtil {

    fun reset() {
        for (opt in GhostData.Option.entries) {
            opt.set(0.0)
            opt.set(0.0, true)
        }
        GhostCounter.storage?.totalMF = 0.0
        GhostCounter.update()
    }

    fun isUsingCTGhostCounter(): Boolean {
        return GhostCounter.ghostCounterV3File.exists() && GhostCounter.ghostCounterV3File.isFile
    }

    fun prettyTime(millis: Long): Map<String, String> {
        val seconds = millis / 1000 % 60
        val minutes = millis / 1000 / 60 % 60
        val hours = millis / 1000 / 60 / 60 % 24
        val days = millis / 1000 / 60 / 60 / 24
        return buildMap {
            when {
                millis < 0 -> {
                    clear()
                }

                minutes == 0L && hours == 0L && days == 0L -> {
                    put("seconds", seconds.toString())
                }

                hours == 0L && days == 0L -> {
                    put("seconds", seconds.toString())
                    put("minutes", minutes.toString())
                }

                days == 0L -> {
                    put("seconds", seconds.toString())
                    put("minutes", minutes.toString())
                    put("hours", hours.toString())
                }

                else -> {
                    put("seconds", seconds.toString())
                    put("minutes", minutes.toString())
                    put("hours", hours.toString())
                    put("days", days.toString())
                }
            }
        }
    }

    fun importCTGhostCounterData() {
        val c = ProfileStorageData.profileSpecific?.ghostCounter ?: return
        if (isUsingCTGhostCounter()) {
            if (c.ctDataImported) {
                ChatUtils.userError("You already imported GhostCounterV3 data!")
                return
            }
            val json = ConfigManager.gson.fromJson(
                FileReader(GhostCounter.ghostCounterV3File),
                com.google.gson.JsonObject::class.java,
            )
            GhostData.Option.GHOSTSINCESORROW.add(json["ghostsSinceSorrow"].asDouble)
            GhostData.Option.SORROWCOUNT.add(json["sorrowCount"].asDouble)
            GhostData.Option.BAGOFCASH.add(json["BagOfCashCount"].asDouble)
            GhostData.Option.PLASMACOUNT.add(json["PlasmaCount"].asDouble)
            GhostData.Option.VOLTACOUNT.add(json["VoltaCount"].asDouble)
            GhostData.Option.GHOSTLYBOOTS.add(json["GhostlyBootsCount"].asDouble)
            GhostData.Option.KILLS.add(json["ghostsKilled"].asDouble)
            GhostCounter.storage?.totalMF = GhostCounter.storage?.totalMF?.plus(json["TotalMF"].asDouble)
                ?: json["TotalMF"].asDouble
            GhostData.Option.TOTALDROPS.add(json["TotalDrops"].asDouble)
            c.ctDataImported = true
            ChatUtils.chat("§aImported data successfully!")
        } else
            ErrorManager.skyHanniError("GhostCounterV3 ChatTriggers module not found!")
    }

    fun String.formatText(option: GhostData.Option) = formatText(option.getInt(), option.getInt(true))

    fun String.formatText(value: Int, session: Int = -1) = this.replace("%value%", value.addSeparators())
        .replace("%session%", session.addSeparators())
        .replace("&", "§")

    fun String.formatText(t: String) = this.replace("%value%", t).replace("&", "§")

    fun String.preFormat(t: String, level: Int, nextLevel: Int) = if (nextLevel == 26) {
        replace("%value%", t).replace("%display%", "25")
    } else {
        this.replace("%value%", t)
            .replace(
                "%display%",
                "$level->${if (SkyHanniMod.feature.combat.ghostCounter.showMax) "25" else nextLevel}",
            )
    }

    fun String.formatText(value: Double, session: Double) = this.replace("%value%", value.roundTo(2).addSeparators())
        .replace("%session%", session.roundTo(2).addSeparators())
        .replace("&", "§")

    fun String.formatBestiary(currentKill: Int, killNeeded: Int): String {
        val bestiaryNextLevel = GhostCounter.storage?.bestiaryNextLevel
        val currentLevel =
            bestiaryNextLevel?.let { if (it.toInt() < 0) "25" else "${it.toInt() - 1}" } ?: "§cNo Bestiary Level Data!"
        val nextLevel = bestiaryNextLevel?.let { if (GhostCounter.config.showMax) "25" else "${it.toInt()}" }
            ?: "§cNo Bestiary Level data!"

        return this.replace(
            "%currentKill%",
            if (GhostCounter.config.showMax) GhostCounter.bestiaryCurrentKill.addSeparators() else currentKill.addSeparators(),
        )
            .replace("%percentNumber%", percent(GhostCounter.bestiaryCurrentKill.toDouble()))
            .replace("%killNeeded%", killNeeded.shortFormat())
            .replace("%currentLevel%", currentLevel)
            .replace("%nextLevel%", nextLevel)
            .replace("&", "§")
    }

    private fun percent(number: Double) =
        100.0.coerceAtMost(((number / 100_000) * 100).roundTo(4)).toString()
}
