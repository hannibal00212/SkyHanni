package at.hannibal2.skyhanni.features.inventory.caketracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.features.inventory.CakeTrackerConfig.CakeTrackerDisplayOrderType
import at.hannibal2.skyhanni.config.features.inventory.CakeTrackerConfig.CakeTrackerDisplayType
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.CakeData
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.inventory.patternGroup
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getAllItems
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration.Companion.milliseconds

private typealias DisplayOrder = CakeTrackerDisplayOrderType
private typealias DisplayType = CakeTrackerDisplayType

@SkyHanniModule
object CakeTracker {

    private val storage get() = ProfileStorageData.profileSpecific?.cakeData
    private val config get() = SkyHanniMod.feature.inventory.cakeTracker

    private var currentYear = 0
    private var inCakeBag = false
    private var inCakeInventory = false
    private var timeOpenedCakeInventory = SimpleTimeMark.farPast()
    private var inAuctionHouse = false
    private var auctionCakesCache = mapOf<Int, Boolean>()
    private var searchingForCakes = false
    private var knownCakesInCurrentInventory = listOf<Int>()

    private var cakeRenderables = listOf<Renderable>()
    private var lastKnownCakeDataHash = 0

    /**
     * REGEX-TEST: §cNew Year Cake (Year 360)
     * REGEX-TEST: §cNew Year Cake (Year 1,000)
     * REGEX-TEST: §f§f§cNew Year Cake (Year 330)
     * REGEX-TEST: §f§f§cNew Year Cake (Year 49)
     */
    private val cakeNamePattern by patternGroup.pattern(
        "cake.name",
        "(?:§f§f)?§cNew Year Cake \\(Year (?<year>[\\d,]+)\\)",
    )

    /**
     * REGEX-TEST: Ender Chest (2/9)
     * REGEX-TEST: Jumbo Backpack (Slot #6)
     * REGEX-TEST: New Year Cake Bag
     */
    private val cakeContainerPattern by patternGroup.pattern(
        "cake.container",
        "Ender Chest \\(\\d{1,2}/\\d{1,2}\\)|.*Backpack(?:§r)? \\(Slot #\\d{1,2}\\)|New Year Cake Bag",
    )

    /**
     * REGEX-TEST: New Year Cake Bag
     */
    private val cakeBagPattern by patternGroup.pattern(
        "cake.bag",
        "New Year Cake Bag",
    )

    /**
     * REGEX-TEST: Auctions Browser
     * REGEX-TEST: Auctions: "Test"
     * REGEX-TEST: Auctions: "New Year Cake (Year
     */
    private val auctionBrowserPattern by patternGroup.pattern(
        "auction.search",
        "Auctions Browser|Auctions: \".*",
    )

    /**
     * REGEX-TEST: Auctions: "New Year C
     */
    private val auctionCakeSearchPattern by patternGroup.pattern(
        "auction.cakesearch",
        "Auctions: \"New Year C.*",
    )

    private fun addCake(cakeYear: Int) {
        val storage = storage ?: return
        if (cakeYear !in storage.ownedCakes) storage.ownedCakes.add(cakeYear)
        recalculateMissingCakes()
    }

    private fun removeCake(cakeYear: Int) {
        val storage = storage ?: return
        if (cakeYear in storage.ownedCakes) storage.ownedCakes.remove(cakeYear)
        recalculateMissingCakes()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetcaketracker") {
            description = "Resets the New Year Cake Tracker"
            category = CommandCategory.USERS_RESET
            callback {
                val storage = storage ?: return@callback
                storage.ownedCakes.clear()
                recalculateMissingCakes()
                ChatUtils.chat("New Year Cake tracker data reset")
            }
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (inCakeBag || (inAuctionHouse && (auctionCakesCache.isNotEmpty() || searchingForCakes))) {
            reRenderDisplay()
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (inCakeInventory) checkInventoryCakes()

        if (!inAuctionHouse) return
        (event.gui.inventorySlots as ContainerChest).getAllItems().filter {
            auctionCakesCache.containsKey(it.key.slotIndex) &&
            cakeNamePattern.matches(it.key.stack.displayName)
        }.forEach { (slot, _) ->
            slot.getHighlightColor()?.let { slot highlight it }
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        knownCakesInCurrentInventory = listOf()
        checkCakeContainer(event)
        inAuctionHouse = checkAuctionCakes(event)
    }

    private fun Slot.getHighlightColor(): Color? {
        cakeNamePattern.matchGroup(stack.displayName, "year") ?: return null
        return if (auctionCakesCache[this.slotIndex] == true) config.obtainedAuctionHighlightColor.toChromaColor()
        else config.unobtainedAuctionHighlightColor.toChromaColor()
    }

    private fun reRenderDisplay() {
        config.cakeTrackerPosition.renderRenderables(
            drawDisplay(storage ?: return),
            posLabel = "New Year Cake Tracker",
        )
    }

    private fun checkCakeContainer(event: InventoryFullyOpenedEvent) {
        if (!cakeContainerPattern.matches(event.inventoryName)) return
        if (cakeBagPattern.matches(event.inventoryName)) inCakeBag = true
        knownCakesInCurrentInventory = event.inventoryItems.values.mapNotNull { item ->
            cakeNamePattern.matchMatcher(item.displayName) {
                val year = group("year").formatInt()
                addCake(year)
                year
            }
        }.toMutableList()
        inCakeInventory = true
        timeOpenedCakeInventory = SimpleTimeMark.now()
    }

    private fun checkAuctionCakes(event: InventoryFullyOpenedEvent): Boolean {
        if (!auctionBrowserPattern.matches(event.inventoryName)) return false
        searchingForCakes = auctionCakeSearchPattern.matches(event.inventoryName)
        auctionCakesCache = event.inventoryItems.filter {
            cakeNamePattern.matches(it.value.displayName)
        }.map {
            val year = cakeNamePattern.matchGroup(it.value.displayName, "year")?.toInt() ?: 0
            val owned = storage?.ownedCakes?.contains(year) ?: false
            it.key to owned
        }.toMap().filter { it.key != 0 }
        return true
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inCakeBag = false
        inCakeInventory = false
        knownCakesInCurrentInventory = listOf()
        inAuctionHouse = false
        auctionCakesCache = mapOf()
        searchingForCakes = false
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        val sbTimeNow = SkyBlockTime.now()
        if (currentYear == sbTimeNow.year) return
        if (sbTimeNow.month == 12 && sbTimeNow.day >= 29) {
            currentYear = sbTimeNow.year
            recalculateMissingCakes()
        } else currentYear = sbTimeNow.year - 1
    }

    private fun checkInventoryCakes() {
        if (timeOpenedCakeInventory.passedSince() < 500.milliseconds) return
        val currentYears = InventoryUtils.getItemsInOpenChest().mapNotNull { item ->
            cakeNamePattern.matchGroup(item.stack.displayName, "year")?.toInt()
        }

        val addedYears = currentYears.filter { it !in knownCakesInCurrentInventory }
        val removedYears = knownCakesInCurrentInventory.filter { it !in currentYears }

        addedYears.forEach(::addCake)
        removedYears.forEach(::removeCake)

        if (addedYears.isNotEmpty() || removedYears.isNotEmpty()) {
            knownCakesInCurrentInventory = currentYears.toMutableList()
        }
    }

    private fun recalculateMissingCakes() {
        val storage = storage ?: return
        storage.neededCakes = (1..currentYear).filter { year ->
            year !in storage.ownedCakes
        }
    }

    private data class CakeRange(var start: Int, var end: Int = 0) {
        fun getRenderable(displayType: DisplayType): Renderable {
            val colorCode =
                if (displayType == DisplayType.OWNED_CAKES) "§a"
                else "§c"
            val stringRenderable =
                Renderable.string(
                    if (end != 0) "§fYears $colorCode$start§f-$colorCode$end"
                    else "§fYear $colorCode$start",
                )
            return if (displayType == DisplayType.MISSING_CAKES) Renderable.link(
                stringRenderable,
                { HypixelCommands.auctionSearch("New Year Cake (Year $start)") },
            ) else stringRenderable
        }
    }

    private fun setDisplayType(type: DisplayType) {
        config.displayType = type
        lastKnownCakeDataHash = 0
    }

    private fun buildDisplayTypeToggle(): Renderable = Renderable.horizontalContainer(
        buildList {
            val ownedString =
                if (config.displayType == DisplayType.OWNED_CAKES) "§7§l[§r §a§nOwned§r §7§l]"
                else "§aOwned"
            val missingString =
                if (config.displayType == DisplayType.MISSING_CAKES) "§7§l[§r §c§nMissing§r §7§l]"
                else "§cMissing"

            add(
                Renderable.optionalLink(
                    ownedString,
                    { setDisplayType(DisplayType.OWNED_CAKES) },
                    condition = { config.displayType != DisplayType.OWNED_CAKES },
                ),
            )
            add(Renderable.string(" §7§l- §r"))
            add(
                Renderable.optionalLink(
                    missingString,
                    { setDisplayType(DisplayType.MISSING_CAKES) },
                    condition = { config.displayType != DisplayType.MISSING_CAKES },
                ),
            )
        },
    )

    private fun setDisplayOrderType(type: DisplayOrder) {
        config.displayOrderType = type
        lastKnownCakeDataHash = 0
    }

    private fun buildOrderTypeToggle(): Renderable = Renderable.horizontalContainer(
        buildList {
            val newestString =
                if (config.displayOrderType == DisplayOrder.NEWEST_FIRST) "§7§l[§r §a§nNewest First§r §7§l]"
                else "§aNewest First"
            val oldestString =
                if (config.displayOrderType == DisplayOrder.OLDEST_FIRST) "§7§l[§r §c§nOldest First§r §7§l]"
                else "§cOldest First"

            add(
                Renderable.optionalLink(
                    newestString,
                    { setDisplayOrderType(DisplayOrder.NEWEST_FIRST) },
                    condition = { config.displayOrderType != DisplayOrder.NEWEST_FIRST },
                ),
            )
            add(Renderable.string(" §7§l- §r"))
            add(
                Renderable.optionalLink(
                    oldestString,
                    { setDisplayOrderType(DisplayOrder.OLDEST_FIRST) },
                    condition = { config.displayOrderType != DisplayOrder.OLDEST_FIRST },
                ),
            )
        },
    )

    private fun drawDisplay(data: CakeData): List<Renderable> = buildList {
        val dataHash = data.hashCode()
        if (dataHash != lastKnownCakeDataHash) {
            cakeRenderables = buildCakeRenderables(data)
            lastKnownCakeDataHash = dataHash
        }

        addAll(cakeRenderables)
    }

    private fun buildCakeRenderables(data: CakeData) = buildList {
        add(
            Renderable.hoverTips(
                "§c§lNew §f§lYear §c§lCake §f§lTracker",
                tips = listOf("§aHave§7: §a${data.ownedCakes.count()}§7, §cMissing§7: §c${data.neededCakes.count()}"),
            ),
        )
        add(buildDisplayTypeToggle())
        add(buildOrderTypeToggle())

        val cakeList = when (config.displayType) {
            DisplayType.OWNED_CAKES -> data.ownedCakes
            DisplayType.MISSING_CAKES -> data.neededCakes
            null -> data.neededCakes
        }

        if (cakeList.isEmpty()) {
            val colorCode = if (config.displayType == DisplayType.OWNED_CAKES) "§c" else "§a"
            val verbiage = if (config.displayType == DisplayType.OWNED_CAKES) "missing" else "owned"
            add(Renderable.string("$colorCode§lAll cakes $verbiage!"))
        } else addCakeRanges(cakeList, config.displayOrderType, config.displayType)
    }

    private fun MutableList<Renderable>.addCakeRanges(
        cakeList: List<Int>,
        orderType: DisplayOrder,
        displayType: DisplayType
    ) {
        val sortedCakes = when (orderType) {
            DisplayOrder.OLDEST_FIRST -> cakeList.sorted()
            DisplayOrder.NEWEST_FIRST -> cakeList.sortedDescending()
        }.toMutableList()

        // Combine consecutive years into ranges
        val cakeRanges = mutableListOf<CakeRange>()
        var start = sortedCakes.first()
        var end = start

        for (i in 1 until sortedCakes.size) {
            if ((orderType == DisplayOrder.OLDEST_FIRST && sortedCakes[i] == end + 1) ||
                (orderType == DisplayOrder.NEWEST_FIRST && sortedCakes[i] == end - 1)
            ) {
                end = sortedCakes[i]
            } else {
                if (start != end) cakeRanges.add(CakeRange(start, end))
                else cakeRanges.add(CakeRange(start))
                start = sortedCakes[i]
                end = start
            }
        }

        if (start != end) cakeRanges.add(CakeRange(start, end))
        else cakeRanges.add(CakeRange(start))

        // Store how many lines are 'hidden' as a result of the display limit
        var hiddenRows = 0
        cakeRanges.forEach {
            if(this.size >= config.maxDisplayRows) hiddenRows ++
            else add(it.getRenderable(displayType))
        }

        // Add a line to indicate that there are more rows hidden
        if(hiddenRows > 0) add(Renderable.string("§7§o($hiddenRows hidden rows)"))
    }
}
