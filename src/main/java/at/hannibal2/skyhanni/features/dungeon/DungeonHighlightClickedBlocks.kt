package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.ClickedBlockType
import at.hannibal2.skyhanni.events.DungeonBlockClickEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ExtendedChatColor
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.drawColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object DungeonHighlightClickedBlocks {

    private val config get() = SkyHanniMod.feature.dungeon.clickedBlocks

    private val patternGroup = RepoPattern.group("dungeons.highlightclickedblock")
    private val leverPattern by patternGroup.pattern(
        "lever",
        "§cYou hear the sound of something opening...",
    )
    private val lockedPattern by patternGroup.pattern(
        "locked",
        "§cThat chest is locked!",
    )

    private val blocks = TimeLimitedCache<LorenzVec, ClickedBlock>(3.seconds)
    private var colorIndex = 0
    private val undesirableColors = listOf(
        LorenzColor.BLACK,
        LorenzColor.WHITE,
        LorenzColor.CHROMA,
        LorenzColor.GRAY,
        LorenzColor.DARK_GRAY,
    )
    private val randomColors = LorenzColor.entries.filter { it !in undesirableColors }

    private fun getRandomColor(): LorenzColor {
        var id = colorIndex + 1
        if (id == randomColors.size) id = 0
        colorIndex = id
        return randomColors[colorIndex]
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        if (leverPattern.matches(event.message)) {
            event.blockedReason = "dungeon_highlight_clicked_block"
        }

        if (lockedPattern.matches(event.message)) {
            blocks.lastOrNull { it.value.displayText.contains("Chest") }?.value?.color = config.lockedChestColor.toChromaColor()
        }
    }

    @HandleEvent
    fun onDungeonClickedBlock(event: DungeonBlockClickEvent) {
        if (!isEnabled()) return
        val isWaterRoom = DungeonAPI.getRoomID() == "-60,-60"
        if (isWaterRoom && event.blockType == ClickedBlockType.LEVER) return

        val type = event.blockType

        val color = if (config.randomColor) getRandomColor().toColor() else getBlockProperties(type).color.toChromaColor()
        val displayText = ExtendedChatColor(color.rgb, false).toString() + "Clicked " + getBlockProperties(type).name
        blocks[event.position] = ClickedBlock(displayText, color)

    }

    private fun getBlockProperties(type: ClickedBlockType): BlockProperties {
        return when (type) {
            ClickedBlockType.LEVER -> BlockProperties("Lever", config.leverColor)
            ClickedBlockType.CHEST -> BlockProperties("Chest", config.chestColor)
            ClickedBlockType.TRAPPED_CHEST -> BlockProperties("Trapped Chest", config.trappedChestColor)
            ClickedBlockType.WITHER_ESSENCE -> BlockProperties("Wither Essence", config.witherEssenceColor)
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        blocks.forEach { (position, block) ->
            event.drawColor(position, block.color)
            if (config.showText) {
                event.drawString(position.add(0.5, 0.5, 0.5), block.displayText, true)
            }
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(56, "dungeon.highlightClickedBlocks", "dungeon.clickedBlocks.enabled")
    }

    class ClickedBlock(val displayText: String, var color: Color)
    class BlockProperties(val name: String, val color: String)

    fun isEnabled() = !DungeonAPI.inBossRoom && DungeonAPI.inDungeon() && config.enabled

}
