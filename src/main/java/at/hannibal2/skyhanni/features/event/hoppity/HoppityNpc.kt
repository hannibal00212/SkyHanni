package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.EntityMovementData
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object HoppityNpc {

    private val config get() = HoppityEggsManager.config

    private var lastReminderSent = SimpleTimeMark.farPast()
    private var hoppityYearOpened
        get() = ChocolateFactoryApi.profileStorage?.hoppityShopYearOpened ?: -1
        set(value) {
            ChocolateFactoryApi.profileStorage?.hoppityShopYearOpened = value
        }

    private val slotsToHighlight = mutableSetOf<Int>()
    private var inShop = false

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Hoppity") return
        // TODO maybe we could add an annoying chat message that tells you how many years you have skipped
        //  or the last year you have opened the shop before.
        //  that way we verbally punish non active users in a funny and non harmful way
        hoppityYearOpened = SkyBlockTime.now().year
        inShop = true
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isReminderEnabled()) return
        if (ReminderUtils.isBusy()) return
        if (LorenzUtils.isStrandedProfile) return

        if (hoppityYearOpened == SkyBlockTime.now().year) return
        if (!HoppityApi.isHoppityEvent()) return
        if (lastReminderSent.passedSince() <= 2.minutes) return

        ChatUtils.clickToActionOrDisable(
            "New rabbits are available at §aHoppity's Shop§e!",
            config::hoppityShopReminder,
            actionName = "warp to hub",
            action = {
                HypixelCommands.warp("hub")
                EntityMovementData.onNextTeleport(IslandType.HUB) {
                    IslandGraphs.pathFind(
                        SkyHanniVec3d(6.4, 70.0, 7.4),
                        "§aHoppity's Shop",
                        condition = { config.hoppityShopReminder },
                    )
                }
            },
        )

        lastReminderSent = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        clear()
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        clear()
    }

    @HandleEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!inShop) return
        slotsToHighlight.clear()
        for ((slot, item) in event.inventoryItems) {
            if (item.getLore().contains("§eClick to trade!")) {
                slotsToHighlight.add(slot)
            }
        }
    }

    @HandleEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isHighlightEnabled()) return
        if (!inShop) return
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex in slotsToHighlight) {
                slot highlight LorenzColor.GREEN.addOpacity(200)
            }
        }
    }

    private fun isHighlightEnabled() = LorenzUtils.inSkyBlock && config.highlightHoppityShop
    private fun isReminderEnabled() = LorenzUtils.inSkyBlock && config.hoppityShopReminder

    private fun clear() {
        inShop = false
        slotsToHighlight.clear()
    }
}
