package at.hannibal2.skyhanni.features.minion

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.jsonobjects.repo.InfernoMinionFuelsJson
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object InfernoMinionFeatures {
    private val config get() = SkyHanniMod.feature.misc.minions

    /**
     * REGEX-TEST: Inferno Minion II
     * REGEX-TEST: Inferno Minion IX
     */
    private val infernoMinionTitlePattern by RepoPattern.pattern(
        "minion.infernominiontitle",
        "Inferno Minion .*",
    )
    private var fuelItemIds = listOf<NEUInternalName>()
    private var inInventory = false

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<InfernoMinionFuelsJson>("InfernoMinionFuels")
        fuelItemIds = data.minionFuels
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inInventory = infernoMinionTitlePattern.matches(event.inventoryName)
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.infernoFuelBlocker) return
        if (!inInventory) return

        val containsFuel =
            NEUInternalName.fromItemNameOrNull(event.container.getSlot(19).stack.name) in fuelItemIds
        if (!containsFuel) return

        if (event.slot?.slotNumber == 19 || event.slot?.slotNumber == 53) {
            if (KeyboardManager.isModifierKeyDown()) return
            event.cancel()
        }
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!config.infernoFuelBlocker) return
        if (!inInventory) return

        val containsFuel = NEUInternalName.fromItemNameOrNull(event.itemStack.name) in fuelItemIds
        if (!containsFuel) return

        if (event.slot.slotNumber == 19) {
            event.toolTip.add("")
            event.toolTip.add("§c[SkyHanni] is blocking you from taking this out!")
            event.toolTip.add("  §7(Bypass by holding the ${KeyboardManager.getModifierKeyName()} key)")
        }
        if (event.slot.slotNumber == 53) {
            event.toolTip.add("")
            event.toolTip.add("§c[SkyHanni] is blocking you from picking this minion up!")
            event.toolTip.add("  §7(Bypass by holding the ${KeyboardManager.getModifierKeyName()} key)")
        }
    }
}
