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
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import com.google.gson.JsonElement
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
    centerY: Boolean = true,
) {
    @JvmOverloads
    constructor(
        x: Int,
        y: Int,
        centerX: Boolean,
        centerY: Boolean = true,
    ) : this(x, y, DEFAULT_SCALE, centerX, centerY)

    constructor() : this(0, 0)

    @Expose
    var x: Int = x
        private set

    @Expose
    var y: Int = y
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
        private set

    var clicked: Boolean = false
    var internalName: String? = null
        private set

    val effectiveScale: Float
        get() = if (ignoreCustomScale) DEFAULT_SCALE else (scale * SkyHanniMod.feature.gui.globalScale).coerceIn(MIN_SCALE, MAX_SCALE)

    fun set(other: Position): Position {
        this.x = other.x
        this.y = other.y
        this.centerX = other.centerX
        this.centerY = other.centerY
        this.scale = other.scale
        return this
    }

    fun getOrSetInternalName(lazy: () -> String): String {
        return internalName ?: lazy().also { internalName = it }
    }

    fun moveTo(x: Int, y: Int): Position {
        this.x = x
        this.y = y
        return this
    }

    fun getAbsX0(objWidth: Int): Int {
        val width = GuiScreenUtils.scaledWindowWidth

        return calcAbs0(x, width, objWidth)
    }

    fun getAbsY0(objHeight: Int): Int {
        val height = GuiScreenUtils.scaledWindowHeight

        return calcAbs0(y, height, objHeight)
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
        var newDeltaX = deltaX
        val screenWidth = ScaledResolution(Minecraft.getMinecraft()).scaledWidth
        val wasPositiveX = x >= 0
        this.x += newDeltaX

        if (wasPositiveX) {
            if (x < 0) {
                newDeltaX -= x
                this.x = 0
            } else if (x > screenWidth) {
                newDeltaX += screenWidth - x
                this.x = screenWidth
            }
        } else {
            if (x + 1 > 0) {
                newDeltaX += -1 - x
                this.x = -1
            } else if (x + screenWidth < 0) {
                newDeltaX += -screenWidth - x
                this.x = -screenWidth
            }
        }

        if (x >= 0 && x + objWidth / 2 > screenWidth / 2) {
            this.x -= screenWidth - objWidth
        } else if (x < 0 && x + objWidth / 2 <= -screenWidth / 2) {
            x += screenWidth - objWidth
        }
        return newDeltaX
    }

    fun moveY(deltaY: Int, objHeight: Int): Int {
        var newDeltaY = deltaY
        val screenHeight = ScaledResolution(Minecraft.getMinecraft()).scaledHeight
        val wasPositiveY = y >= 0
        this.y += newDeltaY

        if (wasPositiveY) {
            if (y < 0) {
                newDeltaY -= y
                this.y = 0
            } else if (y > screenHeight) {
                newDeltaY += screenHeight - y
                this.y = screenHeight
            }
        } else {
            if (y + 1 > -0) {
                newDeltaY += -1 - y
                this.y = -1
            } else if (y + screenHeight < 0) {
                newDeltaY += -screenHeight - y
                this.y = -screenHeight
            }
        }

        if (y >= 0 && y - objHeight / 2 > screenHeight / 2) {
            this.y -= screenHeight - objHeight
        } else if (y < 0 && y - objHeight / 2 <= -screenHeight / 2) {
            this.y += screenHeight - objHeight
        }
        return newDeltaY
    }

    fun ignoreScale(value: Boolean = true): Position {
        this.ignoreCustomScale = value
        return this
    }

    fun canJumpToConfigOptions(): Boolean {
        val field = linkField ?: return false
        return getEditorInstance().getOptionFromField(field) != null
    }

    fun jumpToConfigOptions() {
        val editor = getEditorInstance()
        val field = linkField ?: return
        val option = editor.getOptionFromField(field) ?: return
        editor.search("")
        if (!editor.goToOption(option)) return
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }

    fun setLink(configLink: ConfigLink) {
        try {
            linkField = configLink.owner.java.getDeclaredField(configLink.field)
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

        fun migrate(element: JsonElement): JsonElement {
            val obj = element.asJsonObject
            val center = obj["center"]?.asBoolean ?: return element
            if (center) obj.addProperty("centerX", true)
            return obj
        }
    }
}
