package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.EliteBotAPI
import at.hannibal2.skyhanni.config.features.garden.ElitePestKillsDisplayConfig.PestDisplay
import at.hannibal2.skyhanni.data.jsonobjects.other.EliteLeaderboard
import at.hannibal2.skyhanni.data.jsonobjects.other.EliteProfileMember
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.garden.pests.PestKillEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.toDashlessUUID
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.json.fromJson
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object ElitePestKillsDisplay {

    private val config get() = SkyHanniMod.feature.garden.elitePestKillsDisplayConfig

    private val elitePestApiGson by lazy {
        BaseGsonBuilder.gson()
            .registerTypeAdapter(PestType::class.java, object : TypeAdapter<PestType>() {
                override fun write(out: JsonWriter, value: PestType) {}

                override fun read(reader: JsonReader): PestType {
                    val pest = reader.nextString()
                    return PestType.entries.firstOrNull { it.displayName.lowercase() == pest }
                        ?: error("No valid pest type '$pest'")
                }
            }.nullSafe())
            .create()
    }

    private val pestPlacements = mutableMapOf<PestType, Map<Int, Pair<String, Long>>>()
    private val pestRanks = mutableMapOf<PestType, Int>()
    private var currentPests = mutableMapOf<PestType, Long>()
    private var lastFetchedPest: PestType? = null

    private var hasPestBeenFetched = false
    private var commandLastUsed = SimpleTimeMark.farPast()
    private var lastPassed = SimpleTimeMark.farPast()

    private var lastKilledPest: PestType?
        get() = SkyHanniMod.feature.storage.lastPestKilled
        set(value) {
            SkyHanniMod.feature.storage.lastPestKilled = value
        }
    private var lastLeaderboardFetch = SimpleTimeMark.farPast()

    private var display = emptyList<Renderable>()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (GardenAPI.hideExtraGuis()) return
        if (!isEnabled()) return

        config.pos.renderRenderables(display, posLabel = "Pest Kills Display")
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.pest.afterChange {
            lastLeaderboardFetch = SimpleTimeMark.farPast()
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (EliteBotAPI.profileID == null) return

        if (lastLeaderboardFetch.passedSince() > EliteBotAPI.checkDuration) {
            lastLeaderboardFetch = SimpleTimeMark.now()
            val pest = if (config.pest.get() == PestDisplay.AUTO) {
                lastKilledPest ?: PestType.FLY
            } else {
                config.pest.get().pest
            }

            SkyHanniMod.coroutineScope.launch {
                pestPlacements.clear()
                pestRanks.clear()
                getRanksForPest(pest)
            }
        }
        updateDisplay()
    }

    @SubscribeEvent
    fun onProfileChange(event: ProfileJoinEvent) {
        resetData()
    }

    @SubscribeEvent
    fun onPestKill(event: PestKillEvent) {
        val pest = event.pestType
        if (!pestRanks.containsKey(pest) && lastKilledPest != pest) {
            SkyHanniMod.coroutineScope.launch {
                getRanksForPest(pest)
            }
        } else {
            lastFetchedPest = pest
        }
        lastKilledPest = pest
        currentPests.addOrPut(pest, 1)
    }

    fun reset() {
        if (EliteBotAPI.disableRefreshCommand) {
            ChatUtils.userError("§eCommand has been disabled")
        } else if (commandLastUsed.passedSince() < 1.minutes) {
            ChatUtils.userError("Command is on cooldown")
        } else {
            commandLastUsed = SimpleTimeMark.now()
            lastLeaderboardFetch = SimpleTimeMark.farPast()
            ChatUtils.chat("Pest Kills Display refreshing...")
        }
    }

    private fun resetData() {
        hasPestBeenFetched = false
        lastLeaderboardFetch = SimpleTimeMark.farPast()
        pestRanks.clear()
        pestPlacements.clear()
    }

    private fun updateDisplay() {
        if (lastFetchedPest == null) return
        if (pestPlacements.isEmpty()) return
        if (currentPests.isEmpty()) {
            display = listOf(Renderable.wrappedString("§cCheck if your Collections \nAPI is enabled!", width = 200))
            return
        }

        val placements = pestPlacements[lastFetchedPest] ?: return
        val pests = currentPests[lastFetchedPest] ?: 0

        val rankWhenLastFetched = pestRanks[lastFetchedPest]?.let { if (it == -1) 5001 else it } ?: return
        var rank: Int
        var nextRank: Int
        var personToBeat: String
        var amountToBeat: Long
        var difference: Long


        do {
            rank = pestRanks[lastFetchedPest]?.let { if (it == -1) 5001 else it } ?: return
            nextRank = rank - 1
            personToBeat = placements[nextRank]?.first ?: ""
            amountToBeat = placements[nextRank]?.second ?: 0
            difference = amountToBeat - pests


        } while (difference < 0 && (rankWhenLastFetched - nextRank) < placements.size)
        if (rankWhenLastFetched - nextRank > placements.size && placements.isNotEmpty() && !EliteBotAPI.disableFetchingWhenPassed && lastPassed.passedSince() > 1.minutes) {
            lastPassed = SimpleTimeMark.now()
            lastLeaderboardFetch = SimpleTimeMark.farPast()
        }

        val displayPosition = if (config.showPosition && rank != 5001) "§7[§b#$rank§7]" else ""

        val newDisplay = mutableListOf<Renderable>()
        newDisplay.add(
            Renderable.clickAndHover(
                "§6§l${lastFetchedPest?.displayName}: §e${pests.addSeparators()} $displayPosition",
                listOf("§eClick to open your Farming Profile."),
                onClick = {
                    OSUtils.openBrowser("https://elitebot.dev/@${LorenzUtils.getPlayerName()}/")
                    ChatUtils.chat("Opening Farming Profile of player §b${LorenzUtils.getPlayerName()}")
                }
            )
        )
        if (nextRank <= 0) {
            newDisplay.add(
                Renderable.string("§aNo players ahead of you!")
            )
        } else if (difference < 0) {
            newDisplay.add(
                Renderable.clickAndHover(
                    "§7You have passed §b#${nextRank.addSeparators()}",
                    listOf("§bClick to refresh."),
                    onClick = {
                        lastLeaderboardFetch = SimpleTimeMark.farPast()
                        ChatUtils.chat("Pest leaderboard updating...")
                    }
                )
            )
        } else if (config.showPersonToBeat) {
            newDisplay.add(
                Renderable.string("§e${difference.addSeparators()} §7behind §b#${nextRank.addSeparators()} §7(§b$personToBeat§7)")
            )
        } else {
            newDisplay.add(
                Renderable.string("§e${difference.addSeparators()} §7behind §b#${nextRank.addSeparators()}")
            )
        }
        if (config.showTimeUntilRefresh) {
            val time = EliteBotAPI.checkDuration - lastLeaderboardFetch.passedSince()
            val timedisplay = if (time.isNegative()) "Now" else time.format()

            newDisplay.add(
                Renderable.string("§7Refreshes in: §b$timedisplay")
            )
        }
        display = newDisplay
    }

    private fun getRanksForPest(pest: PestType) {
        if (EliteBotAPI.profileID == null) return
        val url =
            "https://api.elitebot.dev/Leaderboard/rank/${pest.displayName.lowercase()}/${LorenzUtils.getPlayerUuid()}/${EliteBotAPI.profileID!!.toDashlessUUID()}?includeUpcoming=true"
        val response = APIUtils.getJSONResponseAsElement(url)

        try {
            val data = elitePestApiGson.fromJson<EliteLeaderboard>(response)

            pestRanks[pest] = data.rank
            val placements = mutableMapOf<Int, Pair<String, Long>>()
            var rank = data.upcomingRank
            data.upcomingPlayers.forEach {
                //weight is amount
                placements[rank] = it.name to it.weight.toLong()
                rank--
            }
            pestPlacements[pest] = placements
            lastFetchedPest = pest

            if (data.amount != 0L) {
                currentPests[pest] = data.amount
            }


            if (!hasPestBeenFetched && data.amount == 0L) {
                hasPestBeenFetched = true
                getCurrentPest()
            }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e,
                "Error loading user pest kills leaderboard\n" +
                    "§eLoading the pest kills leaderboard data from elitebot.dev failed!\n" +
                    "§eYou can re-enter the garden to try to fix the problem.\n" +
                    "§cIf this message repeats, please report it on Discord!\n",
                "url" to url,
                "apiResponse" to response,
            )
        }
    }

    private fun getCurrentPest() {
        if (EliteBotAPI.profileID == null) return
        val url =
            "https://api.elitebot.dev/Profile/${LorenzUtils.getPlayerUuid()}/${EliteBotAPI.profileID!!.toDashlessUUID()}/"
        val response = APIUtils.getJSONResponseAsElement(url)

        try {
            val data = elitePestApiGson.fromJson<EliteProfileMember>(response)

            currentPests = data.farmingWeight.pests.mapValues { it.value.toLong() }.toMutableMap()

        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e,
                "Error loading user pest kills\n" +
                    "§eLoading the pest kills data from elitebot.dev failed!\n" +
                    "§eYou can re-enter the garden to try to fix the problem.\n" +
                    "§cIf this message repeats, please report it on Discord!\n",
                "url" to url,
                "apiResponse" to response,
            )
        }
    }

    private fun isEnabled() =
        config.display && LorenzUtils.inSkyBlock && (GardenAPI.inGarden() || config.showOutsideGarden)
}
