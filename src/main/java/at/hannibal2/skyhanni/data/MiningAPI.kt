package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ColdUpdateEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.events.mining.OreMinedEvent
import at.hannibal2.skyhanni.events.player.PlayerDeathEvent
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardPattern
import at.hannibal2.skyhanni.features.mining.OreBlock
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.countBy
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import io.netty.util.internal.ConcurrentSet
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object MiningAPI {

    private val group = RepoPattern.group("data.miningapi")
    private val glaciteAreaPattern by group.pattern("area.glacite", "Glacite Tunnels|Glacite Lake")
    private val dwarvenBaseCampPattern by group.pattern("area.basecamp", "Dwarven Base Camp")

    // TODO rename to include suffix "pattern", add regex test
    private val coldReset by group.pattern(
        "cold.reset",
        "§6The warmth of the campfire reduced your §r§b❄ Cold §r§6to §r§a0§r§6!|§c ☠ §r§7You froze to death§r§7.",
    )

    private data class MinedBlock(val ore: OreBlock, var confirmed: Boolean, val time: SimpleTimeMark = SimpleTimeMark.now())

    private var lastInitSound = SimpleTimeMark.farPast()

    private var waitingForInitBlock = false
    private var waitingForInitBlockPos: LorenzVec? = null
    private var waitingForInitSound = true

    private var waitingForEffMinerSound = false
    private var waitingForEffMinerBlock = false

    var inGlacite = false
    var inDwarvenMines = false
    var inCrystalHollows = false
    var inCrimsonIsle = false
    var inEnd = false
    var inSpidersDen = false

    var currentAreaOreBlocks = setOf<OreBlock>()

    private var lastSkyblockArea: String? = null

    private val recentClickedBlocks = ConcurrentSet<Pair<LorenzVec, SimpleTimeMark>>()
    private val surroundingMinedBlocks = ConcurrentLinkedQueue<Pair<MinedBlock, LorenzVec>>()
    private val allowedSoundNames = listOf("dig.glass", "dig.stone", "dig.gravel", "dig.cloth", "random.orb")

    var cold: Int = 0
        private set

    var lastColdUpdate = SimpleTimeMark.farPast()
    var lastColdReset = SimpleTimeMark.farPast()

    fun inGlaciteArea() = inGlacialTunnels() || IslandType.MINESHAFT.isInIsland()

    fun inDwarvenBaseCamp() = IslandType.DWARVEN_MINES.isInIsland() && dwarvenBaseCampPattern.matches(LorenzUtils.skyBlockArea)

    fun inRegularDwarven() = IslandType.DWARVEN_MINES.isInIsland() && !inGlacialTunnels()

    fun inCrystalHollows() = IslandType.CRYSTAL_HOLLOWS.isInIsland()

    fun inMineshaft() = IslandType.MINESHAFT.isInIsland()

    fun inGlacialTunnels() = IslandType.DWARVEN_MINES.isInIsland() && glaciteAreaPattern.matches(LorenzUtils.skyBlockArea)

    fun inCustomMiningIsland() = inAnyIsland(
        IslandType.DWARVEN_MINES,
        IslandType.MINESHAFT,
        IslandType.CRYSTAL_HOLLOWS,
        IslandType.THE_END,
        IslandType.CRIMSON_ISLE,
        IslandType.SPIDER_DEN,
    )

    fun inColdIsland() = inAnyIsland(IslandType.DWARVEN_MINES, IslandType.MINESHAFT)

    @SubscribeEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        val newCold = ScoreboardPattern.coldPattern.firstMatcher(event.scoreboard) {
            group("cold").toInt().absoluteValue
        } ?: return

        if (newCold != cold) {
            updateCold(newCold)
        }
    }

    @SubscribeEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!inCustomMiningIsland()) return
        if (event.clickType != ClickType.LEFT_CLICK) return
        if (OreBlock.getByStateOrNull(event.getBlockState) == null) return
        recentClickedBlocks += event.position to SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!inColdIsland()) return
        if (coldReset.matches(event.message)) {
            updateCold(0)
            lastColdReset = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (event.name == LorenzUtils.getPlayerName()) {
            updateCold(0)
            lastColdReset = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onPlaySound(event: PlaySoundEvent) {
        if (!inCustomMiningIsland()) return
        if (event.soundName !in allowedSoundNames) return
        if (waitingForInitSound) {
            if (event.soundName != "random.orb" && event.pitch == 0.7936508f) {
                val pos = event.location.roundLocationToBlock()
                if (recentClickedBlocks.none { it.first == pos }) return
                waitingForInitSound = false
                waitingForInitBlock = true
                waitingForInitBlockPos = event.location.roundLocationToBlock()
                lastInitSound = SimpleTimeMark.now()
            }
            return
        }
        if (waitingForEffMinerSound) {
            val lastBlock = surroundingMinedBlocks.lastOrNull()?.first ?: return
            if (lastBlock.confirmed) return
            waitingForEffMinerSound = false
            lastBlock.confirmed = true
            waitingForEffMinerBlock = true
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!inCustomMiningIsland()) return
        if (event.newState.block.let { it != Blocks.air && it != Blocks.bedrock }) return
        if (event.oldState.block.let { it == Blocks.air || it == Blocks.bedrock }) return
        if (event.oldState.block == Blocks.air) return
        val pos = event.location
        if (pos.distanceToPlayer() > 7) return

        if (lastInitSound.passedSince() > 100.milliseconds) return

        val ore = OreBlock.getByStateOrNull(event.oldState) ?: return

        if (waitingForInitBlock) {
            if (waitingForInitBlockPos != pos) return
            waitingForInitBlock = false
            surroundingMinedBlocks += MinedBlock(ore, true) to pos
            waitingForEffMinerBlock = true
            return
        }
        if (waitingForEffMinerBlock) {
            if (surroundingMinedBlocks.any { it.second == pos }) return
            waitingForEffMinerBlock = false
            surroundingMinedBlocks += MinedBlock(ore, false) to pos
            waitingForEffMinerSound = true
            return
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!inCustomMiningIsland()) return

        if (LorenzUtils.lastWorldSwitch.passedSince() < 4.seconds) return
        updateLocation()

        if (currentAreaOreBlocks.isEmpty()) return

        // if somehow you take more than 20 seconds to mine a single block, congrats
        recentClickedBlocks.removeIf { it.second.passedSince() >= 20.seconds }
        surroundingMinedBlocks.removeIf { it.first.time.passedSince() >= 20.seconds }

        if (waitingForInitSound) return
        if (lastInitSound.passedSince() < 200.milliseconds) return

        resetOreEvent()

        if (surroundingMinedBlocks.isEmpty()) return

        val originalBlock = surroundingMinedBlocks.firstOrNull { it.first.confirmed }?.first ?: run {
            surroundingMinedBlocks.clear()
            recentClickedBlocks.clear()
            return
        }

        val extraBlocks = surroundingMinedBlocks.filter { it.first.confirmed }.countBy { it.first.ore }

        OreMinedEvent(originalBlock.ore, extraBlocks).post()

        surroundingMinedBlocks.clear()
        recentClickedBlocks.removeIf { it.second.passedSince() >= originalBlock.time.passedSince() }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        if (cold != 0) updateCold(0)
        lastColdReset = SimpleTimeMark.now()
        recentClickedBlocks.clear()
        surroundingMinedBlocks.clear()
        currentAreaOreBlocks = setOf()
        resetOreEvent()
    }

    private fun resetOreEvent() {
        lastInitSound = SimpleTimeMark.farPast()
        waitingForInitSound = true
        waitingForInitBlock = false
        waitingForInitBlockPos = null
        waitingForEffMinerSound = false
        waitingForEffMinerBlock = false
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Mining API")
        if (!inCustomMiningIsland()) {
            event.addIrrelevant("not in a mining island")
            return
        }

        event.addData {
            if (lastInitSound.isFarPast()) {
                add("lastInitSound: never")
            } else {
                add("lastInitSound: ${lastInitSound.passedSince().format()}")
            }
            add("waitingForInitSound: $waitingForInitSound")
            add("waitingForInitBlock: $waitingForInitBlock")
            add("waitingForInitBlockPos: $waitingForInitBlockPos")
            add("waitingForEffMinerSound: $waitingForEffMinerSound")
            add("waitingForEffMinerBlock: $waitingForEffMinerBlock")
            add("recentlyClickedBlocks: ${recentClickedBlocks.joinToString { "(${it.first.toCleanString()}" }}")
        }
    }

    private fun updateCold(newCold: Int) {
        // Hypixel sends cold data once in scoreboard even after resetting it
        if (cold == 0 && lastColdUpdate.passedSince() < 1.seconds) return
        lastColdUpdate = SimpleTimeMark.now()
        ColdUpdateEvent(newCold).postAndCatch()
        cold = newCold
    }

    private fun updateLocation() {
        val currentArea = LorenzUtils.skyBlockArea
        // TODO add area change event with HypixelData.skyBlockArea instead
        if (currentArea == lastSkyblockArea) return
        lastSkyblockArea = currentArea

        inGlacite = inGlaciteArea()
        inDwarvenMines = inRegularDwarven()
        inCrystalHollows = inCrystalHollows()
        inCrimsonIsle = IslandType.CRIMSON_ISLE.isInIsland()
        inEnd = IslandType.THE_END.isInIsland()
        inSpidersDen = IslandType.SPIDER_DEN.isInIsland()

        currentAreaOreBlocks = OreBlock.entries.filter { it.checkArea() }.toSet()
    }
}
