package at.hannibal2.skyhanni.features.misc.customscoreboard

import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.features.misc.customscoreboard.CustomScoreboardUtils.extractLobbyCode
import at.hannibal2.skyhanni.utils.LorenzUtils.nextAfter
import at.hannibal2.skyhanni.utils.TabListData

class InformationGetter {
    companion object {
        val dungeonClassList = listOf(
            "§e[M] ",
            "§a[M] ",
            "§c[M] ",
            "§e[A] ",
            "§a[A] ",
            "§c[A] ",
            "§e[B] ",
            "§a[B] ",
            "§c[B] ",
            "§e[H] ",
            "§a[H] ",
            "§c[H] ",
            "§e[T] ",
            "§a[T] ",
            "§c[T] ",
        )
    }

    fun getInformation() {
        val sidebarLines = ScoreboardData.sidebarLinesFormatted

        // Gets some values from the tablist
        for (line in TabListData.getTabList()) {
            when {
                line.startsWith(" Gems: §r§a") -> gems = line.removePrefix(" Gems: §r§a")
                line.startsWith(" Bank: §r§6") -> bank = line.removePrefix(" Bank: §r§6")
                line.startsWith(" §r§fMithril Powder: §r§2") -> mithrilPowder =
                    line.removePrefix(" §r§fMithril Powder: §r§2")

                line.startsWith(" §r§fGemstone Powder: §r§d") -> gemstonePowder =
                    line.removePrefix(" §r§fGemstone Powder: §r§d")
            }
        }

        // Gets some values from the scoreboard
        for (line in sidebarLines) {
            when {
                line.startsWith(" §7⏣ ") || line.startsWith(" §5ф ") -> location = line
                line.startsWith("Purse: §6") || line.startsWith("Piggy: §6") -> purse =
                    line.removePrefix("Purse: §6").removePrefix("Piggy: §6")

                line.startsWith("Motes: §5") -> motes = line.removePrefix("Motes: §5")
                line.startsWith("Motes: §d") -> motes = line.removePrefix("Motes: §d")
                extractLobbyCode(line) is String -> lobbyCode =
                    extractLobbyCode(line)?.substring(1) ?: "<hidden>" //removes first char (number of color code)
                line.startsWith("Heat: ") -> heat = line.removePrefix("Heat: ")
                line.startsWith("Bits: §b") -> bits = line.removePrefix("Bits: §b")
                line.startsWith("Copper: §c") -> copper = line.removePrefix("Copper: §c")
            }
        }

        if (sidebarLines.none { it.startsWith(("Heat: ")) }) {
            heat = "§c♨ 0"
        }
        if (sidebarLines.none { it.startsWith(("Bits: §b")) }) {
            bits = "0"
        }

        val knownLines = listOf<String>(
            "§7⏣ ",
            "§5ф ",
            "Purse: §6",
            "Piggy: §6",
            "Motes: §5",
            "Motes: §d",
            "Heat: ",
            "Bits: §b",
            "Copper: §c",
            "Spring",
            "Summer",
            "Autumn",
            "Winter",
            lobbyCode,
            "§ewww.hyp",
            "§ealpha.hyp",
            "§cServer closing: ",
            "§cServer closing§8",
            "Auto-closing in:",
            "Starting in:",
            "Keys: ",
            "Time Elapsed:",
            "§rCleared: ",
            "Cleared: ",
            "Instance Shutdow",
            "Time Elapsed: ",
            "§f§lWave: §c§l",
            "§fTokens: ",
            "Submerges In: §e",
            "§fObjective",
            "Objective",
            "§eJacob's Contest",
            "§6§lGOLD §fmedals",
            "§f§lSILVER §fmedals",
            "§c§lBRONZE §fmedals",
            "North Stars: §d",
            "Event Start: §a",
            "Next Wave: §a",
            "§cWave",
            "Magma Cubes Left",
            "Your Total Damag",
            "Your Cube Damage",
            "§6Spooky Festival§f",
            "§dNew Year Event",
            "§aTraveling Zoo",
            "§9Wind Compass",
            "Nearby Players:",
            "Event: ",
            "Zone: ",
            "Protector HP: §a",
            "Dragon HP: §a",
            "Your Damage: §c",
            "Essence: ",
            "§e☀",
            "§b☽",
            "☔",
            "⚡",
            "Ⓑ",
            "§a☀",
            "§7♲",
            "Slayer Quest",
            "§4Broodmother§7:",
            "§7Give Tasty Mithril to Don!",
            "Remaining: §a",
            "Your Tasty Mithr",
            "§3§lSolo",
            "§fRift Dimension",
            "§d᠅ §fGemstone",
            "§2᠅ §fMithril",
            "Revenant Horror",
            "Tarantula Broodfa",
            "Sven Packm",
            "Voidgloom Seraph",
            "Inferno Demo",
            "Combat XP",
            "Flight Duration:",
            "§a✌ §",
            "Points: ",
            "Challenge:",
            *dungeonClassList.toTypedArray(),
            "§cLocked",
            "§fCleanup§7:",
            "§6Year ",
            "§7Waiting for",
            "§7your vote...",
            "e: §e§b0%",
            "Pelts: §5",
            "Tracker Mob Location:",
            "§7Boss: §c",
            "§7Damage Soaked:",
            "§6Kill the Magmas:",
            "Time Left: §b",
            "Current Item:",
            "Effigies: ",
        )

        extraLines = sidebarLines.filter { line -> !knownLines.any { line.trim().contains(it) } }

        // filter empty lines
        extraLines = extraLines.filter { it.isNotBlank() }

        // remove objectives
        extraLines = extraLines.filter { sidebarLines.nextAfter("§fObjective") != it }
        val objectiveLine =
            ScoreboardData.sidebarLinesFormatted.firstOrNull { it.startsWith("Objective") } ?: "Objective"
        extraLines = extraLines.filter { sidebarLines.nextAfter(objectiveLine) != it }
        extraLines =
            extraLines.filter { sidebarLines.nextAfter(objectiveLine, 2) != it && !extraObjectiveLines.contains(it) }

        // remove wind compass
        extraLines = extraLines.filter { sidebarLines.nextAfter("§9Wind Compass") != it }

        // Remove jacobs contest
        for (i in 1..3)
            extraLines = extraLines.filter { sidebarLines.nextAfter("§eJacob's Contest", i) != it }

        // Remove slayer
        extraLines = extraLines.filter { sidebarLines.nextAfter("Slayer Quest", 1) != it }
        extraLines = extraLines.filter { sidebarLines.nextAfter("Slayer Quest", 2) != it }

        // remove voting lines
        val votedLine = sidebarLines.firstOrNull { it.startsWith("§6Year ") } ?: "§6Year "
        for (i in 1 until 6) {
            extraLines = extraLines.filter { sidebarLines.nextAfter(votedLine, i) != it }
        }

        // remove trapper mob location
        extraLines = extraLines.filter { sidebarLines.nextAfter("Tracker Mob Location:", 1) != it }

        // magma boss
        extraLines = extraLines.filter { sidebarLines.nextAfter("§7Damage Soaked:") != it }
        extraLines = extraLines.filter { sidebarLines.nextAfter("§6Kill the Magmas:") != it }

        // da
        extraLines = extraLines.filter { sidebarLines.nextAfter("Current Item:") != it }
    }
}
