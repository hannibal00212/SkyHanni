package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.EliteBotAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.EliteFarmingCollectionConfig.CropDisplay
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.jsonobjects.other.EliteCollectionGraphEntry
import at.hannibal2.skyhanni.data.jsonobjects.other.EliteLeaderboard
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.CropType.Companion.getCropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getSpeed
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
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object EliteFarmingCollectionDisplay {

    private val config get() = SkyHanniMod.feature.garden.eliteFarmingCollection

    private val eliteCollectionApiGson by lazy {
        BaseGsonBuilder.gson()
            .registerTypeAdapter(CropType::class.java, object : TypeAdapter<CropType>() {
                override fun write(out: JsonWriter, value: CropType) {}

                override fun read(reader: JsonReader): CropType {
                    val crop = reader.nextString()
                    return CropType.entries.firstOrNull { it.simpleName == crop } ?: error("No valid crop type '$crop'")
                }
            }.nullSafe())
            .create()
    }

    private val collectionPlacements = mutableMapOf<CropType, Map<Int, Pair<String, Long>>>()
    private val collectionRanks = mutableMapOf<CropType, Int>()
    private var currentCollections = mutableMapOf<CropType, Long>()
    private var lastFetchedCrop: CropType? = null

    private var hasCollectionBeenFetched = false
    private var commandLastUsed = SimpleTimeMark.farPast()
    private var lastPassed = SimpleTimeMark.farPast()

    private var lastBrokenCrop: CropType?
        get() = SkyHanniMod.feature.storage.lastCropBroken
        set(value) {
            SkyHanniMod.feature.storage.lastCropBroken = value
        }
    private var lastLeaderboardFetch = SimpleTimeMark.farPast()

    private var display = listOf<Renderable>()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (GardenAPI.hideExtraGuis()) return
        if (!isEnabled()) return

        config.pos.renderRenderables(display, posLabel = "Farming Collection Display")
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.crop.afterChange {
            lastLeaderboardFetch = SimpleTimeMark.farPast()
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (EliteBotAPI.profileID == null) return

        if (lastLeaderboardFetch.passedSince() > EliteBotAPI.checkDuration) {
            lastLeaderboardFetch = SimpleTimeMark.now()
            val crop = if (config.crop.get() == CropDisplay.AUTO) {
                lastBrokenCrop ?: CropType.WHEAT
            } else {
                config.crop.get().crop
            }

            SkyHanniMod.coroutineScope.launch {
                collectionPlacements.clear()
                collectionRanks.clear()
                getRanksForCollection(crop)
            }
        }

        updateDisplay()
    }

    //This uses the block click event instead of the crop click event, so it still works outside the garden.
    @HandleEvent
    fun onBlockClicked(event: BlockClickEvent) {
        if (event.clickType == ClickType.RIGHT_CLICK) return
        val crop = event.getBlockState.getCropType() ?: return
        if (!collectionRanks.containsKey(crop) && lastBrokenCrop != crop) {
            SkyHanniMod.coroutineScope.launch {
                getRanksForCollection(crop)
            }
        } else {
            lastFetchedCrop = crop
        }
        lastBrokenCrop = crop
    }

    @SubscribeEvent
    fun onProfileChange(event: ProfileJoinEvent) {
        resetData()
    }

    fun refresh() {
        if (EliteBotAPI.disableRefreshCommand) {
            ChatUtils.userError("§eCommand has been disabled")
        } else if (commandLastUsed.passedSince() < 1.minutes) {
            ChatUtils.userError("Command is on cooldown")
        } else {
            commandLastUsed = SimpleTimeMark.now()
            lastLeaderboardFetch = SimpleTimeMark.farPast()
            ChatUtils.chat("Farming Collection Display refreshing...")
        }
    }

    fun addCrop(crop: CropType, amount: Int) {
        if (!isEnabled()) return
        if (!config.estimateCollected) return

        currentCollections.addOrPut(crop, amount.toLong())
    }

    private fun resetData() {
        hasCollectionBeenFetched = false
        lastLeaderboardFetch = SimpleTimeMark.farPast()
        collectionRanks.clear()
        collectionPlacements.clear()
    }

    private fun updateDisplay() {
        if (lastFetchedCrop == null) return
        if (collectionPlacements.isEmpty()) return
        if (currentCollections.isEmpty()) {
            display = listOf(Renderable.wrappedString("§cCheck if your Collections \nAPI is enabled!", width = 200))
            return
        }

        val placements = collectionPlacements[lastFetchedCrop] ?: return
        val collection = currentCollections[lastFetchedCrop] ?: 0

        val rankWhenLastFetched = collectionRanks[lastFetchedCrop]?.let { if (it == -1) 5001 else it } ?: return
        var rank: Int
        var nextRank: Int
        var personToBeat: String
        var amountToBeat: Long
        var difference: Long

        do {
            rank = collectionRanks[lastFetchedCrop]?.let { if (it == -1) 5001 else it } ?: return
            nextRank = rank - 1
            personToBeat = placements[nextRank]?.first ?: ""
            amountToBeat = placements[nextRank]?.second ?: 0
            difference = amountToBeat - collection


        } while (difference < 0 && (rankWhenLastFetched - nextRank) < placements.size)
        if (rankWhenLastFetched - nextRank > placements.size && placements.isNotEmpty() && !EliteBotAPI.disableFetchingWhenPassed && lastPassed.passedSince() > 1.minutes) {
            lastPassed = SimpleTimeMark.now()
            lastLeaderboardFetch = SimpleTimeMark.farPast()
        }

        val displayPosition = if (config.showPosition && rank != 5001) "§7[§b#$rank§7]" else ""

        val newDisplay = mutableListOf<Renderable>()
        newDisplay.add(
            Renderable.clickAndHover(
                "§6§l$lastFetchedCrop: §e${collection.addSeparators()} $displayPosition",
                listOf("§eClick to open your Farming Profile."),
                onClick = {
                    OSUtils.openBrowser("https://elitebot.dev/@${LorenzUtils.getPlayerName()}/")
                    ChatUtils.chat("Opening Farming Profile of player §b${LorenzUtils.getPlayerName()}")
                }
            )
        )
        if (config.showTimeUntilReached) {
            val speed = lastFetchedCrop?.getSpeed() ?: 0
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
                        ChatUtils.chat("Collection leaderboard updating...")
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

    private fun getRanksForCollection(crop: CropType) {
        if (EliteBotAPI.profileID == null) return
        val url =
            "https://api.elitebot.dev/Leaderboard/rank/${getEliteBotLeaderboardForCrop(crop)}/${LorenzUtils.getPlayerUuid()}/${EliteBotAPI.profileID!!.toDashlessUUID()}?includeUpcoming=true"
        val response = APIUtils.getJSONResponseAsElement(url)

        try {
            val data = eliteCollectionApiGson.fromJson<EliteLeaderboard>(response)

            collectionRanks[crop] = data.rank
            val placements = mutableMapOf<Int, Pair<String, Long>>()
            var rank = data.upcomingRank
            data.upcomingPlayers.forEach {
                //weight is amount
                placements[rank] = it.name to it.weight.toLong()
                rank--
            }
            collectionPlacements[crop] = placements
            lastFetchedCrop = crop

            if (data.amount != 0L) {
                currentCollections[crop] = data.amount
            }


            if (!hasCollectionBeenFetched && data.amount == 0L) {
                hasCollectionBeenFetched = true
                getCurrentCollection()
            }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e,
                "Error loading user farming collection leaderboard\n" +
                    "§eLoading the farming collection leaderboard data from elitebot.dev failed!\n" +
                    "§eYou can re-enter the garden to try to fix the problem.\n" +
                    "§cIf this message repeats, please report it on Discord!\n",
                "url" to url,
                "apiResponse" to response,
            )
        }
    }

    private fun getCurrentCollection() {
        if (EliteBotAPI.profileID == null) return
        val url =
            "https://api.elitebot.dev/Graph/${LorenzUtils.getPlayerUuid()}/${EliteBotAPI.profileID!!.toDashlessUUID()}/crops?days=1"
        val response = APIUtils.getJSONResponseAsElement(url)

        try {
            val data = eliteCollectionApiGson.fromJson<Array<EliteCollectionGraphEntry>>(response)

            data.sortBy { it.timestamp }
            currentCollections = data.lastOrNull()?.crops?.toMutableMap() ?: mutableMapOf()

        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e,
                "Error loading user farming collection\n" +
                    "§eLoading the farming collection data from elitebot.dev failed!\n" +
                    "§eYou can re-enter the garden to try to fix the problem.\n" +
                    "§cIf this message repeats, please report it on Discord!\n",
                "url" to url,
                "apiResponse" to response,
            )
        }
    }

    private fun getEliteBotLeaderboardForCrop(crop: CropType) = when (crop) {
        CropType.NETHER_WART -> "netherwart"
        CropType.SUGAR_CANE -> "sugarcane"
        else -> crop.simpleName
    }

    private fun isEnabled() = config.display && LorenzUtils.inSkyBlock && (GardenAPI.inGarden() || config.showOutsideGarden)
}
