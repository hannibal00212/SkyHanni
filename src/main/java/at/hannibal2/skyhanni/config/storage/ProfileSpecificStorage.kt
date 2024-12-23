package at.hannibal2.skyhanni.config.storage

import at.hannibal2.skyhanni.api.HotmAPI.PowderType
import at.hannibal2.skyhanni.api.SkillAPI
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.MaxwellAPI.ThaumaturgyPowerTuning
import at.hannibal2.skyhanni.data.jsonobjects.local.HotmTree
import at.hannibal2.skyhanni.data.model.ComposterUpgrade
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.features.combat.endernodetracker.EnderNodeTracker
import at.hannibal2.skyhanni.features.combat.ghosttracker.GhostTracker
import at.hannibal2.skyhanni.features.dungeon.CroesusChestTracker.OpenedState
import at.hannibal2.skyhanni.features.dungeon.CroesusChestTracker.generateMaxChestAsList
import at.hannibal2.skyhanni.features.dungeon.DungeonFloor
import at.hannibal2.skyhanni.features.event.carnival.CarnivalGoal
import at.hannibal2.skyhanni.features.event.diana.DianaProfitTracker
import at.hannibal2.skyhanni.features.event.diana.MythologicalCreatureTracker
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats.LocationRabbit
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.event.jerry.frozentreasure.FrozenTreasureTracker
import at.hannibal2.skyhanni.features.fame.UpgradeReminder.CommunityShopUpgrade
import at.hannibal2.skyhanni.features.fishing.tracker.FishingProfitTracker
import at.hannibal2.skyhanni.features.fishing.tracker.SeaCreatureTracker
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import at.hannibal2.skyhanni.features.garden.CropAccessory
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.PlotData
import at.hannibal2.skyhanni.features.garden.farming.ArmorDropTracker
import at.hannibal2.skyhanni.features.garden.farming.DicerRngDropTracker
import at.hannibal2.skyhanni.features.garden.farming.lane.FarmingLane
import at.hannibal2.skyhanni.features.garden.fortuneguide.FarmingItems
import at.hannibal2.skyhanni.features.garden.pests.PestProfitTracker
import at.hannibal2.skyhanni.features.garden.pests.VinylType
import at.hannibal2.skyhanni.features.garden.visitor.VisitorReward
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker
import at.hannibal2.skyhanni.features.inventory.experimentationtable.ExperimentsProfitTracker
import at.hannibal2.skyhanni.features.inventory.wardrobe.WardrobeAPI.WardrobeData
import at.hannibal2.skyhanni.features.mining.MineshaftPityDisplay.PityData
import at.hannibal2.skyhanni.features.mining.fossilexcavator.ExcavatorProfitTracker
import at.hannibal2.skyhanni.features.mining.glacitemineshaft.CorpseTracker.BucketData
import at.hannibal2.skyhanni.features.mining.powdertracker.PowderTracker
import at.hannibal2.skyhanni.features.misc.DraconicSacrificeTracker
import at.hannibal2.skyhanni.features.misc.trevor.TrevorTracker.TrapperMobRarity
import at.hannibal2.skyhanni.features.rift.area.westvillage.VerminTracker
import at.hannibal2.skyhanni.features.rift.area.westvillage.kloon.KloonTerminal
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import at.hannibal2.skyhanni.features.slayer.SlayerProfitTracker
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.NONE
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.farPast
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack
import java.time.LocalDate
import java.util.*
import kotlin.time.Duration

class ProfileSpecificStorage {
    @Expose
    var currentPet: String = ""

    @Expose
    var experimentation: ExperimentationStorage = ExperimentationStorage()

    class ExperimentationStorage {
        @Expose
        var tablePos: LorenzVec = LorenzVec()

        @Expose
        var dryStreak: ExperimentsDryStreakStorage = ExperimentsDryStreakStorage()

        class ExperimentsDryStreakStorage {
            @Expose
            var attemptsSince: Int = 0

            @Expose
            var xpSince: Int = 0
        }

        @Expose
        var experimentsProfitTracker: ExperimentsProfitTracker.Data = ExperimentsProfitTracker.Data()
    }

    @Expose
    var chocolateFactory: ChocolateFactoryStorage = ChocolateFactoryStorage()

    class ChocolateFactoryStorage {
        @Expose
        var currentRabbits: Int = 0

        @Expose
        var maxRabbits: Int = -1

        @Expose
        var currentChocolate: Long = 0

        @Expose
        var maxChocolate: Long = 0

        @Expose
        var chocolateThisPrestige: Long = 0

        @Expose
        var chocolateAllTime: Long = 0

        @Expose
        var rawChocPerSecond: Int = 0

        @Expose
        var chocolateMultiplier: Double = 1.0

        @Expose
        var rawChocolateMultiplier: Double = 1.0

        @Expose
        var timeTowerLevel: Int = 0

        @Expose
        var currentTimeTowerEnds: SimpleTimeMark = farPast()

        @Expose
        var nextTimeTower: SimpleTimeMark = farPast()

        @Expose
        var currentTimeTowerUses: Int = -1

        @Expose
        var timeTowerCooldown: Int = 8

        @Expose
        var maxTimeTowerUses: Int = 0

        @Expose
        var bestUpgradeAvailableAt: SimpleTimeMark = farPast()

        @Expose
        var bestUpgradeCost: Long = 0

        @Expose
        var lastDataSave: SimpleTimeMark = farPast()

        @Expose
        var positionChange: PositionChange = PositionChange()

        class PositionChange {
            @Expose
            var lastTime: SimpleTimeMark? = null

            @Expose
            var lastPosition: Int = -1

            @Expose
            var lastLeaderboard: String? = null
        }

        @Expose
        var targetGoal: Long? = null

        @Expose
        var targetName: String? = null

        @Expose
        var rabbitCounts: MutableMap<String, Int> = HashMap()

        @Expose
        var locationRabbitRequirements: MutableMap<String, LocationRabbit> = HashMap()

        @Expose
        var collectedEggLocations: MutableMap<IslandType, MutableSet<LorenzVec>> = EnumMap(IslandType::class.java)

        @Expose
        var residentRabbits: MutableMap<IslandType, MutableMap<String, Boolean?>> = EnumMap(IslandType::class.java)

        class HotspotRabbitStorage(@field:Expose var skyblockYear: Int?) {
            @Expose
            var hotspotRabbits: MutableMap<IslandType, MutableMap<String, Boolean?>> = EnumMap(IslandType::class.java)
        }

        @Expose
        var hotspotRabbitStorage: HotspotRabbitStorage = HotspotRabbitStorage(null)

        @Expose
        var hoppityShopYearOpened: Int? = null

        @Expose
        var strayTracker: ChocolateFactoryStrayTracker.Data = ChocolateFactoryStrayTracker.Data()

        @Expose
        var mealLastFound: MutableMap<HoppityEggType, SimpleTimeMark> = EnumMap(HoppityEggType::class.java)

        class HitmanStatsStorage {
            @Expose
            var availableEggs: Int? = null

            @Expose
            var singleSlotCooldownMark: SimpleTimeMark? = null

            @Expose
            var allSlotsCooldownMark: SimpleTimeMark? = null

            @Expose
            var purchasedSlots: Int? = null
        }

        @Expose
        var hitmanStats: HitmanStatsStorage = HitmanStatsStorage()
    }

    @Expose
    var carnival: CarnivalStorage = CarnivalStorage()

    class CarnivalStorage {
        @Expose
        var lastClaimedDay: LocalDate? = null

        @Expose
        var carnivalYear: Int = 0

        @Expose
        var goals: MutableMap<CarnivalGoal, Boolean> = EnumMap(CarnivalGoal::class.java)

        // shop name -> (item name, tier)
        @Expose
        var carnivalShopProgress: MutableMap<String, Map<String, Int>> = HashMap()
    }

    @Expose
    var stats: MutableMap<SkyblockStat, Double?> = HashMap<SkyblockStat, Double?>(SkyblockStat.entries.size)

    @Expose
    var maxwell: MaxwellPowerStorage = MaxwellPowerStorage()

    class MaxwellPowerStorage {
        @Expose
        var currentPower: String? = null

        @Expose
        var magicalPower: Int = -1

        @Expose
        var tunings: List<ThaumaturgyPowerTuning> = ArrayList()

        @Expose
        var favoritePowers: List<String> = ArrayList()
    }

    @Expose
    var arrows: ArrowsStorage = ArrowsStorage()

    class ArrowsStorage {
        @Expose
        var currentArrow: String? = null

        @Expose
        var arrowAmount: MutableMap<NEUInternalName, Int> = HashMap()
    }

    @Expose
    var bits: BitsStorage = BitsStorage()

    class BitsStorage {
        @Expose
        var bits: Int = -1

        @Expose
        var bitsAvailable: Int = -1

        @Expose
        var boosterCookieExpiryTime: SimpleTimeMark? = null
    }

    @Expose
    var minions: Map<LorenzVec, MinionConfig>? = HashMap()

    class MinionConfig {
        @Expose
        var displayName: String = ""

        @Expose
        var lastClicked: SimpleTimeMark = farPast()

        override fun toString(): String {
            return "MinionConfig{" +
                "displayName='$displayName'" +
                ", lastClicked=$lastClicked" +
                "}"
        }
    }

    @Expose
    var beaconPower: BeaconPowerStorage = BeaconPowerStorage()

    class BeaconPowerStorage {
        @Expose
        var beaconPowerExpiryTime: SimpleTimeMark? = null

        @Expose
        var boostedStat: String? = null
    }

    @Expose
    var crimsonIsle: CrimsonIsleStorage = CrimsonIsleStorage()

    class CrimsonIsleStorage {
        @Expose
        var quests: MutableList<String> = ArrayList()

        @Expose
        var miniBossesDoneToday: MutableList<String> = ArrayList()

        @Expose
        var kuudraTiersDone: MutableList<String> = ArrayList()

        @Expose
        var trophyFishes: MutableMap<String, MutableMap<TrophyRarity, Int>> = HashMap()
    }

    @Expose
    var garden: GardenStorage = GardenStorage()

    class GardenStorage {
        @Expose
        var experience: Long? = null

        @Expose
        var cropCounter: MutableMap<CropType, Long> = EnumMap(CropType::class.java)

        @Expose
        var cropUpgrades: MutableMap<CropType, Int> = EnumMap(CropType::class.java)

        @Expose
        var cropsPerSecond: MutableMap<CropType, Int> = EnumMap(CropType::class.java)

        @Expose
        var latestBlocksPerSecond: MutableMap<CropType, Double> = EnumMap(CropType::class.java)

        @Expose
        var latestTrueFarmingFortune: MutableMap<CropType, Double> = EnumMap(CropType::class.java)

        // TODO use in /ff guide
        @Expose
        var personalBestFF: MutableMap<CropType, Double> = EnumMap(CropType::class.java)

        @Expose
        var savedCropAccessory: CropAccessory? = CropAccessory.NONE

        @Expose
        var dicerDropTracker: DicerRngDropTracker.Data = DicerRngDropTracker.Data()

        @Expose
        var informedAboutLowMatter: SimpleTimeMark = farPast()

        @Expose
        var informedAboutLowFuel: SimpleTimeMark = farPast()

        @Expose
        var visitorInterval: Long = 15 * 60000L

        @Expose
        var nextSixthVisitorArrival: SimpleTimeMark = farPast()

        @Expose
        var armorDropTracker: ArmorDropTracker.Data = ArmorDropTracker.Data()

        @Expose
        var composterUpgrades: MutableMap<ComposterUpgrade, Int> = EnumMap(ComposterUpgrade::class.java)

        @Expose
        var toolWithBountiful: MutableMap<CropType, Boolean> = EnumMap(CropType::class.java)

        @Expose
        var composterCurrentOrganicMatterItem: NEUInternalName? = NONE

        @Expose
        var composterCurrentFuelItem: NEUInternalName? = NONE

        @Expose
        var uniqueVisitors: Int = 0

        @Expose
        var visitorDrops: VisitorDrops = VisitorDrops()

        class VisitorDrops {
            @Expose
            var acceptedVisitors: Int = 0

            @Expose
            var deniedVisitors: Int = 0

            @Expose
            var visitorRarities: MutableList<Long> = ArrayList()

            @Expose
            var copper: Int = 0

            @Expose
            var farmingExp: Long = 0

            @Expose
            var gardenExp: Int = 0

            @Expose
            var coinsSpent: Long = 0

            @Expose
            var bits: Long = 0

            @Expose
            var mithrilPowder: Long = 0

            @Expose
            var gemstonePowder: Long = 0

            @Expose
            var rewardsCount: Map<VisitorReward, Int> = EnumMap(VisitorReward::class.java)
        }

        @Expose
        var plotIcon: PlotIcon = PlotIcon()

        class PlotIcon {
            @Expose
            var plotList: MutableMap<Int, NEUInternalName> = HashMap()
        }

        @Expose
        var plotData: MutableMap<Int, PlotData> = HashMap()

        @Expose
        var scoreboardPests: Int = 0

        @Expose
        var cropStartLocations: MutableMap<CropType, LorenzVec> = EnumMap(CropType::class.java)

        @Expose
        var cropLastFarmedLocations: MutableMap<CropType, LorenzVec> = EnumMap(CropType::class.java)

        @Expose
        var farmingLanes: MutableMap<CropType, FarmingLane> = EnumMap(CropType::class.java)

        @Expose
        var fortune: Fortune = Fortune()

        class Fortune {
            @Expose
            var outdatedItems: MutableMap<FarmingItems, Boolean> = EnumMap(FarmingItems::class.java)

            @Expose
            var farmingLevel: Int = -1

            @Expose
            var bestiary: Double = -1.0

            @Expose
            var plotsUnlocked: Int = -1

            @Expose
            var anitaUpgrade: Int = -1

            @Expose
            var farmingStrength: Int = -1

            @Expose
            var cakeExpiring: SimpleTimeMark? = null

            @Expose
            var carrolyn: MutableMap<CropType, Boolean> = EnumMap(CropType::class.java)

            @Expose
            var farmingItems: MutableMap<FarmingItems, ItemStack> = EnumMap(FarmingItems::class.java)
        }

        @Expose
        var composterEmptyTime: SimpleTimeMark = farPast()

        @Expose
        var lastComposterEmptyWarningTime: SimpleTimeMark = farPast()

        @Expose
        var farmingWeight: FarmingWeightConfig = FarmingWeightConfig()

        class FarmingWeightConfig {
            @Expose
            var lastFarmingWeightLeaderboard: Int = -1
        }

        @Expose
        var npcVisitorLocations: MutableMap<String, LorenzVec> = HashMap()

        @Expose
        var customGoalMilestone: MutableMap<CropType, Int> = EnumMap(CropType::class.java)

        @Expose
        var pestProfitTracker: PestProfitTracker.Data = PestProfitTracker.Data()

        @Expose
        var activeVinyl: VinylType? = null
    }

    @Expose
    var ghostStorage: GhostStorage = GhostStorage()

    class GhostStorage {
        @Expose
        var ghostTracker: GhostTracker.Data = GhostTracker.Data()

        @Expose
        var bestiaryKills: Long = 0L

        @Expose
        var migratedTotalKills: Boolean = false
    }

    class CakeData {
        @Expose
        var ownedCakes: MutableSet<Int> = HashSet()

        @Expose
        var missingCakes: MutableSet<Int> = HashSet()

        override fun hashCode(): Int {
            val prime = 31
            var result = 1
            result = prime * result + ownedCakes.hashCode()
            result = prime * result + missingCakes.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null) return false
            if (javaClass != other.javaClass) return false
            val otherProp = other as CakeData
            return otherProp.hashCode() == hashCode()
        }
    }

    @Expose
    var cakeData: CakeData = CakeData()

    @Expose
    var powderTracker: PowderTracker.Data = PowderTracker.Data()

    @Expose
    var frozenTreasureTracker: FrozenTreasureTracker.Data = FrozenTreasureTracker.Data()

    @Expose
    var enderNodeTracker: EnderNodeTracker.Data = EnderNodeTracker.Data()

    @Expose
    var rift: RiftStorage = RiftStorage()

    class RiftStorage {
        @Expose
        var completedKloonTerminals: MutableList<KloonTerminal> = ArrayList()

        @Expose
        var verminTracker: VerminTracker.Data = VerminTracker.Data()
    }

    @Expose
    var slayerProfitData: MutableMap<String, SlayerProfitTracker.Data> = HashMap()

    @Expose
    var slayerRngMeter: MutableMap<String, SlayerRngMeterStorage> = HashMap()

    class SlayerRngMeterStorage {
        @Expose
        var currentMeter: Long = -1

        @Expose
        var gainPerBoss: Long = -1

        @Expose
        var goalNeeded: Long = -1

        @Expose
        var itemGoal: String = "?"

        override fun toString(): String {
            return "SlayerRngMeterStorage{" +
                "currentMeter=$currentMeter" +
                ", gainPerBoss=$gainPerBoss" +
                ", goalNeeded=$goalNeeded" +
                ", itemGoal='$itemGoal'" +
                "}"
        }
    }

    @Expose
    var mining: MiningConfig = MiningConfig()

    class MiningConfig {
        @Expose
        var kingsTalkedTo: MutableList<String> = ArrayList()

        @Expose
        var fossilExcavatorProfitTracker: ExcavatorProfitTracker.Data = ExcavatorProfitTracker.Data()

        @Expose
        var hotmTree: HotmTree = HotmTree()

        @Expose
        var powder: MutableMap<PowderType, PowderStorage> = EnumMap(PowderType::class.java)

        class PowderStorage {
            @Expose
            var available: Long? = null

            @Expose
            var total: Long? = null
        }

        @Expose
        var tokens: Int = 0

        @Expose
        var availableTokens: Int = 0

        @Expose
        var mineshaft: MineshaftStorage = MineshaftStorage()

        class MineshaftStorage {
            @Expose
            var mineshaftTotalBlocks: Long = 0L

            @Expose
            var mineshaftTotalCount: Int = 0

            @Expose
            var blocksBroken: MutableList<PityData> = ArrayList()

            @Expose
            var corpseProfitTracker: BucketData = BucketData()
        }
    }

    @Expose
    var trapperData: TrapperData = TrapperData()

    class TrapperData {
        @Expose
        var questsDone: Int = 0

        @Expose
        var peltsGained: Int = 0

        @Expose
        var killedAnimals: Int = 0

        @Expose
        var selfKillingAnimals: Int = 0

        // TODO change to sh tracker
        @Expose
        var animalRarities: Map<TrapperMobRarity, Int> = EnumMap(TrapperMobRarity::class.java)
    }

    @Expose
    var dungeons: DungeonStorage = DungeonStorage()

    class DungeonStorage {
        @Expose
        var bosses: MutableMap<DungeonFloor, Int> = EnumMap(DungeonFloor::class.java)

        @Expose
        var runs: MutableList<DungeonRunInfo> = generateMaxChestAsList()

        class DungeonRunInfo {
            constructor()

            constructor(floor: String?) {
                this.floor = floor
                this.openState = OpenedState.UNOPENED
            }

            @Expose
            var floor: String? = null

            @Expose
            var openState: OpenedState? = null

            @Expose
            var kismetUsed: Boolean? = null
        }
    }

    @Expose
    var fishing: FishingStorage = FishingStorage()

    class FishingStorage {
        @Expose
        var fishingProfitTracker: FishingProfitTracker.Data = FishingProfitTracker.Data()

        @Expose
        var seaCreatureTracker: SeaCreatureTracker.Data = SeaCreatureTracker.Data()
    }

    @Expose
    var diana: DianaStorage = DianaStorage()

    class DianaStorage {
        @Expose
        var profitTracker: DianaProfitTracker.Data = DianaProfitTracker.Data()

        @Expose
        var profitTrackerPerElection: MutableMap<Int, DianaProfitTracker.Data> = HashMap()

        @Expose
        var mythologicalMobTracker: MythologicalCreatureTracker.Data = MythologicalCreatureTracker.Data()

        @Expose
        var mythologicalMobTrackerPerElection: MutableMap<Int, MythologicalCreatureTracker.Data> = HashMap()
    }

    @Expose
    var skillData: MutableMap<SkillType, SkillAPI.SkillInfo> = EnumMap(SkillType::class.java)

    @Expose
    var wardrobe: WardrobeStorage = WardrobeStorage()

    class WardrobeStorage {
        @Expose
        var data: MutableMap<Int, WardrobeData> = HashMap()

        @Expose
        var currentSlot: Int? = null
    }

    @Expose
    var draconicSacrificeTracker: DraconicSacrificeTracker.Data = DraconicSacrificeTracker.Data()

    @Expose
    var communityShopProfileUpgrade: CommunityShopUpgrade? = null

    @Expose
    var abiphoneContactAmount: Int? = null

    @Expose
    var hoppityEventStats: MutableMap<Int, HoppityEventStats> = HashMap()

    @Expose
    var hoppityStatLiveDisplayToggledOff: Boolean = false

    class HoppityEventStats {
        @Expose
        var mealsFound: MutableMap<HoppityEggType, Int> = EnumMap(HoppityEggType::class.java)

        @Expose
        var rabbitsFound: MutableMap<LorenzRarity, RabbitData> = EnumMap(LorenzRarity::class.java)

        class RabbitData {
            @Expose
            var uniques: Int = 0

            @Expose
            var dupes: Int = 0

            @Expose
            var strays: Int = 0

            override fun hashCode(): Int {
                return Objects.hash(uniques, dupes, strays)
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null) return false
                if (javaClass != other.javaClass) return false
                val otherProp = other as RabbitData
                return otherProp.hashCode() == hashCode()
            }
        }

        @Expose
        var dupeChocolateGained: Long = 0

        @Expose
        var strayChocolateGained: Long = 0

        @Expose
        var millisInCf: Duration = Duration.ZERO

        @Expose
        var rabbitTheFishFinds: Int = 0

        class LeaderboardPosition(@field:Expose var position: Int, @field:Expose var percentile: Double) {
            override fun hashCode(): Int {
                return Objects.hash(position, percentile)
            }
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null) return false
                if (javaClass != other.javaClass) return false
                val otherProp = other as LeaderboardPosition
                return otherProp.hashCode() == hashCode()
            }
        }

        @Expose
        var initialLeaderboardPosition: LeaderboardPosition = LeaderboardPosition(-1, -1.0)

        @Expose
        var finalLeaderboardPosition: LeaderboardPosition = LeaderboardPosition(-1, -1.0)

        @Expose
        var lastLbUpdate: SimpleTimeMark = farPast()

        @Expose
        var summarized: Boolean = false

        override fun hashCode(): Int {
            return Objects.hash(
                mealsFound,
                rabbitsFound,
                dupeChocolateGained,
                strayChocolateGained,
                millisInCf,
                rabbitTheFishFinds,
                initialLeaderboardPosition,
                finalLeaderboardPosition,
                summarized
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null) return false
            if (javaClass != other.javaClass) return false
            val otherProp = other as HoppityEventStats
            return otherProp.hashCode() == hashCode()
        }
    }
}
