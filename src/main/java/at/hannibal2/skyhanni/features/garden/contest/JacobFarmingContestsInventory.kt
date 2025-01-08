package at.hannibal2.skyhanni.features.garden.contest

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderItemEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.garden.GardenNextJacobContest
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.text.SimpleDateFormat
import java.util.Locale

@SkyHanniModule
object JacobFarmingContestsInventory {

    private val realTime = mutableMapOf<Int, String>()

    private val formatDay = SimpleDateFormat("dd MMMM yyyy", Locale.US)
    private val formatTime = SimpleDateFormat("HH:mm", Locale.US)
    private val config get() = SkyHanniMod.feature.inventory.jacobFarmingContests

    // Render the contests a tick delayed to feel smoother
    private var hideEverything = true
    private val medalPattern by RepoPattern.pattern(
        "garden.jacob.contests.inventory.medal",
        "§7§7You placed in the (?<medal>.*) §7bracket!"
    )

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        realTime.clear()
        hideEverything = true
    }

    @HandleEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (event.inventoryName != "Your Contests") return

        realTime.clear()

        val foundEvents = mutableListOf<String>()
        for ((slot, item) in event.inventoryItems) {
            if (!item.getLore().any { it.startsWith("§7Your score: §e") }) continue

            foundEvents.add(item.name)
            val time = FarmingContestAPI.getSbTimeFor(item.name) ?: continue
            FarmingContestAPI.addContest(time, item)
            if (config.realTime) {
                readRealTime(time, slot)
            }
        }
        hideEverything = false
    }

    private fun readRealTime(time: Long, slot: Int) {
        val dayFormat = formatDay.format(time)
        val startTimeFormat = formatTime.format(time)
        val endTimeFormat = formatTime.format(time + 1000 * 60 * 20)
        realTime[slot] = "$dayFormat $startTimeFormat-$endTimeFormat"
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        // TODO add tooltip line "click + press <keybind> to open on elite website
        if (!config.openOnElite.isKeyHeld()) return
        if (!LorenzUtils.inSkyBlock) return

        val slot = event.slot ?: return
        val itemName = slot.stack.name

        when (val chestName = InventoryUtils.openInventoryName()) {
            "Your Contests" -> {
                val (year, month, day) = FarmingContestAPI.getSbDateFromItemName(itemName) ?: return
                openContest(year, month, day)
                event.cancel()
            }

            "Jacob's Farming Contests" -> {
                openFromJacobMenu(itemName)
                event.cancel()
            }

            else -> {
                openFromCalendar(chestName, itemName, event, slot)
            }
        }
    }

    private fun openContest(year: String, month: String, day: String) {
        val date = "$year/${LorenzUtils.getSBMonthByName(month)}/$day"
        OSUtils.openBrowser("https://elitebot.dev/contests/$date")
        ChatUtils.chat("Opening contest in elitebot.dev")
    }

    private fun openFromJacobMenu(itemName: String) {
        when (itemName) {
            "§6Upcoming Contests" -> {
                OSUtils.openBrowser("https://elitebot.dev/contests/upcoming")
                ChatUtils.chat("Opening upcoming contests in elitebot.dev")
            }

            "§bClaim your rewards!" -> {
                OSUtils.openBrowser("https://elitebot.dev/@${LorenzUtils.getPlayerName()}/${HypixelData.profileName}/contests")
                ChatUtils.chat("Opening your contests in elitebot.dev")
            }

            "§aWhat is this?" -> {
                OSUtils.openBrowser("https://elitebot.dev/contests")
                ChatUtils.chat("Opening contest page in elitebot.dev")
            }

            else -> return
        }
    }

    private fun openFromCalendar(
        chestName: String,
        itemName: String,
        event: GuiContainerEvent.SlotClickEvent,
        slot: Slot,
    ) {
        GardenNextJacobContest.monthPattern.matchMatcher(chestName) {
            if (!slot.stack.getLore().any { it.contains("§eJacob's Farming Contest") }) return

            val day = GardenNextJacobContest.dayPattern.matchMatcher(itemName) { group("day") } ?: return
            val year = group("year")
            val month = group("month")
            val time = SkyBlockTime(year.toInt(), LorenzUtils.getSBMonthByName(month), day.toInt()).toMillis()
            if (time < SkyBlockTime.now().toMillis()) {
                openContest(year, month, day)
            } else {
                val timestamp = time / 1000
                OSUtils.openBrowser("https://elitebot.dev/contests/upcoming#$timestamp")
                ChatUtils.chat("Opening upcoming contests in elitebot.dev")
            }
            event.cancel()
        }
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!InventoryUtils.openInventoryName().contains("Your Contests")) return
        if (!config.highlightRewards) return

        // hide green border for a tick
        if (hideEverything) return

        if (event.gui !is GuiChest) return
        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest

        for ((slot, stack) in chest.getUpperItems()) {
            if (stack.getLore().any { it == "§eClick to claim reward!" }) {
                slot highlight LorenzColor.GREEN
            }
        }
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!InventoryUtils.openInventoryName().contains("Your Contests")) return

        val slot = event.slot.slotNumber
        if (config.realTime) {
            realTime[slot]?.let {
                val toolTip = event.toolTip
                if (toolTip.size > 1) {
                    toolTip.add(1, it)
                }
            }
        }
    }

    @HandleEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.medalIcon) return
        if (!InventoryUtils.openInventoryName().contains("Your Contests")) return

        val stack = event.stack ?: return
        var finneganContest = false

        for (line in stack.getLore()) {
            if (line.contains("Contest boosted by Finnegan!")) finneganContest = true

            val name = medalPattern.matchMatcher(line) { group("medal").removeColor() } ?: continue
            val medal = LorenzUtils.enumValueOfOrNull<ContestBracket>(name) ?: return

            var stackTip = "§${medal.color}✦"
            var x = event.x + 9
            var y = event.y + 1
            var scale = .7f

            if (finneganContest && config.finneganIcon) {
                stackTip = "§${medal.color}▲"
                x = event.x + 5
                y = event.y - 2
                scale = 1.3f
            }

            event.drawSlotText(x, y, stackTip, scale)
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(
            3,
            "inventory.jacobFarmingContestHighlightRewards",
            "inventory.jacobFarmingContests.highlightRewards"
        )
        event.move(3, "inventory.jacobFarmingContestHideDuplicates", "inventory.jacobFarmingContests.hideDuplicates")
        event.move(3, "inventory.jacobFarmingContestRealTime", "inventory.jacobFarmingContests.realTime")
        event.move(3, "inventory.jacobFarmingContestFinneganIcon", "inventory.jacobFarmingContests.finneganIcon")
        event.move(3, "inventory.jacobFarmingContestMedalIcon", "inventory.jacobFarmingContests.medalIcon")
    }
}
