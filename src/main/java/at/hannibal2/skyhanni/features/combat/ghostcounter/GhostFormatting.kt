package at.hannibal2.skyhanni.features.combat.ghostcounter

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.nio.charset.StandardCharsets
import java.util.Base64

object GhostFormatting {

    private const val exportPrefix = "gc/"

    fun importFormat() {
        val base64: String = try {
            Toolkit.getDefaultToolkit().systemClipboard.getData(DataFlavor.stringFlavor) as String
        } catch (e: Exception) {
            return
        }

        if (base64.length <= exportPrefix.length) return
        val jsonString = try {
            val t = String(Base64.getDecoder().decode(base64.trim()))
            if (!t.startsWith(exportPrefix)) return
            t.substring(exportPrefix.length)
        } catch (e: IllegalArgumentException) {
            return
        }

        val list = try {
            JsonParser().parse(jsonString).asJsonArray
                .filter { it.isJsonPrimitive }
                .map { it.asString }
        } catch (e: Exception) {
            return
        }

        if (list.isNotEmpty()) {
            with(GhostCounter.config.textFormatting) {
                titleFormat = list[0]
                ghostKilledFormat = list[1]
                sorrowsFormat = list[2]
                ghostSinceSorrowFormat = list[3]
                ghostKillPerSorrowFormat = list[4]
                voltasFormat = list[5]
                plasmasFormat = list[6]
                ghostlyBootsFormat = list[7]
                bagOfCashFormat = list[8]
                avgMagicFindFormat = list[9]
                scavengerCoinsFormat = list[10]
                killComboFormat = list[11]
                highestKillComboFormat = list[12]
                skillXPGainFormat = list[13]
                with(xpHourFormatting) {
                    base = list[14]
                    noData = list[15]
                    paused = list[16]
                }
                with(bestiaryFormatting) {
                    base = list[17]
                    openMenu = list[18]
                    maxed = list[19]
                    showMax_progress = list[20]
                    progress = list[21]
                }
                with(killHourFormatting) {
                    base = list[22]
                    noData = list[23]
                    paused = list[24]
                }
                with(etaFormatting) {
                    base = list[25]
                    maxed = list[26]
                    noData = list[27]
                    progress = list[28]
                    time = list[29]
                }
                moneyHourFormat = list[30]
                moneyMadeFormat = list[31]
            }
        }
    }

    fun export() {
        val list = mutableListOf<String>()
        with(GhostCounter.config.textFormatting) {
            list.add(titleFormat)
            list.add(ghostKilledFormat)
            list.add(sorrowsFormat)
            list.add(ghostSinceSorrowFormat)
            list.add(ghostKillPerSorrowFormat)
            list.add(voltasFormat)
            list.add(plasmasFormat)
            list.add(ghostlyBootsFormat)
            list.add(bagOfCashFormat)
            list.add(avgMagicFindFormat)
            list.add(scavengerCoinsFormat)
            list.add(killComboFormat)
            list.add(highestKillComboFormat)
            list.add(skillXPGainFormat)
            with(xpHourFormatting) {
                list.add(base)
                list.add(noData)
                list.add(paused)
            }
            with(bestiaryFormatting) {
                list.add(base)
                list.add(openMenu)
                list.add(maxed)
                list.add(showMax_progress)
                list.add(progress)
            }
            with(killHourFormatting) {
                list.add(base)
                list.add(noData)
                list.add(paused)
            }
            with(etaFormatting) {
                list.add(base)
                list.add(maxed)
                list.add(noData)
                list.add(progress)
                list.add(time)
            }
            list.add(moneyHourFormat)
            list.add(moneyMadeFormat)
        }
        val jsonArray = JsonArray()
        for (l in list) {
            jsonArray.add(JsonPrimitive(l))
        }
        val base64 = Base64.getEncoder().encodeToString((exportPrefix + jsonArray).toByteArray(StandardCharsets.UTF_8))
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(base64), null)
    }

    fun reset() {
        with(GhostCounter.config.textFormatting) {
            titleFormat = "&6Ghost Counter"
            ghostKilledFormat = "  &6Ghosts Killed: &b%value% &7(%session%)"
            sorrowsFormat = "  &6Sorrow: &b%value% &7(%session%)"
            ghostSinceSorrowFormat = "  &6Ghost since Sorrow: &b%value%"
            ghostKillPerSorrowFormat = "  &6Ghosts/Sorrow: &b%value%"
            voltasFormat = "  &6Volta: &b%value% &7(%session%)"
            plasmasFormat = "  &6Plasmas: &b%value% &7(%session%)"
            ghostlyBootsFormat = "  &6Ghostly Boots: &b%value% &7(%session%)"
            bagOfCashFormat = "  &6Bag Of Cash: &b%value% &7(%session%)"
            avgMagicFindFormat = "  &6Avg Magic Find: &b%value%"
            scavengerCoinsFormat = "  &6Scavenger Coins: &b%value% &7(%session%)"
            killComboFormat = "  &6Kill Combo: &b%value%"
            highestKillComboFormat = "  &6Highest Kill Combo: &b%value% &7(%session%)"
            skillXPGainFormat = "  &6Skill XP Gained: &b%value% &7(%session%)"
            with(xpHourFormatting) {
                base = "  &6XP/h: &b%value%"
                noData = "&bN/A"
                paused = "&c(PAUSED)"
            }
            with(bestiaryFormatting) {
                base = "  &6Bestiary %currentLevel%->%nextLevel%: &b%value%"
                openMenu = "§cOpen Bestiary Menu !"
                maxed = "%currentKill% (&c&lMaxed!)"
                showMax_progress = "%currentKill%/100k (%percentNumber%%)"
                progress = "%currentKill%/%killNeeded%"
            }
            with(killHourFormatting) {
                base = "  &6Kill/h: &b%value%"
                noData = "§bN/A"
                paused = "&c(PAUSED)"
            }
            with(etaFormatting) {
                base = "  &6ETA: &b%value%"
                maxed = "§c§lMAXED!"
                noData = "§bN/A"
                progress = "§b%value%"
                time = "&6%days%%hours%%minutes%%seconds%"
            }
            moneyHourFormat = "  &6$/h: &b%value%"
            moneyMadeFormat = "  &6Money made: &b%value%"
        }
    }
}
