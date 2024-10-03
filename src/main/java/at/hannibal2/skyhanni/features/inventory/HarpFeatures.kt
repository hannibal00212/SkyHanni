package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.events.minecraft.ClientDisconnectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.player.inventory.ContainerLocalMenu
import net.minecraft.item.Item
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// Delaying key presses by 300ms comes from NotEnoughUpdates
@SkyHanniModule
object HarpFeatures {

    private val config get() = SkyHanniMod.feature.inventory.helper.harp
    private var lastClick = SimpleTimeMark.farPast()

    private const val closeButtonSlot = 40

    private val buttonColors = listOf('d', 'e', 'a', '2', '5', '9', 'b')

    private val patternGroup = RepoPattern.group("harp")
    private val inventoryTitlePattern by patternGroup.pattern(
        "inventory",
        "Harp.*",
    )
    private val menuTitlePattern by patternGroup.pattern(
        "menu",
        "Melody.*",
    )
    private val songSelectedPattern by patternGroup.pattern(
        "song.selected",
        "§aSong is selected!",
    )

    private fun isHarpGui(chestName: String) = inventoryTitlePattern.matches(chestName)
    private fun isMenuGui(chestName: String) = menuTitlePattern.matches(chestName)

    @SubscribeEvent
    fun onGui(event: GuiKeyPressEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.keybinds) return
        if (!isHarpGui(InventoryUtils.openInventoryName())) return
        val chest = event.guiContainer as? GuiChest ?: return

        for (index in 0..6) {
            val key = getKey(index) ?: error("no key for index $index")
            if (!key.isKeyHeld()) continue
            if (lastClick.passedSince() < 200.milliseconds) break

            event.cancel()

            Minecraft.getMinecraft().playerController.windowClick(
                chest.inventorySlots.windowId,
                37 + index,
                2,
                3,
                Minecraft.getMinecraft().thePlayer,
            ) // middle clicks > left clicks
            lastClick = SimpleTimeMark.now()
            break
        }
    }

    private fun getKey(index: Int) = when (index) {
        0 -> config.harpKeybinds.key1
        1 -> config.harpKeybinds.key2
        2 -> config.harpKeybinds.key3
        3 -> config.harpKeybinds.key4
        4 -> config.harpKeybinds.key5
        5 -> config.harpKeybinds.key6
        6 -> config.harpKeybinds.key7

        else -> null
    }

    private var openTime: SimpleTimeMark = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (config.quickRestart && isMenuGui(event.inventoryName)) {
            openTime = SimpleTimeMark.now()
        }
        if (config.guiScale && (isMenuGui(event.inventoryName) || isHarpGui(event.inventoryName))) {
            setGUIScale()
        }
    }

    private fun updateScale() {
        if (Minecraft.getMinecraft().currentScreen == null) {
            DelayedRun.runNextTick {
                updateScale()
            }
            return
        }
        // Copied from Minecraft Code to update the scale
        val minecraft = Minecraft.getMinecraft()
        val scaledresolution = ScaledResolution(minecraft)
        val i = scaledresolution.scaledWidth
        val j = scaledresolution.scaledHeight
        minecraft.currentScreen.setWorldAndResolution(minecraft, i, j)
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.guiScale) return
        unSetGUIScale()
    }

    @HandleEvent
    fun onDisconnect(event: ClientDisconnectEvent) {
        if (!config.guiScale) return
        unSetGUIScale()
    }

    private var guiSetting: Int = 0
    private var isGUIScaled = false

    private fun setGUIScale() {
        val gameSettings = Minecraft.getMinecraft().gameSettings ?: return
        guiSetting = gameSettings.guiScale
        gameSettings.guiScale = 0
        isGUIScaled = true
        updateScale()
    }

    private fun unSetGUIScale() {
        if (!isGUIScaled) return
        Minecraft.getMinecraft().gameSettings.guiScale = guiSetting
        isGUIScaled = false
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return

        if (isHarpGui(InventoryUtils.openInventoryName())) {
            if (config.keybinds) {
                // needed to not send duplicate clicks via keybind feature
                if (event.clickTypeEnum == GuiContainerEvent.ClickType.HOTBAR) {
                    event.cancel()
                    return
                }
            }
        }

        if (!config.quickRestart) return
        if (!isMenuGui(InventoryUtils.openInventoryName())) return
        if (event.slot?.slotNumber != closeButtonSlot) return
        if (openTime.passedSince() > 2.seconds) return
        event.container.inventory.filterNotNull().indexOfFirst {
            songSelectedPattern.anyMatches(it.getLore())
        }.takeIf { it != -1 }?.let {
            event.cancel()
            Minecraft.getMinecraft().playerController.windowClick(
                event.container.windowId,
                it,
                event.clickedButton,
                event.clickType,
                Minecraft.getMinecraft().thePlayer,
            )
        }
    }

    @SubscribeEvent
    fun onRenderItemTip(event: RenderItemTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.showNumbers) return
        if (!isHarpGui(InventoryUtils.openInventoryName())) return
        if (Item.getIdFromItem(event.stack.item) != 159) return // Stained hardened clay item id = 159

        // Example: §9| §7Click! will select the 9
        val index = buttonColors.indexOfFirst { it == event.stack.displayName[1] }
        if (index == -1) return // this should never happen unless there's an update

        val keyCode = getKey(index) ?: return
        event.stackTip = KeyboardManager.getKeyName(keyCode).take(3)
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.harpKeybinds", "inventory.helper.harp.keybinds")
        event.move(2, "misc.harpNumbers", "inventory.helper.harp.showNumbers")
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.hideMelodyTooltip) return
        if (!isHarpGui(InventoryUtils.openInventoryName())) return
        if (event.slot.inventory !is ContainerLocalMenu) return
        event.cancel()
    }
}
