package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.misc.update.UpdateManager
import at.hannibal2.skyhanni.features.misc.visualwords.ModifyVisualWords
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI
import at.hannibal2.skyhanni.mixins.transformers.AccessorGuiEditSign
import at.hannibal2.skyhanni.test.SkyBlockIslandTest
import at.hannibal2.skyhanni.test.TestBingo
import at.hannibal2.skyhanni.utils.ChatUtils.lastButtonClicked
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.StringUtils.capAtMinecraftLength
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.stripHypixelMessage
import at.hannibal2.skyhanni.utils.StringUtils.toDashlessUUID
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.google.gson.JsonPrimitive
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.FMLCommonHandler
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.util.regex.Matcher
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object LorenzUtils {

    val connectedToHypixel get() = HypixelData.hypixelLive || HypixelData.hypixelAlpha

    val onHypixel get() = connectedToHypixel && Minecraft.getMinecraft().thePlayer != null

    val isOnAlphaServer get() = onHypixel && HypixelData.hypixelAlpha

    val inSkyBlock get() = onHypixel && HypixelData.skyBlock

    val inHypixelLobby get() = onHypixel && HypixelData.inLobby

    @Deprecated("Use DungeonAPI.inDungeon() instead", ReplaceWith("DungeonAPI.inDungeon()"))
    val inDungeons get() = DungeonAPI.inDungeon()

    /**
     * Consider using [IslandType.isInIsland] instead
     */
    val skyBlockIsland get() = SkyBlockIslandTest.testIsland ?: HypixelData.skyBlockIsland

    val skyBlockArea get() = if (inSkyBlock) HypixelData.skyBlockArea else null

    val inKuudraFight get() = inSkyBlock && KuudraAPI.inKuudra()

    val noTradeMode get() = HypixelData.noTrade

    val isStrandedProfile get() = inSkyBlock && HypixelData.stranded

    val isBingoProfile get() = inSkyBlock && (HypixelData.bingo || TestBingo.testBingo)

    val isIronmanProfile get() = inSkyBlock && HypixelData.ironman

    val lastWorldSwitch get() = HypixelData.joinedWorld

    val isAprilFoolsDay: Boolean
        get() {
            val itsTime = LocalDate.now().let { it.month == Month.APRIL && it.dayOfMonth == 1 }
            val always = SkyHanniMod.feature.dev.debug.alwaysFunnyTime
            val never = SkyHanniMod.feature.dev.debug.neverFunnyTime
            val result = (!never && (always || itsTime))
            if (previousApril != result) {
                ModifyVisualWords.textCache.clear()
            }
            previousApril = result
            return result
        }

    val debug: Boolean = onHypixel && SkyHanniMod.feature.dev.debug.enabled

    private var previousApril = false

    // TODO move into lorenz logger. then rewrite lorenz logger and use something different entirely
    fun SimpleDateFormat.formatCurrentTime(): String = this.format(System.currentTimeMillis())

    // TODO move to string utils
    @Deprecated("outdated", ReplaceWith("originalMessage.stripHypixelMessage()"))
    fun stripVanillaMessage(originalMessage: String): String {
        return originalMessage.stripHypixelMessage()
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        val result = kotlin.math.round(this * multiplier) / multiplier
        val a = result.toString()
        val b = toString()
        return if (a.length > b.length) this else result
    }

    fun Float.round(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        val result = kotlin.math.round(this * multiplier) / multiplier
        val a = result.toString().length
        val b = toString().length
        return if (a > b) this else result.toFloat()
    }

    // TODO replace all calls with regex
    @Deprecated("Do not use complicated string operations", ReplaceWith("Regex"))
    fun String.between(start: String, end: String): String = this.split(start, end)[1]

    // TODO use derpy() on every use case
    val EntityLivingBase.baseMaxHealth: Int
        get() = this.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.toInt()

    // TODO create extension function
    fun formatPercentage(percentage: Double): String = formatPercentage(percentage, "0.00")

    fun formatPercentage(percentage: Double, format: String?): String =
        DecimalFormat(format).format(percentage * 100).replace(',', '.') + "%"

    // TODO move into chat utils
    fun consoleLog(text: String) {
        SkyHanniMod.consoleLog(text)
    }

    // TODO move into crimson api
    fun getPointsForDojoRank(rank: String): Int {
        return when (rank) {
            "S" -> 1000
            "A" -> 800
            "B" -> 600
            "C" -> 400
            "D" -> 200
            "F" -> 0
            else -> 0
        }
    }

    // TODO move into time utils
    fun getSBMonthByName(month: String): Int {
        var monthNr = 0
        for (i in 1..12) {
            val monthName = SkyBlockTime.monthName(i)
            if (month == monthName) {
                monthNr = i
            }
        }
        return monthNr
    }

    fun getPlayerUuid() = getRawPlayerUuid().toDashlessUUID()

    fun getRawPlayerUuid() = Minecraft.getMinecraft().thePlayer.uniqueID

    fun getPlayerName(): String = Minecraft.getMinecraft().thePlayer.name

    fun getPlayer(): EntityPlayerSP? = Minecraft.getMinecraft()?.thePlayer

    // TODO move into renderable utils
    fun fillTable(
        data: List<DisplayTableEntry>,
        padding: Int = 1,
        itemScale: Double = NEUItems.itemFontSize,
    ): Renderable {
        val sorted = data.sortedByDescending { it.sort }

        val outerList = mutableListOf<List<Renderable>>()
        for (entry in sorted) {
            val item = entry.item.getItemStackOrNull()?.let {
                Renderable.itemStack(it, scale = itemScale)
            } ?: continue
            val left = Renderable.hoverTips(
                entry.left,
                tips = entry.hover,
                highlightsOnHoverSlots = entry.highlightsOnHoverSlots,
            )
            val right = Renderable.string(entry.right)
            outerList.add(listOf(item, left, right))
        }
        return Renderable.table(outerList, xPadding = 5, yPadding = padding)
    }

    fun setTextIntoSign(text: String, line: Int = 0) {
        val gui = Minecraft.getMinecraft().currentScreen
        if (gui !is AccessorGuiEditSign) return
        gui.tileSign.signText[line] = ChatComponentText(text)
    }

    fun addTextIntoSign(addedText: String) {
        val gui = Minecraft.getMinecraft().currentScreen
        if (gui !is AccessorGuiEditSign) return
        val lines = gui.tileSign.signText
        val index = gui.editLine
        val text = lines[index].unformattedText + addedText
        lines[index] = ChatComponentText(text.capAtMinecraftLength(91))
    }

    // TODO move into string api
    fun colorCodeToRarity(colorCode: Char): String {
        return when (colorCode) {
            'f' -> "Common"
            'a' -> "Uncommon"
            '9' -> "Rare"
            '5' -> "Epic"
            '6' -> "Legendary"
            'd' -> "Mythic"
            'b' -> "Divine"
            '4' -> "Supreme" // legacy items
            else -> "Special"
        }
    }

    @Deprecated("do not use List<Any>, use List<Renderable> instead", ReplaceWith(""))
    inline fun <reified T : Enum<T>> MutableList<List<Any>>.addSelector(
        prefix: String,
        getName: (T) -> String,
        isCurrent: (T) -> Boolean,
        crossinline onChange: (T) -> Unit,
    ) {
        add(buildSelector<T>(prefix, getName, isCurrent, onChange))
    }

    @Deprecated("do not use List<Any>, use List<Renderable> instead", ReplaceWith(""))
    inline fun <reified T : Enum<T>> buildSelector(
        prefix: String,
        getName: (T) -> String,
        isCurrent: (T) -> Boolean,
        crossinline onChange: (T) -> Unit,
    ) = buildList {
        add(prefix)
        for (entry in enumValues<T>()) {
            val display = getName(entry)
            if (isCurrent(entry)) {
                add("§a[$display]")
            } else {
                add("§e[")
                add(
                    Renderable.link("§e$display") {
                        onChange(entry)
                    },
                )
                add("§e]")
            }
            add(" ")
        }
    }

    @Deprecated("do not use List<Any>, use List<Renderable> instead", ReplaceWith(""))
    inline fun MutableList<List<Any>>.addButton(
        prefix: String,
        getName: String,
        crossinline onChange: () -> Unit,
        tips: List<String> = emptyList(),
    ) {
        val onClick = {
            if ((System.currentTimeMillis() - lastButtonClicked) > 150) { // funny thing happen if I don't do that
                onChange()
                SoundUtils.playClickSound()
                lastButtonClicked = System.currentTimeMillis()
            }
        }
        add(
            buildList {
                add(prefix)
                add("§a[")
                if (tips.isEmpty()) {
                    add(Renderable.link("§e$getName", false, onClick))
                } else {
                    add(Renderable.clickAndHover("§e$getName", tips, false, onClick))
                }
                add("§a]")
            },
        )
    }

    fun GuiEditSign.isRancherSign(): Boolean {
        if (this !is AccessorGuiEditSign) return false

        val tileSign = (this as AccessorGuiEditSign).tileSign
        return (
            tileSign.signText[1].unformattedText.removeColor() == "^^^^^^" &&
                tileSign.signText[2].unformattedText.removeColor() == "Set your" &&
                tileSign.signText[3].unformattedText.removeColor() == "speed cap!"
            )
    }

    fun IslandType.isInIsland() = inSkyBlock && skyBlockIsland == this

    fun inAnyIsland(vararg islandTypes: IslandType) = inSkyBlock && HypixelData.skyBlockIsland in islandTypes
    fun inAnyIsland(islandTypes: Collection<IslandType>) = inSkyBlock && HypixelData.skyBlockIsland in islandTypes

    fun GuiContainerEvent.SlotClickEvent.makeShiftClick() {
        if (this.clickedButton == 1 && slot?.stack?.getItemCategoryOrNull() == ItemCategory.SACK) return
        slot?.slotNumber?.let { slotNumber ->
            Minecraft.getMinecraft().playerController.windowClick(
                container.windowId,
                slotNumber,
                0,
                1,
                Minecraft.getMinecraft().thePlayer,
            )
            this.cancel()
        }
    }

    // TODO move into mayor api
    val isDerpy by RecalculatingValue(1.seconds) { Perk.DOUBLE_MOBS_HP.isActive }

    // TODO move into mayor api
    fun Int.derpy() = if (isDerpy) this / 2 else this

    // TODO move into mayor api
    fun Int.ignoreDerpy() = if (isDerpy) this * 2 else this

    // TODO move into json api
    val JsonPrimitive.asIntOrNull get() = takeIf { it.isNumber }?.asInt

    fun sendTitle(text: String, duration: Duration, height: Double = 1.8, fontSize: Float = 4f) {
        TitleManager.sendTitle(text, duration, height, fontSize)
    }

    inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String): T? {
        val enums = enumValues<T>()
        return enums.firstOrNull { it.name == name }
    }

    inline fun <reified T : Enum<T>> enumValueOf(name: String) =
        enumValueOfOrNull<T>(name)
            ?: error("Unknown enum constant for ${enumValues<T>().first().name.javaClass.simpleName}: '$name'")

    inline fun <reified T : Enum<T>> enumJoinToPattern(noinline transform: (T) -> CharSequence = { it.name }) =
        enumValues<T>().joinToString("|", transform = transform)

    inline fun <reified T : Enum<T>> T.isAnyOf(vararg array: T): Boolean = array.contains(this)

    fun shutdownMinecraft(reason: String? = null) {
        System.err.println("SkyHanni-${SkyHanniMod.version} forced the game to shutdown.")
        reason?.let {
            System.err.println("Reason: $it")
        }
        FMLCommonHandler.instance().handleExit(-1)
    }

    /**
     * Get the group, otherwise, return null
     * @param groupName The group name in the pattern
     */
    @Deprecated("Use the new one instead", ReplaceWith("RegexUtils.groupOrNull"))
    fun Matcher.groupOrNull(groupName: String): String? = runCatching { this.group(groupName) }.getOrNull()

    @Deprecated("Use the new one instead", ReplaceWith("RegexUtils.hasGroup"))
    fun Matcher.hasGroup(groupName: String): Boolean = groupOrNull(groupName) != null

    // TODO move into Mining API
    fun inAdvancedMiningIsland() = IslandType.DWARVEN_MINES.isInIsland() ||
        IslandType.CRYSTAL_HOLLOWS.isInIsland() || IslandType.MINESHAFT.isInIsland()

    fun inMiningIsland() = IslandType.GOLD_MINES.isInIsland() ||
        IslandType.DEEP_CAVERNS.isInIsland() || inAdvancedMiningIsland()

    fun isBetaVersion() = UpdateManager.isCurrentlyBeta()

    private var lastGuiTime = SimpleTimeMark.farPast()

    fun isAnyGuiActive(): Boolean {
        val gui = Minecraft.getMinecraft().currentScreen != null
        if (gui) {
            lastGuiTime = 3.ticks.fromNow()
        }
        return !lastGuiTime.isInPast()
    }

    // TODO move into location utils
    fun AxisAlignedBB.getCorners(y: Double): List<LorenzVec> {
        val cornerOne = LorenzVec(minX, y, minZ)
        val cornerTwo = LorenzVec(minX, y, maxZ)
        val cornerThree = LorenzVec(maxX, y, maxZ)
        val cornerFour = LorenzVec(maxX, y, minZ)

        return listOf(cornerOne, cornerTwo, cornerThree, cornerFour)
    }
}
