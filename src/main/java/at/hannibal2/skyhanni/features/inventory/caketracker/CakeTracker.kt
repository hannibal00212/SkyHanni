package at.hannibal2.skyhanni.features.inventory.caketracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.inventory.caketracker.CakeTrackerConfig.CakeTrackerDisplayOrderType
import at.hannibal2.skyhanni.features.inventory.caketracker.CakeTrackerConfig.CakeTrackerDisplayType
import at.hannibal2.skyhanni.features.inventory.patternGroup
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.renderables.toSearchable
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object CakeTracker {

    private fun getCakeTrackerData() = ProfileStorageData.profileSpecific?.cakeTracker
    private val config get() = SkyHanniMod.feature.inventory.cakeTracker
    private var currentYear = 0

    private var inCakeBag = false
    private var inCakeInventory = false
    private var timeOpenedCakeInventory = SimpleTimeMark.farPast()
    private var inAuctionHouse = false
    private var unobtainedCakesDisplayed = false
    private var searchingForCakes = false
    private var knownCakesInCurrentInventory = mutableListOf<Int>()

    /**
     * REGEX-TEST: §cNew Year Cake (Year 360)
     * REGEX-TEST: §cNew Year Cake (Year 1,000)
     * REGEX-TEST: §f§f§cNew Year Cake (Year 330)
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
        "^(Ender Chest \\(\\d{1,2}/\\d{1,2}\\)|.*Backpack(?:§r)? \\(Slot #\\d{1,2}\\)|New Year Cake Bag)$",
    )

    /**
     * REGEX-TEST: New Year Cake Bag
     */
    private val cakeBagPattern by patternGroup.pattern(
        "cake.bag",
        "^New Year Cake Bag$",
    )

    /**
     * REGEX-TEST: Auctions Browser
     * REGEX-TEST: Auctions: "Test"
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
        "^Auctions: \"New Year C.*$",
    )

    private val tracker = SkyHanniTracker("New Year Cake Tracker", { Data() }, { it.cakeTracker }) {
        drawDisplay(it)
    }

    class Data : TrackerData() {
        override fun reset() {
            cakesOwned.clear()
        }

        @Expose
        val cakesOwned: MutableSet<Int> = mutableSetOf()

        @Expose
        var cakesMissing: MutableSet<Int> = mutableSetOf()
    }

    private fun addCake(cakeYear: Int) {
        val cakeTrackerData = getCakeTrackerData() ?: return
        if (cakeYear !in cakeTrackerData.cakesOwned) {
            tracker.modify {
                it.cakesOwned.add(cakeYear)
            }
        }
        recalculateMissingCakes()
    }

    private fun removeCake(cakeYear: Int) {
        val cakeTrackerData = getCakeTrackerData() ?: return
        if (cakeYear in cakeTrackerData.cakesOwned) {
            tracker.modify {
                it.cakesOwned.remove(cakeYear)
            }
        }
        recalculateMissingCakes()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shresetcaketracker") {
            description = "Resets the New Year Cake Tracker"
            category = CommandCategory.USERS_RESET
            callback { tracker.resetCommand() }
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (inCakeBag || (inAuctionHouse && (unobtainedCakesDisplayed || searchingForCakes))) {
            tracker.renderDisplay(config.cakeTrackerPosition, displayModeToggleable = false)
        }
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (inAuctionHouse) {
            unobtainedCakesDisplayed = getCakeTrackerData()?.let { data ->
                InventoryUtils.getItemsInOpenChest().onEach { cakeItem ->
                    cakeNamePattern.matchMatcher(cakeItem.stack.displayName) {
                        group("year").toInt().takeIf {
                            it !in data.cakesOwned
                        }?.let {
                            cakeItem highlight config.auctionHighlightColor.toChromaColor()
                        }
                    }
                }.isNotEmpty()
            } ?: false
        }
        if (inCakeInventory) checkInventoryCakes()
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        knownCakesInCurrentInventory.clear()
        val inventoryName = event.inventoryName
        if (cakeContainerPattern.matches(inventoryName)) {
            if (cakeBagPattern.matches(inventoryName)) inCakeBag = true
            knownCakesInCurrentInventory = event.inventoryItems.values.mapNotNull { item ->
                cakeNamePattern.matchMatcher(item.displayName) {
                    groupOrNull("year")?.formatInt()?.let {
                        addCake(it)
                        it
                    }
                }
            }.toMutableList()
            inCakeInventory = true
            timeOpenedCakeInventory = SimpleTimeMark.now()
            tracker.firstUpdate()
        }
        if (auctionBrowserPattern.matches(inventoryName)) {
            inAuctionHouse = true
            searchingForCakes = auctionCakeSearchPattern.matches(inventoryName)
        } else inAuctionHouse = false
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inCakeBag = false
        inCakeInventory = false
        knownCakesInCurrentInventory.clear()
        inAuctionHouse = false
        unobtainedCakesDisplayed = false
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
            cakeNamePattern.matchMatcher(item.stack.displayName) {
                group("year")?.toInt()
                cakeNamePattern.matchGroup(item.stack.displayName, "year")?.toInt()
            }
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
        tracker.modify {
            it.cakesMissing = (1..currentYear).filterNot { year ->
                year !in it.cakesOwned
            }.toMutableSet()
        }
    }

    private class CakeRange(var start: Int, var end: Int = 0) {
        fun getRenderable(displayType: CakeTrackerDisplayType): Renderable {
            val colorCode =
                if (displayType == CakeTrackerDisplayType.OWNED_CAKES) "§a"
                else "§c"
            val stringRenderable =
                Renderable.string(
                    if (end != 0) "§fYears $colorCode$start§f-$colorCode$end"
                    else "§fYear $colorCode$start",
                )
            return if (displayType == CakeTrackerDisplayType.MISSING_CAKES) Renderable.link(
                stringRenderable,
                { HypixelCommands.auctionSearch("New Year Cake (Year $start)") },
            ) else stringRenderable
        }
    }

    private fun setDisplayType(type: CakeTrackerDisplayType) {
        val cakeTrackerData = getCakeTrackerData() ?: return
        config.displayType = type
        drawDisplay(cakeTrackerData)
        tracker.update()
    }

    private fun buildDisplayTypeToggle(): Renderable = Renderable.horizontalContainer(
        buildList {
            val ownedString =
                if (config.displayType == CakeTrackerDisplayType.OWNED_CAKES) "§7§l[§r §a§nOwned§r §7§l]"
                else "§aOwned"
            val missingString =
                if (config.displayType == CakeTrackerDisplayType.MISSING_CAKES) "§7§l[§r §c§nMissing§r §7§l]"
                else "§cMissing"

            add(
                Renderable.optionalLink(
                    ownedString,
                    { setDisplayType(CakeTrackerDisplayType.OWNED_CAKES) },
                    condition = { config.displayType != CakeTrackerDisplayType.OWNED_CAKES },
                ),
            )
            add(Renderable.string(" §7§l- §r"))
            add(
                Renderable.optionalLink(
                    missingString,
                    { setDisplayType(CakeTrackerDisplayType.MISSING_CAKES) },
                    condition = { config.displayType != CakeTrackerDisplayType.MISSING_CAKES },
                ),
            )
        },
    )

    private fun setDisplayOrderType(type: CakeTrackerDisplayOrderType) {
        val cakeTrackerData = getCakeTrackerData() ?: return
        config.displayOrderType = type
        drawDisplay(cakeTrackerData)
        tracker.update()
    }

    private fun buildOrderTypeToggle(): Renderable = Renderable.horizontalContainer(
        buildList {
            val newestString =
                if (config.displayOrderType == CakeTrackerDisplayOrderType.NEWEST_FIRST) "§7§l[§r §a§nNewest First§r §7§l]"
                else "§aNewest First"
            val oldestString =
                if (config.displayOrderType == CakeTrackerDisplayOrderType.OLDEST_FIRST) "§7§l[§r §c§nOldest First§r §7§l]"
                else "§cOldest First"

            add(
                Renderable.optionalLink(
                    newestString,
                    { setDisplayOrderType(CakeTrackerDisplayOrderType.NEWEST_FIRST) },
                    condition = { config.displayOrderType != CakeTrackerDisplayOrderType.NEWEST_FIRST },
                ),
            )
            add(Renderable.string(" §7§l- §r"))
            add(
                Renderable.optionalLink(
                    oldestString,
                    { setDisplayOrderType(CakeTrackerDisplayOrderType.OLDEST_FIRST) },
                    condition = { config.displayOrderType != CakeTrackerDisplayOrderType.OLDEST_FIRST },
                ),
            )
        },
    )

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        add(
            Renderable.hoverTips(
                "§c§lNew §f§lYear §c§lCake §f§lTracker",
                tips = listOf("§aHave§7: §a${data.cakesOwned.count()}§7, §cMissing§7: §c${data.cakesMissing.count()}"),
            ).toSearchable(),
        )
        add(buildDisplayTypeToggle().toSearchable("Display Type"))
        add(buildOrderTypeToggle().toSearchable("Order Type"))

        val cakeList = when (config.displayType) {
            CakeTrackerDisplayType.OWNED_CAKES -> data.cakesOwned
            CakeTrackerDisplayType.MISSING_CAKES -> data.cakesMissing
            null -> data.cakesMissing
        }

        if (cakeList.isEmpty()) {
            val colorCode = if (config.displayType == CakeTrackerDisplayType.OWNED_CAKES) "§c" else "§a"
            val verbiage = if (config.displayType == CakeTrackerDisplayType.OWNED_CAKES) "missing" else "owned"
            addSearchString("$colorCode§lAll cakes $verbiage!")
        } else {
            val sortedCakes = when (config.displayOrderType) {
                CakeTrackerDisplayOrderType.OLDEST_FIRST -> cakeList.sorted()
                CakeTrackerDisplayOrderType.NEWEST_FIRST -> cakeList.sortedDescending()
                null -> cakeList
            }.toMutableList()

            // Combine consecutive years into ranges
            val cakeRanges = mutableListOf<CakeRange>()
            var start = sortedCakes.first()
            var end = start

            for (i in 1 until sortedCakes.size) {
                if ((config.displayOrderType == CakeTrackerDisplayOrderType.OLDEST_FIRST && sortedCakes[i] == end + 1) ||
                    (config.displayOrderType == CakeTrackerDisplayOrderType.NEWEST_FIRST && sortedCakes[i] == end - 1)
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

            cakeRanges.forEach { add(it.getRenderable(config.displayType).toSearchable("${it.start}")) }
        }
    }
}
