/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */
package at.hannibal2.skyhanni.config.core.config.gui

import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.GuiEditManager
import at.hannibal2.skyhanni.data.GuiEditManager.getAbsX
import at.hannibal2.skyhanni.data.GuiEditManager.getAbsY
import at.hannibal2.skyhanni.data.GuiEditManager.getDummySize
import at.hannibal2.skyhanni.data.OtherInventoryData
import at.hannibal2.skyhanni.mixins.transformers.gui.AccessorGuiContainer
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import at.hannibal2.skyhanni.utils.compat.SkyhanniBaseScreen
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.io.IOException

class GuiPositionEditor(
    private val positions: List<Position>,
    private val border: Int,
    private val oldScreen: GuiContainer? = null,
) : SkyhanniBaseScreen() {

    private var grabbedX = 0
    private var grabbedY = 0
    private var clickedPos = -1

    override fun onGuiClosed() {
        super.onGuiClosed()
        clickedPos = -1
        for (position in positions) {
            position.clicked = false
        }
        OtherInventoryData.close("ShPositionEditor")
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Items aren't drawn due to a bug in neu rendering
        drawDefaultBackground()
        if (oldScreen != null) {
            val accessor = oldScreen as AccessorGuiContainer
            accessor.invokeDrawGuiContainerBackgroundLayer_skyhanni(partialTicks, -1, -1)
        }

        super.drawScreen(mouseX, mouseY, partialTicks)

        GlStateManager.disableLighting()
        val hoveredPos = renderRectangles()

        renderLabels(hoveredPos)
    }

    private fun renderLabels(hoveredPos: Int) {
        GuiRenderUtils.drawStringCentered("§cSkyHanni Position Editor", getScaledWidth() / 2, 8)

        var displayPos = -1
        if (clickedPos != -1 && positions[clickedPos].clicked) {
            displayPos = clickedPos
        }
        if (displayPos == -1) {
            displayPos = hoveredPos
        }

        // When the mouse isn't currently hovering over a gui element
        if (displayPos == -1) {
            GuiRenderUtils.drawStringCentered(
                "§eTo edit hidden GUI elements set a key in /sh edit",
                getScaledWidth() / 2,
                20,
            )
            GuiRenderUtils.drawStringCentered(
                "§ethen click that key while the GUI element is visible",
                getScaledWidth() / 2,
                32,
            )
            return
        }

        val pos = positions[displayPos]
        val location = "§7x: §e${pos.rawX}§7, y: §e${pos.rawY}§7, scale: §e${pos.scale.roundTo(2)}"
        GuiRenderUtils.drawStringCentered("§b" + pos.internalName, getScaledWidth() / 2, 18)
        GuiRenderUtils.drawStringCentered(location, getScaledWidth() / 2, 28)
        if (pos.canJumpToConfigOptions())
            GuiRenderUtils.drawStringCentered(
                "§aRight-Click to open associated config options",
                getScaledWidth() / 2,
                38,
            )
    }

    private fun renderRectangles(): Int {
        var hoveredPos = -1
        GlStateManager.pushMatrix()
        width = getScaledWidth()
        height = getScaledHeight()

        val (mouseX, mouseY) = GuiScreenUtils.mousePos

        for ((index, position) in positions.withIndex()) {
            var elementWidth = position.getDummySize(true).x
            var elementHeight = position.getDummySize(true).y
            if (position.clicked) {
                grabbedX += position.moveX(mouseX - grabbedX, elementWidth)
                grabbedY += position.moveY(mouseY - grabbedY, elementHeight)
            }
            val x = position.getAbsX()
            val y = position.getAbsY()

            elementWidth = position.getDummySize().x
            elementHeight = position.getDummySize().y
            drawRect(x - border, y - border, x + elementWidth + border * 2, y + elementHeight + border * 2, -0x7fbfbfc0)

            if (GuiRenderUtils.isPointInRect(
                    mouseX,
                    mouseY,
                    x - border,
                    y - border,
                    elementWidth + border * 2,
                    elementHeight + border * 2,
                )
            ) {
                hoveredPos = index
            }
        }
        GlStateManager.popMatrix()
        return hoveredPos
    }

    private fun getScaledHeight() = GuiScreenUtils.scaledWindowHeight
    private fun getScaledWidth() = GuiScreenUtils.scaledWindowWidth

    @Throws(IOException::class)
    override fun mouseClicked(originalX: Int, priginalY: Int, mouseButton: Int) {
        super.mouseClicked(originalX, priginalY, mouseButton)

        val (mouseX, mouseY) = GuiScreenUtils.mousePos

        for (i in positions.indices.reversed()) {
            val position = positions[i]
            val elementWidth = position.getDummySize().x
            val elementHeight = position.getDummySize().y
            val x = position.getAbsX()
            val y = position.getAbsY()
            val isHovered = GuiRenderUtils.isPointInRect(
                mouseX,
                mouseY,
                x - border,
                y - border,
                elementWidth + border * 2,
                elementHeight + border * 2,
            )
            if (!isHovered) continue
            if (mouseButton == 1) {
                position.jumpToConfigOptions()
                break
            }
            if (!position.clicked && mouseButton == 0) {
                clickedPos = i
                position.clicked = true
                grabbedX = mouseX
                grabbedY = mouseY
                break
            }
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)

        if (clickedPos == -1) return
        val position = positions[clickedPos]
        if (position.clicked) return

        val dist = if (KeyboardManager.isShiftKeyDown()) 10 else 1
        val elementWidth = position.getDummySize(true).x
        val elementHeight = position.getDummySize(true).y
        when (keyCode) {
            Keyboard.KEY_DOWN -> position.moveY(dist, elementHeight)
            Keyboard.KEY_UP -> position.moveY(-dist, elementHeight)
            Keyboard.KEY_LEFT -> position.moveX(-dist, elementWidth)
            Keyboard.KEY_RIGHT -> position.moveX(dist, elementWidth)
            Keyboard.KEY_MINUS -> position.scale -= .1F
            Keyboard.KEY_EQUALS -> position.scale += .1F
            Keyboard.KEY_SUBTRACT -> position.scale -= .1F
            Keyboard.KEY_ADD -> position.scale += .1F
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)

        for (position in positions) {
            position.clicked = false
        }
    }

    override fun mouseClickMove(originalX: Int, priginalY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        super.mouseClickMove(originalX, priginalY, clickedMouseButton, timeSinceLastClick)

        for (position in positions) {
            if (!position.clicked) continue

            val (mouseX, mouseY) = GuiScreenUtils.mousePos

            val elementWidth = position.getDummySize(true).x
            val elementHeight = position.getDummySize(true).y
            grabbedX += position.moveX(mouseX - grabbedX, elementWidth)
            grabbedY += position.moveY(mouseY - grabbedY, elementHeight)
            GuiEditManager.handleGuiPositionMoved(position.internalName)
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val mw = Mouse.getEventDWheel()
        if (mw == 0) return

        val (mouseX, mouseY) = GuiScreenUtils.mousePos

        val hovered = positions.firstOrNull { it.clicked }
            ?: positions.lastOrNull {
                val size = it.getDummySize()
                GuiRenderUtils.isPointInRect(
                    mouseX, mouseY,
                    it.getAbsX() - border, it.getAbsY() - border,
                    size.x + border * 2, size.y + border * 2,
                )
            } ?: return
        if (mw < 0)
            hovered.scale -= .1F
        else
            hovered.scale += .1F
    }
}
