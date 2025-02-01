package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.entity.EntityMoveEvent
import at.hannibal2.skyhanni.events.garden.PlotChangeEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.garden.pests.PestApi
import at.hannibal2.skyhanni.features.garden.pests.SprayType
import at.hannibal2.skyhanni.features.misc.LockMouseLook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LocationUtils.isInside
import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.annotations.Expose
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.AxisAlignedBB
import java.awt.Color
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenPlotApi {

    private val patternGroup = RepoPattern.group("garden.plot")
    private val config get() = PestApi.config.spray

    /**
     * REGEX-TEST: §aPlot §7- §b4
     */
    private val plotNamePattern by patternGroup.pattern(
        "name",
        "§.Plot §7- §b(?<name>.*)",
    )

    /**
     * REGEX-TEST: §aThe Barn
     */
    private val barnNamePattern by patternGroup.pattern(
        "barnname",
        "§.(?<name>The Barn)",
    )

    /**
     * REGEX-TEST: §7Cleanup: §b0% Completed
     */
    private val uncleanedPlotPattern by patternGroup.pattern(
        "uncleaned",
        "§7Cleanup: .* (?:§.)*Completed",
    )

    /**
     * REGEX-TEST: §aUnlocked Garden §r§aPlot §r§7- §r§b10§r§a!
     */
    private val unlockPlotChatPattern by patternGroup.pattern(
        "chat.unlock",
        "§aUnlocked Garden §r§aPlot §r§7- §r§b(?<plot>.*)§r§a!",
    )

    /**
     * REGEX-TEST: §aPlot §r§7- §r§b10 §r§ais now clean!
     */
    private val cleanPlotChatPattern by patternGroup.pattern(
        "chat.clean",
        "§aPlot §r§7- §r§b(?<plot>.*) §r§ais now clean!",
    )
    private val plotSprayedPattern by patternGroup.pattern(
        "spray.target",
        "§a§lSPRAYONATOR! §r§7You sprayed §r§aPlot §r§7- §r§b(?<plot>.*) §r§7with §r§a(?<spray>.*)§r§7!"
    )

    /**
     * REGEX-TEST: §9§lSPLASH! §r§6Your §r§aGarden §r§6was cleared of all active §r§aSprayonator §r§6effects!
     * REGEX-TEST: §9§lSPLASH! §r§6Your §r§bGarden §r§6was cleared of all active §r§aSprayonator §r§6effects!
     */
    private val portableWasherPattern by patternGroup.pattern(
        "spray.cleared.portablewasher",
        "§9§lSPLASH! §r§6Your §r§[ba]Garden §r§6was cleared of all active §r§aSprayonator §r§6effects!",
    )

    /**
     * REGEX-TEST: Spray: §r§7None
     * REGEX-TEST: Spray: §r§aCompost §r§7(12m)
     * REGEX-TEST: Spray: §r§aCompost §r§7(1m 3s)
     * REGEX-TEST: Spray: §r§aCompost §r§7(53s)
     * REGEX-TEST: Spray: §r§aHoney Jar §r§7(53s)
     */
    private val plotSprayedTablistPattern by patternGroup.pattern(
        "tablist.spray",
        "Spray: §r§[7a](?<spray>[\\w\\s]+)(?:§r§7\\((?:(?<minutes>\\d+)m)? ?(?:(?<seconds>\\d+)s)?\\))?",
    )
    var plots = listOf<Plot>()

    fun getCurrentPlot(): Plot? {
        return plots.firstOrNull { it.isPlayerInside() }
    }

    class Plot(val id: Int, var inventorySlot: Int, val box: AxisAlignedBB, val middle: LorenzVec)

    private var currentPlot: Plot? = null

    fun checkCurrentPlot() {
        if (getCurrentPlot() != currentPlot) {
            currentPlot = getCurrentPlot()
            updateCurrentPlot()
        }
    }

    private fun updateCurrentPlot() {
        PlotChangeEvent(currentPlot).post()
    }

    class PlotData(
        @Expose
        val id: Int,

        @Expose
        var name: String,

        @Expose
        var pests: Int,

        @Expose
        var sprayExpiryTime: SimpleTimeMark?,

        @Expose
        var sprayType: SprayType?,

        @Expose
        var sprayHasNotified: Boolean,

        @Expose
        var isBeingPasted: Boolean,

        @Expose
        var isPestCountInaccurate: Boolean,

        @Expose
        var locked: Boolean,

        @Expose
        var uncleared: Boolean,
    )

    data class SprayData(
        val expiry: SimpleTimeMark,
        val type: SprayType,
    )

    private fun Plot.getData() = GardenApi.storage?.plotData?.getOrPut(id) {
        PlotData(
            id,
            "$id",
            0,
            null,
            null,
            false,
            false,
            false,
            true,
            false,
        )
    }

    var Plot.name: String
        get() = getData()?.name ?: "$id"
        set(value) {
            getData()?.name = value
        }

    var Plot.pests: Int
        get() = getData()?.pests ?: 0
        set(value) {
            getData()?.pests = value
        }

    val Plot.currentSpray: SprayData?
        get() = this.getData()?.let { plot ->
            val expiry = plot.sprayExpiryTime?.takeIf { !it.isInPast() } ?: return null
            val type = plot.sprayType ?: return null
            return SprayData(expiry, type)
        }

    val Plot.isSprayExpired: Boolean
        get() = this.getData()?.let {
            !it.sprayHasNotified && it.sprayExpiryTime?.isInPast() == true
        } == true

    var Plot.isBeingPasted: Boolean
        get() = this.getData()?.isBeingPasted ?: false
        set(value) {
            this.getData()?.isBeingPasted = value
        }

    var Plot.isPestCountInaccurate: Boolean
        get() = this.getData()?.isPestCountInaccurate ?: false
        set(value) {
            this.getData()?.isPestCountInaccurate = value
        }

    var Plot.uncleared: Boolean
        get() = this.getData()?.uncleared ?: false
        set(value) {
            this.getData()?.uncleared = value
        }

    var Plot.locked: Boolean
        get() = this.getData()?.locked ?: false
        set(value) {
            this.getData()?.locked = value
        }

    fun Plot.markExpiredSprayAsNotified() {
        getData()?.sprayHasNotified = true
    }

    private fun Plot.setSpray(spray: SprayType, duration: Duration) {
        getData()?.apply {
            sprayType = spray
            sprayExpiryTime = SimpleTimeMark.now() + duration
            sprayHasNotified = false
        }
    }

    private fun Plot.removeSpray() {
        getData()?.apply {
            sprayType = null
            sprayExpiryTime = SimpleTimeMark.now()
            sprayHasNotified = true
        }
    }

    private fun sendSprayMessage(plot: String, spray: String, time: String) {
        ChatUtils.chat("§r§aPlot §r§7- §r§b$plot §r§7was sprayed with §r§a$spray§r§7!§r")
        ChatUtils.chat("§r§7This will expire in §r§a$time§r§7!§r")
    }

    private fun isSprayAccurate(
        sprayExpiryTime: SimpleTimeMark, expectedExpireTime: SimpleTimeMark, currentSpray: SprayType, newSpray: SprayType,
    ): Boolean {
        return sprayExpiryTime >= expectedExpireTime + 6.seconds ||
            sprayExpiryTime <= expectedExpireTime - 1.minutes ||
            currentSpray != newSpray
    }

    private fun sprayMessageEligible(
        sprayExpiryTime: SimpleTimeMark, expectedExpireTime: SimpleTimeMark, currentSpray: SprayType, newSpray: SprayType,
    ): Boolean {
        return (sprayExpiryTime <= expectedExpireTime - 10.minutes || currentSpray != newSpray) &&
            (config.newSprayNotification && sprayExpiryTime >= SimpleTimeMark.now() + 1.minutes)
    }

    fun Plot.isBarn() = id == 0

    fun Plot.isPlayerInside() = box.isPlayerInside()

    fun closestCenterPlot(location: LorenzVec) = plots.find { it.box.isInside(location) }?.middle

    fun Plot.sendTeleportTo() {
        if (isBarn()) HypixelCommands.teleportToPlot("barn")
        else HypixelCommands.teleportToPlot(name)
        LockMouseLook.autoDisable()
    }

    init {
        val plotMap = listOf(
            listOf(21, 13, 9, 14, 22),
            listOf(15, 5, 1, 6, 16),
            listOf(10, 2, 0, 3, 11),
            listOf(17, 7, 4, 8, 18),
            listOf(23, 19, 12, 20, 24),
        )
        val list = mutableListOf<Plot>()
        var slot = 2
        for ((y, rows) in plotMap.withIndex()) {
            for ((x, id) in rows.withIndex()) {
                val minX = ((x - 2) * 96 - 48).toDouble()
                val minY = ((y - 2) * 96 - 48).toDouble()
                val maxX = ((x - 2) * 96 + 48).toDouble()
                val maxY = ((y - 2) * 96 + 48).toDouble()
                val a = LorenzVec(minX, 0.0, minY)
                val b = LorenzVec(maxX, 256.0, maxY)
                val middle = a.interpolate(b, 0.5).copy(y = 10.0)
                val box = a.axisAlignedTo(b).expand(0.0001, 0.0, 0.0001)
                list.add(Plot(id, slot, box, middle))
                slot++
            }
            slot += 4
        }
        plots = list
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onChat(event: SkyHanniChatEvent) {

        plotSprayedPattern.matchMatcher(event.message) {
            val sprayName = group("spray")
            val plotName = group("plot")

            val plot = getPlotByName(plotName)
            val spray = SprayType.getByNameOrNull(sprayName) ?: return

            plot?.setSpray(spray, 30.minutes)
        }
        cleanPlotChatPattern.matchMatcher(event.message) {
            val plotId = group("plot").toInt()
            val plot = getPlotByID(plotId)
            plot?.uncleared = false
        }
        unlockPlotChatPattern.matchMatcher(event.message) {
            val plotId = group("plot").toInt()
            val plot = getPlotByID(plotId)
            plot?.locked = false
        }

        portableWasherPattern.matchMatcher(event.message) {
            for (plot in plots) {
                if (plot.currentSpray != null) {
                    plot.removeSpray()
                }
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Configure Plots") return

        for (plot in plots) {
            val itemStack = event.inventoryItems[plot.inventorySlot] ?: continue
            val lore = itemStack.getLore()
            plotNamePattern.matchMatcher(itemStack.name) {
                val plotName = group("name")
                plot.name = plotName
            }
            barnNamePattern.matchMatcher(itemStack.name) {
                plot.name = group("name")
            }
            plot.locked = false
            plot.isBeingPasted = false
            for (line in lore) {
                if (line.contains("§7Cost:")) plot.locked = true
                if (line.contains("§7Pasting in progress:")) plot.isBeingPasted = true
                plot.uncleared = false
                uncleanedPlotPattern.matchMatcher(line) {
                    plot.uncleared = true
                }
            }
        }
    }

    @HandleEvent
    fun onTabListUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.PESTS)) return
        val plot = getCurrentPlot() ?: return
        if (plot.isBarn()) return

        for (line in event.lines) {
            plotSprayedTablistPattern.matchMatcher(line.trim()) {

                val sprayName = group("spray").trim()
                val minutes = group("minutes")?.toInt() ?: 0
                val seconds = group("seconds")?.toInt() ?: 0

                val time = if (seconds == 0) (minutes + 1).minutes
                else minutes.minutes + seconds.seconds

                val timeString = when {
                    minutes != 0 && seconds != 0 -> "${minutes}m ${seconds}s"
                    minutes != 0 -> "${minutes + 1}m"
                    else -> "${seconds}s"
                }

                val newSpray: SprayType? = SprayType.getByNameOrNull(sprayName)

                if (plot.currentSpray != null) {
                    val expectedExpireTime = SimpleTimeMark.now() + time
                    val data = plot.getData() ?: return

                    val sprayExpiryTime = data.sprayExpiryTime ?: return
                    val currentSpray = data.sprayType ?: return

                    if (newSpray == null) {
                        plot.removeSpray()
                        return
                    } else {
                        if (isSprayAccurate(sprayExpiryTime, expectedExpireTime, currentSpray, newSpray)) {
                            if (sprayMessageEligible(sprayExpiryTime, expectedExpireTime, currentSpray, newSpray)) {
                                sendSprayMessage(plot.name, sprayName, timeString)
                            }
                            plot.setSpray(newSpray, time)
                        }
                    }
                } else {
                    if (newSpray == null) return
                    if (config.newSprayNotification) {
                        sendSprayMessage(plot.name, sprayName, timeString)
                    }
                    plot.setSpray(newSpray, time)
                }
            }
        }
    }

    @HandleEvent
    fun onPlotChange(event: PlotChangeEvent) {
        ChatUtils.debug("Current Plot: " + event.plot?.name)
        DelayedRun.runDelayed(3.seconds) {
            TabWidget.forceUpdateWidget(TabWidget.PESTS)
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onPlayerMove(event: EntityMoveEvent<EntityPlayerSP>) {
        if (event.isLocalPlayer) {
            DelayedRun.runDelayed(.5.seconds) {
                checkCurrentPlot()
            }
        }
    }

    fun getPlotByName(plotName: String) = plots.firstOrNull { it.name == plotName }

    fun getPlotByID(plotId: Int) = plots.firstOrNull { it.id == plotId }

    fun SkyHanniRenderWorldEvent.renderPlot(
        plot: Plot,
        lineColor: Color,
        cornerColor: Color,
        showBuildLimit: Boolean = false,
    ) {

        // These don't refer to Minecraft chunks but rather garden plots, but I use
        // the word chunk as the logic closely represents how chunk borders are rendered in latter mc versions
        val plotSize = 96
        val chunkX = floor((plot.middle.x + 48) / plotSize).toInt()
        val chunkZ = floor((plot.middle.z + 48) / plotSize).toInt()
        val chunkMinX = (chunkX * plotSize) - 48
        val chunkMinZ = (chunkZ * plotSize) - 48

        // Lowest point in the garden
        val minHeight = 66
        val maxHeight = 66 + 36

        // Render 4 vertical corners
        for (i in 0..plotSize step plotSize) {
            for (j in 0..plotSize step plotSize) {
                val start = LorenzVec(chunkMinX + i, minHeight, chunkMinZ + j)
                val end = LorenzVec(chunkMinX + i, maxHeight, chunkMinZ + j)
                tryDraw3DLine(start, end, cornerColor, 3, true)
            }
        }

        // Render vertical on X-Axis
        for (x in 4..<plotSize step 4) {
            val start = LorenzVec(chunkMinX + x, minHeight, chunkMinZ)
            val end = LorenzVec(chunkMinX + x, maxHeight, chunkMinZ)
            // Front lines
            tryDraw3DLine(start, end, lineColor, 2, true)
            // Back lines
            tryDraw3DLine(start.add(z = plotSize), end.add(z = plotSize), lineColor, 2, true)
        }

        // Render vertical on Z-Axis
        for (z in 4..<plotSize step 4) {
            val start = LorenzVec(chunkMinX, minHeight, chunkMinZ + z)
            val end = LorenzVec(chunkMinX, maxHeight, chunkMinZ + z)
            // Left lines
            tryDraw3DLine(start, end, lineColor, 2, true)
            // Right lines
            tryDraw3DLine(start.add(x = plotSize), end.add(x = plotSize), lineColor, 2, true)
        }

        // Render horizontal
        val buildLimit = minHeight + 11
        val iterable = if (showBuildLimit) {
            (minHeight..maxHeight step 4) + buildLimit
        } else {
            minHeight..maxHeight step 4
        }
        for (y in iterable) {
            val start = LorenzVec(chunkMinX, y, chunkMinZ)
            val isRedLine = y == buildLimit
            val color = if (isRedLine) Color.red else lineColor
            val depth = if (isRedLine) 3 else 2
            // (minX, minZ) -> (minX, minZ + 96)
            tryDraw3DLine(start, start.add(z = plotSize), color, depth, true)
            // (minX, minZ + 96) -> (minX + 96, minZ + 96)
            tryDraw3DLine(start.add(z = plotSize), start.add(x = plotSize, z = plotSize), color, depth, true)
            // (minX + 96, minZ + 96) -> (minX + 96, minZ)
            tryDraw3DLine(start.add(x = plotSize, z = plotSize), start.add(x = plotSize), color, depth, true)
            // (minX + 96, minZ) -> (minX, minZ)
            tryDraw3DLine(start.add(x = plotSize), start, color, depth, true)
        }
    }

    private fun SkyHanniRenderWorldEvent.tryDraw3DLine(
        p1: LorenzVec,
        p2: LorenzVec,
        color: Color,
        lineWidth: Int,
        depth: Boolean,
    ) {
        if (isOutOfBorders(p1)) return
        if (isOutOfBorders(p2)) return
        draw3DLine(p1, p2, color, lineWidth, depth)
    }

    private fun isOutOfBorders(location: LorenzVec) = when {
        location.x > 240 -> true
        location.x < -240 -> true
        location.z > 240 -> true
        location.z < -240 -> true

        else -> false
    }
}
