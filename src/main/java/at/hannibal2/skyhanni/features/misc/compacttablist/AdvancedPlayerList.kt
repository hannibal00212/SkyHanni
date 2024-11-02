package at.hannibal2.skyhanni.features.misc.compacttablist

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.misc.compacttablist.AdvancedPlayerListConfig.PlayerSortEntry
import at.hannibal2.skyhanni.data.FriendAPI
import at.hannibal2.skyhanni.data.GuildAPI
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.misc.ContributorManager
import at.hannibal2.skyhanni.features.misc.MarkedPlayerManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Matcher
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object AdvancedPlayerList {

    val tabPlayerData = mutableMapOf<String, PlayerData>()

    private val config get() = SkyHanniMod.feature.gui.compactTabList.advancedPlayerList

    private val levelPattern by RepoPattern.pattern(
        "misc.compacttablist.advanced.level",
        ".*\\[(?<level>.*)] §r(?<name>.*)",
    )

    private var playerData = mutableMapOf<String, PlayerData>()

    fun createTabLine(text: String, type: TabStringType) = playerData[text]?.let {
        TabLine(text, type, createCustomName(it))
    } ?: TabLine(text, type)

    fun newSorting(original: List<String>): List<String> {
        if (LorenzUtils.inKuudraFight) return original
        if (DungeonAPI.inDungeon()) return original

        if (ignoreCustomTabList()) return original
        val newList = mutableListOf<String>()
        val currentData = mutableMapOf<String, PlayerData>()
        newList.add(original.first())

        var extraTitles = 0
        var i = 0

        for (line in original) {
            i++
            if (i == 1) continue
            if (line.isEmpty() || line.contains("Server Info")) break
            if (line == "               §r§3§lInfo") break
            if (line.contains("§r§a§lPlayers")) {
                extraTitles++
                continue
            }
            val playerData: PlayerData? = levelPattern.matchMatcher(line) {
                val levelText = group("level")
                val removeColor = levelText.removeColor()
                try {
                    val sbLevel = removeColor.toInt()
                    readPlayerData(sbLevel, levelText, line)
                } catch (e: NumberFormatException) {
                    ErrorManager.logErrorWithData(
                        e, "Advanced Player List failed to parse username",
                        "line" to line,
                        "i" to i,
                        "original" to original,
                    )
                    null
                }
            }
            playerData?.let {
                val name = it.name
                if (name != "?") {
                    tabPlayerData[name] = it
                }
                currentData[line] = it
            }
        }
        playerData = currentData
        val prepare = currentData.entries

        val sorted = when (config.playerSortOrder) {

            // SB Level
            PlayerSortEntry.SB_LEVEL -> prepare.sortedBy { -(it.value.sbLevel) }

            // Name (Abc)
            PlayerSortEntry.NAME -> prepare.sortedBy {
                it.value.name.lowercase().replace("_", "")
            }

            // Ironman/Bingo
            PlayerSortEntry.PROFILE_TYPE -> prepare.sortedBy {
                -if (it.value.ironman) 10 else it.value.bingoLevel ?: -1
            }

            // Party/Friends/Guild First
            PlayerSortEntry.SOCIAL_STATUS -> prepare.sortedBy { -getSocialIcon(it.value.name).score }

            // Random
            PlayerSortEntry.RANDOM -> prepare.sortedBy { getRandomOrder(it.value.name) }

            // Rank (Default)
            else -> prepare
        }

        var newPlayerList = sorted.map { it.key }.toMutableList()
        if (config.reverseSort) {
            newPlayerList = newPlayerList.reversed().toMutableList()
        }
        if (extraTitles > 0 && newPlayerList.size >= 19) {
            newPlayerList.add(19, original.first())
        }
        newList.addAll(newPlayerList)

        val rest = original.drop(playerData.size + extraTitles + 1)
        newList.addAll(rest)
        return newList
    }

    private fun Matcher.readPlayerData(
        sbLevel: Int,
        levelText: String,
        line: String,
    ): PlayerData {
        val playerData = PlayerData(sbLevel)
        var index = 0
        val fullName = group("name")
        if (fullName.contains("[")) index++
        val name = fullName.split(" ")
        val coloredName = name[index]
        if (index == 1) {
            playerData.coloredName = name[0] + " " + coloredName
        } else {
            playerData.coloredName = coloredName
        }
        playerData.name = coloredName.removeColor()
        playerData.levelText = levelText
        index++
        if (name.size > index) {
            var nameSuffix = name.drop(index).joinToString(" ")
            if (nameSuffix.contains("♲")) {
                playerData.ironman = true
            } else {
                playerData.bingoLevel = BingoAPI.getRank(line)
            }
            if (IslandType.CRIMSON_ISLE.isInIsland()) {
                playerData.faction = if (line.contains("§c⚒")) {
                    nameSuffix = nameSuffix.replace("§c⚒", "")
                    CrimsonIsleFaction.BARBARIAN
                } else if (line.contains("§5ቾ")) {
                    nameSuffix = nameSuffix.replace("§5ቾ", "")
                    CrimsonIsleFaction.MAGE
                } else {
                    CrimsonIsleFaction.NONE
                }
            }
            playerData.nameSuffix = nameSuffix
        } else {
            playerData.nameSuffix = ""
        }
        return playerData
    }

    fun ignoreCustomTabList(): Boolean {
        val denyKeyPressed = SkyHanniMod.feature.dev.debug.bypassAdvancedPlayerTabList.isKeyHeld()
        return denyKeyPressed || !SkyHanniDebugsAndTests.globalRender
    }

    private fun createCustomName(data: PlayerData): String {

        val playerName = if (config.useLevelColorForName) {
            val c = data.levelText[3]
            "§$c" + data.name
        } else if (config.hideRankColor) "§b" + data.name else data.coloredName

        val level = if (!config.hideLevel) {
            if (config.hideLevelBrackets) data.levelText else "§8[${data.levelText}§8]"
        } else ""

        var suffix = if (config.hideEmblem) {
            if (data.ironman) "§7♲" else data.bingoLevel?.let {
                BingoAPI.getBingoIcon(if (config.showBingoRankNumber) it else -1)
            }.orEmpty()
        } else data.nameSuffix

        if (config.markSpecialPersons) {
            suffix += " ${getSocialIcon(data.name).icon()}"
        }
        ContributorManager.getSuffix(data.name)?.let {
            suffix += " $it"
        }

        if (IslandType.CRIMSON_ISLE.isInIsland() && !config.hideFactions) {
            suffix += data.faction.icon.orEmpty()
        }

        return "$level $playerName ${suffix.trim()}"
    }

    private var randomOrderCache = TimeLimitedCache<String, Int>(20.minutes)

    private fun getRandomOrder(name: String) = randomOrderCache.getOrPut(name) {
        (Random.nextDouble() * 500).toInt()
    }

    private fun getSocialIcon(name: String) = when {
        LorenzUtils.getPlayerName() == name -> SocialIcon.ME
        MarkedPlayerManager.isMarkedPlayer(name) -> SocialIcon.MARKED
        PartyAPI.partyMembers.contains(name) -> SocialIcon.PARTY
        FriendAPI.getAllFriends().any { it.name.equals(name, ignoreCase = true) } -> SocialIcon.FRIEND
        GuildAPI.isInGuild(name) -> SocialIcon.GUILD
        else -> SocialIcon.OTHER
    }

    class PlayerData(val sbLevel: Int) {

        var name: String = "?"
        var coloredName: String = "?"
        var nameSuffix: String = "?"
        var levelText: String = "?"
        var ironman: Boolean = false
        var bingoLevel: Int? = null
        var faction: CrimsonIsleFaction = CrimsonIsleFaction.NONE
    }

    enum class CrimsonIsleFaction(val icon: String?) {
        BARBARIAN(" §c⚒"),
        MAGE(" §5ቾ"),
        NONE(null)
    }

    enum class SocialIcon(val icon: () -> String, val score: Int) {
        ME("", 10),
        MARKED({ "${MarkedPlayerManager.config.chatColor.getChatColor()}§lMARKED" }, 8),
        PARTY("§9§lP", 5),
        FRIEND("§d§lF", 4),
        GUILD("§2§lG", 3),
        OTHER("", 1)
        ;

        constructor(icon: String, score: Int) : this({ icon }, score)
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(15, "misc.compactTabList.advancedPlayerList.playerSortOrder") { element ->
            ConfigUtils.migrateIntToEnum(element, PlayerSortEntry::class.java)
        }
    }
}
