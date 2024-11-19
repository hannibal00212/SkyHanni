package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.createCommaSeparatedList
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// TODO Remove all removeColor calls in this class. Deal with the color code in regex.
// TODO also fix up this all being coded very poorly and having the same patterns in multiple places
@SkyHanniModule
object DungeonFinderFeatures {
    private val config get() = SkyHanniMod.feature.dungeon.partyFinder

    //  Repo group and patterns
    private val patternGroup = RepoPattern.group("dungeon.finder")
    private val pricePattern by patternGroup.pattern(
        "price",
        "(?i).*([0-9]{2,3}K|[0-9]{1,3}M|[0-9]+\\.[0-9]M|[0-9] ?MIL).*",
    )
    private val carryPattern by patternGroup.pattern(
        "carry",
        "(?i).*(CARRY|CARY|CARRIES|CARIES|COMP|TO CATA [0-9]{2}).*",
    )
    private val nonPugPattern by patternGroup.pattern(
        "nonpug",
        "(?i).*(PERM|VC|DISCORD).*",
    )

    /**
     * REGEX-TEST:  §b4sn_§f: §eArcher§b (§e29§b)
     * REGEX-TEST:  §akaydo_odyak§f: §eBerserk§b (§e26§b)
     */
    private val memberPattern by patternGroup.pattern(
        "member",
        ".*§.(?<playerName>.*)§f: §e(?<className>.*)§b \\(§e(?<level>.*)§b\\)",
    )

    /**
     * REGEX-TEST: §cRequires a Class at Level 25!
     */
    private val ineligiblePattern by patternGroup.pattern(
        "ineligible",
        "§c(Requires .*$|You don't meet the requirement!|Complete previous floor first!$)",
    )

    // TODO why is this same pattern here twice?
    /**
     * REGEX-TEST:  §b4sn_§f: §eArcher§b (§e29§b)
     * REGEX-TEST:  §akaydo_odyak§f: §eBerserk§b (§e26§b)
     */
    private val classLevelPattern by patternGroup.pattern(
        "class.level",
        " §.(?<playerName>.*)§f: §e(?<className>.*)§b \\(§e(?<level>.*)§b\\)",
    )

    /**
     * REGEX-TEST: §7§7Note: §fs+ clear first
     */
    private val notePattern by patternGroup.pattern(
        "note",
        "§7§7Note: §f(?<note>.*)",
    )

    /**
     * REGEX-TEST: The Catacombs
     * REGEX-TEST: MM The Catacombs
     */
    private val floorTypePattern by patternGroup.pattern(
        "floor.type",
        "(The Catacombs).*|.*(MM The Catacombs).*",
    )

    /**
     * REGEX-TEST: JohnRealNoob's Party
     */
    private val checkIfPartyPattern by patternGroup.pattern(
        "check.if.party",
        ".*'s Party",
    )
    private val partyFinderTitlePattern by patternGroup.pattern(
        "party.finder.title",
        "Party Finder",
    )
    private val catacombsGatePattern by patternGroup.pattern(
        "catacombs.gate",
        "Catacombs Gate",
    )
    private val selectFloorPattern by patternGroup.pattern(
        "select.floor",
        "Select Floor",
    )
    private val entranceFloorPattern by patternGroup.pattern(
        "entrance",
        "(.*Entrance)",
    )
    private val floorPattern by patternGroup.pattern(
        "floor",
        "(Floor .*)",
    )
    private val anyFloorPattern by patternGroup.pattern(
        "floor.any",
        "Any",
    )

    /**
     * REGEX-TEST: Master Mode The Catacombs
     * REGEX-TEST: MM The Catacombs
     */
    private val masterModeFloorPattern by patternGroup.pattern(
        "floor.mastermode",
        "(MM|.*Master Mode) The Catacombs.*",
    )
    private val dungeonFloorPattern by patternGroup.pattern(
        "floor.dungeon",
        "(Dungeon: .*)",
    )
    private val floorFloorPattern by patternGroup.pattern(
        "floor.pattern",
        "(Floor: .*)",
    )
    private val floorNumberPattern by patternGroup.pattern(
        "floor.number",
        ".* (?<floorNum>[IV\\d]+)",
    )

    /**
     * REGEX-TEST: Currently Selected: Mage
     */
    private val getDungeonClassPattern by patternGroup.pattern(
        "get.dungeon.class",
        "Currently Selected: (?<class>.*)",
    )
    private val detectDungeonClassPattern by patternGroup.pattern(
        "detect.dungeon.class",
        "§7View and select a dungeon class\\.",
    )

    //  Variables used
    private var selectedClass = ""
    private var floorStackSize = mapOf<Int, String>()
    private var highlightParty = mapOf<Int, LorenzColor>()
    private var toolTipMap = mapOf<Int, List<String>>()
    private var inInventory = false

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled()) return

        floorStackSize = stackTip(event)
        highlightParty = highlightingHandler(event)
        toolTipMap = toolTipHandler(event)
    }

    private fun stackTip(event: InventoryOpenEvent): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        val inventoryName = event.inventoryName
        if (catacombsGatePattern.matches(inventoryName)) catacombsGateStackTip(event.inventoryItems, map)
        if (!config.floorAsStackSize) return map
        if (selectFloorPattern.matches(inventoryName)) selectFloorStackTip(event.inventoryItems, map)
        if (partyFinderTitlePattern.matches(inventoryName)) partyFinderStackTip(event.inventoryItems, map)
        return map
    }

    private fun selectFloorStackTip(inventoryItems: Map<Int, ItemStack>, map: MutableMap<Int, String>) {
        inInventory = true
        for ((slot, stack) in inventoryItems) {
            val name = stack.displayName.removeColor()
            map[slot] = if (anyFloorPattern.matches(name)) {
                "A"
            } else if (entranceFloorPattern.matches(name)) {
                "E"
            } else if (floorPattern.matches(name)) {
                floorNumberPattern.matchMatcher(name) {
                    group("floorNum").romanToDecimalIfNecessary().toString()
                } ?: continue
            } else continue
        }
    }

    private fun partyFinderStackTip(inventoryItems: Map<Int, ItemStack>, map: MutableMap<Int, String>) {
        inInventory = true
        for ((slot, stack) in inventoryItems) {
            val name = stack.displayName.removeColor()
            if (!checkIfPartyPattern.matches(name)) continue
            val lore = stack.getLore()
            val floor = lore.find { floorFloorPattern.matches(it.removeColor()) } ?: continue
            val dungeon = lore.find { dungeonFloorPattern.matches(it.removeColor()) } ?: continue
            val floorNum = floorNumberPattern.matchMatcher(floor) {
                group("floorNum").romanToDecimalIfNecessary()
            }
            map[slot] = getFloorName(floor, dungeon, floorNum)
        }
    }

    private fun catacombsGateStackTip(inventoryItems: Map<Int, ItemStack>, map: MutableMap<Int, String>) {
        val dungeonClassItemIndex = 45
        inInventory = true
        inventoryItems[dungeonClassItemIndex]?.getLore()?.let {
            if (it.size > 3 && detectDungeonClassPattern.matches(it[0])) {
                getDungeonClassPattern.matchMatcher(it[2].removeColor()) {
                    selectedClass = group("class")
                }
            }
        }

        if (!config.floorAsStackSize) return
        for ((slot, stack) in inventoryItems) {
            val name = stack.displayName.removeColor()
            if (!floorTypePattern.matches(name)) continue
            val floorNum = floorNumberPattern.matchMatcher(name) {
                group("floorNum").romanToDecimalIfNecessary()
            } ?: continue
            map[slot] = getFloorName(name, name, floorNum)
        }
    }

    private fun getFloorName(floor: String, dungeon: String, floorNum: Int?): String = if (entranceFloorPattern.matches(floor)) {
        "E"
    } else if (masterModeFloorPattern.matches(dungeon)) {
        "M$floorNum"
    } else {
        "F$floorNum"
    }

    private fun highlightingHandler(event: InventoryOpenEvent): Map<Int, LorenzColor> {
        val map = mutableMapOf<Int, LorenzColor>()
        if (!partyFinderTitlePattern.matches(event.inventoryName)) return map
        inInventory = true
        // TODO: Refactor this to not have so many continue statements
        @Suppress("LoopWithTooManyJumpStatements")
        for ((slot, stack) in event.inventoryItems) {
            val lore = stack.getLore()
            if (!checkIfPartyPattern.matches(stack.displayName)) continue
            if (config.markIneligibleGroups && ineligiblePattern.anyMatches(lore)) {
                map[slot] = LorenzColor.DARK_RED
                continue
            }

            if (config.markPaidCarries) {
                val note = lore.filter { notePattern.matches(it) }.joinToString(" ").uppercase()

                if (pricePattern.matches(note) && carryPattern.matches(note)) {
                    map[slot] = LorenzColor.RED
                    continue
                }
            }

            if (config.markNonPugs) {
                val note = lore.filter { notePattern.matches(it) }.joinToString(" ").uppercase()

                if (nonPugPattern.matches(note)) {
                    map[slot] = LorenzColor.LIGHT_PURPLE
                    continue
                }
            }

            val members = lore.filter { memberPattern.matches(it) }
            val memberLevels = members.map {
                memberPattern.matchMatcher(it) {
                    group("level").toInt()
                }
            }
            val memberClasses = members.map {
                memberPattern.matchMatcher(it) {
                    group("className")
                }
            }
            if (config.markBelowClassLevel != 0) {
                val hasLowLevelMembers = memberLevels.any { (it ?: Integer.MAX_VALUE) <= config.markBelowClassLevel }
                if (hasLowLevelMembers) {
                    map[slot] = LorenzColor.YELLOW
                    continue
                }
            }

            if (config.markMissingClass && memberClasses.none { it == selectedClass }) {
                map[slot] = LorenzColor.GREEN
            }
        }
        return map
    }

    private fun toolTipHandler(event: InventoryOpenEvent): Map<Int, List<String>> {
        val map = mutableMapOf<Int, List<String>>()
        val inventoryName = event.inventoryName
        if (!partyFinderTitlePattern.matches(inventoryName)) return map
        inInventory = true
        for ((slot, stack) in event.inventoryItems) {
            // TODO use enum
            val classNames = mutableListOf("Healer", "Mage", "Berserk", "Archer", "Tank")
            val toolTip = stack.getLore().toMutableList()
            for ((index, line) in stack.getLore().withIndex()) {
                classLevelPattern.matchMatcher(line) {
                    val playerName = group("playerName")
                    val className = group("className")
                    val level = group("level").toInt()
                    val color = DungeonAPI.getColor(level)
                    if (config.coloredClassLevel) toolTip[index] = " §b$playerName§f: §e$className $color$level"
                    classNames.remove(className)
                }
            }
            val name = stack.getLore().firstOrNull()?.removeColor()
            if (config.showMissingClasses && dungeonFloorPattern.matches(name)) {
                if (classNames.contains(selectedClass)) {
                    classNames[classNames.indexOf(selectedClass)] = "§a$selectedClass§7"
                }
                toolTip.add("")
                toolTip.add("§cMissing: §7" + classNames.createCommaSeparatedList())
            }
            if (toolTip.isNotEmpty()) {
                map[slot] = toolTip
            }
        }
        return map
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        val featureActive = config.let { it.coloredClassLevel || it.showMissingClasses }
        if (!featureActive) return

        val toolTip = toolTipMap[event.slot.slotNumber]
        if (toolTip.isNullOrEmpty()) return
        // TODO @Thunderblade73 fix that to "event.toolTip = toolTip"
        val oldToolTip = event.toolTip
        for ((index, line) in toolTip.withIndex()) {
            if (index >= event.toolTip.size - 1) {
                event.toolTip.add(line)
                continue
            }
            if (oldToolTip[index] != line) event.toolTip[index + 1] = line
        }
    }

    @SubscribeEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (!isEnabled()) return
        if (!config.floorAsStackSize) return
        val slot = event.slot
        if (slot.slotNumber != slot.slotIndex) return
        event.stackTip = (floorStackSize[slot.slotIndex]?.takeIf { it.isNotEmpty() } ?: return)
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        event.gui.inventorySlots.inventorySlots.associateWith { highlightParty[it.slotNumber] }.forEach { (slot, color) ->
            color?.let { slot.highlight(it) }
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        floorStackSize = emptyMap()
        highlightParty = emptyMap()
        toolTipMap = emptyMap()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "dungeon.partyFinderColoredClassLevel", "dungeon.partyFinder.coloredClassLevel")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && LorenzUtils.skyBlockArea == "Dungeon Hub"
}
