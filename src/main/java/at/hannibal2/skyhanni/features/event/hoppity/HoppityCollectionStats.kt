package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.CollectionUtils.collectWhile
import at.hannibal2.skyhanni.utils.CollectionUtils.consumeWhile
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KSerializable
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.find
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
        "(?:\\((?<page>\\d+)\\/(?<maxPage>\\d+)\\) )?Hoppity's Collection",
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
     * REGEX-TEST: Find 15 unique egg locations in the Deep Caverns.
     */
    private val locationRequirementDescription by patternGroup.pattern(
        "rabbit.requirement.location",
        "Find 15 unique egg locations in (the )?(?<location>.*)\\..*",
    )

    private var display = emptyList<Renderable>()
    private val loggedRabbits
        get() = ProfileStorageData.profileSpecific?.chocolateFactory?.rabbitCounts ?: mutableMapOf()

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

    var inInventory = false

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!(LorenzUtils.inSkyBlock)) return
        if (!pagePattern.matches(event.inventoryName)) return

        inInventory = true
        if (config.hoppityCollectionStats) {
            display = buildDisplay(event)
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        display = emptyList()
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

    // TODO cache with inventory update event
    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!config.highlightRabbitsWithRequirement) return
        if (!inInventory) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            val lore = slot.stack.getLore()
            if (lore.any { requirementMet.find(it) } && !config.onlyHighlightRequirementNotMet)
                slot highlight LorenzColor.GREEN
            if (lore.any { requirementNotMet.find(it) }) {
                val found = !rabbitNotFoundPattern.anyMatches(lore)
                // Hypixel allows purchasing Rabbits from Hoppity NPC even when the requirement is not yet met.
                if (!found) {
                    slot highlight LorenzColor.RED
                }
            }
        }
    }

    private fun addLocationRequirementRabbitsToHud(newList: MutableList<Renderable>) {
        if (!config.showLocationRequirementsRabbitsInHoppityStats) return
        val missingLocationRabbits = locationRabbitRequirements.values.filter { !it.hasMetRequirements() }

        val tips = locationRabbitRequirements.map {
            it.key + " §7(§e" + it.value.locationName + "§7): " + (if (it.value.hasMetRequirements()) "§a" else "§c") +
                it.value.foundCount + "§7/§a" + it.value.requiredCount
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
                add("§7Chocolate Multiplier: §a${displayChocolateMultiplier.round(3)}")
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

    fun incrementRabbit(name: String) {
        val rabbit = name.removeColor()
        if (!HoppityCollectionData.isKnownRabbit(rabbit)) return
        loggedRabbits[rabbit] = (loggedRabbits[rabbit] ?: 0) + 1
    }

    // Gets the found rabbits according to the Hypixel progress bar
    // used to make sure that mod data is synchronized with Hypixel
    private fun getFoundRabbitsFromHypixel(event: InventoryFullyOpenedEvent): Int {
        return event.inventoryItems.firstNotNullOf {
            it.value.getLore().matchFirst(rabbitsFoundPattern) {
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

            if (!found) continue

            val duplicates = itemLore.matchFirst(duplicatesFoundPattern) {
                group("duplicates").formatInt()
            } ?: 0

            loggedRabbits[itemName] = duplicates + 1
        }
    }


    // bugfix for some weird potential user errors (e.g. if users play on alpha and get rabbits)
    fun clearSavedRabbits() {
        loggedRabbits.clear()
        ChatUtils.chat("Cleared saved rabbit data.")
    }

    fun hasFoundRabbit(rabbit: String): Boolean = loggedRabbits.containsKey(rabbit)

    enum class RabbitCollectionRarity(
        val displayName: String,
        val item: NEUInternalName,
    ) {
        COMMON("§fCommon", "STAINED_GLASS".asInternalName()),
        UNCOMMON("§aUncommon", "STAINED_GLASS-5".asInternalName()),
        RARE("§9Rare", "STAINED_GLASS-11".asInternalName()),
        EPIC("§5Epic", "STAINED_GLASS-10".asInternalName()),
        LEGENDARY("§6Legendary", "STAINED_GLASS-1".asInternalName()),
        MYTHIC("§dMythic", "STAINED_GLASS-6".asInternalName()),
        DIVINE("§bDivine", "STAINED_GLASS-3".asInternalName()),
        TOTAL("§cTotal", "STAINED_GLASS-14".asInternalName()),
        ;

        companion object {
            fun fromDisplayName(displayName: String) = entries.firstOrNull { it.name == displayName }
        }
    }
}
