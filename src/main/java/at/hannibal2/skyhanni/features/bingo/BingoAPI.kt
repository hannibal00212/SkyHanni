package at.hannibal2.skyhanni.features.bingo

import at.hannibal2.skyhanni.config.storage.PlayerSpecificStorage.BingoSession
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.BingoData
import at.hannibal2.skyhanni.data.jsonobjects.repo.BingoJson
import at.hannibal2.skyhanni.data.jsonobjects.repo.BingoRanksJson
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.bingo.card.goals.BingoGoal
import at.hannibal2.skyhanni.features.bingo.card.goals.GoalType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@SkyHanniModule
object BingoAPI {

    private var ranks = mapOf<String, Int>()
    private var data: Map<String, BingoData> = emptyMap()

    val bingoGoals get() = bingoStorage.goals
    val personalGoals get() = bingoGoals.values.filter { it.type == GoalType.PERSONAL }
    val communityGoals get() = bingoGoals.values.filter { it.type == GoalType.COMMUNITY }
    var lastBingoCardOpenTime = SimpleTimeMark.farPast()

    private val detectionPattern by RepoPattern.pattern(
        "bingo.detection.scoreboard",
        " §.Ⓑ §.Bingo"
    )

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Bingo Card")

        if (!LorenzUtils.isBingoProfile) {
            event.addIrrelevant("not on bingo")
            return
        }

        event.addData {
            add("bingoGoals: ${bingoGoals.size}")
            for (bingoGoal in bingoGoals) {
                val goal = bingoGoal.value
                add("  type: '${goal.type}'")
                add("  displayName: '${goal.displayName}'")
                add("  description: '${goal.description}'")
                add("  guide: '${goal.guide}'")
                add("  done: '${goal.done}'")
                add("  highlight: '${goal.highlight}'")
                add("  communityGoalPercentage: '${goal.communtyGoalPercentage}'")
                val hiddenGoalData = goal.hiddenGoalData
                add("  hiddenGoalData")
                add("    unknownTip: '${hiddenGoalData.unknownTip}'")
                add("    nextHintTime: '${hiddenGoalData.nextHintTime}'")
                add("    tipNote: '${hiddenGoalData.tipNote}'")
                add(" ")

            }
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        ranks = event.getConstant<BingoRanksJson>("BingoRanks").ranks
        data = event.getConstant<BingoJson>("Bingo").bingoTips
    }

    fun getRankFromScoreboard(text: String) = if (detectionPattern.matches(text)) getRank(text) else null

    fun getIconFromScoreboard(text: String) = getRankFromScoreboard(text)?.let { getIcon(it) }

    fun getRank(text: String) = ranks.entries.find { text.contains(it.key) }?.value

    fun getIcon(searchRank: Int) = ranks.entries.find { it.value == searchRank }?.key

    // We added the suffix (Community Goal) so that older skyhanni versions don't crash with the new repo data.
    fun getData(itemName: String) =
        data.filter { itemName.startsWith(it.key.split(" (Community Goal)")[0]) }.values.firstOrNull()

    fun BingoGoal.getData(): BingoData? = if (type == GoalType.COMMUNITY) {
        getData(displayName)
    } else {
        data[displayName]
    }

    val bingoStorage: BingoSession by lazy {
        val playerSpecific = ProfileStorageData.playerSpecific ?: error("playerSpecific is null")
        playerSpecific.bingoSessions.getOrPut(getStartOfMonthInMillis()) { BingoSession() }
    }

    private fun getStartOfMonthInMillis() = OffsetDateTime.of(
        TimeUtils.getCurrentLocalDate().plusDays(5).withDayOfMonth(1),
        LocalTime.MIDNIGHT, ZoneOffset.UTC
    ).toEpochSecond()

    fun getCommunityPercentageColor(percentage: Double): String = when {
        percentage < 0.01 -> "§a"
        percentage < 0.05 -> "§e"
        percentage < 0.1 -> "§6"
        percentage < 0.25 -> "§6"

        else -> "§c"
    } + LorenzUtils.formatPercentage(percentage)

    fun getBingoIcon(rank: Int): String {
        val rankIcon = getIcon(rank).orEmpty()
        return if (rank != -1) {
            "$rankIcon $rank"
        } else {
            rankIcon
        }
    }
}
