package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.events.ColdUpdateEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.events.mining.OreMinedEvent
import at.hannibal2.skyhanni.events.player.PlayerDeathEvent
import at.hannibal2.skyhanni.events.skyblock.ScoreboardAreaChangeEvent
import at.hannibal2.skyhanni.features.mining.OreBlock
import at.hannibal2.skyhanni.features.mining.isTitanium
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object MiningAPI {

    private val group = RepoPattern.group("data.miningapi")

    /**
     * REGEX-TEST: Glacite Tunnels
     * REGEX-TEST: Great Glacite Lake
     */
    private val glaciteAreaPattern by group.pattern("area.glacite", "Glacite Tunnels|Great Glacite Lake")
    private val dwarvenBaseCampPattern by group.pattern("area.basecamp", "Dwarven Base Camp")

    /**
     * REGEX-TEST: §6The warmth of the campfire reduced your §r§b❄ Cold §r§6to §r§a0§r§6!
     * REGEX-TEST: §c ☠ §r§7You froze to death§r§7.
     */
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

    /**
     * REGEX-TEST: §7Your §r§aPickobulus §r§7destroyed §r§e140 §r§7blocks!
     */
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

    private var lastClickedPos: LorenzVec? = null
    private var lastClicked = SimpleTimeMark.farPast()
    private var ignoreInit = false

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
        private set
    var inTunnels = false
        private set
    var inMineshaft = false
        private set
    var inDwarvenMines = false
        private set
    var inCrystalHollows = false
        private set
    var inCrimsonIsle = false
        private set
    var inEnd = false
        private set
    var inSpidersDen = false
        private set

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

    @SubscribeEvent
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
        val now = SimpleTimeMark.now()
        recentClickedBlocks += event.position to now
        lastClickedPos = event.position
        lastClicked = now
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
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
            if (event.soundName != "random.orb") {
                if (event.pitch != 0.7936508f) return
                val pos = event.location.roundLocationToBlock()
                if (recentClickedBlocks.none { it.first == pos }) return
                waitingForInitSound = false
                waitingForEffMinerBlock = true
                initBlockPos = event.location.roundLocationToBlock()
                lastInitSound = SimpleTimeMark.now()
            } else {
                if (lastClicked.passedSince() > 1.seconds) return
                val block = lastClickedPos ?: return
                val ore = OreBlock.getByStateOrNull(block.getBlockStateAt()) ?: return
                if (ore.hasInitSound) return
                ignoreInit = true
                waitingForInitSound = false
                waitingForEffMinerBlock = true
                lastInitSound = SimpleTimeMark.now()
            }
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

        if (waitingForEffMinerBlock && (!ignoreInit || !ore.hasInitSound)) {
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
        if (currentAreaOreBlocks.isEmpty()) return

        // if somehow you take more than 10 seconds to mine a single block, congrats
        recentClickedBlocks.removeIf { it.second.passedSince() >= 10.seconds }
        surroundingMinedBlocks.removeIf { it.first.time.passedSince() >= 5.seconds }

        if (!waitingForInitSound && lastInitSound.passedSince() > 200.milliseconds) {
            if (ignoreInit) runEvent()
            else resetOreEvent()
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

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        updateLocation()
    }

    private fun runEvent() {
        val ignoreFilter = ignoreInit
        resetOreEvent()

        if (surroundingMinedBlocks.isEmpty()) return

        val originalBlock = surroundingMinedBlocks.firstOrNull { it.first.confirmed }?.first ?: run {
            surroundingMinedBlocks.clear()
            recentClickedBlocks.clear()
            return
        }

        val extraBlocks = surroundingMinedBlocks.filter {
            // We can do this because all blocks that don't have an init sound also cannot be mined by
            // efficient miner when other blocks are mined.
            // The more correct way of doing this would be making sure the oretype of the originally mined
            // block matches
            if (ignoreFilter) it.first.ore == originalBlock.ore else it.first.confirmed
        }.countBy { it.first.ore }

        OreMinedEvent(originalBlock.ore, extraBlocks).post()
        lastOreMinedTime = SimpleTimeMark.now()

        surroundingMinedBlocks.clear()
        recentClickedBlocks.removeIf { it.second.passedSince() >= originalBlock.time.passedSince() }
        lastClickedPos = null
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        if (cold != 0) updateCold(0)
        lastColdReset = SimpleTimeMark.now()
        recentClickedBlocks.clear()
        surroundingMinedBlocks.clear()
        lastClickedPos = null
        pickobulusMinedBlocks.clear()
        currentAreaOreBlocks = setOf()
        resetOreEvent()
        resetPickobulusEvent()
        lastOreMinedTime = SimpleTimeMark.farPast()
        inDwarvenMines = false
        inCrystalHollows = false
        inGlacite = false
    }

    private fun resetOreEvent() {
        lastInitSound = SimpleTimeMark.farPast()
        waitingForInitSound = true
        ignoreInit = false
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

    @SubscribeEvent
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

        fun SimpleTimeMark.formatTime(): String {
            if (isFarPast()) return "never"
            return passedSince().format()
        }

        event.addData {
            add("lastClickedPos: ${lastClickedPos?.toCleanString()}")
            add("lastClicked: ${lastClicked.formatTime()}")
            add("ignoreInit: $ignoreInit")
            add("lastInitSound: ${lastInitSound.formatTime()}")
            add("initBlockPos: ${initBlockPos?.toCleanString()}")
            add("waitingForInitSound: $waitingForInitSound")
            add("waitingForEffMinerSound: $waitingForEffMinerSound")
            add("waitingForEffMinerBlock: $waitingForEffMinerBlock")
            add("")
            add("lastPickobulusUse: ${lastPickobulusUse.formatTime()}")
            add("lastPickobulusExplosion: ${lastPickobulusExplosion.formatTime()}")
            add("pickobulusExplosionPos: ${pickobulusExplosionPos?.toCleanString()}")
            add("pickobulusWaitingForSound: $pickobulusWaitingForSound")
            add("pickobulusWaitingForBlock: $pickobulusWaitingForBlock")
            add("")
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
