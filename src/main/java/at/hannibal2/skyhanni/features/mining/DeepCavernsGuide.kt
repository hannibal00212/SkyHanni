package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.ParkourJson
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.ParkourHelper
import net.minecraft.client.player.inventory.ContainerLocalMenu
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DeepCavernsGuide {

    private val config get() = SkyHanniMod.feature.mining.deepCavernsGuide

    private var parkourHelper: ParkourHelper? = null
    private var show = false
    private var showStartIcon = false

    private val startIcon by lazy {
        val neuItem = "MAP".asInternalName().getItemStack()
        ItemUtils.createItemStack(
            neuItem.item,
            "§bDeep Caverns Guide",
            "§8(From SkyHanni)",
            "",
            "§7Manually enable the ",
            "§7guide to the bottom",
            "§7of the Deep Caverns."
        )
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        parkourHelper?.reset()
        show = false
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ParkourJson>("DeepCavernsParkour")
        parkourHelper = ParkourHelper(
            data.locations,
            data.shortCuts,
            platformSize = 1.0,
            detectionRange = 3.5,
            depth = false,
            onEndReach = {
                show = false
            }
        )
        updateConfig()
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.rainbowColor, config.monochromeColor, config.lookAhead) {
            updateConfig()
        }
    }

    private fun updateConfig() {
        parkourHelper?.run {
            rainbowColor = config.rainbowColor.get()
            monochromeColor = config.monochromeColor.get().toChromaColor()
            lookAhead = config.lookAhead.get() + 1
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        showStartIcon = false
        if (!isEnabled()) return
        if (event.inventoryName != "Lift") return
        if (LorenzUtils.skyBlockArea != "Gunpowder Mines") return
        showStartIcon = true

        event.inventoryItems[30]?.let {
            if (it.displayName != "§aObsidian Sanctuary") {
                if (!show) {
                    start()
                    ChatUtils.chat(
                        "Automatically enabling Deep Caverns Guide, " +
                            "helping you find the way to the bottom of the Deep Caverns and the path to Rhys."
                    )
                }
            }
        }
    }

    private fun start() {
        show = true
        parkourHelper?.reset()
        if (parkourHelper == null) {
            ChatUtils.clickableChat(
                "DeepCavernsParkour missing in SkyHanni Repo! Try /shupdaterepo to fix it!",
                onClick = {
                    SkyHanniMod.repo.updateRepo()
                },
                "§eClick to update the repo!",
                prefixColor = "§c"
            )
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        showStartIcon = false
    }

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (show) return
        if (event.inventory is ContainerLocalMenu && showStartIcon && event.slot == 49) {
            event.replace(startIcon)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (showStartIcon && event.slotId == 49) {
            event.cancel()
            ChatUtils.chat("Manually enabled Deep Caverns Guide.")
            start()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (!show) return

        parkourHelper?.render(event)
    }

    fun isEnabled() = IslandType.DEEP_CAVERNS.isInIsland() && config.enabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(38, "mining.deepCavernsParkour", "mining.deepCavernsGuide")
    }
}
