package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

@SkyHanniModule
object DungeonSpiritLeapOverlay {
    private val config get() = SkyHanniMod.feature.dungeon.spiritLeapOverlay

    private var scaleFactor: Double = 1.0
    private var overlayPosition: Position? = null
    private var containerWidth = 0
    private var containerHeight = 0

    data class PlayerStackInfo(val playerInfo: DungeonAPI.TeamMember?, val stack: ItemStack, val slotNumber: Int)

    @SubscribeEvent
    fun onSpiritLeapGuiDraw(event: GuiContainerEvent.PreDraw) {
        if (!isEnabled()) return

        val gui = event.gui
        if (gui !is GuiChest || InventoryUtils.openInventoryName().removeColor() != "Spirit Leap") return
        containerWidth = gui.width
        containerHeight = gui.height
        scaleFactor = min(containerWidth, containerHeight).toDouble() / max(containerWidth, containerHeight).toDouble()

        val chest = gui.inventorySlots as ContainerChest

        val playerList = buildList {
            for ((slot, stack) in chest.getUpperItems()) {
                val lore = stack.getLore()
                if (lore.isNotEmpty()) {
                    val playerInfo = DungeonAPI.getPlayerInfo(stack.displayName)
                    add(PlayerStackInfo(playerInfo, stack, slot.slotNumber))
                }
            }
        }.sortedBy { it.playerInfo?.dungeonClass?.ordinal }

        val leapRenderItems = playerList.mapNotNull { createLeapItem(it) }
        val spiritLeapOverlay = createSpiritLeapOverlay(leapRenderItems)

        overlayPosition = Position(
            (gui.width - spiritLeapOverlay.width) / 2,
            (gui.height - spiritLeapOverlay.height) / 2,
            false, true
        ).apply {
            renderRenderable(spiritLeapOverlay, posLabel = "Spirit Leap Overlay", addToGuiManager = false)
        }
        event.cancel()
    }

    private fun createSpiritLeapOverlay(leapRenderItems: List<Renderable>): Renderable {
        val layout = leapRenderItems.take(4).chunked(2)
        return if (layout.isNotEmpty()) {
            Renderable.table(
                layout,
                xPadding = 18,
                yPadding = 18,
                horizontalAlign = HorizontalAlignment.CENTER,
                verticalAlign = VerticalAlignment.CENTER
            )
        } else {
            Renderable.wrappedString(
                width = (containerWidth * 0.8).toInt(),
                text = "No targets available for leap.",
                scale = scaleFactor * 3
            )
        }
    }

    private fun createLeapItem(playerStackInfo: PlayerStackInfo): Renderable? {
        val player = playerStackInfo.playerInfo ?: return null
        val classInfo = buildString {
            player.dungeonClass?.let {
                append(it.scoreboardName)
                if (config.showDungeonClassLevel) append(" ${player.classLevel}")
                if (player.playerDead) append(" (Dead)")
            }
        }

        val backgroundColor = if (player.playerDead) deadTeammateColor else getClassColor(player.dungeonClass)
        val itemRenderable = Renderable.drawInsideRoundedRect(
            Renderable.itemStack(playerStackInfo.stack, scale = scaleFactor * 0.9 + 2.7),
            color = Color(255, 255, 255, 100),
            radius = 5
        )

        val playerInfoRenderable = Renderable.verticalContainer(
            listOf(
                Renderable.wrappedString(
                    player.username,
                    width = (containerWidth * 0.25).toInt(),
                    scale = scaleFactor + 1.5),
                Renderable.placeholder(0, (containerHeight * 0.03).toInt()),
                Renderable.wrappedString(
                    classInfo,
                    width = (containerWidth * 0.25).toInt(),
                    scale = (scaleFactor * 0.9) + 1.1
                )
            ),
            horizontalAlign = HorizontalAlignment.CENTER,
            verticalAlign = VerticalAlignment.CENTER
        )

        val buttonLayout = Renderable.horizontalContainer(
            listOf(
                Renderable.placeholder((containerWidth * 0.01).toInt(), 0),
                itemRenderable,
                Renderable.placeholder((containerWidth * 0.01).toInt(), 0),
                playerInfoRenderable
            ),
            verticalAlign = VerticalAlignment.CENTER
        )

        return Renderable.clickable(
            Renderable.drawInsideRoundedRect(
                Renderable.fixedSizeColumn(
                    Renderable.fixedSizeLine(
                        buttonLayout,
                        width = (containerWidth * 0.40).toInt(),
                        verticalAlign = VerticalAlignment.CENTER
                    ),
                    height = (containerHeight * 0.35).toInt(),
                ),
                verticalAlign = VerticalAlignment.CENTER,
                color = backgroundColor.toChromaColor(),
                radius = 7,
                smoothness = 10,
                padding = 5
            ),
            onClick = { InventoryUtils.clickSlot(playerStackInfo.slotNumber) }
        )
    }

    private val deadTeammateColor = config.deadTeammateColor

    private fun getClassColor(dungeonClass: DungeonAPI.DungeonClass?): String {
        return when (dungeonClass) {
            DungeonAPI.DungeonClass.ARCHER -> config.archerClassColor
            DungeonAPI.DungeonClass.MAGE -> config.mageClassColor
            DungeonAPI.DungeonClass.BERSERK -> config.berserkClassColor
            DungeonAPI.DungeonClass.TANK -> config.tankClassColor
            DungeonAPI.DungeonClass.HEALER -> config.healerClassColor
            else -> config.defaultColor
        }
    }

    private fun isEnabled() =
        config.enabled &&
        DungeonAPI.inDungeon() &&
        DungeonAPI.started &&
        !DungeonAPI.completed
}
