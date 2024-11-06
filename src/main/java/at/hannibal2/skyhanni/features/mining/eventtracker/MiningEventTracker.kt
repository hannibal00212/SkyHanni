package at.hannibal2.skyhanni.features.mining.eventtracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.BossbarData
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.BossbarUpdateEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.json.fromJson
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object MiningEventTracker {
    private val config get() = SkyHanniMod.feature.mining.miningEvent

    private val patternGroup = RepoPattern.group("mining.eventtracker")
    private val bossbarPassivePattern by patternGroup.pattern(
        "bossbar.passive",
        "§e§lPASSIVE EVENT (?<event>.+) §e§lRUNNING FOR §a§l(?<time>\\S+)§r"
    )
    private val bossbarActivePattern by patternGroup.pattern(
        "bossbar.active",
        "§e§lEVENT (?<event>.+) §e§lACTIVE IN (?<area>.+) §e§lfor §a§l(?<time>\\S+)§r"
    )

    // TODO add test messages
    private val eventStartedPattern by patternGroup.pattern(
        "started",
        "(?:§.)*\\s+(?:§.)+§l(?<event>.+) STARTED!"
    )
    private val eventEndedPattern by patternGroup.pattern(
        "ended",
        "(?:§.)*\\s+(?:§.)+§l(?<event>.+) ENDED!"
    )

    private val defaultCooldown = 1.minutes

    private var eventEndTime = SimpleTimeMark.farPast()
    private var lastSentEvent: MiningEventType? = null

    private var canRequestAt = SimpleTimeMark.farPast()

    var apiErrorCount = 0

    val apiError get() = apiErrorCount > 0

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        eventEndTime = SimpleTimeMark.farPast()
        lastSentEvent = null
    }

    @SubscribeEvent
    fun onBossbarChange(event: BossbarUpdateEvent) {
        if (!isMiningIsland()) return
        if (LorenzUtils.lastWorldSwitch.passedSince() < 5.seconds) return
        if (!eventEndTime.isInPast()) {
            return
        }

        bossbarPassivePattern.matchMatcher(event.bossbar) {
            sendData(group("event"), group("time"))
        }
        bossbarActivePattern.matchMatcher(event.bossbar) {
            sendData(group("event"), group("time"))
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isMiningIsland()) return

        eventStartedPattern.matchMatcher(event.message) {
            sendData(group("event"), null)
        }
        eventEndedPattern.matchMatcher(event.message) {
            lastSentEvent = null
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!config.enabled) return
        if (!LorenzUtils.inSkyBlock || (!config.outsideMining && !isMiningIsland())) return
        if (!canRequestAt.isInPast()) return

        fetchData()
    }

    private fun sendData(eventName: String, time: String?) {
        // we now ignore mineshaft events.
        if (IslandType.MINESHAFT.isInIsland()) return
        // TODO fix this via regex
        if (eventName == "SLAYER QUEST") return

        val eventType = MiningEventType.fromEventName(eventName) ?: run {
            if (!config.enabled) return
            ErrorManager.logErrorWithData(
                Exception("UnknownMiningEvent"), "Unknown mining event detected from string $eventName",
                "eventName" to eventName,
                "bossbar" to BossbarData.getBossbar(),
                "serverType" to LorenzUtils.skyBlockIsland,
                "fromChat" to (time == null)
            )
            return
        }

        if (!IslandType.DWARVEN_MINES.isInIsland() && eventType.dwarvenSpecific) return

        if (lastSentEvent == eventType) return
        lastSentEvent = eventType

        val timeRemaining = if (time == null) {
            eventType.defaultLength
        } else {
            TimeUtils.getDuration(time)
        }
        eventEndTime = SimpleTimeMark.now() + timeRemaining

        val serverId = HypixelData.serverId ?: return

        val miningEventData = MiningEventDataSend(
            LorenzUtils.skyBlockIsland,
            serverId,
            eventType,
            timeRemaining.inWholeMilliseconds,
            LorenzUtils.getPlayerUuid()
        )
        val miningEventJson = ConfigManager.gson.toJson(miningEventData)

        if (apiError) {
            ChatUtils.debug("blocked sending mining event data: api error")
            return
        }
        SkyHanniMod.coroutineScope.launch {
            sendData(miningEventJson)
        }
    }

    private fun sendData(json: String) {
        val response = try {
            APIUtils.postJSON("https://api.soopy.dev/skyblock/chevents/set", json)
        } catch (e: IOException) {
            if (LorenzUtils.debug) {
                ErrorManager.logErrorWithData(
                    e, "Sending mining event data was unsuccessful",
                    "sentData" to json
                )
            }
            return
        }
        if (!response.success) return

        val formattedResponse = ConfigManager.gson.fromJson<MiningEventDataReceive>(response.data)
        if (!formattedResponse.success) {
            if (!config.enabled) return
            ErrorManager.logErrorWithData(
                Exception("PostFailure"), "Sending mining event data was unsuccessful",
                "cause" to formattedResponse.cause,
                "sentData" to json
            )
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (apiError) {
            canRequestAt = SimpleTimeMark.now()
        }
    }

    private fun fetchData() {
        canRequestAt = SimpleTimeMark.now() + defaultCooldown
        SkyHanniMod.coroutineScope.launch {
            val data = try {
                APIUtils.getJSONResponse("https://api.soopy.dev/skyblock/chevents/get")
            } catch (e: Exception) {
                apiErrorCount++
                canRequestAt = SimpleTimeMark.now() + 20.minutes
                if (LorenzUtils.debug) {
                    ErrorManager.logErrorWithData(
                        e,
                        "Failed to load Mining Event data!",
                    )
                }
                return@launch
            }
            val miningEventData = ConfigManager.gson.fromJson(data, MiningEventDataReceive::class.java)

            if (!miningEventData.success) {
                if (data.toString() == "{}") {
                    ChatUtils.chat(
                        "§cFailed loading Mining Event data!\n" +
                            "Please wait until the server problem fixes itself! There is nothing else to do at the moment."
                    )
                } else {
                    ErrorManager.logErrorWithData(
                        Exception("miningEventData.success = false"),
                        "Failed to load Mining Event data!",
                        "cause" to miningEventData.cause,
                        "recievedData" to data
                    )
                }
                return@launch
            }
            apiErrorCount = 0

            canRequestAt = SimpleTimeMark.now() + miningEventData.data.updateIn.milliseconds

            MiningEventDisplay.updateData(miningEventData.data)
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(29, "mining.miningEvent.showType") { element ->
            if (element.asString == "BOTH") JsonPrimitive("ALL") else element
        }
    }

    // ignoring mineshaft here is intentional
    fun isMiningIsland() = IslandType.DWARVEN_MINES.isInIsland() || IslandType.CRYSTAL_HOLLOWS.isInIsland()
}
