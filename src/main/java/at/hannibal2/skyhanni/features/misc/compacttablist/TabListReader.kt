package at.hannibal2.skyhanni.features.misc.compacttablist

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.removeSFormattingCode
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpaceAndResets
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

// heavily inspired by SBA code
@SkyHanniModule
object TabListReader {

    private val config get() = SkyHanniMod.feature.gui.compactTabList

    private val patternGroup = RepoPattern.group("misc.compacttablist")

    /**
     * REGEX-TEST: [164] CalMWolfs ᛝ♲
     * REGEX-TEST: [328] vayness ☠
     */
    val usernamePattern by patternGroup.pattern(
        "username",
        "^\\[(?<sblevel>\\d+)] (?:\\[\\w+] )?(?<username>\\w+)"
    )
    /**
     * REGEX-TEST: §r§r§7You have a §r§cGod Potion §r§7active! §r§d12 Hours§r
     */
    private val godPotPattern by patternGroup.pattern(
        "effects.godpot",
        "§r§r§7You have a §r§cGod Potion §r§7active! §r§d(?<timer>[\\w ]+)§r"
    )
    /**
     * REGEX-TEST: §r§r§a§lActive Effects§r
     */
    private val activeEffectPattern by patternGroup.pattern(
        "effects.active",
        "Active Effects(?:§.)*(?:\\n(?:§.)*§7.+)*"
    )
    /**
     * REGEX-TEST: §r§r§7§r§7You have §r§e1 §r§7active effect. Use "§r§6/effects§r§7" to see it!§r
     */
    private val effectCountPattern by patternGroup.pattern(
        "effects.count",
        "You have (?:§.)*(?<effectCount>[0-9]+) (?:§.)*active effect"
    )

    /**
     * REGEX-TEST: §r§r§d§lCookie Buff§r§r§r§7§r§7Not active! Obtain booster cookies from the community§r
     */
    private val cookiePattern by patternGroup.pattern(
        "cookie",
        "Cookie Buff(?:§.)*(?:\\n(§.)*§7.+)*"
    )

    /**
     * REGEX-TEST: §r§r§6§lDungeon Buffs§r§r§r§7No Buffs active. Find them by exploring the Dungeon!§r
     */
    private val dungeonBuffPattern by patternGroup.pattern(
        "dungeonbuff",
        "Dungeon Buffs(?:§.)*(?:\\n(§.)*§7.+)*"
    )
    private val upgradesPattern by patternGroup.pattern(
        "upgrades",
        "(?<firstPart>§e[A-Za-z ]+)(?<secondPart> §f[\\w ]+)"
    )
    private val winterPowerUpsPattern by patternGroup.pattern(
        "winterpowerups",
        "Active Power Ups(?:§.)*(?:\\n(§.)*§7.+)*"
    )

    var hypixelAdvertisingString = "HYPIXEL.NET"

    val renderColumns = mutableListOf<RenderColumn>()

    private fun updateTablistData(tablist: List<String>? = null) {
        if (!LorenzUtils.inSkyBlock) return

        var tabLines = tablist ?: TabListData.getTabList()

        if (tabLines.size < 80) return

        tabLines = tabLines.subList(0, 80)

        val columns = parseColumns(tabLines)
        val footerColumn = parseFooterAsColumn()

        if (footerColumn != null) {
            columns.add(footerColumn)
        }

        parseSections(columns)

        renderColumns.clear()
        val renderColumn = RenderColumn()
        renderColumns.add(renderColumn)
        combineColumnsToRender(columns, renderColumn)
    }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        updateTablistData(event.tabList)
    }

    private fun parseColumns(original: List<String>): MutableList<TabColumn> {
        val columns = mutableListOf<TabColumn>()
        val fullTabList = AdvancedPlayerList.newSorting(original)

        for (entry in fullTabList.indices step 20) {
            val title = fullTabList[entry].trimWhiteSpaceAndResets()
            var column = getColumnFromName(columns, title)

            if (column == null) {
                column = TabColumn(title)
                columns.add(column)
            }

            for (columnEntry in (entry + 1) until fullTabList.size.coerceAtMost(entry + 20)) {
                column.addLine(fullTabList[columnEntry])
            }
        }
        return columns
    }

    private fun parseFooterAsColumn(): TabColumn? {
        var footer = TabListData.getFooter().removeSFormattingCode()
        if (footer.isEmpty()) return null

        footer = godPotPattern.findMatcher(footer) {
            activeEffectPattern.matcher(footer)
                .replaceAll("Active Effects:\n§cGod Potion§r: ${group("timer")}")
        } ?: run {
            effectCountPattern.findMatcher(footer) {
                activeEffectPattern.matcher(footer).replaceAll("Active Effects: §r§e" + group("effectCount"))
            } ?: activeEffectPattern.matcher(footer).replaceAll("Active Effects: §r§e0")
        }

        cookiePattern.findMatcher(footer) {
            if (group().contains("Not active!")) {
                footer = this.replaceAll("Cookie Buff \n§r§7Not Active")
            }
        }

        dungeonBuffPattern.findMatcher(footer) {
            if (group().contains("No Buffs active.")) {
                footer = this.replaceAll("Dungeon Buffs \n§r§7None Found")
            }
        }

        winterPowerUpsPattern.findMatcher(footer) {
            if (group().contains("No Power Ups active.")) {
                footer = this.replaceAll("Active Power Ups \n§r§7None")
            }
        }

        val column = TabColumn("§2§lOther")

        for (line in footer.split("\n")) {
            if (line.contains(hypixelAdvertisingString)) continue

            var newLine = line

            upgradesPattern.matchMatcher(newLine.removeResets()) {
                var firstPart = group("firstPart").trimWhiteSpaceAndResets()
                if (!firstPart.contains("§l")) {
                    firstPart = " $firstPart"
                }
                column.addLine(firstPart)

                newLine = group("secondPart")
            }

            newLine = newLine.trimWhiteSpaceAndResets()
            if (!newLine.contains("§l")) {
                newLine = " $newLine"
            }
            column.addLine(newLine)
        }

        return column
    }

    private fun getColumnFromName(columns: List<TabColumn>, name: String): TabColumn? {
        for (tabColumn in columns) {
            if (name == tabColumn.columnTitle) {
                return tabColumn
            }
        }
        return null
    }

    private fun parseSections(columns: MutableList<TabColumn>) {
        for (column in columns) {
            var currentTabSection: TabSection? = null
            for (line in column.lines) {
                if (line.trimWhiteSpaceAndResets().isEmpty()) {
                    currentTabSection = null
                    continue
                }

                if (currentTabSection == null) {
                    column.addSection(TabSection(column).also { currentTabSection = it })
                }

                currentTabSection?.addLine(line)
            }
        }
    }

    private fun combineColumnsToRender(columns: MutableList<TabColumn>, firstColumn: RenderColumn) {
        var firstColumnCopy = firstColumn
        var lastTitle: String? = null

        for (section in columns.flatMap { it.sections }) {
            var sectionSize = section.size()

            var needsTitle = false
            if (lastTitle != section.columnValue.columnTitle) {
                needsTitle = true
                sectionSize++
            }

            var currentCount = firstColumnCopy.size()

            if (sectionSize >= TabListRenderer.MAX_LINES / 2) {
                if (currentCount >= TabListRenderer.MAX_LINES) {
                    renderColumns.add(RenderColumn().also { firstColumnCopy = it })
                    currentCount = 1
                } else {
                    if (firstColumnCopy.size() > 0) {
                        firstColumnCopy.addLine(AdvancedPlayerList.createTabLine("", TabStringType.TEXT))
                    }
                }

                if (needsTitle) {
                    lastTitle = section.columnValue.columnTitle
                    firstColumnCopy.addLine(AdvancedPlayerList.createTabLine(lastTitle, TabStringType.TITLE))
                    currentCount++
                }

                for (line in section.lines) {
                    if (currentCount >= TabListRenderer.MAX_LINES) {
                        renderColumns.add(RenderColumn().also { firstColumnCopy = it })
                        currentCount = 1
                    }

                    firstColumnCopy.addLine(AdvancedPlayerList.createTabLine(line, TabStringType.fromLine(line)))
                    currentCount++
                }
            } else {
                if (currentCount + sectionSize > TabListRenderer.MAX_LINES) {
                    renderColumns.add(RenderColumn().also { firstColumnCopy = it })
                } else {
                    if (firstColumnCopy.size() > 0) {
                        firstColumnCopy.addLine(AdvancedPlayerList.createTabLine("", TabStringType.TEXT))
                    }
                }

                if (needsTitle) {
                    lastTitle = section.columnValue.columnTitle
                    firstColumnCopy.addLine(AdvancedPlayerList.createTabLine(lastTitle, TabStringType.TITLE))
                }

                for (line in section.lines) {
                    firstColumnCopy.addLine(AdvancedPlayerList.createTabLine(line, TabStringType.fromLine(line)))
                }
            }
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.enabled) {
            updateTablistData()
        }
    }
}
