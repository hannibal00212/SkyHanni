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

package at.hannibal2.skyhanni.config.core.config;

import at.hannibal2.skyhanni.SkyHanniMod;
import at.hannibal2.skyhanni.config.ConfigGuiManager;
import at.hannibal2.skyhanni.config.Features;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper;
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor;
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class Position {
    @Expose
    private int x;
    @Expose
    private int y;
    @Expose
    private float scale = 1F;
    @Expose
    private boolean center = false;

    @Expose
    private boolean centerX;
    @Expose
    private boolean centerY;

    @Expose
    private boolean ignoreCustomScale = false;

    public transient Field linkField;

    private boolean clicked = false;
    public String internalName = null;

    public Position() {
        this(0, 0);
    }

    public Position(int x, int y) {
        this(x, y, false, false);
    }

    public Position(int x, int y, float scale) {
        this.x = x;
        this.y = y;
        this.centerX = false;
        this.centerY = true;
        this.scale = scale;
    }

    public Position(int x, int y, float scale, boolean center) {
        this.x = x;
        this.y = y;
        this.centerX = false;
        this.centerY = true;
        this.scale = scale;
        this.center = center;
    }

    public Position(int x, int y, boolean centerX, boolean centerY) {
        this.x = x;
        this.y = y;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void set(Position other) {
        this.x = other.x;
        this.y = other.y;
        this.centerX = other.centerX;
        this.centerY = other.centerY;
        this.scale = other.getScale();
        this.center = other.isCenter();
    }

    public Position setIgnoreCustomScale(boolean ignoreCustomScale) {
        this.ignoreCustomScale = ignoreCustomScale;
        return this;
    }

    public float getEffectiveScale() {
        if (ignoreCustomScale) return 1F;
        return Math.max(Math.min(getScale() * SkyHanniMod.feature.gui.globalScale, 10F), 0.1F);
    }

    public float getScale() {
        if (scale == 0) return 1f;
        return scale;
    }

    public boolean isCenter() {
        return center;
    }

    public void setScale(float newScale) {
        scale = Math.max(Math.min(10F, newScale), 0.1f);
    }

    public int getRawX() {
        return x;
    }

    public int getRawY() {
        return y;
    }

    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setClicked(boolean state) {
        this.clicked = state;
    }

    public boolean getClicked() {
        return clicked;
    }

    public int getAbsX0(int objWidth) {
        int width = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();

        return calcAbs0(x, width, objWidth);
    }

    public int getAbsY0(int objHeight) {
        int height = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();

        return calcAbs0(y, height, objHeight);
    }

    private int calcAbs0(int axis, int length, int objLength) {
        int ret = axis;
        if (axis < 0) {
            ret = length + axis - objLength;
        }

        if (ret < 0) ret = 0;
        if (ret > length - objLength) ret = length - objLength;

        return ret;
    }

    public int moveX(int deltaX, int objWidth) {
        int screenWidth = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
        boolean wasPositiveX = this.x >= 0;
        this.x += deltaX;

        if (wasPositiveX) {
            if (this.x < 0) {
                deltaX -= this.x;
                this.x = 0;
            }
            if (this.x > screenWidth) {
                deltaX += screenWidth - this.x;
                this.x = screenWidth;
            }
        } else {
            if (this.x + 1 > 0) {
                deltaX += -1 - this.x;
                this.x = -1;
            }
            if (this.x + screenWidth < 0) {
                deltaX += -screenWidth - this.x;
                this.x = -screenWidth;
            }
        }

        if (this.x >= 0 && this.x + objWidth / 2 > screenWidth / 2) {
            this.x -= screenWidth - objWidth;
        }
        if (this.x < 0 && this.x + objWidth / 2 <= -screenWidth / 2) {
            this.x += screenWidth - objWidth;
        }
        return deltaX;
    }

    public int moveY(int deltaY, int objHeight) {
        int screenHeight = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
        boolean wasPositiveY = this.y >= 0;
        this.y += deltaY;

        if (wasPositiveY) {
            if (this.y < 0) {
                deltaY -= this.y;
                this.y = 0;
            }
            if (this.y > screenHeight) {
                deltaY += screenHeight - this.y;
                this.y = screenHeight;
            }
        } else {
            if (this.y + 1 > -0) {
                deltaY += -1 - this.y;
                this.y = -1;
            }
            if (this.y + screenHeight < 0) {
                deltaY += -screenHeight - this.y;
                this.y = -screenHeight;
            }
        }

        if (this.y >= 0 && this.y - objHeight / 2 > screenHeight / 2) {
            this.y -= screenHeight - objHeight;
        }
        if (this.y < 0 && this.y - objHeight / 2 <= -screenHeight / 2) {
            this.y += screenHeight - objHeight;
        }
        return deltaY;
    }

    public boolean canJumpToConfigOptions() {
        return linkField != null && ConfigGuiManager.INSTANCE.getEditorInstance().getProcessedConfig().getOptionFromField(linkField) != null;
    }

    public void jumpToConfigOptions() {
        MoulConfigEditor<Features> editor = ConfigGuiManager.INSTANCE.getEditorInstance();
        if (linkField == null) return;
        ProcessedOption option = editor.getProcessedConfig().getOptionFromField(linkField);
        if (option == null) return;
        editor.search("");
        if (!editor.goToOption(option)) return;
        SkyHanniMod.Companion.setScreenToOpen(new GuiScreenElementWrapper(editor));
    }

    public void setLink(@NotNull ConfigLink configLink) throws NoSuchFieldException {
        linkField = configLink.owner().getField(configLink.field());
    }
}
