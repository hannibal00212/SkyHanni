package at.hannibal2.skyhanni.features.inventory.caketracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.config.features.inventory.CakeTrackerConfig.CakeTrackerDisplayOrderType
import at.hannibal2.skyhanni.config.features.inventory.CakeTrackerConfig.CakeTrackerDisplayType
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.CakeData
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.inventory.patternGroup
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

private typealias DisplayOrder = CakeTrackerDisplayOrderType
private typealias DisplayType = CakeTrackerDisplayType

@SkyHanniModule
object CakeTracker {

    private val storage get() = ProfileStorageData.profileSpecific?.cakeData
    private val config get() = SkyHanniMod.feature.inventory.cakeTracker
    private val maxTrackerHeight get() = config.maxHeight.get()

    private var currentYear = 0
    private var inCakeInventory = false
    private var timeOpenedCakeInventory = SimpleTimeMark.farPast()
    private var inAuctionHouse = false
    private var slotHighlightCache = mapOf<Int, Color>()
    private var searchingForCakes = false
    private var knownCakesInCurrentInventory = listOf<Int>()
    private val cakePriceCache: TimeLimitedCache<Int, Double> = TimeLimitedCache(5.minutes)

    private var cakeRenderables = listOf<Renderable>()
    private var lastKnownCakeDataHash = 0

    private val unobtainedHighlightColor: Color get() = config.unobtainedAuctionHighlightColor.toChromaColor()
    private val obtainedHighlightColor: Color get() = config.obtainedAuctionHighlightColor.toChromaColor()

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
     * REGEX-TEST: §eYou purchased §r§f§r§f§r§cNew Year Cake (Year 143) §r§efor §r§62,000,000 coins§r§e!
     */
    private val cakePurchasedPattern by patternGroup.pattern(
        "cake.purchased",
        "§eYou purchased (?:§.)*New Year Cake \\(Year (?<year>[\\d,]+)\\) (?:§.)*for (?:§.)+(?<coins>[\\d,]+) coins(?:§.)+!",
    )

    /**
     * REGEX-TEST: Ender Chest (2/9)
     * REGEX-TEST: Jumbo Backpack (Slot #6)
     * REGEX-TEST: New Year Cake Bag
     * REGEX-TEST: Large Chest
     * REGEX-TEST: Chest
     */
    private val cakeContainerPattern by patternGroup.pattern(
        "cake.container",
        "Ender Chest \\(\\d{1,2}/\\d{1,2}\\)|.*Backpack(?:§r)? \\(Slot #\\d{1,2}\\)|New Year Cake Bag|(Large )?Chest",
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
        val changed = storage.ownedCakes.add(cakeYear)
        if (changed) recalculateMissingCakes()
    }

    private fun removeCake(cakeYear: Int) {
        val storage = storage ?: return
        val changed = storage.ownedCakes.remove(cakeYear)
        if (changed) recalculateMissingCakes()
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
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        cakePurchasedPattern.matchMatcher(event.message) {
            val year = group("year").formatInt()
            addCake(year)
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.maxHeight) {
            lastKnownCakeDataHash = 0
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        val inInvWithCakes = inCakeInventory && knownCakesInCurrentInventory.any()
        val inAuctionWithCakes = inAuctionHouse && (slotHighlightCache.isNotEmpty() || searchingForCakes)
        if (inInvWithCakes || inAuctionWithCakes) {
            reRenderDisplay()
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (inCakeInventory) checkInventoryCakes()
        if (!inAuctionHouse) return

        (event.gui.inventorySlots as ContainerChest).getUpperItems().forEach { (slot, _) ->
            slotHighlightCache[slot.slotIndex]?.let { color ->
                slot highlight color
            }
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        knownCakesInCurrentInventory = listOf()
        checkCakeContainer(event)
        inAuctionHouse = checkAuctionCakes(event)
    }

    private fun reRenderDisplay() {
        config.cakeTrackerPosition.renderRenderables(
            drawDisplay(storage ?: return),
            posLabel = "New Year Cake Tracker",
        )
    }

    private fun checkCakeContainer(event: InventoryFullyOpenedEvent) {
        if (!cakeContainerPattern.matches(event.inventoryName)) return
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
        slotHighlightCache = event.inventoryItems.filter {
            cakeNamePattern.matches(it.value.displayName)
        }.mapValues { (_, item) ->
            val year = cakeNamePattern.matchGroup(item.displayName, "year")?.toInt() ?: -1
            val owned = storage?.ownedCakes?.contains(year) ?: false
            if (owned) obtainedHighlightColor else unobtainedHighlightColor
        }
        return true
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inCakeInventory = false
        knownCakesInCurrentInventory = listOf()
        inAuctionHouse = false
        slotHighlightCache = mapOf()
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
        storage.missingCakes = (1..currentYear).toSet() - storage.ownedCakes
    }

    private fun getCakePrice(year: Int): Double {
        return cakePriceCache.getOrPut(year) {
            val cakeItem = "NEW_YEAR_CAKE+$year".toInternalName()
            cakeItem.getPrice()
        }
    }

    private fun getCakePriceString(year: Int): String {
        return getCakePrice(year).takeIf { it > 0 }?.let {
            "§6${it.addSeparators()}"
        } ?: "§7Unknown (no auctions)"
    }

    private data class CakeRange(var start: Int, var end: Int = 0) {
        val isSingular = (start == end || end == 0)

        fun getRenderable(displayType: DisplayType): Renderable {
            val colorCode = if (displayType == DisplayType.OWNED_CAKES) "§a" else "§c"
            val baseRenderable = getHoverable(displayType, colorCode)
            return if (displayType == DisplayType.MISSING_CAKES && config.priceOnHover) Renderable.link(
                baseRenderable,
                { HypixelCommands.auctionSearch("New Year Cake (Year $start)") },
            ) else baseRenderable
        }

        fun getHoverable(displayType: DisplayType, colorCode: String): Renderable {
            val displayString =
                if (isSingular) "§fYear $colorCode$start"
                else "§fYears $colorCode$start§f-$colorCode$end"

            return if (!config.priceOnHover) Renderable.string(displayString)
            else Renderable.hoverTips(
                displayString,
                getPriceHoverTooltip(displayType, colorCode)
            )
        }

        fun getPriceHoverTooltip(displayType: DisplayType, colorCode: String): List<String> {
            return if (isSingular) {
                listOf(
                    "${colorCode}Year $start§7: ${getCakePriceString(start)}",
                    "§eClick to search auction house",
                )
            } else buildList {
                val largerNumber = if (start > end) start else end
                val smallerNumber = if (start < end) start else end
                val numericalRange = smallerNumber..largerNumber
                val rangeLength = abs(end - start) + 1

                numericalRange.take(5).forEach { year ->
                    add("${colorCode}Year $year§7: ${getCakePriceString(year)}")
                }
                if (rangeLength >= 5) add("§7§o... and ${rangeLength - 5} more")
                add("")
                add("§aTotal§7: §6${numericalRange.sumOf(::getCakePrice).addSeparators()}")
                if (displayType == DisplayType.MISSING_CAKES) {
                    add("§eClick to search auction house")
                }
            }
        }
    }

    private fun setDisplayType(type: DisplayType) {
        config.displayType = type
        lastKnownCakeDataHash = 0
    }

    private fun buildDisplayTypeToggle(): Renderable = Renderable.horizontalContainer(
        buildList {
            val ownedColor = if (config.displayType == DisplayType.OWNED_CAKES) "§a" else "§e"
            val missingColor = if (config.displayType == DisplayType.MISSING_CAKES) "§a" else "§e"

            add(
                Renderable.optionalLink(
                    "$ownedColor[Owned]",
                    { setDisplayType(DisplayType.OWNED_CAKES) },
                    condition = { config.displayType != DisplayType.OWNED_CAKES },
                ),
            )
            add(Renderable.string(" "))
            add(
                Renderable.optionalLink(
                    "$missingColor[Missing]",
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
            val newestColor = if (config.displayOrderType == DisplayOrder.NEWEST_FIRST) "§a" else "§e"
            val oldestColor = if (config.displayOrderType == DisplayOrder.OLDEST_FIRST) "§a" else "§e"

            add(
                Renderable.optionalLink(
                    "$newestColor[Newest First]",
                    { setDisplayOrderType(DisplayOrder.NEWEST_FIRST) },
                    condition = { config.displayOrderType != DisplayOrder.NEWEST_FIRST },
                ),
            )
            add(Renderable.string(" "))
            add(
                Renderable.optionalLink(
                    "$oldestColor[Oldest First]",
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

    private fun getHeaderTips(data: CakeData) = buildList {
        val unknownOwned = data.ownedCakes.count { getCakePrice(it) == 0.0 }
        val unknownMissing = data.missingCakes.count { getCakePrice(it) == 0.0 }

        add("§aHave§7: §a${data.ownedCakes.size}")
        add("§6Total value§7: §6${data.ownedCakes.sumOf(::getCakePrice).addSeparators()}")
        if (unknownOwned > 0) add("  §7§o* $unknownOwned unknown prices")
        add("")
        add("§cMissing§7: §c${data.missingCakes.size}")
        add("§6Total cost§7: §6${data.missingCakes.sumOf(::getCakePrice).addSeparators()}")
        if (unknownMissing > 0) add("  §7§o* $unknownMissing unknown prices")
        add("")
        add("§bPercent owned§7: §a${"%.2f".format(data.ownedCakes.size * 100.0 / currentYear)}%")
    }

    private fun buildCakeRenderables(data: CakeData) = buildList {
        add(Renderable.hoverTips("§c§lNew §f§lYear §c§lCake §f§lTracker", getHeaderTips(data)))
        add(buildDisplayTypeToggle())
        add(buildOrderTypeToggle())

        val cakeList = when (config.displayType) {
            DisplayType.OWNED_CAKES -> data.ownedCakes
            DisplayType.MISSING_CAKES -> data.missingCakes
            null -> data.missingCakes
        }

        if (cakeList.isEmpty()) {
            val colorCode = if (config.displayType == DisplayType.OWNED_CAKES) "§c" else "§a"
            val verbiage = if (config.displayType == DisplayType.OWNED_CAKES) "missing" else "owned"
            add(Renderable.string("$colorCode§lAll cakes $verbiage!"))
        } else add(
            Renderable.scrollList(
                getCakeRanges(cakeList, config.displayOrderType, config.displayType),
                height = maxTrackerHeight,
                velocity = 20.0,
                showScrollableTipsInList = true
            )
        )
    }

    private fun getCakeRanges(
        cakeList: Set<Int>,
        orderType: DisplayOrder,
        displayType: DisplayType,
    ): List<Renderable> = buildList {
        val sortedCakes = when (orderType) {
            DisplayOrder.OLDEST_FIRST -> cakeList.sorted()
            DisplayOrder.NEWEST_FIRST -> cakeList.sortedDescending()
        }

        var start = sortedCakes.first()
        var end = start

        for (year in sortedCakes.drop(1)) { // Skip the first item to prevent duplicate addition
            val oldestFirstAtEnd = orderType == DisplayOrder.OLDEST_FIRST && year == end + 1
            val newestFirstAtEnd = orderType == DisplayOrder.NEWEST_FIRST && year == end - 1

            if (oldestFirstAtEnd || newestFirstAtEnd) end = year
            else {
                val range = if (start != end) CakeRange(start, end) else CakeRange(start)
                add(range.getRenderable(displayType))
                start = year
                end = start
            }
        }

        val lastRange =
            if (start != end) CakeRange(start, end)
            else CakeRange(start)

        add(lastRange.getRenderable(displayType))
    }
}
