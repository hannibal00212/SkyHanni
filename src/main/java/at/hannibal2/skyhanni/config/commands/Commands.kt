package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.SkillAPI
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.ConfigGuiManager
import at.hannibal2.skyhanni.config.features.About.UpdateStream
import at.hannibal2.skyhanni.data.ChatManager
import at.hannibal2.skyhanni.data.GardenCropMilestonesCommunityFix
import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.data.PartyAPI
import at.hannibal2.skyhanni.data.SackAPI
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.data.TrackerManager
import at.hannibal2.skyhanni.data.bazaar.HypixelBazaarFetcher
import at.hannibal2.skyhanni.features.bingo.card.BingoCardDisplay
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.BingoNextStepHelper
import at.hannibal2.skyhanni.features.chat.ColorFormattingHelper
import at.hannibal2.skyhanni.features.chat.translation.Translator
import at.hannibal2.skyhanni.features.combat.endernodetracker.EnderNodeTracker
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostUtil
import at.hannibal2.skyhanni.features.commands.HelpCommand
import at.hannibal2.skyhanni.features.commands.PartyChatCommands
import at.hannibal2.skyhanni.features.commands.PartyCommands
import at.hannibal2.skyhanni.features.commands.WikiManager
import at.hannibal2.skyhanni.features.dungeon.CroesusChestTracker
import at.hannibal2.skyhanni.features.event.diana.AllBurrowsList
import at.hannibal2.skyhanni.features.event.diana.BurrowWarpHelper
import at.hannibal2.skyhanni.features.event.diana.DianaProfitTracker
import at.hannibal2.skyhanni.features.event.diana.GriffinBurrowHelper
import at.hannibal2.skyhanni.features.event.diana.InquisitorWaypointShare
import at.hannibal2.skyhanni.features.event.diana.MythologicalCreatureTracker
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggLocations
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggLocator
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEventSummary
import at.hannibal2.skyhanni.features.event.jerry.frozentreasure.FrozenTreasureTracker
import at.hannibal2.skyhanni.features.fishing.tracker.FishingProfitTracker
import at.hannibal2.skyhanni.features.fishing.tracker.SeaCreatureTracker
import at.hannibal2.skyhanni.features.garden.FarmingMilestoneCommand
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenCropTimeCommand
import at.hannibal2.skyhanni.features.garden.GardenCropsInCommand
import at.hannibal2.skyhanni.features.garden.SensitivityReducer
import at.hannibal2.skyhanni.features.garden.composter.ComposterOverlay
import at.hannibal2.skyhanni.features.garden.farming.ArmorDropTracker
import at.hannibal2.skyhanni.features.garden.farming.CropMoneyDisplay
import at.hannibal2.skyhanni.features.garden.farming.CropSpeedMeter
import at.hannibal2.skyhanni.features.garden.farming.DicerRngDropTracker
import at.hannibal2.skyhanni.features.garden.farming.FarmingWeightDisplay
import at.hannibal2.skyhanni.features.garden.farming.GardenStartLocation
import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLaneCreator
import at.hannibal2.skyhanni.features.garden.fortuneguide.CaptureFarmingGear
import at.hannibal2.skyhanni.features.garden.fortuneguide.FFGuideGUI
import at.hannibal2.skyhanni.features.garden.pests.PestFinder
import at.hannibal2.skyhanni.features.garden.pests.PestProfitTracker
import at.hannibal2.skyhanni.features.garden.visitor.GardenVisitorDropStatistics
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentsProfitTracker
import at.hannibal2.skyhanni.features.mining.KingTalismanHelper
import at.hannibal2.skyhanni.features.mining.MineshaftPityDisplay
import at.hannibal2.skyhanni.features.mining.fossilexcavator.ExcavatorProfitTracker
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.CorpseTracker
import at.hannibal2.skyhanni.features.mining.powdertracker.PowderTracker
import at.hannibal2.skyhanni.features.minion.MinionFeatures
import at.hannibal2.skyhanni.features.misc.CarryTracker
import at.hannibal2.skyhanni.features.misc.CollectionTracker
import at.hannibal2.skyhanni.features.misc.LockMouseLook
import at.hannibal2.skyhanni.features.misc.MarkedPlayerManager
import at.hannibal2.skyhanni.features.misc.TpsCounter
import at.hannibal2.skyhanni.features.misc.discordrpc.DiscordRPCManager
import at.hannibal2.skyhanni.features.misc.limbo.LimboTimeTracker
import at.hannibal2.skyhanni.features.misc.massconfiguration.DefaultConfigFeatures
import at.hannibal2.skyhanni.features.misc.pathfind.NavigationHelper
import at.hannibal2.skyhanni.features.misc.reminders.ReminderManager
import at.hannibal2.skyhanni.features.misc.update.UpdateManager
import at.hannibal2.skyhanni.features.misc.visualwords.VisualWordGui
import at.hannibal2.skyhanni.features.rift.area.westvillage.VerminTracker
import at.hannibal2.skyhanni.features.rift.everywhere.PunchcardHighlight
import at.hannibal2.skyhanni.features.slayer.SlayerProfitTracker
import at.hannibal2.skyhanni.test.DebugCommand
import at.hannibal2.skyhanni.test.PacketTest
import at.hannibal2.skyhanni.test.SkyBlockIslandTest
import at.hannibal2.skyhanni.test.SkyHanniConfigSearchResetCommand
import at.hannibal2.skyhanni.test.SkyHanniDebugsAndTests
import at.hannibal2.skyhanni.test.TestBingo
import at.hannibal2.skyhanni.test.WorldEdit
import at.hannibal2.skyhanni.test.command.CopyActionBarCommand
import at.hannibal2.skyhanni.test.command.CopyBossbarCommand
import at.hannibal2.skyhanni.test.command.CopyItemCommand
import at.hannibal2.skyhanni.test.command.CopyNearbyEntitiesCommand
import at.hannibal2.skyhanni.test.command.CopyScoreboardCommand
import at.hannibal2.skyhanni.test.command.TestChatCommand
import at.hannibal2.skyhanni.test.command.TrackParticlesCommand
import at.hannibal2.skyhanni.test.command.TrackSoundsCommand
import at.hannibal2.skyhanni.test.graph.GraphEditor
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ExtendedChatColor
import at.hannibal2.skyhanni.utils.ItemPriceUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.chat.ChatClickActionManager
import at.hannibal2.skyhanni.utils.repopatterns.RepoPatternGui
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraftforge.client.ClientCommandHandler

object Commands {

    private val openMainMenu: (Array<String>) -> Unit = {
        if (it.isNotEmpty()) {
            if (it[0].lowercase() == "gui") {
                GuiEditManager.openGuiPositionEditor(hotkeyReminder = true)
            } else {
                ConfigGuiManager.openConfigGui(it.joinToString(" "))
            }
        } else {
            ConfigGuiManager.openConfigGui()
        }
    }

    // command -> description
    private val commands = mutableListOf<CommandInfo>()

    enum class CommandCategory(val color: String, val categoryName: String, val description: String) {
        MAIN("§6", "Main Command", "Most useful commands of SkyHanni"),
        USERS_NORMAL("§e", "Normal Command", "Normal Command for everyone to use"),
        USERS_BUG_FIX("§f", "User Bug Fix", "A Command to fix small bugs"),
        DEVELOPER_CODING_HELP(
            "§5",
            "Developer Coding Help",
            "A Command that can help with developing new features. §cIntended for developers only!",
        ),
        DEVELOPER_DEBUG_FEATURES(
            "§9",
            "Developer Debug Features",
            "A Command that is useful for monitoring/debugging existing features. §cIntended for developers only!",
        ),
        INTERNAL("§8", "Internal Command", "A Command that should §cnever §7be called manually!"),
        SHORTENED_COMMANDS("§b", "Shortened Commands", "Commands that shorten or improve existing Hypixel commands!")
    }

    class CommandInfo(val name: String, val description: String, val category: CommandCategory)

    private var currentCategory = CommandCategory.MAIN

    fun init() {
        currentCategory = CommandCategory.MAIN
        usersMain()

        currentCategory = CommandCategory.USERS_NORMAL
        usersNormal()

        currentCategory = CommandCategory.USERS_BUG_FIX
        usersBugFix()

        currentCategory = CommandCategory.DEVELOPER_CODING_HELP
        developersCodingHelp()

        currentCategory = CommandCategory.DEVELOPER_DEBUG_FEATURES
        developersDebugFeatures()

        currentCategory = CommandCategory.INTERNAL
        internalCommands()

        currentCategory = CommandCategory.SHORTENED_COMMANDS
        shortenedCommands()
    }

    private fun usersMain() {
        registerCommand("sh", "Opens the main SkyHanni config", openMainMenu)
        registerCommand("skyhanni", "Opens the main SkyHanni config", openMainMenu)
        registerCommand("ff", "Opens the Farming Fortune Guide") { openFortuneGuide() }
        registerCommand("shcommands", "Shows this list") { HelpCommand.onCommand(it, commands) }
        registerCommand0(
            "shdefaultoptions",
            "Select default options",
            { DefaultConfigFeatures.onCommand(it) },
            DefaultConfigFeatures::onComplete,
        )
        registerCommand("shremind", "Set a reminder for yourself") { ReminderManager.command(it) }
        registerCommand("shwords", "Opens the config list for modifying visual words") { openVisualWords() }
        registerCommand("shnavigate", "Using path finder to go to locatons") { NavigationHelper.onCommand(it) }
    }

    @Suppress("LongMethod")
    private fun usersNormal() {
        registerCommand(
            "shmarkplayer",
            "Add a highlight effect to a player for better visibility",
        ) { MarkedPlayerManager.command(it) }
        registerCommand("shtrackcollection", "Tracks your collection gain over time") { CollectionTracker.command(it) }
        registerCommand(
            "shcroptime",
            "Calculates with your current crop per second speed how long you need to farm a crop to collect this amount of items",
        ) { GardenCropTimeCommand.onCommand(it) }
        registerCommand(
            "shcropsin",
            "Calculates with your current crop per second how many items you can collect in this amount of time",
        ) { GardenCropsInCommand.onCommand(it) }
        registerCommand(
            "shrpcstart",
            "Manually starts the Discord Rich Presence feature",
        ) { DiscordRPCManager.startCommand() }
        registerCommand(
            "shcropstartlocation",
            "Manually sets the crop start location",
        ) { GardenStartLocation.setLocationCommand() }
        registerCommand(
            "shclearslayerprofits",
            "Clearing the total slayer profit for the current slayer type",
        ) { SlayerProfitTracker.clearProfitCommand(it) }
        registerCommand(
            "shimportghostcounterdata",
            "Manually importing the ghost counter data from GhostCounterV3",
        ) { GhostUtil.importCTGhostCounterData() }
        registerCommand(
            "shclearfarmingitems",
            "Clear farming items saved for the Farming Fortune Guide",
        ) { clearFarmingItems() }
        registerCommand("shresetghostcounter", "Resets the ghost counter") { GhostUtil.reset() }
        registerCommand("shresetpowdertracker", "Resets the Powder Tracker") { PowderTracker.resetCommand() }
        registerCommand("shresetdicertracker", "Resets the Dicer Drop Tracker") { DicerRngDropTracker.resetCommand() }
        registerCommand("shresetcorpsetracker", "Resets the Glacite Mineshaft Corpse Tracker") { CorpseTracker.resetCommand() }
        registerCommand(
            "shresetendernodetracker",
            "Resets the Ender Node Tracker",
        ) { EnderNodeTracker.resetCommand() }
        registerCommand(
            "shresetarmordroptracker",
            "Resets the Armor Drop Tracker",
        ) { ArmorDropTracker.resetCommand() }
        registerCommand(
            "shresetfrozentreasuretracker",
            "Resets the Frozen Treasure Tracker",
        ) { FrozenTreasureTracker.resetCommand() }
        registerCommand(
            "shresetfishingtracker",
            "Resets the Fishing Profit Tracker",
        ) { FishingProfitTracker.resetCommand() }
        registerCommand(
            "shresetvisitordrops",
            "Reset the Visitors Drop Statistics",
        ) { GardenVisitorDropStatistics.resetCommand() }
        registerCommand("shbingotoggle", "Toggle the bingo card display mode") { BingoCardDisplay.toggleCommand() }
        registerCommand(
            "shfarmingprofile",
            "Look up the farming profile from yourself or another player on elitebot.dev",
        ) { FarmingWeightDisplay.lookUpCommand(it) }
        registerCommand(
            "shcopytranslation",
            "Copy the English translation of a message in another language to the clipboard.\n" + "Uses a 2 letter language code that can be found at the end of a translation message.",
        ) { Translator.fromEnglish(it) }
        registerCommand(
            "shtranslate",
            "Translate a message in another language to English.",
        ) { Translator.toEnglish(it) }
        registerCommand(
            "shmouselock",
            "Lock/Unlock the mouse so it will no longer rotate the player (for farming)",
        ) { LockMouseLook.toggleLock() }
        registerCommand(
            "shsensreduce",
            "Lowers the mouse sensitivity for easier small adjustments (for farming)",
        ) { SensitivityReducer.manualToggle() }
        registerCommand(
            "shresetvermintracker",
            "Resets the Vermin Tracker",
        ) { VerminTracker.resetCommand() }
        registerCommand(
            "shresetdianaprofittracker",
            "Resets the Diana Profit Tracker",
        ) { DianaProfitTracker.resetCommand() }
        registerCommand(
            "shresetpestprofittracker",
            "Resets the Pest Profit Tracker",
        ) { PestProfitTracker.resetCommand() }
        registerCommand(
            "shresetexperimentsprofittracker",
            "Resets the Experiments Profit Tracker",
        ) { ExperimentsProfitTracker.resetCommand() }
        registerCommand(
            "shresetmythologicalcreaturetracker",
            "Resets the Mythological Creature Tracker",
        ) { MythologicalCreatureTracker.resetCommand() }
        registerCommand(
            "shresetseacreaturetracker",
            "Resets the Sea Creature Tracker",
        ) { SeaCreatureTracker.resetCommand() }
        registerCommand(
            "shresetstrayrabbittracker",
            "Resets the Stray Rabbit Tracker",
        ) { ChocolateFactoryStrayTracker.resetCommand() }
        registerCommand(
            "shresetexcavatortracker",
            "Resets the Fossil Excavator Profit Tracker",
        ) { ExcavatorProfitTracker.resetCommand() }
        registerCommand(
            "shfandomwiki",
            "Searches the fandom wiki with SkyHanni's own method.",
        ) { WikiManager.otherWikiCommands(it, true) }
        registerCommand(
            "shfandomwikithis",
            "Searches the fandom wiki with SkyHanni's own method.",
        ) { WikiManager.otherWikiCommands(it, true, true) }
        registerCommand(
            "shofficialwiki",
            "Searches the official wiki with SkyHanni's own method.",
        ) { WikiManager.otherWikiCommands(it, false) }
        registerCommand(
            "shofficialwikithis",
            "Searches the official wiki with SkyHanni's own method.",
        ) { WikiManager.otherWikiCommands(it, false, true) }
        registerCommand0(
            "shcalccrop",
            "Calculate how many crops need to be farmed between different crop milestones.",
            {
                FarmingMilestoneCommand.onCommand(it.getOrNull(0), it.getOrNull(1), it.getOrNull(2), false)
            },
            FarmingMilestoneCommand::onComplete,
        )
        registerCommand0(
            "shcalccroptime",
            "Calculate how long you need to farm crops between different crop milestones.",
            {
                FarmingMilestoneCommand.onCommand(it.getOrNull(0), it.getOrNull(1), it.getOrNull(2), true)
            },
            FarmingMilestoneCommand::onComplete,
        )
        registerCommand0(
            "shcropgoal",
            "Define a custom milestone goal for a crop.",
            { FarmingMilestoneCommand.setGoal(it) },
            FarmingMilestoneCommand::onComplete,
        )
        registerCommand0(
            "shskills",
            "Skills XP/Level related command",
            { SkillAPI.onCommand(it) },
            SkillAPI::onComplete,
        )
        registerCommand(
            "shlimbostats",
            "Prints your Limbo Stats.\n §7This includes your Personal Best, Playtime, and §aSkyHanni User Luck§7!",
        ) { LimboTimeTracker.printStats() }
        registerCommand(
            "shlanedetection",
            "Detect a farming lane in the Garden",
        ) { FarmingLaneCreator.commandLaneDetection() }
        registerCommand(
            "shignore",
            "Add/Remove a user from your",
        ) { PartyChatCommands.blacklist(it) }
        registerCommand(
            "shtpinfested",
            "Teleports you to the nearest infested plot",
        ) { PestFinder.teleportNearestInfestedPlot() }
        registerCommand(
            "shhoppitystats",
            "Look up stats for a Hoppity's Event (by SkyBlock year).\nRun standalone for a list of years that have stats.",
        ) { HoppityEventSummary.sendStatsMessage(it) }
        registerCommand(
            "shcolors",
            "Prints a list of all Minecraft color & formatting codes in chat.",
        ) { ColorFormattingHelper.printColorCodeList() }
        registerCommand(
            "shtps",
            "Informs in chat about the server ticks per second (TPS).",
        ) { TpsCounter.tpsCommand() }
        registerCommand(
            "shcarry",
            "Keep track of carries you do.",
        ) { CarryTracker.onCommand(it) }
    }

    private fun usersBugFix() {
        registerCommand("shupdaterepo", "Download the SkyHanni repo again") { SkyHanniMod.repo.updateRepo() }
        registerCommand(
            "shresetburrowwarps",
            "Manually resetting disabled diana burrow warp points",
        ) { BurrowWarpHelper.resetDisabledWarps() }
        registerCommand(
            "shtogglehypixelapierrors",
            "Show/hide hypixel api error messages in chat",
        ) { APIUtils.toggleApiErrorMessages() }
        registerCommand(
            "shclearcropspeed",
            "Reset garden crop speed data and best crop time data",
        ) { GardenAPI.clearCropSpeed() }
        registerCommand(
            "shclearminiondata",
            "Removed bugged minion locations from your private island",
        ) { MinionFeatures.removeBuggedMinions(isCommand = true) }
        registerCommand(
            "shwhereami",
            "Print current island in chat",
        ) { SkyHanniDebugsAndTests.whereAmI() }
        registerCommand(
            "shclearcontestdata",
            "Resets Jacob's Contest Data",
        ) { SkyHanniDebugsAndTests.clearContestData() }
        registerCommand(
            "shconfig",
            "Search or reset config elements §c(warning, dangerous!)",
        ) { SkyHanniConfigSearchResetCommand.command(it) }
        registerCommand(
            "shdebug",
            "Copies SkyHanni debug data in the clipboard.",
        ) { DebugCommand.command(it) }
        registerCommand(
            "shversion",
            "Prints the SkyHanni version in the chat",
        ) { SkyHanniDebugsAndTests.debugVersion() }
        registerCommand(
            "shrendertoggle",
            "Disables/enables the rendering of all skyhanni guis.",
        ) { SkyHanniDebugsAndTests.toggleRender() }
        registerCommand(
            "shcarrolyn",
            "Toggles if the specified crops effect is active from carrolyn",
        ) {
            CaptureFarmingGear.handelCarrolyn(it)
        }
        registerCommand(
            "shrepostatus",
            "Shows the status of all the mods constants",
        ) { SkyHanniMod.repo.displayRepoStatus(false) }
        registerCommand(
            "shclearkismet",
            "Clears the saved values of the applied kismet feathers in Croesus",
        ) { CroesusChestTracker.resetChest() }
        registerCommand(
            "shkingfix",
            "Resets the local King Talisman Helper offset.",
        ) { KingTalismanHelper.kingFix() }
        registerCommand(
            "shupdate",
            "Updates the mod to the specified update stream.",
        ) { forceUpdate(it) }
        registerCommand(
            "shUpdateBazaarPrices",
            "Forcefully updating the bazaar prices right now.",
        ) { HypixelBazaarFetcher.fetchNow() }
        registerCommand(
            "shclearsavedrabbits",
            "Clears the saved rabbits on this profile.",
        ) { HoppityCollectionStats.clearSavedRabbits() }
        registerCommand(
            "shresetpunchcard",
            "Resets the Rift Punchcard Artifact player list.",
        ) { PunchcardHighlight.clearList() }
        registerCommand(
            "shedittracker",
            "Changes the tracked item amount for Diana, Fishing, Pest, Excavator, and Slayer Item Trackers.",
        ) { TrackerManager.commandEditTracker(it) }
    }

    private fun developersDebugFeatures() {
        registerCommand("shtestbingo", "dev command") { TestBingo.toggle() }
        registerCommand("shprintbingohelper", "dev command") { BingoNextStepHelper.command() }
        registerCommand("shreloadbingodata", "dev command") { BingoCardDisplay.command() }
        registerCommand("shtestgardenvisitors", "dev command") { SkyHanniDebugsAndTests.testGardenVisitors() }
        registerCommand("shtestcomposter", "dev command") { ComposterOverlay.onCommand(it) }
        registerCommand("shtestinquisitor", "dev command") { InquisitorWaypointShare.test() }
        registerCommand("shshowcropmoneycalculation", "dev command") { CropMoneyDisplay.toggleShowCalculation() }
        registerCommand("shcropspeedmeter", "Debugs how many crops you collect over time") { CropSpeedMeter.toggle() }
        registerCommand0(
            "shworldedit",
            "Select regions in the world",
            { WorldEdit.command(it) },
            { listOf("copy", "reset", "help", "left", "right") },
        )
        registerCommand(
            "shconfigsave",
            "Manually saving the config",
        ) { SkyHanniMod.configManager.saveConfig(ConfigFileType.FEATURES, "manual-command") }
        registerCommand(
            "shtestburrow",
            "Sets a test burrow waypoint at your location",
        ) { GriffinBurrowHelper.setTestBurrow(it) }
        registerCommand(
            "shtestsackapi",
            "Get the amount of an item in sacks according to internal feature SackAPI",
        ) { SackAPI.testSackAPI(it) }
        registerCommand(
            "shtestgriffinspots",
            "Show potential griffin spots around you.",
        ) { GriffinBurrowHelper.testGriffinSpots() }
        registerCommand(
            "shtestisland",
            "Sets the current skyblock island for testing purposes.",
        ) { SkyBlockIslandTest.onCommand(it) }
        registerCommand(
            "shdebugprice",
            "Debug different price sources for an item.",
        ) { ItemPriceUtils.debugItemPrice(it) }
        registerCommand(
            "shdebugscoreboard",
            "Monitors the scoreboard changes: Prints the raw scoreboard lines in the console after each update, with time since last update.",
        ) { ScoreboardData.toggleMonitor() }
    }

    @Suppress("LongMethod")
    private fun developersCodingHelp() {
        registerCommand("shrepopatterns", "See where regexes are loaded from") { RepoPatternGui.open() }
        registerCommand("shtest", "Unused test command.") { SkyHanniDebugsAndTests.testCommand(it) }
        registerCommand("shtestrabbitpaths", "Tests pathfinding to rabbit eggs. Use a number 0-14.") { HoppityEggLocator.testPathfind(it) }
        registerCommand(
            "shtestitem",
            "test item internal name resolving",
        ) { SkyHanniDebugsAndTests.testItemCommand(it) }
        registerCommand(
            "shfindnullconfig",
            "Find config elements that are null and prints them into the console",
        ) { SkyHanniDebugsAndTests.findNullConfig(it) }
        registerCommand("shtestwaypoint", "Set a waypoint on that location") { SkyHanniDebugsAndTests.waypoint(it) }
        registerCommand("shtesttablist", "Set your clipboard as a fake tab list.") { TabListData.toggleDebug() }
        registerCommand("shreloadlocalrepo", "Reloading the local repo data") { SkyHanniMod.repo.reloadLocalRepo() }
        registerCommand("shchathistory", "Show the unfiltered chat history") { ChatManager.openChatFilterGUI(it) }
        registerCommand(
            "shstoplisteners",
            "Unregistering all loaded forge event listeners",
        ) { SkyHanniDebugsAndTests.stopListeners() }
        registerCommand(
            "shreloadlisteners",
            "Trying to load all forge event listeners again. Might not work at all",
        ) { SkyHanniDebugsAndTests.reloadListeners() }
        registerCommand(
            "shcopylocation",
            "Copies the player location as LorenzVec format to the clipboard",
        ) { SkyHanniDebugsAndTests.copyLocation(it) }
        registerCommand(
            "shcopyentities",
            "Copies entities in the specified radius around the player to the clipboard",
        ) { CopyNearbyEntitiesCommand.command(it) }
        registerCommand(
            "shtracksounds",
            "Tracks the sounds for the specified duration (in seconds) and copies it to the clipboard",
        ) { TrackSoundsCommand.command(it) }
        registerCommand(
            "shtrackparticles",
            "Tracks the particles for the specified duration (in seconds) and copies it to the clipboard",
        ) { TrackParticlesCommand.command(it) }
        registerCommand(
            "shcopytablist",
            "Copies the tab list data to the clipboard",
        ) { TabListData.copyCommand(it) }
        registerCommand(
            "shcopyactionbar",
            "Copies the action bar to the clipboard, including formatting codes",
        ) { CopyActionBarCommand.command(it) }
        registerCommand(
            "shcopyscoreboard",
            "Copies the scoreboard data to the clipboard",
        ) { CopyScoreboardCommand.command(it) }
        registerCommand(
            "shcopybossbar",
            "Copies the name of the bossbar to the clipboard, including formatting codes",
        ) { CopyBossbarCommand.command(it) }
        registerCommand(
            "shcopyitem",
            "Copies information about the item in hand to the clipboard",
        ) { CopyItemCommand.command() }
        registerCommand("shtestpacket", "Logs incoming and outgoing packets to the console") { PacketTest.command(it) }
        registerCommand(
            "shtestmessage",
            "Sends a custom chat message client side in the chat",
        ) { TestChatCommand.command(it) }
        registerCommand(
            "shtestrainbow",
            "Sends a rainbow in chat",
        ) { ExtendedChatColor.testCommand() }
        registerCommand(
            "shcopyinternalname",
            "Copies the internal name of the item in hand to the clipboard.",
        ) { SkyHanniDebugsAndTests.copyItemInternalName() }
        registerCommand(
            "shpartydebug",
            "List persons into the chat SkyHanni thinks are in your party.",
        ) { PartyAPI.listMembers() }
        registerCommand(
            "shplaysound",
            "Play the specified sound effect at the given pitch and volume.",
        ) { SoundUtils.command(it) }
        registerCommand(
            "shsendtitle",
            "Display a title on the screen with the specified settings.",
        ) { TitleManager.command(it) }
        registerCommand(
            "shresetconfig",
            "Reloads the config manager and rendering processors of MoulConfig. " + "This §cWILL RESET §7your config, but also updating the java config files " + "(names, description, orderings and stuff).",
        ) { SkyHanniDebugsAndTests.resetConfigCommand() }
        registerCommand(
            "shreadcropmilestonefromclipboard",
            "Read crop milestone from clipboard. This helps fixing wrong crop milestone data",
        ) { GardenCropMilestonesCommunityFix.readDataFromClipboard() }
        registerCommand(
            "shcopyfoundburrowlocations",
            "Copy all ever found burrow locations to clipboard",
        ) { AllBurrowsList.copyToClipboard() }
        registerCommand(
            "shaddfoundburrowlocationsfromclipboard",
            "Add all ever found burrow locations from clipboard",
        ) { AllBurrowsList.addFromClipboard() }
        registerCommand(
            "shgraph",
            "Enables the graph editor",
        ) { GraphEditor.commandIn() }
        registerCommand(
            "shtoggleegglocationdebug",
            "Shows Hoppity egg locations with their internal API names and status.",
        ) { HoppityEggLocations.toggleDebug() }
        registerCommand(
            "shresetmineshaftpitystats",
            "Resets the mineshaft pity display stats",
        ) { MineshaftPityDisplay.fullResetCounter() }
    }

    private fun internalCommands() {
        registerCommand("shaction", "") { ChatClickActionManager.onCommand(it) }
    }

    private fun shortenedCommands() {
        registerCommand("pko", "Kicks offline party members") { PartyCommands.kickOffline() }
        registerCommand("pw", "Warps your party") { PartyCommands.warp() }
        registerCommand("pk", "Kick a specific party member") { PartyCommands.kick(it) }
        registerCommand("pt", "Transfer the party to another party member") { PartyCommands.transfer(it) }
        registerCommand("pp", "Promote a specific party member") { PartyCommands.promote(it) }
        registerCommand("pd", "Disbands the party") { PartyCommands.disband() }
        registerCommand("rpt", "Reverse transfer party to the previous leader") { PartyCommands.reverseTransfer() }
    }

    @JvmStatic
    fun openFortuneGuide() {
        if (!LorenzUtils.inSkyBlock) {
            ChatUtils.userError("Join SkyBlock to open the fortune guide!")
        } else {
            FFGuideGUI.open()
        }
    }

    @JvmStatic
    fun openVisualWords() {
        if (!LorenzUtils.onHypixel) {
            ChatUtils.userError("You need to join Hypixel to use this feature!")
        } else {
            if (VisualWordGui.sbeConfigPath.exists()) VisualWordGui.drawImport = true
            SkyHanniMod.screenToOpen = VisualWordGui()
        }
    }

    private fun clearFarmingItems() {
        val storage = GardenAPI.storage?.fortune ?: return
        ChatUtils.chat("clearing farming items")
        storage.farmingItems.clear()
        storage.outdatedItems.clear()
    }

    private fun forceUpdate(args: Array<String>) {
        val currentStream = SkyHanniMod.feature.about.updateStream.get()
        val arg = args.firstOrNull() ?: "current"
        val updateStream = when {
            arg.equals("(?i)(?:full|release)s?".toRegex()) -> UpdateStream.RELEASES
            arg.equals("(?i)(?:beta|latest)s?".toRegex()) -> UpdateStream.BETA
            else -> currentStream
        }

        val switchingToBeta = updateStream == UpdateStream.BETA && (currentStream != UpdateStream.BETA || !UpdateManager.isCurrentlyBeta())
        if (switchingToBeta) {
            ChatUtils.clickableChat(
                "Are you sure you want to switch to beta? These versions may be less stable.",
                onClick = {
                    UpdateManager.checkUpdate(true, updateStream)
                },
                "§eClick to confirm!",
                oneTimeClick = true,
            )
        } else {
            UpdateManager.checkUpdate(true, updateStream)
        }
    }

    private fun registerCommand(rawName: String, description: String, function: (Array<String>) -> Unit) {
        val name = rawName.lowercase()
        if (commands.any { it.name == name }) {
            error("The command '$name is already registered!'")
        }
        ClientCommandHandler.instance.registerCommand(SimpleCommand(name, createCommand(function)))
        commands.add(CommandInfo(name, description, currentCategory))
    }

    private fun registerCommand0(
        name: String,
        description: String,
        function: (Array<String>) -> Unit,
        autoComplete: ((Array<String>) -> List<String>) = { listOf() },
    ) {
        val command = SimpleCommand(
            name,
            createCommand(function),
            object : SimpleCommand.TabCompleteRunnable {
                override fun tabComplete(
                    sender: ICommandSender?,
                    args: Array<String>?,
                    pos: BlockPos?,
                ): List<String> {
                    return autoComplete(args ?: emptyArray())
                }
            },
        )
        ClientCommandHandler.instance.registerCommand(command)
        commands.add(CommandInfo(name, description, currentCategory))
    }

    private fun createCommand(function: (Array<String>) -> Unit) = object : SimpleCommand.ProcessCommandRunnable() {
        override fun processCommand(sender: ICommandSender?, args: Array<String>?) {
            if (args != null) function(args.asList().toTypedArray())
        }
    }
}
