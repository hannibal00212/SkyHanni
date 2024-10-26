package at.hannibal2.skyhanni.features.bingo.card

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.features.bingo.BingoAPI.getData
import at.hannibal2.skyhanni.features.bingo.card.goals.GoalType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getAllItems
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object BingoCardTips {

    private val config get() = SkyHanniMod.feature.event.bingo.bingoCard

    private val patternGroup = RepoPattern.group("bingo.card.tips")
    private val inventoryPattern by patternGroup.pattern(
        "card",
        "Bingo Card"
    )
    private val rewardPattern by patternGroup.pattern(
        "reward",
        "§.§.§7Reward"
    )
    private val contributionRewardsPattern by patternGroup.pattern(
        "reward.contribution",
        "§.§.§7Contribution Rewards.*"
    )
    private val rowNamePattern by patternGroup.pattern(
        "row.name",
        "§o§.Row #.*"
    )

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        if (!inventoryPattern.matches(InventoryUtils.openInventoryName())) return

        val slot = event.slot
        val goal = BingoAPI.bingoGoals[slot.slotNumber] ?: return

        val toolTip = event.toolTip
        // When hovering over a row
        if (rowNamePattern.matches(toolTip.firstOrNull())) return
        val bingoTip = goal.getData() ?: return

        val communityGoal = goal.type == GoalType.COMMUNITY

        val difficulty = Difficulty.valueOf(bingoTip.difficulty.uppercase())
        toolTip[0] = toolTip[0] + " §7(" + difficulty.displayName + "§7)"

        var index = if (!communityGoal) {
            toolTip.indexOfFirst { rewardPattern.matches(it) }
        } else {
            toolTip.indexOfFirst { contributionRewardsPattern.matches(it) }
        } - 1

        if (index == -2) {
            ErrorManager.logErrorWithData(
                IndexOutOfBoundsException(),
                "BingoCardTips reward line not found",
                "goal displayName" to goal.displayName,
                "slot slotNumber" to slot.slotNumber,
                "toolTip" to toolTip
            )
            return
        }

        toolTip.add(index++, "")
        toolTip.add(index++, "§eGuide:")
        for (line in bingoTip.guide) {
            toolTip.add(index++, " $line")
        }
        toolTip.add(index++, "§7Found by: §e${bingoTip.found}")
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (!inventoryPattern.matches(InventoryUtils.openInventoryName())) return

        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest
        for ((slot, _) in chest.getAllItems()) {
            val goal = BingoAPI.bingoGoals[slot.slotNumber] ?: continue
            if (config.hideDoneDifficulty && goal.done) continue

            val color = goal.getData()?.let {
                val difficulty = Difficulty.valueOf(it.difficulty.uppercase())
                difficulty.color
            } ?: LorenzColor.GRAY
            slot highlight color.addOpacity(120)
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.bingoSplashGuide

    enum class Difficulty(rawName: String, val color: LorenzColor) {
        EASY("Easy", LorenzColor.GREEN),
        MEDIUM("Medium", LorenzColor.YELLOW),
        HARD("Hard", LorenzColor.RED),
        ;

        val displayName = color.getChatColor() + rawName
    }
}
