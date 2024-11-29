package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.jsonobjects.repo.HideNotClickableItemsJson
import at.hannibal2.skyhanni.data.jsonobjects.repo.SalvageFilter
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.garden.composter.ComposterOverlay
import at.hannibal2.skyhanni.features.garden.visitor.VisitorAPI
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.features.mining.fossilexcavator.FossilExcavatorAPI
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.features.rift.RiftAPI.motesNpcPrice
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.equalsOneOf
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getLowerItems
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.isCoopSoulBound
import at.hannibal2.skyhanni.utils.ItemUtils.isEnchanted
import at.hannibal2.skyhanni.utils.ItemUtils.isSoulBound
import at.hannibal2.skyhanni.utils.ItemUtils.isVanilla
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.MultiFilter
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawBorder
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.hasAttributes
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.isMuseumDonated
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.isRiftExportable
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.isRiftTransferable
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HideNotClickableItems {

    private val config get() = SkyHanniMod.feature.inventory.hideNotClickable

    private var hideReason = ""
    private var showGreenLine = false

    private var lastClickTime = SimpleTimeMark.farPast()

    private val hideNpcSellFilter = MultiFilter()
    private val hideInStorageFilter = MultiFilter()
    private val itemsToSalvage = mutableListOf<String>()
    private val hidePlayerTradeFilter = MultiFilter()
    private val notAuctionableFilter = MultiFilter()

    /**
     * REGEX-TEST: SEEDS
     * REGEX-TEST: CARROT_ITEM
     * REGEX-TEST: POTATO_ITEM
     * REGEX-TEST: PUMPKIN_SEEDS
     * REGEX-TEST: SUGAR_CANE
     * REGEX-TEST: MELON_SEEDS
     * REGEX-TEST: CACTUS
     * REGEX-TEST: INK_SACK-3
     */
    private val seedsPattern by RepoPattern.pattern(
        "inventory.hidenotclickable.seeds",
        "SEEDS|CARROT_ITEM|POTATO_ITEM|PUMPKIN_SEEDS|SUGAR_CANE|MELON_SEEDS|CACTUS|INK_SACK-3",
    )

    private val netherWart by lazy { "NETHER_STALK".toInternalName() }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val hideNotClickable = event.getConstant<HideNotClickableItemsJson>("HideNotClickableItems")
        hideNpcSellFilter.load(hideNotClickable.hideNpcSell)
        hideInStorageFilter.load(hideNotClickable.hideInStorage)
        hidePlayerTradeFilter.load(hideNotClickable.hidePlayerTrade)
        notAuctionableFilter.load(hideNotClickable.notAuctionable)
        updateSalvageList(hideNotClickable.salvage)
    }

    private fun updateSalvageList(data: SalvageFilter) {
        itemsToSalvage.clear()

        itemsToSalvage.addAll(data.items)
        for (armor in data.armor) {
            itemsToSalvage.add("$armor Helmet")
            itemsToSalvage.add("$armor Chestplate")
            itemsToSalvage.add("$armor Leggings")
            itemsToSalvage.add("$armor Boots")
        }
    }

    @SubscribeEvent
    fun onForegroundDrawn(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!isEnabled()) return
        if (bypassActive()) return
        if (event.gui !is GuiChest) return
        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest
        val chestName = chest.getInventoryName()

        for ((slot, stack) in chest.getLowerItems()) {
            if (hide(chestName, stack)) {
                slot highlight LorenzColor.DARK_GRAY.addOpacity(config.opacity)
            } else if (showGreenLine && config.itemsGreenLine) {
                slot drawBorder LorenzColor.GREEN.addOpacity(200)
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        if (bypassActive()) return

        val guiChest = Minecraft.getMinecraft().currentScreen
        if (guiChest !is GuiChest) return
        val chestName = (guiChest.inventorySlots as ContainerChest).getInventoryName()

        val stack = event.itemStack
        if (InventoryUtils.getItemsInOpenChest().map { it.stack }.contains(stack)) return
        if (!ItemUtils.getItemsInInventory().contains(stack)) return

        if (hide(chestName, stack)) {
            val first = event.toolTip[0]
            event.toolTip.clear()
            event.toolTip.add("§7" + first.removeColor())
            event.toolTip.add("")
            if (hideReason == "") {
                event.toolTip.add("§4No hide reason!")
                ErrorManager.skyHanniError("No hide reason for not clickable item!")
            } else {
                event.toolTip.add("§c$hideReason")
                if (config.itemsBypass && !hideReason.contains("SkyBlock Menu")) {
                    event.toolTip.add("  §7(Bypass by holding the ${KeyboardManager.getModifierKeyName()} key)")
                }
            }
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        if (!config.itemsBlockClicks) return
        if (bypassActive()) return
        if (event.gui !is GuiChest) return
        val chestName = InventoryUtils.openInventoryName()

        val slot = event.slot ?: return

        if (slot.slotNumber == slot.slotIndex) return
        if (slot.stack == null) return

        val stack = slot.stack

        if (hide(chestName, stack)) {
            event.cancel()

            if (lastClickTime.passedSince() > 5.seconds) {
                lastClickTime = SimpleTimeMark.now()
            }
            return
        }
    }

    private fun bypassActive() = config.itemsBypass && KeyboardManager.isModifierKeyDown()

    private fun hide(chestName: String, stack: ItemStack): Boolean {
        hideReason = ""
        showGreenLine = false

        return when {
            hideNpcSell(stack) -> true
            hideInStorage(chestName, stack) -> true
            hideSalvage(chestName, stack) -> true
            hidePlayerTrade(chestName, stack) -> true
            hideBazaarOrAH(chestName, stack) -> true
            hideAccessoryBag(chestName, stack) -> true
            hideBasketOfSeeds(chestName, stack) -> true
            hideNetherWartPouch(chestName, stack) -> true
            hideTrickOrTreatBag(chestName, stack) -> true
            hideSackOfSacks(chestName, stack) -> true
            hideFishingBag(chestName, stack) -> true
            hidePotionBag(chestName, stack) -> true
            hidePrivateIslandChest(chestName, stack) -> true
            hideAttributeFusion(chestName, stack) -> true
            hideYourEquipment(chestName, stack) -> true
            hideComposter(chestName, stack) -> true
            hideRiftMotesGrubber(chestName, stack) -> true
            hideRiftTransferChest(chestName, stack) -> true
            hideFossilExcavator(stack) -> true
            hideResearchCenter(chestName, stack) -> true

            else -> false
        }
    }

    private fun hideFossilExcavator(stack: ItemStack): Boolean {
        if (!FossilExcavatorAPI.inExcavatorMenu) return false

        showGreenLine = true

        val internalName = stack.getInternalNameOrNull() ?: return true
        if (internalName == FossilExcavatorAPI.scrapItem) {
            return false
        }

        val category = stack.getItemCategoryOrNull() ?: return true
        if (category == ItemCategory.CHISEL) {
            return false
        }

        hideReason = "§cNot a chisel or scrap!"
        return true
    }

    private fun hideResearchCenter(chestName: String, stack: ItemStack): Boolean {
        if (chestName != "Research Center") return false

        showGreenLine = true

        val internalName = stack.getInternalNameOrNull() ?: return false

        // TODO add more special named fossils (hypixel why)
        val list = listOf(
            "HELIX".toInternalName(),
        )

        if (internalName in list) {
            return false
        }
        if (internalName.endsWith("_FOSSIL")) {
            return false
        }

        hideReason = "§cNot a fossil!"
        return true
    }

    private fun hideRiftTransferChest(chestName: String, stack: ItemStack): Boolean {
        if (chestName != "Rift Transfer Chest") return false

        showGreenLine = true
        val riftTransferable = stack.isRiftTransferable() ?: return true
        if (riftTransferable) {
            return false
        }
        if (RiftAPI.inRift()) {
            val riftExportable = stack.isRiftExportable() ?: return true
            if (riftExportable) {
                return false
            }
        }

        hideReason = "Not Rift-Transferable!"
        return true
    }

    private fun hideRiftMotesGrubber(chestName: String, stack: ItemStack): Boolean {
        if (!RiftAPI.inRift()) return false
        if (chestName != "Motes Grubber" && !ShiftClickNPCSell.inInventory) return false

        showGreenLine = true

        if (stack.motesNpcPrice() != null) return false

        hideReason = "Not sellable for Motes!"
        return true
    }

    @Suppress("UnusedParameter")
    private fun hideComposter(chestName: String, stack: ItemStack): Boolean {
        if (!ComposterOverlay.inInventory) return false

        showGreenLine = true

        val internalName = stack.getInternalName()
        if (internalName == ComposterOverlay.currentOrganicMatterItem) {
            return false
        }
        if (internalName == ComposterOverlay.currentFuelItem) {
            return false
        }

        hideReason = "Only insert the selected items!"
        return true
    }

    private fun hideYourEquipment(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Your Equipment")) return false

        val list = listOf(
            "HELMET",
            "CARNIVAL MASK",
            "CHESTPLATE",
            "LEGGINGS",
            "BOOTS",

            "NECKLACE",
            "CLOAK",
            "BELT",
            "GLOVES",
            "BRACELET",
        )
        for (type in list) {
            if (stack.getLore().any { it.contains("§l") && it.contains(type) }) { // todo use item api
                showGreenLine = true
                return false
            }
        }

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be put into your equipment!"
            return true
        }

        hideReason = "This item cannot be put into your equipment!"
        return true
    }

    private fun hideAttributeFusion(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Attribute Fusion")) return false

        showGreenLine = true

        if (stack.hasAttributes()) return false

        hideReason = "This item has no attributes!"
        return true
    }

    private fun hidePrivateIslandChest(chestName: String, stack: ItemStack): Boolean {
        if (chestName != "Chest" && chestName != "Large Chest") return false

        // TODO make check if player is on private island

        if (!stack.isSoulBound()) return false

        hideReason = "This item cannot be stored into a chest!"
        return true
    }

    private fun hidePotionBag(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Potion Bag")) return false

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be put into the potion bag!"
            return true
        }

        showGreenLine = true
        if (stack.cleanName().endsWith(" Potion") || stack.cleanName() == "Water Bottle") return false

        hideReason = "This item is not a potion!"
        return true
    }

    private fun hideFishingBag(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Fishing Bag")) return false

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be put into the fishing bag!"
            return true
        }

        showGreenLine = true
        if (stack.getLore().any { it.removeColor() == "Fishing Bait" }) {
            return false
        }
        hideReason = "This item is not a fishing bait!"
        return true
    }

    private fun hideSackOfSacks(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Sack of Sacks")) return false
        if (ItemUtils.isSkyBlockMenuItem(stack)) return false

        showGreenLine = true
        if (ItemUtils.isSack(stack)) return false


        hideReason = "This item is not a sack!"
        return true
    }

    private fun hideAccessoryBag(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Accessory Bag") && !chestName.startsWith("Accessory Bag (")) return false
        if (ItemUtils.isSkyBlockMenuItem(stack)) return false

        showGreenLine = true
        if (stack.getLore().any { it.contains("ACCESSORY") || it.contains("HATCESSORY") }) return false

        hideReason = "This item is not an accessory!"
        return true
    }

    private fun hideBasketOfSeeds(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Basket of Seeds")) return false

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be put into the basket of seeds!"
            return true
        }

        seedsPattern.matchMatcher(stack.getInternalName().asString()) {
            return false
        }

        hideReason = "This item is not a seed!"
        return true
    }

    private fun hideNetherWartPouch(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Nether Wart Pouch")) return false

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be put into the nether wart pouch!"
            return true
        }

        if (stack.getInternalName() == netherWart) return false

        hideReason = "This item is not a nether wart!"
        return true
    }

    private fun hideTrickOrTreatBag(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("Trick or Treat Bag")) return false

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be put into the trick or treat bag!"
            return true
        }

        if (stack.cleanName() == "Green Candy" || stack.cleanName() == "Purple Candy" || stack.cleanName() == "Dark Candy") return false

        hideReason = "This item is not a spooky candy!"
        return true
    }

    private fun hidePlayerTrade(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.startsWith("You    ")) return false

        if (stack.isCoopSoulBound()) {
            hideReason = "Soulbound items cannot be traded!"
            return true
        }

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be traded!"
            return true
        }

        val name = stack.cleanName()

        if (ItemUtils.isSack(stack)) {
            hideReason = "Sacks cannot be traded!"
            return true
        }

        val result = hidePlayerTradeFilter.match(name)

        if (result) hideReason = "This item cannot be traded!"
        return result
    }

    private fun hideNpcSell(stack: ItemStack): Boolean {
        if (RiftAPI.inRift()) return false
        if (!ShiftClickNPCSell.inInventory) return false
        if (VisitorAPI.inInventory) return false
        showGreenLine = true

        var name = stack.cleanName()
        val size = stack.stackSize
        val amountText = " x$size"
        if (name.endsWith(amountText)) {
            name = name.substring(0, name.length - amountText.length)
        }

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be sold at the NPC!"
            return true
        }

        if (!ItemUtils.isRecombobulated(stack)) {
            if (LorenzUtils.noTradeMode && BazaarApi.isBazaarItem(stack)) {
                return false
            }

            if (hideNpcSellFilter.match(name)) return false

            if (stack.isVanilla() && !stack.isEnchanted()) {
                return false
            }
        }

        hideReason = "This item should not be sold at the NPC!"
        return true
    }

    private fun hideInStorage(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.contains("Ender Chest") && !chestName.contains("Backpack") && chestName != "Storage") return false

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be put into the storage!"
            return true
        }

        val name = stack.cleanName()

        val result = hideInStorageFilter.match(name)

        if (result) hideReason = "Bags cannot be put into the storage!"
        return result
    }

    private fun hideSalvage(chestName: String, stack: ItemStack): Boolean {
        if (!chestName.equalsOneOf("Salvage Item", "Salvage Items")) return false
        showGreenLine = true

        if (ItemUtils.isRecombobulated(stack)) {
            hideReason = "This item should not be salvaged! (Recombobulated)"
            return true
        }
        // TODO replace with rarity check
        for (line in stack.getLore()) {
            if (line.contains("LEGENDARY DUNGEON")) {
                hideReason = "This item should not be salvaged! (Legendary)"
                return true
            }
        }

        if (stack.isMuseumDonated()) {
            hideReason = "This item cannot be salvaged! (Donated to Museum)"
            return true
        }

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            hideReason = "The SkyBlock Menu cannot be salvaged!"
            return true
        }

        val name = stack.cleanName()
        for (item in itemsToSalvage) {
            if (name.endsWith(item)) {
                return false
            }
        }

        hideReason = "This item cannot be salvaged!"
        return true
    }

    private fun hideBazaarOrAH(chestName: String, stack: ItemStack): Boolean {
        val bazaarInventory = BazaarApi.inBazaarInventory

        val auctionHouseInventory =
            chestName == "Co-op Auction House" || chestName == "Auction House" ||
                chestName == "Create BIN Auction" || chestName == "Create Auction"
        if (!bazaarInventory && !auctionHouseInventory) return false
        showGreenLine = true

        if (ItemUtils.isSkyBlockMenuItem(stack)) {
            if (bazaarInventory) hideReason = "The SkyBlock Menu is not a Bazaar Product!"
            if (auctionHouseInventory) hideReason = "The SkyBlock Menu cannot be auctioned!"
            return true
        }

        if (bazaarInventory != BazaarApi.isBazaarItem(stack)) {
            if (bazaarInventory) hideReason = "This item is not a Bazaar Product!"
            if (auctionHouseInventory) hideReason = "Bazaar Products cannot be auctioned!"

            return true
        }

        if (isNotAuctionable(stack)) return true

        return false
    }

    private fun isNotAuctionable(stack: ItemStack): Boolean {
        if (stack.isCoopSoulBound()) {
            hideReason = "Soulbound items cannot be auctioned!"
            return true
        }

        val name = stack.cleanName()

        if (ItemUtils.isSack(stack)) {
            hideReason = "Sacks cannot be auctioned!"
            return true
        }

        val result = notAuctionableFilter.match(name)
        if (result) hideReason = "This item cannot be auctioned!"
        return result
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.items

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "inventory.hideNotClickableItems", "inventory.hideNotClickable.items")
        event.move(3, "inventory.hideNotClickableItemsBlockClicks", "inventory.hideNotClickable.itemsBlockClicks")
        event.move(3, "inventory.hideNotClickableOpacity", "inventory.hideNotClickable.opacity")
        event.move(3, "inventory.notClickableItemsBypass", "inventory.hideNotClickable.itemsBypass")
        event.move(3, "inventory.hideNotClickableItemsGreenLine", "inventory.hideNotClickable.itemsGreenLine")
    }
}
