/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * This file was translated to Kotlin and modified, 2024.
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
package at.hannibal2.skyhanni.config.core.config

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigGuiManager.getEditorInstance
import at.hannibal2.skyhanni.test.command.ErrorManager
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import java.lang.reflect.Field

class Position @JvmOverloads constructor(
    x: Int,
    y: Int,
    scale: Float = DEFAULT_SCALE,
    centerX: Boolean = false,
    centerY: Boolean = false,
) {
    @JvmOverloads
    constructor(
        x: Int,
        y: Int,
        centerX: Boolean,
        centerY: Boolean = false,
    ) : this(x, y, DEFAULT_SCALE, centerX, centerY)

    @Expose
    var rawX: Int = x
        private set

    @Expose
    var rawY: Int = y
        private set

    @Expose
    var scale: Float = scale
        get() = if (field == 0f) DEFAULT_SCALE else field

    @Expose
    var centerX: Boolean = centerX
        private set

    // Note: currently unused?
    @Expose
    var centerY: Boolean = centerY
        private set

    @Expose
    private var ignoreCustomScale = false

    @Transient
    var linkField: Field? = null

    var clicked: Boolean = false
    var internalName: String? = null

    val effectiveScale: Float
        get() = if (ignoreCustomScale) DEFAULT_SCALE else (scale * SkyHanniMod.feature.gui.globalScale).coerceIn(MIN_SCALE, MAX_SCALE)

    fun set(other: Position) {
        this.rawX = other.rawX
        this.rawY = other.rawY
        this.centerX = other.centerX
        this.centerY = other.centerY
        this.scale = other.scale
    }

    fun setIgnoreCustomScale(ignoreCustomScale: Boolean): Position {
        this.ignoreCustomScale = ignoreCustomScale
        return this
    }

    fun moveTo(x: Int, y: Int) {
        this.rawX = x
        this.rawY = y
    }

    fun getAbsX0(objWidth: Int): Int {
        val width = ScaledResolution(Minecraft.getMinecraft()).scaledWidth

        return calcAbs0(rawX, width, objWidth)
    }

    fun getAbsY0(objHeight: Int): Int {
        val height = ScaledResolution(Minecraft.getMinecraft()).scaledHeight

        return calcAbs0(rawY, height, objHeight)
    }

    private fun calcAbs0(axis: Int, length: Int, objLength: Int): Int {
        var ret = axis
        if (axis < 0) {
            ret = length + axis - objLength
        }

        if (ret < 0) ret = 0
        if (ret > length - objLength) ret = length - objLength

        return ret
    }

    fun moveX(deltaX: Int, objWidth: Int): Int {
        val (newX, newDeltaX) = adjustWithinBounds(rawX, deltaX, ScaledResolution(Minecraft.getMinecraft()).scaledWidth, objWidth)
        this.rawX = newX
        return newDeltaX
    }

    fun moveY(deltaY: Int, objHeight: Int): Int {
        val (newY, newDeltaY) = adjustWithinBounds(rawY, deltaY, ScaledResolution(Minecraft.getMinecraft()).scaledHeight, objHeight)
        this.rawY = newY
        return newDeltaY
    }

    private fun adjustWithinBounds(axis: Int, delta: Int, length: Int, objLength: Int): Pair<Int, Int> {
        var adjustedAxis = axis + delta
        var adjustedDelta = delta

        if (adjustedAxis < 0) {
            adjustedDelta -= adjustedAxis
            adjustedAxis = 0
        } else if (adjustedAxis + objLength > length) {
            adjustedDelta += length - adjustedAxis - objLength
            adjustedAxis = length - objLength
        }

        return adjustedAxis to adjustedDelta
    }

    fun canJumpToConfigOptions(): Boolean {
        val field = linkField ?: return false
        return getEditorInstance().processedConfig.getOptionFromField(field) != null
    }

    fun jumpToConfigOptions() {
        val editor = getEditorInstance()
        val field = linkField ?: return
        val option = editor.processedConfig.getOptionFromField(field) ?: return
        editor.search("")
        if (!editor.goToOption(option)) return
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }

    fun setLink(configLink: ConfigLink) {
        try {
            linkField = configLink.owner.java.getField(configLink.field)
        } catch (e: NoSuchFieldException) {
            ErrorManager.logErrorWithData(
                FieldNotFoundException(configLink.field, configLink.owner.java),
                "Failed to set ConfigLink for ${configLink.field} in ${configLink.owner}",
                "owner" to configLink.owner,
                "field" to configLink.field,
            )
        }
    }

    companion object {
        const val DEFAULT_SCALE = 1f
        const val MIN_SCALE = 0.1f
        const val MAX_SCALE = 10.0f

        private class FieldNotFoundException(field: String, owner: Class<*>) :
            Exception("Config Link for field $field in class $owner not found")
    }
}
