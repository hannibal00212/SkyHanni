package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.HoppityEggLocationsJson
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.CollectionUtils.collectWhile
import at.hannibal2.skyhanni.utils.CollectionUtils.consumeWhile
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.setLore
import at.hannibal2.skyhanni.utils.KSerializable
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HoppityCollectionStats {

    private val config get() = ChocolateFactoryAPI.config

    private val patternGroup = ChocolateFactoryAPI.patternGroup.group("collection")

    /**
     * REGEX-TEST: (1/17) Hoppity's Collection
     * REGEX-TEST: (12/17) Hoppity's Collection
     * REGEX-TEST: Hoppity's Collection
     */
    private val pagePattern by patternGroup.pattern(
        "page.current",
        "(?:\\((?<page>\\d+)/(?<maxPage>\\d+)\\) )?Hoppity's Collection",
    )
    private val duplicatesFoundPattern by patternGroup.pattern(
        "duplicates.found",
        "§7Duplicates Found: §a(?<duplicates>[\\d,]+)",
    )

    /**
     * REGEX-TEST: §7§8You cannot find this rabbit until you
     * REGEX-TEST: §7§8You have not found this rabbit yet!
     */
    private val rabbitNotFoundPattern by patternGroup.pattern(
        "rabbit.notfound",
        "(?:§.)+You (?:have not found this rabbit yet!|cannot find this rabbit until you)",
    )

    private val rabbitsFoundPattern by patternGroup.pattern(
        "rabbits.found",
        "§.§l§m[ §a-z]+§r §.(?<current>[0-9]+)§./§.(?<total>[0-9]+)",
    )

    /**
     * REGEX-TEST: §a✔ §7Requirement
     */
    private val requirementMet by patternGroup.pattern(
        "rabbit.requirement.met",
        "§a✔ §7Requirement",
    )

    /**
     * REGEX-TEST: §c✖ §7Requirement §e0§7/§a15
     * REGEX-TEST: §c✖ §7Requirement §e6§7/§a20
     * REGEX-TEST: §c✖ §7Requirement §e651§7/§a1,000
     */
    private val requirementNotMet by patternGroup.pattern(
        "rabbit.requirement.notmet",
        "§c✖ §7Requirement.*",
    )

    /**
     * REGEX-TEST: §c✖ §7Requirement §e0§7/§a15
     * REGEX-TEST: §c✖ §7Requirement §e6§7/§a20
     * REGEX-TEST: §c✖ §7Requirement §e651§7/§a1,000
     */
    private val requirementAmountNotMet by patternGroup.pattern(
        "rabbit.requirement.notmet.amount",
        "§c✖ §7Requirement §e(?<acquired>[\\d,]+)§7/§a(?<required>[\\d,]+)",
    )

    /**
     * REGEX-TEST: §6Factory Milestones§7.
     */
    private val factoryMilestone by RepoPattern.pattern(
        "rabbit.requirement.factory",
        "§6Factory Milestones§7.",
    )

    /**
     * REGEX-TEST: §6Shop Milestones§7.
     */
    private val shopMilestone by RepoPattern.pattern(
        "rabbit.requirement.shop",
        "§6Shop Milestones§7.",
    )

    /**
     * REGEX-TEST: §7§7Obtained by finding a §6Golden Stray
     */
    private val strayRabbit by RepoPattern.pattern(
        "rabbit.requirement.stray",
        "(?:§.)+Obtained by finding a §6Golden Stray",
    )

    /**
     * REGEX-TEST: Find 15 unique egg locations in the Deep Caverns.
     */
    private val locationRequirementDescription by patternGroup.pattern(
        "rabbit.requirement.location",
        "Find 15 unique egg locations in (the )?(?<location>.*)\\..*",
    )

    /**
     * REGEX-TEST: §7§2Dwarven Mines Resident
     * REGEX-TEST: §7§5Crystal Hollows Resident
     * REGEX-TEST: §7§eThe Farming Islands Resident
     * REGEX-TEST: §7§cDungeon Hub Resident
     * REGEX-TEST: §7§dThe End Resident
     * REGEX-TEST: §7§6Gold Mine Resident
     * REGEX-TEST: §7§aThe Park Resident
     * REGEX-TEST: §7§cSpider's Den Resident
     * REGEX-TEST: §7§bDeep Caverns Resident
     * REGEX-TEST: §7§cCrimson Isle Resident
     * REGEX-TEST: §7§bHub Resident
     */
    private val residentRabbitPattern by patternGroup.pattern(
        "rabbit.requirement.resident",
        "§7§.(?<island>.*) Resident",
    )

    /**
     * REGEX-TEST: §7Current Hotspot: §dThe End
     */
    private val hotspotLocationPattern by patternGroup.pattern(
        "rabbit.hotspot",
        "§7Current Hotspot: §.(?<location>.*)",
    )

    private var display = emptyList<Renderable>()
    private val loggedRabbits
        get() = ProfileStorageData.profileSpecific?.chocolateFactory?.rabbitCounts ?: mutableMapOf()

    enum class HighlightRabbitTypes(
        private val displayName: String,
        val color: LorenzColor,
    ) {
        ABI("§2Abi", LorenzColor.DARK_GREEN),
        FACTORY("§eFactory Milestones", LorenzColor.YELLOW),
        MET("§aRequirement Met", LorenzColor.GREEN),
        NOT_MET("§cRequirement Not Met.", LorenzColor.RED),
        SHOP("§6Shop Milestones", LorenzColor.GOLD),
        STRAYS("§3Stray Rabbits", LorenzColor.DARK_AQUA),
        RESIDENTS("§bResident Rabbits §7(current island)", LorenzColor.AQUA),
        HOTSPOTS("§dHotspot Rabbits §7(current island)", LorenzColor.LIGHT_PURPLE)
        ;

        override fun toString(): String = displayName
    }

    @KSerializable
    data class LocationRabbit(
        val locationName: String,
        val loreFoundCount: Int,
        val requiredCount: Int,
    ) {
        private fun getSkyhanniFoundCount(): Int {
            val islandType = IslandType.getByNameOrNull(locationName) ?: return 0
            val foundLocations = HoppityEggLocations.getEggsIn(islandType)
            return foundLocations.size
        }

        val foundCount get() = maxOf(getSkyhanniFoundCount(), loreFoundCount)

        fun hasMetRequirements(): Boolean {
            return foundCount >= requiredCount
        }
    }

    private val locationRabbitRequirements: MutableMap<String, LocationRabbit>
        get() = ProfileStorageData.profileSpecific?.chocolateFactory?.locationRabbitRequirements ?: mutableMapOf()

    private val residentRabbitData: MutableMap<IslandType, MutableMap<String, Boolean?>>
        get() = ProfileStorageData.profileSpecific?.chocolateFactory?.residentRabbits ?: mutableMapOf()

    var inInventory = false

    private val highlightConfigMap: Map<Pattern, HighlightRabbitTypes> = mapOf(
        factoryMilestone to HighlightRabbitTypes.FACTORY,
        requirementMet to HighlightRabbitTypes.MET,
        requirementNotMet to HighlightRabbitTypes.NOT_MET,
        shopMilestone to HighlightRabbitTypes.SHOP,
        strayRabbit to HighlightRabbitTypes.STRAYS,
    )

    private fun missingRabbitStackNeedsFix(stack: ItemStack): Boolean =
        stack.item == Items.dye && (stack.metadata == 8 || stack.getLore().any { it.lowercase().contains("milestone") })

    private val replacementCache: MutableMap<String, ItemStack> = mutableMapOf()

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        replacementCache[event.originalItem.displayName]?.let { event.replace(it) }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<HoppityEggLocationsJson>("HoppityEggLocations")
        ChatUtils.chat("data.residentLocations.size: ${data.residentLocations.size}")
        for ((island, residents) in data.residentLocations) {
            val storageSet = residentRabbitData.getOrPut(island) { mutableMapOf() }
            if (storageSet.size == residents.size) continue
            residents.forEach { resident ->
                storageSet[resident] = null
            }
            residentRabbitData[island] = storageSet
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!(LorenzUtils.inSkyBlock)) return
        if (!pagePattern.matches(event.inventoryName)) {
            // Clear highlight cache in case options are toggled
            highlightMap.clear()
            return
        }

        event.inventoryItems.values.filter { it.hasDisplayName() && missingRabbitStackNeedsFix(it) }.forEach { stack ->
            val rarity = HoppityAPI.rarityByRabbit(stack.displayName)
            // Add NBT for the dye color itself
            val newItemStack = if (config.rarityDyeRecolor) ItemStack(
                Items.dye, 1,
                when (rarity) {
                    LorenzRarity.COMMON -> 7 // Light gray dye
                    LorenzRarity.UNCOMMON -> 10 // Lime dye
                    LorenzRarity.RARE -> 4 // Lapis lazuli
                    LorenzRarity.EPIC -> 5 // Purple dye
                    LorenzRarity.LEGENDARY -> 14 // Orange dye
                    LorenzRarity.MYTHIC -> 13 // Magenta dye
                    LorenzRarity.DIVINE -> 12 // Light blue dye
                    LorenzRarity.SPECIAL -> 1 // Rose Red - Covering bases for future (?)
                    else -> return
                },
            ) else stack

            newItemStack.setLore(buildDescriptiveMilestoneLore(stack))
            newItemStack.setStackDisplayName(stack.displayName)
            replacementCache[stack.displayName] = newItemStack
        }

        inInventory = true
        if (config.hoppityCollectionStats) {
            display = buildDisplay(event)
        }

        if (config.showResidentSummaryInHoppityStats) {
            for ((_, stack) in event.inventoryItems) setResidentDataFromStack(stack)
        }

        if (config.highlightRabbits.isNotEmpty()) {
            for ((_, stack) in event.inventoryItems) filterRabbitToHighlight(stack)
        }
    }

    private fun buildDescriptiveMilestoneLore(itemStack: ItemStack): List<String> {
        val existingLore = itemStack.getLore().toMutableList()
        var replaceIndex: Int? = null
        var milestoneType: HoppityEggType = HoppityEggType.BREAKFAST

        if (factoryMilestone.anyMatches(existingLore)) {
            milestoneType = HoppityEggType.CHOCOLATE_FACTORY_MILESTONE
            replaceIndex = existingLore.indexOfFirst { loreMatch -> factoryMilestone.matches(loreMatch) }
        } else if (shopMilestone.anyMatches(existingLore)) {
            milestoneType = HoppityEggType.CHOCOLATE_SHOP_MILESTONE
            replaceIndex = existingLore.indexOfFirst { loreMatch -> shopMilestone.matches(loreMatch) }
        }

        replaceIndex?.let {
            ChocolateFactoryAPI.milestoneByRabbit(itemStack.displayName)?.let {
                val displayAmount = it.amount.shortFormat()
                val operationFormat = when (milestoneType) {
                    HoppityEggType.CHOCOLATE_SHOP_MILESTONE -> "spending"
                    HoppityEggType.CHOCOLATE_FACTORY_MILESTONE -> "reaching"
                    else -> "" // Never happens
                }

                // List indexing is weird
                existingLore[replaceIndex - 1] = "§7Obtained by $operationFormat §6$displayAmount"
                existingLore[replaceIndex] = "§7all-time §6Chocolate."
                return existingLore
            }
        }

        return existingLore
    }

    private fun setResidentDataFromStack(stack: ItemStack) {
        val lore = stack.getLore()
        if (lore.isEmpty()) return

        residentRabbitPattern.firstMatcher(lore) {
            val island = IslandType.getByNameOrNull(group("island")) ?: return@firstMatcher
            stack.displayName.removeColor().takeIf { HoppityCollectionData.isKnownRabbit(it) }?.let { residentName ->
                val found = !rabbitNotFoundPattern.anyMatches(lore)
                residentRabbitData[island]?.let { data ->
                    data[residentName] = found
                }
            }
        }
    }

    private fun filterRabbitToHighlight(stack: ItemStack) {
        val lore = stack.getLore()

        if (lore.isEmpty()) return
        if (!rabbitNotFoundPattern.anyMatches(lore) && !config.highlightFoundRabbits) return

        if (highlightMap.containsKey(stack.displayName)) return

        if (stack.displayName == "§aAbi" && config.highlightRabbits.contains(HighlightRabbitTypes.ABI)) {
            highlightMap[stack.displayName] = HighlightRabbitTypes.ABI.color
            return
        }

        // cache rabbits until collection is closed
        for ((pattern, rabbitType) in highlightConfigMap) {
            if (pattern.anyMatches(lore) && config.highlightRabbits.contains(rabbitType)) {
                highlightMap[stack.displayName] = rabbitType.color
                break
            }
        }

        residentRabbitPattern.firstMatcher(lore) {
            val island = IslandType.getByNameOrNull(group("island")) ?: return@firstMatcher
            if (island.isInIsland() && config.highlightRabbits.contains(HighlightRabbitTypes.RESIDENTS)) {
                highlightMap[stack.displayName] = HighlightRabbitTypes.RESIDENTS.color
            }
        }

        hotspotLocationPattern.firstMatcher(lore) {
            val island = IslandType.getByNameOrNull(group("location")) ?: return@firstMatcher
            if (island.isInIsland() && config.highlightRabbits.contains(HighlightRabbitTypes.HOTSPOTS)) {
                highlightMap[stack.displayName] = HighlightRabbitTypes.HOTSPOTS.color
            }
        }
    }

    private var highlightMap = mutableMapOf<String, LorenzColor>()

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        display = emptyList()
        replacementCache.clear()
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!inInventory) return
        if (!config.hoppityCollectionStats) return

        config.hoppityStatsPosition.renderRenderables(
            display,
            extraSpace = 5,
            posLabel = "Hoppity's Collection Stats",
        )
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!inInventory) return
        if (config.highlightRabbits.isEmpty()) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            val name = slot.stack.displayName

            if (name.isEmpty()) continue
            highlightMap[name]?.let {
                slot highlight it
            }
        }
    }

    private fun addResidentRabbitsInformationToHud(newList: MutableList<Renderable>) {
        if (!config.showResidentSummaryInHoppityStats) return
        val residentRabbitData = residentRabbitData

        val totalResidentRabbitsCount = residentRabbitData.values.sumOf { it.size }
        val foundResidentRabbitsCount = residentRabbitData.values.sumOf {
            it.values.count { found ->
                found == true
            }
        }
        val indeterminateResidentRabbitsCount = residentRabbitData.values.sumOf {
            it.values.count { found ->
                found == null
            }
        }

        val tips: List<String> = buildList {
            addAll(
                residentRabbitData.map { (island, residents) ->
                    val foundResidents = residents.values.count { it == true }
                    val totalResidents = residents.size
                    val indeterminateResidents = residents.values.count { it == null }
                    val islandName = island.displayName
                    val color = if (foundResidents == totalResidents) "§a" else "§c"
                    val foundFormat =
                        if (indeterminateResidents > 0) "???"
                        else foundResidents.toString()
                    "$islandName: $color$foundFormat§7/§a$totalResidents"
                }
            )
            if (indeterminateResidentRabbitsCount > 0) {
                add("")
                add("§cScroll through all pages to refresh resident rabbit data")
            }
        }

        newList.add(
            Renderable.hoverTips(
                Renderable.wrappedString(
                    "§6Resident Rabbits§7: §a$foundResidentRabbitsCount§7/§a$totalResidentRabbitsCount",
                    width = 200,
                ),
                tips,
            ),
        )

    }

    private fun addLocationRequirementRabbitsToHud(newList: MutableList<Renderable>) {
        if (!config.showLocationRequirementsRabbitsInHoppityStats) return
        val missingLocationRabbits = locationRabbitRequirements.values.filter { !it.hasMetRequirements() }

        val tips = locationRabbitRequirements.map { (name, rabbit) ->
            "$name §7(§e${rabbit.locationName}§7): ${
                if (rabbit.hasMetRequirements()) "§a" else "§c"
            }${rabbit.foundCount}§7/§a${rabbit.requiredCount}"
        }

        newList.add(
            Renderable.hoverTips(
                if (missingLocationRabbits.isEmpty()) {
                    Renderable.wrappedString("§aFound enough eggs in all locations", width = 200)
                } else {
                    Renderable.wrappedString(
                        "§cMissing Locations§7:§c " + missingLocationRabbits.joinToString("§7, §c") {
                            it.locationName
                        },
                        width = 200,
                    )
                },
                tips,
            ),
        )
    }

    private fun buildDisplay(event: InventoryFullyOpenedEvent): MutableList<Renderable> {
        logRabbits(event)

        val newList = mutableListOf<Renderable>()
        newList.add(Renderable.string("§eHoppity Rabbit Collection§f:"))
        newList.add(LorenzUtils.fillTable(getRabbitStats(), padding = 5))

        addLocationRequirementRabbitsToHud(newList)
        addResidentRabbitsInformationToHud(newList)

        val loggedRabbitCount = loggedRabbits.size
        val foundRabbitCount = getFoundRabbitsFromHypixel(event)

        if (loggedRabbitCount < foundRabbitCount) {
            newList.addString("")
            newList.add(
                Renderable.wrappedString(
                    "§cPlease Scroll through \n" + "§call pages!",
                    width = 200,
                ),
            )
        }
        return newList
    }

    private fun getRabbitStats(): MutableList<DisplayTableEntry> {
        var totalUniquesFound = 0
        var totalDuplicates = 0
        var totalChocolatePerSecond = 0
        var totalChocolateMultiplier = 0.0

        val table = mutableListOf<DisplayTableEntry>()
        for (rarity in RabbitCollectionRarity.entries) {
            val isTotal = rarity == RabbitCollectionRarity.TOTAL

            val foundOfRarity = loggedRabbits.filterKeys {
                HoppityCollectionData.getRarity(it) == rarity
            }

            val title = "${rarity.displayName} Rabbits"
            val uniquesFound = foundOfRarity.size
            val duplicates = foundOfRarity.values.sum() - uniquesFound

            val chocolateBonuses = foundOfRarity.keys.map {
                HoppityCollectionData.getChocolateBonuses(it)
            }

            val chocolatePerSecond = chocolateBonuses.sumOf { it.chocolate }
            val chocolateMultiplier = chocolateBonuses.sumOf { it.multiplier }

            if (hasFoundRabbit("Sigma") && rarity == RabbitCollectionRarity.MYTHIC) {
                totalChocolatePerSecond += uniquesFound * 5
            }

            if (!isTotal) {
                totalUniquesFound += uniquesFound
                totalDuplicates += duplicates
                totalChocolatePerSecond += chocolatePerSecond
                totalChocolateMultiplier += chocolateMultiplier
            }

            val displayFound = if (isTotal) totalUniquesFound else uniquesFound
            val displayTotal = if (isTotal) {
                HoppityCollectionData.knownRabbitCount
            } else {
                HoppityCollectionData.knownRabbitsOfRarity(rarity)
            }
            val displayDuplicates = if (isTotal) totalDuplicates else duplicates
            val displayChocolatePerSecond = if (isTotal) totalChocolatePerSecond else chocolatePerSecond
            val displayChocolateMultiplier = if (isTotal) totalChocolateMultiplier else chocolateMultiplier

            val hover = buildList {
                add(title)
                add("")
                add("§7Unique Rabbits: §a$displayFound§7/§a$displayTotal")
                add("§7Duplicate Rabbits: §a$displayDuplicates")
                add("§7Total Rabbits Found: §a${displayFound + displayDuplicates}")
                add("")
                add("§7Chocolate Per Second: §a${displayChocolatePerSecond.addSeparators()}")
                add("§7Chocolate Multiplier: §a${displayChocolateMultiplier.roundTo(3)}")
            }
            table.add(
                DisplayTableEntry(
                    title,
                    "§a$displayFound§7/§a$displayTotal",
                    displayTotal.toDouble(),
                    rarity.item,
                    hover,
                ),
            )
        }
        return table
    }

    fun getRabbitCount(name: String): Int = name.removeColor().run {
        loggedRabbits[this]?.takeIf { HoppityCollectionData.isKnownRabbit(this) } ?: 0
    }

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onRabbitFound(event: RabbitFoundEvent) {
        DelayedRun.runDelayed(1.seconds) {
            val rabbit = event.rabbitName.removeColor()
            if (!HoppityCollectionData.isKnownRabbit(rabbit)) return@runDelayed
            loggedRabbits.addOrPut(rabbit, 1)
            residentRabbitData.entries.firstOrNull { (_, residentMap) ->
                residentMap.any { it.key == rabbit && (it.value == false || it.value == null) }
            }?.also { (_, residentMap) ->
                residentMap[rabbit] = true
            }
        }
    }

    // Gets the found rabbits according to the Hypixel progress bar
    // used to make sure that mod data is synchronized with Hypixel
    private fun getFoundRabbitsFromHypixel(event: InventoryFullyOpenedEvent): Int {
        return event.inventoryItems.firstNotNullOf {
            rabbitsFoundPattern.firstMatcher(it.value.getLore()) {
                group("current").formatInt()
            }
        }
    }

    private fun saveLocationRabbit(rabbitName: String, lore: List<String>) {
        val iterator = lore.iterator()

        val requirement = iterator.consumeWhile { line ->
            val requirementMet = requirementMet.matches(line)
            if (requirementMet) Pair(15, 15) // This is kind of hardcoded?
            else requirementAmountNotMet.findMatcher(line) {
                group("acquired").formatInt() to group("required").formatInt()
            }
        } ?: return

        val requirementDescriptionCollate = iterator.collectWhile { line ->
            line.isNotEmpty()
        }.joinToString(" ") { it.removeColor() }

        val location = locationRequirementDescription.findMatcher(requirementDescriptionCollate) {
            group("location")
        } ?: return

        locationRabbitRequirements[rabbitName] = LocationRabbit(location, requirement.first, requirement.second)
    }

    private fun logRabbits(event: InventoryFullyOpenedEvent) {
        for (item in event.inventoryItems.values) {
            val itemName = item.displayName?.removeColor() ?: continue
            val isRabbit = HoppityCollectionData.isKnownRabbit(itemName)

            if (!isRabbit) continue

            val itemLore = item.getLore()

            saveLocationRabbit(itemName, itemLore)

            val found = !rabbitNotFoundPattern.anyMatches(itemLore)

            if (!found) {
                // if the config has wrong data, remove it
                loggedRabbits.remove(itemName)
                continue
            }

            val duplicates = duplicatesFoundPattern.firstMatcher(itemLore) {
                group("duplicates").formatInt()
            } ?: 0

            loggedRabbits[itemName] = duplicates + 1
        }
    }

    // bugfix for some weird potential user errors (e.g. if users play on alpha and get rabbits)
    fun resetSavedRabbits() {
        loggedRabbits.clear()
        ChatUtils.chat("Reset saved rabbit data.")
    }

    fun hasFoundRabbit(rabbit: String): Boolean = loggedRabbits.containsKey(rabbit)

    enum class RabbitCollectionRarity(
        val displayName: String,
        val item: NEUInternalName,
    ) {
        COMMON("§fCommon", "STAINED_GLASS".toInternalName()),
        UNCOMMON("§aUncommon", "STAINED_GLASS-5".toInternalName()),
        RARE("§9Rare", "STAINED_GLASS-11".toInternalName()),
        EPIC("§5Epic", "STAINED_GLASS-10".toInternalName()),
        LEGENDARY("§6Legendary", "STAINED_GLASS-1".toInternalName()),
        MYTHIC("§dMythic", "STAINED_GLASS-6".toInternalName()),
        DIVINE("§bDivine", "STAINED_GLASS-3".toInternalName()),
        TOTAL("§cTotal", "STAINED_GLASS-14".toInternalName()),
        ;

        companion object {
            fun fromDisplayName(displayName: String) = entries.firstOrNull { it.name == displayName }
        }
    }
}
