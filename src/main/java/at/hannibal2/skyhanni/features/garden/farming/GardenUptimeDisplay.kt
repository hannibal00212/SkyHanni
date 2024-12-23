package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.config.features.garden.cropmilestones.CropMilestonesConfig.MilestoneTextEntry
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.data.TrackerManager
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.entity.EntityMoveEvent
import at.hannibal2.skyhanni.events.garden.pests.PestKillEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorAcceptedEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorOpenEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorRefusedEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenCropMilestoneDisplay.needsInventory
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.buildSearchBox
import at.hannibal2.skyhanni.utils.renderables.toRenderable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.tracker.SkyHanniItemTracker
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker.DisplayMode
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.time.LocalDate

@SkyHanniModule
object GardenUptimeDisplay {
    private val config = GardenAPI.config.gardenUptime
    private val tracker = SkyHanniTracker<Data>(
        "Garden Uptime Tracker",
        { Data() },
        { it.garden.gardenUptimeStorage.tracker },
        DisplayMode.WEEK to { it.garden.gardenUptimeStorage.week.getOrPut(getWeekString(LocalDate.now()), ::Data) },
        DisplayMode.DAY to { it.garden.gardenUptimeStorage.day.getOrPut(getDayString(LocalDate.now()), ::Data) }) {
            drawDisplay(it)
    }

    class Data : TrackerData() {
        override fun reset() {
            activeTime = 0
            visitorTime = 0
            pestTime = 0
            farmingTime = 0
            blocksBroken = 0
        }
        @Expose
        var activeTime: Int = 0

        @Expose
        var visitorTime: Int = 0

        @Expose
        var pestTime: Int = 0

        @Expose
        var farmingTime: Int = 0

        @Expose
        var blocksBroken: Int = 0

    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (isAFK || !isEnabled() ) return

        if (!pauseTimer) {
            tracker.modify { it.activeTime += 1 }
        }

        secondsActive += 1
        secondsAFK += 1
        secondsSinceLastMove += 1

        if (secondsAFK >= config.afkTimeout) {
            isAFK = true
            tracker.modify { it.activeTime -= secondsAFK }
            secondsAFK = 0
        }
    }

    @HandleEvent
    fun onCropBreak(event: CropClickEvent) {
        if (event.clickType != ClickType.LEFT_CLICK || !GardenAPI.inGarden()) return
        resetAFK()
    }

    @HandleEvent
    fun onPestKill(event: PestKillEvent) {
        tracker.modify { it.pestTime += secondsAFK }
        resetAFK()
    }

    @HandleEvent
    fun onVistorOpen(event: VisitorOpenEvent) {
        tracker.modify { it.visitorTime += secondsAFK }
        resetAFK()
    }

    @HandleEvent
    fun onVistorAccepted(event: VisitorAcceptedEvent) {
        tracker.modify { it.visitorTime += secondsAFK }
        resetAFK()
    }

    @HandleEvent
    fun onVistorRefused(event: VisitorRefusedEvent) {
        tracker.modify { it.visitorTime += secondsAFK }
        resetAFK()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (isEnabled()) return
        tracker.renderDisplay(config.pos)
    }

    var storage = GardenAPI.storage
    var secondsActive = 0

    var pauseTimer = false
    var isAFK = false
    var secondsSinceLastMove = 0
    var secondsAFK = 0

    private fun drawDisplay(data: Data) = buildList<Searchable> {
        val topLine = mutableListOf<Renderable>()
        topLine.add(Renderable.string("§7Farming Uptime Tracker"))
        add(Renderable.horizontalContainer(topLine).toSearchable())


    }

    private fun formatDisplay(lineMap: MutableMap<MilestoneTextEntry, Renderable>): List<Renderable> {
        val newList = mutableListOf<Renderable>()
        newList.addAll(GardenCropMilestoneDisplay.config.text.mapNotNull { lineMap[it] })

        return newList
    }

    private fun resetAFK() {
        isAFK = false
        secondsAFK = 0
    }

    private fun getDayString(date: LocalDate): String {
        return "${date.dayOfMonth}.${date.monthValue}.${date.year}"
    }

    private fun getWeekString(date: LocalDate): String {
        return "${date.dayOfYear/7}.${date.year}"
    }

    private fun isEnabled() = GardenAPI.inGarden() && config.display
}
