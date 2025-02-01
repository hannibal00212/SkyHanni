package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockAt
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.decodeBase64
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.Gson
import net.minecraft.block.material.Material
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object FruitDigging {

    private val config get() = SkyHanniMod.feature.event.carnival.fruitDigging

    private var shovelMode = ""
    private var lastTimeDigging = SimpleTimeMark.farPast()
    private var lastMined = mutableListOf<LorenzVec>()
    private var hasStarted = false

    private var itemsOnGround = mutableMapOf<LorenzVec, EntityInfo>()

    private var mineBlocks = mutableMapOf<LorenzVec, PosInfo>()

    private val patternGroup = RepoPattern.group("event.carnival")

    /**
     * REGEX-TEST: Dowsing Mode: Mines
     * REGEX-TEST: Dowsing Mode: Anchor
     * REGEX-TEST: Dowsing Mode: Treasure
     */

    private val modePattern by patternGroup.pattern(
        "info.tooltip.mode",
        "Dowsing Mode: (?<mode>\\S+)",
    )

    /**
     * REGEX-TEST: MINES! There are 0 bombs hidden nearby.
     * REGEX-TEST: MINES! There are 4 bombs hidden nearby.
     */
    val minesPattern by patternGroup.pattern(
        "info.chat.mines",
        "^MINES! There are (?<amount>\\d) bombs hidden nearby\\.\$",
    )

    /**
     * REGEX-TEST: TREASURE! There is a Mango nearby.
     * REGEX-TEST: TREASURE! There is an Apple nearby.
     */
    val treasurePattern by patternGroup.pattern(
        "info.chat.treasure",
        "^TREASURE! There is an? (?<fruit>\\S+) nearby\\.$",
    )

    val noFruitPattern by patternGroup.pattern(
        "info.chat.nofruit",
        ".*There are no fruits nearby!",
    )

    /**
     * REGEX-TEST: [NPC] Carnival Pirateman: Good luck, matey!
     */
    private val startedPattern by patternGroup.pattern(
        "info.chat.started",
        "^\\[NPC] Carnival Pirateman: Good luck, matey!$",
    )

    /**
     * REGEX-TEST:                                Fruit Digging
     */
    private val endedPattern by patternGroup.pattern(
        "info.chat.ended",
        "^ {31}Fruit Digging$",
    )

    private fun LorenzVec.convertCords(): Pair<Int, Int> = add(112, 0, 11).let { it.x.toInt() to it.z.toInt() }
    private fun Pair<Int, Int>.convertCords(): LorenzVec = LorenzVec(first - 112, 72, second - 11)

    @SubscribeEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!isEnabled() || !hasStarted) return
        print(event.old to event.new)
        println(event.location.distanceToPlayer())

        if (event.location.distanceToPlayer() <= 7) {
            val itemHeld = InventoryUtils.getItemInHand() ?: return
            shovelMode = modePattern.matchMatcher(itemHeld.getLore()[3].removeColor()) { group("mode") }.toString()
            FruitDiggingSolver.ShovelType.active = when (shovelMode) {
                "Mines" -> FruitDiggingSolver.ShovelType.MINES
                "Treasure" -> FruitDiggingSolver.ShovelType.TREASURE
                "Anchor" -> FruitDiggingSolver.ShovelType.ANCHOR
                else -> FruitDiggingSolver.ShovelType.MINES
            }

            if (event.new == "sandstone") {
                lastTimeDigging = SimpleTimeMark.now()
                lastMined.add(event.location)
                println("Block converted")
            } else if (event.new == "sandstone_stairs") {
                mineBlocks[event.location] = PosInfo(true, null, null, null, null)
                val entry = mineBlocks.entries.find { it.key == lastMined.last() } ?: return
                entry.value.uncovered = true;
                entry.value.dropTypes = mutableSetOf(DropType.BOMB)
                FruitDiggingSolver.setBombed(event.location.convertCords())
            }

            if (mineBlocks.size != 49) {
                for (x in -112..-106) {
                    for (z in -11..-5) {
                        mineBlocks.putIfAbsent(LorenzVec(x, 72, z), PosInfo(false, null, null, null, null))
                    }
                }
            }
        }
    }

    private val fruitStack = mutableSetOf<LorenzVec>()
    private var lastPos = LorenzVec()
    private var ticksSinceLastFound = 0

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        for (entityItem in EntityUtils.getEntitiesNextToPlayer<EntityItem>(7.0)) {
            val position = entityItem.position.let { pos -> LorenzVec(pos.x, 72, pos.z) }
            val itemStack = entityItem.entityItem

            if (itemStack.item != Items.skull) continue

            val dropType =
                itemsOnGround[position]?.type ?: run { convertToType(null, itemStack)?.takeIf { it != DropType.NONE } } ?: continue

            /*             if (lastPos == position) { // TODO
                            fruitStack.clear()
                            break
                        } */

            if (fruitStack.contains(position)) continue

            fruitStack.add(position)
            ticksSinceLastFound = 0

            println("Detected $dropType")

            val mineBlocksEntry = mineBlocks.entries.find { it.key == position } ?: continue

            val types = mineBlocksEntry.value.dropTypes ?: mutableSetOf()
            types.add(dropType)
            mineBlocksEntry.value.dropTypes = types
            if (lastMined.lastOrNull() == position) mineBlocksEntry.value.uncovered = true
            itemsOnGround[position] = EntityInfo(itemStack, dropType)
        }
        if (ticksSinceLastFound > (fruitStack.count { itemsOnGround[it]!!.type == DropType.WATERMELON } * 5 + 3) * 3) {
            if (lastPos == fruitStack.firstOrNull()) {
                fruitStack.clear()
            }
            if (fruitStack.isNotEmpty()) {
                val pos = fruitStack.first()
                val dropType = itemsOnGround[pos]!!.type
                dig(dropType)
                if (dropType == DropType.RUM) {
                    rummed = true
                }
            }
        } else {
            ticksSinceLastFound++
        }
    }

    private fun isDug(pos: LorenzVec) = mineBlocks[pos]?.uncovered ?: false

    private var lastChat: String = ""
    private var rummed = false

    private fun dig(drop: DropType) {
        try {
            println("Fruit Stack: $fruitStack")
            val watermeloned = fruitStack.filter { isDug(it) }.drop(1).toSet()
            val anchor = (fruitStack - watermeloned).drop(1).firstOrNull()
            val result = if (rummed) 0 else when (FruitDiggingSolver.ShovelType.active) {
                FruitDiggingSolver.ShovelType.ANCHOR -> FruitDiggingSolver.ShovelType.active.getResult(lastChat)?.also { lastChat = "" }
                    ?: (anchor ?: watermeloned.firstOrNull())?.let {
                        val (x, z) = it.convertCords()
                        Triple(itemsOnGround[it]!!.type, x, z)
                    }

                else -> FruitDiggingSolver.ShovelType.active.getResult(lastChat)
            }
            if (result != null) {
                FruitDiggingSolver.onDig(
                    lastMined.get(lastMined.size - 1 - watermeloned.size).convertCords(),
                    drop,
                    if (rummed) null else FruitDiggingSolver.ShovelType.active,
                    result,
                )
                for (it in watermeloned) {
                    FruitDiggingSolver.setWatermeloned(
                        it.convertCords(),
                        itemsOnGround[it]!!.type,
                    ) // TODO Fix ANCHOR + WATERMELON on same Block
                }
                rummed = false
                lastPos = fruitStack.first()
            }
        } finally {
            fruitStack.clear()
        }
    }

    private fun reset() {
        itemsOnGround.clear()
        mineBlocks.clear()
        rummed = false
        lastChat = ""
        fruitStack.clear()
        lastPos = LorenzVec()
        FruitDiggingSolver.reset()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        val message = event.message.removeColor()

        if (startedPattern.matches(message)) {
            hasStarted = true
            reset()
            return
        }
        if (endedPattern.matches(message)) hasStarted = false

        if (lastTimeDigging.passedSince() > 1.seconds) return

        val entry = mineBlocks.entries.find { it.key == lastMined.last() } ?: return

        lastChat = message

        println("Chat ability")

        when (shovelMode) {
            "Mines" -> minesPattern.matchMatcher(message) {
                entry.value.uncovered = true
                entry.value.minesNear = group("amount").toInt()
            }

            "Treasure" -> {
                treasurePattern.matchMatcher(message) {
                    entry.value.uncovered = true
                    entry.value.highestFruit = convertToType(group("fruit"), null)

                }

                if (message == "TREASURE! There are no fruits nearby!") {
                    getAdjacent(lastMined.last()).forEach {
                        val types = mutableSetOf(DropType.BOMB, DropType.RUM)
                        if (it.value.dropTypes == null) it.value.dropTypes = types
                    }
                }
            }

            "Anchor" -> if (message == "ANCHOR! There are no fruits nearby!") {
                entry.value.uncovered = true
                entry.value.lowestFruit = DropType.NONE
                getAdjacent(lastMined.last()).forEach {
                    val types = mutableSetOf(DropType.BOMB, DropType.RUM)
                    if (it.value.dropTypes == null) it.value.dropTypes = types
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        for (info in FruitDiggingSolver.getBoardState()) {
            val text: String
            val color: Color
            when {
                info.isBombed() -> {
                    text = if (info.possibilities.size <= 2) info.fancyPrint() else ""
                    color = LorenzColor.GRAY.toColor()
                }

                !info.diggable -> {
                    text = info.fancyPrint()
                    color = config.uncovered.toColor()
                }

                info.isOnlyBombOrRum() -> {
                    text = info.fancyPrint()
                    color = config.mine.toColor()
                }

                info.isOnlyFruit() -> {
                    text = info.fancyPrint()
                    color = config.safe.toColor()
                }

                info.possibilities.size <= 3 -> {
                    text = info.fancyPrint()
                    color = LorenzColor.BLUE.toColor()
                }

                else -> continue
            }
            val loc = info.pos.convertCords()

            event.drawWaypointFilled(loc, color, minimumAlpha = 0.5F)
            event.drawDynamicText(loc.add(0.0, 1.5, 0.0), text, 1.0)
        }
    }

    private fun getAdjacent(blockPos: LorenzVec): MutableMap<LorenzVec, PosInfo> {
        val directions = listOf(
            LorenzVec(1, 0, 0),
            LorenzVec(-1, 0, 0),
            LorenzVec(0, 0, 1),
            LorenzVec(0, 0, -1),
            LorenzVec(1, 0, 1),
            LorenzVec(1, 0, -1),
            LorenzVec(-1, 0, 1),
            LorenzVec(-1, 0, -1),
        )

        val validBlocks = mutableMapOf<LorenzVec, PosInfo>()

        for (direction in directions) {
            val pos = blockPos.plus(direction)
            if (pos.getBlockAt().material == Material.sand) {
                val entry = mineBlocks.entries.find { it.key == pos } ?: continue
                validBlocks[pos] = entry.value
            }
        }
        return validBlocks.ifEmpty { return mutableMapOf() }
    }

    fun convertToType(name: String?, itemStack: ItemStack?): DropType? {
        val skullTextureURL = if (itemStack != null) Gson().fromJson(
            itemStack.getSkullTexture()?.let { decodeBase64(it) },
            MinecraftTextures::class.java,
        )
            .textures.SKIN.url else ""
        val nameClean = name?.removeColor() ?: ""

        return DropType.entries.find { it.skullTexture == skullTextureURL || it.display.removeColor() == nameClean }
    }

    private fun isEnabled() =
        config.enabled && LorenzUtils.inSkyBlock && LorenzUtils.skyBlockArea == "Carnival"
}
