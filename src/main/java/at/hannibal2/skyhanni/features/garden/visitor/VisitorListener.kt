package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.garden.visitor.VisitorConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SkyhanniRenderWorldEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorOpenEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorRenderEvent
import at.hannibal2.skyhanni.events.item.ItemHoverEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketSentEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI.ACCEPT_SLOT
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI.INFO_SLOT
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI.lastClickedNpc
import at.hannibal2.skyhanni.mixins.transformers.gui.AccessorGuiContainer
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object VisitorListener {
    private val offersAcceptedPattern by RepoPattern.pattern(
        "garden.visitor.offersaccepted",
        "§7Offers Accepted: §a(?<offersAccepted>\\d+)",
    )

    private val config get() = VisitorAPI.config

    private val logger = LorenzLogger("garden/visitors/listener")

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        VisitorAPI.reset()
    }

    // TODO make event
    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onSendEvent(event: PacketSentEvent) {
        val packet = event.packet
        if (packet !is C02PacketUseEntity) return

        val theWorld = Minecraft.getMinecraft().theWorld
        val entity = packet.getEntityFromWorld(theWorld) ?: return
        val entityId = entity.entityId

        lastClickedNpc = entityId
    }

    @HandleEvent
    fun onTabListUpdate(event: WidgetUpdateEvent) {
        if (!GardenAPI.inGarden()) return
        if (!event.isWidget(TabWidget.VISITORS)) return

        val hasVisitorInfo = event.lines.any { VisitorAPI.visitorCountPattern.matches(it) }
        if (!hasVisitorInfo) return

        val visitorsInTab = VisitorAPI.visitorsInTabList(event.lines)

        if (LorenzUtils.lastWorldSwitch.passedSince() > 2.seconds) {
            for (visitor in VisitorAPI.getVisitors()) {
                val name = visitor.visitorName
                val removed = name !in visitorsInTab
                if (removed) {
                    logger.log("Removed old visitor: '$name'")
                    VisitorAPI.removeVisitor(name)
                }
            }
        }

        for (name in visitorsInTab) {
            VisitorAPI.addVisitor(name)
        }
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!GardenAPI.inGarden()) return
        val npcItem = event.inventoryItems[INFO_SLOT] ?: return
        val lore = npcItem.getLore()
        if (!VisitorAPI.isVisitorInfo(lore)) return

        val offerItem = event.inventoryItems[ACCEPT_SLOT] ?: return
        if (offerItem.name != "§aAccept Offer") return

        VisitorAPI.inInventory = true

        val visitorOffer = VisitorAPI.VisitorOffer(offerItem)

        var name = npcItem.name
        if (name.length == name.removeColor().length + 4) {
            name = name.substring(2)
        }

        val visitor = VisitorAPI.getOrCreateVisitor(name) ?: return

        visitor.offersAccepted = offersAcceptedPattern.matchMatcher(lore[3]) { group("offersAccepted").toInt() }
        visitor.entityId = lastClickedNpc
        visitor.offer = visitorOffer
        VisitorOpenEvent(visitor).post()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        VisitorAPI.inInventory = false
    }

    @HandleEvent
    fun onKeybind(event: GuiKeyPressEvent) {
        if (!VisitorAPI.inInventory) return
        if (!config.acceptHotkey.isKeyHeld()) return
        val inventory = event.guiContainer as? AccessorGuiContainer ?: return
        inventory as GuiContainer
        val slot = inventory.inventorySlots.getSlot(29)
        inventory.handleMouseClick_skyhanni(slot, slot.slotIndex, 0, 0)
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onTooltip(event: ItemHoverEvent) {
        if (!GardenAPI.onBarnPlot) return
        if (!VisitorAPI.inInventory) return
        val visitor = VisitorAPI.getVisitor(lastClickedNpc) ?: return
        GardenVisitorFeatures.onTooltip(visitor, event.itemStack, event.toolTip)
    }

    @HandleEvent
    fun onCheckRender(event: CheckRenderEntityEvent<EntityArmorStand>) {
        if (!GardenAPI.inGarden()) return
        if (!GardenAPI.onBarnPlot) return
        if (config.highlightStatus != VisitorConfig.HighlightMode.NAME && config.highlightStatus != VisitorConfig.HighlightMode.BOTH) return

        val entity = event.entity
        if (entity.name == "§e§lCLICK") {
            event.cancel()
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyhanniRenderWorldEvent) {
        if (!GardenAPI.inGarden()) return
        if (!GardenAPI.onBarnPlot) return
        if (config.highlightStatus != VisitorConfig.HighlightMode.NAME && config.highlightStatus != VisitorConfig.HighlightMode.BOTH) return

        for (visitor in VisitorAPI.getVisitors()) {
            visitor.getNameTagEntity()?.let {
                if (it.distanceToPlayer() > 15) return@let
                VisitorRenderEvent(visitor, event.exactLocation(it), event).post()
            }
        }
    }
}
