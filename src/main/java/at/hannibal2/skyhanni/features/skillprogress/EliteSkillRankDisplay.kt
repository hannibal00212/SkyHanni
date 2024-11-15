package at.hannibal2.skyhanni.features.skillprogress

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.EliteBotAPI
import at.hannibal2.skyhanni.api.SkillAPI
import at.hannibal2.skyhanni.config.features.skillprogress.EliteSkillsDisplayConfig.SkillDisplay
import at.hannibal2.skyhanni.data.jsonobjects.other.EliteLeaderboard
import at.hannibal2.skyhanni.data.jsonobjects.other.EliteSkillGraphEntry
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.SkillExpGainEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.StringUtils.toDashlessUUID
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.json.fromJson
import at.hannibal2.skyhanni.utils.renderables.Renderable
import kotlinx.coroutines.launch
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object EliteSkillRankDisplay {
    private val config get() = SkyHanniMod.feature.skillProgress.rankDisplay

    private val eliteCollectionApiGson by lazy {
        BaseGsonBuilder.gson()
            .create()
    }

    private val skillPlacements = mutableMapOf<String, Map<Int, Pair<String, Long>>>()
    private val skillRanks = mutableMapOf<String, Int>()
    private var currentSkills = mutableMapOf<String, Long>()

    private var lastSkillGained: String?
        get() = SkyHanniMod.feature.storage.lastSkillObtained
        set(value) {
            SkyHanniMod.feature.storage.lastSkillObtained = value
        }

    private var lastSkillFetched: String? = null
    private var lastPassed = SimpleTimeMark.farPast()
    private var lastLeaderboardFetch = SimpleTimeMark.farPast()
    private var lastXPGained = SimpleTimeMark.farPast()
    private var hasSkillsBeenFetched = false

    private var display = emptyList<Renderable>()
    private var commandLastUsed = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (GardenAPI.hideExtraGuis()) return
        if (!isEnabled()) return
        if (!config.alwaysShow && lastXPGained.passedSince() > config.alwaysShowTime.seconds) return

        config.pos.renderRenderables(display, posLabel = "Skill Rank Display")
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.skill.afterChange {
            lastLeaderboardFetch = SimpleTimeMark.farPast()
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (EliteBotAPI.profileID == null) return

        if (lastLeaderboardFetch.passedSince() > EliteBotAPI.checkDuration) {
            lastLeaderboardFetch = SimpleTimeMark.now()
            val skill = if (config.skill.get() == SkillDisplay.AUTO) {
                lastSkillGained ?: "carpentry"
            } else {
                config.skill.get().skill
            }

            SkyHanniMod.coroutineScope.launch {
                skillPlacements.clear()
                skillRanks.clear()
                getRanksForSkill(skill)
            }
        }
        updateDisplay()
    }


    //TODO this event doesn't seem to work :(
    @SubscribeEvent
    fun onSkillGained(event: SkillExpGainEvent) {
        val skillName = event.skill.name.lowercase()
        val skillInfo = SkillAPI.skillXPInfoMap[event.skill] ?: return

        if (lastSkillGained != skillName) {
            lastSkillGained = skillName

            SkyHanniMod.coroutineScope.launch {
                getRanksForSkill(skillName)
            }
        }
        lastXPGained = SimpleTimeMark.now()
        currentSkills[skillName] = skillInfo.lastTotalXp.toLong()
    }

    @SubscribeEvent
    fun onProfileChange(event: ProfileJoinEvent) {
        resetData()
    }

    fun reset() {
        if (EliteBotAPI.disableRefreshCommand) {
            ChatUtils.userError("§eCommand has been disabled")
        } else if (commandLastUsed.passedSince() < 1.minutes) {
            ChatUtils.userError("Command is on cooldown")
        } else {
            commandLastUsed = SimpleTimeMark.now()
            lastLeaderboardFetch = SimpleTimeMark.farPast()
            ChatUtils.chat("Skill Rank Display refreshing...")
        }
    }

    private fun resetData() {
        hasSkillsBeenFetched = false
        lastLeaderboardFetch = SimpleTimeMark.farPast()
        skillRanks.clear()
        skillPlacements.clear()
    }

    private fun updateDisplay() {
        if (lastSkillFetched == null) return
        if (skillPlacements.isEmpty()) return
        if (currentSkills.isEmpty()) {
            display = listOf(Renderable.wrappedString("§cCheck if your Skills \nAPI is enabled!", width = 200))
            return
        }
        val placements = skillPlacements[lastSkillFetched] ?: return
        val skill = currentSkills[lastSkillFetched] ?: 0

        val rankWhenLastFetched = skillRanks[lastSkillFetched]?.let { if (it == -1) 5001 else it } ?: return
        var rank: Int
        var nextRank: Int
        var personToBeat: String
        var amountToBeat: Long
        var difference: Long

        do {
            rank = skillRanks[lastSkillFetched]?.let { if (it == -1) 5001 else it } ?: return
            nextRank = rank - 1
            personToBeat = placements[nextRank]?.first ?: ""
            amountToBeat = placements[nextRank]?.second ?: 0
            difference = amountToBeat - skill


        } while (difference < 0 && (rankWhenLastFetched - nextRank) < placements.size)
        if (rankWhenLastFetched - nextRank > placements.size && placements.isNotEmpty() && !EliteBotAPI.disableFetchingWhenPassed && lastPassed.passedSince() > 1.minutes) {
            lastPassed = SimpleTimeMark.now()
            lastLeaderboardFetch = SimpleTimeMark.farPast()
        }

        val displayPosition = if (config.showPosition && rank != 5001) "§7[§b#$rank§7]" else ""

        val newDisplay = mutableListOf<Renderable>()
        newDisplay.add(
            Renderable.clickAndHover(
                "§6§l${lastSkillFetched?.firstLetterUppercase()}: §e${skill.addSeparators()} $displayPosition",
                listOf("§eClick to open your Elite Bot Profile."),
                onClick = {
                    OSUtils.openBrowser("https://elitebot.dev/@${LorenzUtils.getPlayerName()}/")
                    ChatUtils.chat("Opening Elite Bot Profile of player §b${LorenzUtils.getPlayerName()}")
                }
            )
        )
        val skillType = SkillType.getByNameOrNull(lastSkillFetched ?: "")
        if (config.showTimeUntilReached && skillType != null) {
            val speed = ((SkillAPI.skillXPInfoMap[skillType]?.xpGainHour ?: 0f) / 3600f).toInt()
            if (difference < 0) {
                newDisplay.add(
                    Renderable.string("§7Time until reached: §a§lNow")
                )
            } else if (speed != 0) {
                val timeUntilReached = (difference / speed).seconds

                newDisplay.add(
                    Renderable.string("§7Time until reached: §b${timeUntilReached.format()}")
                )
            } else {
                newDisplay.add(
                    Renderable.string("§cPAUSED")
                )
            }

        }
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
                        ChatUtils.chat("Skills leaderboard updating...")
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

    private fun getRanksForSkill(skill: String) {
        if (EliteBotAPI.profileID == null) return
        val url =
            "https://api.elitebot.dev/Leaderboard/rank/$skill/${LorenzUtils.getPlayerUuid()}/${EliteBotAPI.profileID!!.toDashlessUUID()}?includeUpcoming=true"

        val response = APIUtils.getJSONResponseAsElement(url)

        try {
            val data = eliteCollectionApiGson.fromJson<EliteLeaderboard>(response)

            skillPlacements.clear()

            skillRanks[skill] = data.rank
            val placements = mutableMapOf<Int, Pair<String, Long>>()
            var rank = data.upcomingRank
            data.upcomingPlayers.forEach {
                //weight is amount
                placements[rank] = it.name to it.weight.toLong()
                rank--
            }
            skillPlacements[skill] = placements
            lastSkillFetched = skill
            if (data.amount != 0L) {
                currentSkills[skill] = data.amount
            }

            if (!hasSkillsBeenFetched && data.amount == 0L) {
                hasSkillsBeenFetched = true
                getCurrentSkills()
            }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e,
                "Error loading user skill leaderboard\n" +
                    "§eLoading the skill leaderboard data from elitebot.dev failed!\n" +
                    "§eYou can switch worlds to try to fix the problem.\n" +
                    "§cIf this message repeats, please report it on Discord!\n",
                "url" to url,
                "apiResponse" to response,
            )
        }
    }

    private fun getCurrentSkills() {
        if (EliteBotAPI.profileID == null) return
        val url =
            "https://api.elitebot.dev/Graph/${LorenzUtils.getPlayerUuid()}/${EliteBotAPI.profileID!!.toDashlessUUID()}/skills?days=1"
        val response = APIUtils.getJSONResponseAsElement(url)

        try {
            val data = eliteCollectionApiGson.fromJson<Array<EliteSkillGraphEntry>>(response)

            data.sortBy { it.timestamp }
            currentSkills = data.lastOrNull()?.skills?.toMutableMap() ?: mutableMapOf()

        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e,
                "Error loading user skill\n" +
                    "§eLoading the skill data from elitebot.dev failed!\n" +
                    "§eYou can switch worlds to try to fix the problem.\n" +
                    "§cIf this message repeats, please report it on Discord!\n",
                "url" to url,
                "apiResponse" to response,
            )
        }
    }

    private fun isEnabled() = config.display && LorenzUtils.inSkyBlock && (GardenAPI.inGarden() || !config.showInGarden)
}
