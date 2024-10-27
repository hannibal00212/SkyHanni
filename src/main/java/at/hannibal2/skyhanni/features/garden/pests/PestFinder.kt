package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.pests.PestFinderConfig.VisibilityType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.garden.pests.PestUpdateEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isPestCountInaccurate
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.isPlayerInside
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.name
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.pests
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.renderPlot
import at.hannibal2.skyhanni.features.garden.GardenPlotAPI.sendTeleportTo
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PestFinder {

    private val config get() = PestAPI.config.pestFinder

    private var display = emptyList<Renderable>()
    val noPestsChatPattern by RepoPattern.pattern(
        "chat.garden.no.pest",
        "§cThere are not any Pests on your Garden right now! Keep farming!",
    )

    @HandleEvent
    fun onPestUpdate(event: PestUpdateEvent) {
        update()
    }

    private fun update() {
        if (isEnabled()) {
            display = drawDisplay()
        }
    }

    private fun drawDisplay() = buildList {
        add(Renderable.string("§6Total pests: §e${PestAPI.scoreboardPests}§6/§e8"))

        for (plot in PestAPI.getInfestedPlots()) {
            val pests = plot.pests
            val plotName = plot.name
            val isInaccurate = plot.isPestCountInaccurate
            val pestsName = StringUtils.pluralize(pests, "pest")
            val name = "§e" + if (isInaccurate) "1+?" else {
                pests
            } + " §c$pestsName §7in §b$plotName"
            val renderable = Renderable.clickAndHover(
                name,
                listOf(
                    "§7Pests Found: §e" + if (isInaccurate) "Unknown" else pests,
                    "§7In plot §b$plotName",
                    "",
                    "§eClick here to warp!",
                ),
                onClick = {
                    plot.sendTeleportTo()
                },
            )
            add(renderable)
        }

        if (PestAPI.getInfestedPlots().isEmpty() && PestAPI.scoreboardPests != 0) {
            add(Renderable.string("§e${PestAPI.scoreboardPests} §6Bugged pests!"))
            add(
                Renderable.clickAndHover(
                    "§cTry opening your plots menu.",
                    listOf(
                        "Runs /desk.",
                    ),
                    onClick = {
                        HypixelCommands.gardenDesk()
                    },
                ),
            )
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        display = listOf()
        update()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (!config.showDisplay) return
        if (config.onlyWithVacuum && !PestAPI.hasVacuumInHand()) return

        if (GardenAPI.inGarden() && config.showDisplay) {
            config.position.renderRenderables(display, posLabel = "Pest Finder")
        }
    }

    private fun heldItemDisabled() = config.onlyWithVacuum && !PestAPI.hasVacuumInHand()
    private fun timePassedDisabled() = PestAPI.lastTimeVacuumHold.passedSince() > config.showBorderForSeconds.seconds

    // priority to low so that this happens after other renderPlot calls.
    @SubscribeEvent(priority = EventPriority.LOW)
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (!config.showPlotInWorld) return
        if (heldItemDisabled() && timePassedDisabled()) return

        val playerLocation = event.exactPlayerEyeLocation()
        val visibility = config.visibilityType
        val showBorder = visibility == VisibilityType.BOTH || visibility == VisibilityType.BORDER
        val showName = visibility == VisibilityType.BOTH || visibility == VisibilityType.NAME
        for (plot in PestAPI.getInfestedPlots()) {
            if (plot.isPlayerInside()) {
                if (showBorder) {
                    event.renderPlot(plot, LorenzColor.RED.toColor(), LorenzColor.DARK_RED.toColor())
                }
                continue
            }
            if (showBorder) {
                event.renderPlot(plot, LorenzColor.GOLD.toColor(), LorenzColor.RED.toColor())
            }
            if (showName) {
                drawName(plot, playerLocation, event)
            }
        }
    }

    private fun drawName(
        plot: GardenPlotAPI.Plot,
        playerLocation: LorenzVec,
        event: LorenzRenderWorldEvent,
    ) {
        val pests = plot.pests
        val pestsName = StringUtils.pluralize(pests, "pest")
        val plotName = plot.name
        val middle = plot.middle
        val isInaccurate = plot.isPestCountInaccurate
        val location = playerLocation.copy(x = middle.x, z = middle.z)
        event.drawWaypointFilled(location, LorenzColor.RED.toColor())
        val text = "§e" + (
            if (isInaccurate) "?" else pests
            ) + " §c$pestsName §7in §b$plotName"
        event.drawDynamicText(
            location, text, 1.5,
        )
    }

    private var lastKeyPress = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!GardenAPI.inGarden()) return
        if (!config.noPestTitle) return

        if (noPestsChatPattern.matches(event.message)) LorenzUtils.sendTitle("§eNo pests!", 2.seconds)
    }

    @SubscribeEvent
    fun onKeyClick(event: LorenzKeyPressEvent) {
        if (!GardenAPI.inGarden()) return
        if (Minecraft.getMinecraft().currentScreen != null) return
        if (NEUItems.neuHasFocus()) return

        if (event.keyCode != config.teleportHotkey) return
        if (lastKeyPress.passedSince() < 2.seconds) return
        lastKeyPress = SimpleTimeMark.now()

        teleportNearestInfestedPlot()
    }

    fun teleportNearestInfestedPlot() {
        // need to check again for the command
        if (!GardenAPI.inGarden()) {
            ChatUtils.userError("This command only works while on the Garden!")
        }

        val plot = PestAPI.getNearestInfestedPlot() ?: run {
            if (config.backToGarden) return HypixelCommands.warp("garden")

            ChatUtils.userError("No infested plots detected to warp to!")
            return
        }

        if (plot.isPlayerInside() && !config.alwaysTp) {
            ChatUtils.userError("You're already in an infested plot!")
            return
        }

        plot.sendTeleportTo()
    }

    fun isEnabled() = GardenAPI.inGarden() && (config.showDisplay || config.showPlotInWorld)
}
