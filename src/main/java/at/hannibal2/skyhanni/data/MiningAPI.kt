package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ColdUpdateEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.events.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.WorldChangeEvent
import at.hannibal2.skyhanni.events.mining.OreMinedEvent
import at.hannibal2.skyhanni.events.player.PlayerDeathEvent
import at.hannibal2.skyhanni.events.skyblock.ScoreboardAreaChangeEvent
import at.hannibal2.skyhanni.features.mining.OreBlock
import at.hannibal2.skyhanni.features.mining.isTitanium
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.countBy
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import io.netty.util.internal.ConcurrentSet
import net.minecraft.init.Blocks
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object MiningAPI {

    private val group = RepoPattern.group("data.miningapi")
    private val glaciteAreaPattern by group.pattern("area.glacite", "Glacite Tunnels|Great Glacite Lake")
    private val dwarvenBaseCampPattern by group.pattern("area.basecamp", "Dwarven Base Camp")

    // TODO add regex tests
    private val coldResetPattern by group.pattern(
        "cold.reset",
        "§6The warmth of the campfire reduced your §r§b❄ Cold §r§6to §r§a0§r§6!|§c ☠ §r§7You froze to death§r§7\\.",
    )

    /**
     * REGEX-TEST: Cold: §b-1❄
     */
    val coldPattern by group.pattern(
        "cold",
        "(?:§.)*Cold: §.(?<cold>-?\\d+)❄",
    )

    private val pickbobulusGroup = group.group("pickobulus")

    /**
     * REGEX-TEST: §aYou used your §r§6Pickobulus §r§aPickaxe Ability!
     */

    private val pickobulusUsePattern by pickbobulusGroup.pattern(
        "use",
        "§aYou used your §r§6Pickobulus §r§aPickaxe Ability!",
    )

    // TODO add regex test
    private val pickobulusEndPattern by pickbobulusGroup.pattern(
        "end",
        "§7Your §r§aPickobulus §r§7destroyed §r§e(?<amount>[\\d,.]+) §r§7blocks!",
    )

    /**
     * REGEX-TEST: §7Your §r§aPickobulus §r§7didn't destroy any blocks!
     */
    private val pickobulusFailPattern by pickbobulusGroup.pattern(
        "fail",
        "§7Your §r§aPickobulus §r§7didn't destroy any blocks!",
    )

    private data class MinedBlock(val ore: OreBlock, var confirmed: Boolean) {
        val time: SimpleTimeMark = SimpleTimeMark.now()
    }

    // normal mining
    private val recentClickedBlocks = ConcurrentSet<Pair<LorenzVec, SimpleTimeMark>>()
    private val surroundingMinedBlocks = ConcurrentLinkedQueue<Pair<MinedBlock, LorenzVec>>()

    private var lastInitSound = SimpleTimeMark.farPast()

    private var initBlockPos: LorenzVec? = null
    private var waitingForInitSound = true

    private var waitingForEffMinerSound = false
    private var waitingForEffMinerBlock = false

    // pickobulus
    private var lastPickobulusUse = SimpleTimeMark.farPast()
    private var lastPickobulusExplosion = SimpleTimeMark.farPast()
    private var pickobulusExplosionPos: LorenzVec? = null
    private val pickobulusMinedBlocks = ConcurrentLinkedQueue<Pair<LorenzVec, OreBlock>>()

    private val pickobulusActive get() = lastPickobulusUse.passedSince() < 2.seconds

    private var pickobulusWaitingForSound = false
    private var pickobulusWaitingForBlock = false

    // oreblock data
    var inGlacite = false
    var inTunnels = false
    var inMineshaft = false
    var inDwarvenMines = false
    var inCrystalHollows = false
    var inCrimsonIsle = false
    var inEnd = false
    var inSpidersDen = false

    var currentAreaOreBlocks = setOf<OreBlock>()
        private set

    private val allowedSoundNames = setOf("dig.glass", "dig.stone", "dig.gravel", "dig.cloth", "random.orb")

    var cold: Int = 0
        private set

    var lastColdUpdate = SimpleTimeMark.farPast()
        private set
    var lastColdReset = SimpleTimeMark.farPast()
        private set

    private var lastOreMinedTime = SimpleTimeMark.farPast()

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

    fun inAdvancedMiningIsland() = inAnyIsland(IslandType.DWARVEN_MINES, IslandType.CRYSTAL_HOLLOWS, IslandType.MINESHAFT)

    fun inMiningIsland() = inAdvancedMiningIsland() || inAnyIsland(IslandType.GOLD_MINES, IslandType.DEEP_CAVERNS)

    fun inColdIsland() = inAnyIsland(IslandType.DWARVEN_MINES, IslandType.MINESHAFT)

    @HandleEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        val newCold = coldPattern.firstMatcher(event.added) {
            group("cold").toInt().absoluteValue
        } ?: return

        if (newCold != cold) {
            updateCold(newCold)
        }
    }

    @HandleEvent
    fun onBlockClick(event: BlockClickEvent) {
        if (!inCustomMiningIsland()) return
        if (event.clickType != ClickType.LEFT_CLICK) return
        if (OreBlock.getByStateOrNull(event.getBlockState) == null) return
        recentClickedBlocks += event.position to SimpleTimeMark.now()
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!inColdIsland()) return
        if (coldResetPattern.matches(event.message)) {
            updateCold(0)
            lastColdReset = SimpleTimeMark.now()
            return
        }
        if (pickobulusUsePattern.matches(event.message)) {
            lastPickobulusUse = SimpleTimeMark.now()
            return
        }
        if (pickobulusFailPattern.matches(event.message)) {
            resetPickobulusEvent()
            pickobulusMinedBlocks.clear()
            return
        }
        pickobulusEndPattern.matchMatcher(event.message) {
            val amount = group("amount").formatInt()
            resetPickobulusEvent()
            val blocks = pickobulusMinedBlocks.take(amount).countBy { it.second }
            if (blocks.isNotEmpty()) OreMinedEvent(null, blocks).post()
            pickobulusMinedBlocks.clear()
            return
        }

    }

    @HandleEvent
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (event.name == LorenzUtils.getPlayerName()) {
            updateCold(0)
            lastColdReset = SimpleTimeMark.now()
        }
    }

    @HandleEvent
    fun onPlaySound(event: PlaySoundEvent) {
        if (!inCustomMiningIsland()) return
        if (event.soundName == "random.explode" && lastPickobulusUse.passedSince() < 5.seconds) {
            lastPickobulusExplosion = SimpleTimeMark.now()
            pickobulusExplosionPos = event.location
            pickobulusWaitingForSound = true
            return
        }
        if (event.soundName !in allowedSoundNames) return
        if (pickobulusActive && pickobulusWaitingForSound) {
            pickobulusWaitingForSound = false
            pickobulusWaitingForBlock = true
            return
        }
        if (waitingForInitSound) {
            if (event.soundName != "random.orb" && event.pitch == 0.7936508f) {
                val pos = event.location.roundLocationToBlock()
                if (recentClickedBlocks.none { it.first == pos }) return
                waitingForInitSound = false
                waitingForEffMinerBlock = true
                initBlockPos = event.location.roundLocationToBlock()
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

    @HandleEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!inCustomMiningIsland()) return
        val oldState = event.oldState
        val newState = event.newState
        val oldBlock = oldState.block
        val newBlock = newState.block

        if (oldState == newState) return
        if (oldBlock == Blocks.air || oldBlock == Blocks.bedrock) return
        if (newBlock != Blocks.air && newBlock != Blocks.bedrock && !isTitanium(newState)) return

        val pos = event.location
        if (pickobulusActive && pickobulusWaitingForBlock) {
            val explosionPos = pickobulusExplosionPos ?: return
            if (explosionPos.distance(pos) > 15) return
            val ore = OreBlock.getByStateOrNull(oldState) ?: return
            if (pickobulusMinedBlocks.any { it.first == pos }) return
            pickobulusMinedBlocks += pos to ore
            pickobulusWaitingForBlock = false
            pickobulusWaitingForSound = true
            return
        }

        if (lastInitSound.passedSince() > 100.milliseconds) return
        if (pos.distanceToPlayer() > 7) return

        val ore = OreBlock.getByStateOrNull(oldState) ?: return

        if (initBlockPos == pos) {
            surroundingMinedBlocks += MinedBlock(ore, true) to pos
            runEvent()
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

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!inCustomMiningIsland()) return
        if (currentAreaOreBlocks.isEmpty()) return

        // if somehow you take more than 20 seconds to mine a single block, congrats
        recentClickedBlocks.removeIf { it.second.passedSince() >= 20.seconds }
        surroundingMinedBlocks.removeIf { it.first.time.passedSince() >= 20.seconds }

        if (!waitingForInitSound && lastInitSound.passedSince() > 200.milliseconds) {
            resetOreEvent()
        }
        if (!lastPickobulusUse.isFarPast() && lastPickobulusUse.passedSince() > 5.seconds) {
            resetPickobulusEvent()
            pickobulusMinedBlocks.clear()
        }
    }

    @HandleEvent
    fun onAreaChange(event: ScoreboardAreaChangeEvent) {
        if (!inCustomMiningIsland()) return
        updateLocation()
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        updateLocation()
    }

    private fun runEvent() {
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

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        if (cold != 0) updateCold(0)
        lastColdReset = SimpleTimeMark.now()
        recentClickedBlocks.clear()
        surroundingMinedBlocks.clear()
        pickobulusMinedBlocks.clear()
        currentAreaOreBlocks = setOf()
        resetOreEvent()
        resetPickobulusEvent()
        lastOreMinedTime = SimpleTimeMark.farPast()
    }

    private fun resetOreEvent() {
        lastInitSound = SimpleTimeMark.farPast()
        waitingForInitSound = true
        initBlockPos = null
        waitingForEffMinerSound = false
        waitingForEffMinerBlock = false
    }

    private fun resetPickobulusEvent() {
        lastPickobulusUse = SimpleTimeMark.farPast()
        lastPickobulusExplosion = SimpleTimeMark.farPast()
        pickobulusExplosionPos = null
        pickobulusWaitingForSound = false
        pickobulusWaitingForBlock = false
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onOreMined(event: OreMinedEvent) {
        lastOreMinedTime = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Mining API")
        if (!inCustomMiningIsland()) {
            event.addIrrelevant("not in a mining island")
            return
        }
        if (lastOreMinedTime.passedSince() > 30.seconds) {
            event.addIrrelevant("not mined recently")
            return
        }

        event.addData {
            if (lastInitSound.isFarPast()) {
                add("lastInitSound: never")
            } else {
                add("lastInitSound: ${lastInitSound.passedSince().format()}")
            }
            add("waitingForInitSound: $waitingForInitSound")
            add("waitingForInitBlockPos: $initBlockPos")
            add("waitingForEffMinerSound: $waitingForEffMinerSound")
            add("waitingForEffMinerBlock: $waitingForEffMinerBlock")
            add("recentlyClickedBlocks: ${recentClickedBlocks.joinToString { "(${it.first.toCleanString()}" }}")
        }
    }

    private fun updateCold(newCold: Int) {
        // Hypixel sends cold data once in scoreboard even after resetting it
        if (cold == 0 && lastColdUpdate.passedSince() < 1.seconds) return
        lastColdUpdate = SimpleTimeMark.now()
        ColdUpdateEvent(newCold).post()
        cold = newCold
    }

    private fun updateLocation() {
        inGlacite = inGlaciteArea()
        inTunnels = inGlacialTunnels()
        inMineshaft = inMineshaft()
        inDwarvenMines = inRegularDwarven()
        inCrystalHollows = inCrystalHollows()
        inCrimsonIsle = IslandType.CRIMSON_ISLE.isInIsland()
        inEnd = IslandType.THE_END.isInIsland()
        inSpidersDen = IslandType.SPIDER_DEN.isInIsland()

        currentAreaOreBlocks = OreBlock.entries.filter { it.checkArea() }.toSet()
    }
}
