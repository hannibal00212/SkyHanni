package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class TooltipTweaksConfig {
    @Expose
    @ConfigOption(
        name = "Compact Descriptions",
        desc = "Hide redundant parts of reforge descriptions, generic counter description, and Farmhand perk explanation."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean compactToolTooltips = false;

    @Expose
    @ConfigOption(
        name = "Breakdown Hotkey",
        desc = "When the keybind is pressed, show a breakdown of all fortune sources on a tool."
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_LSHIFT)
    public int fortuneTooltipKeybind = Keyboard.KEY_LSHIFT;

    @Expose
    @ConfigOption(
        name = "Tooltip Format",
        desc = "Show crop-specific Farming Fortune in tooltip.\n" +
            "§fShow: §7Crop-specific Fortune indicated as §6[+196]\n" +
            "§fReplace: §7Edits the total Fortune to include crop-specific Fortune."
    )
    @ConfigEditorDropdown
    public CropTooltipFortuneEntry cropTooltipFortune = CropTooltipFortuneEntry.SHOW;

    public enum CropTooltipFortuneEntry implements HasLegacyId {
        DEFAULT("Default", 0),
        SHOW("Show", 1),
        REPLACE("Replace", 2);
        private final String displayName;
        private final int legacyId;

        CropTooltipFortuneEntry(String displayName, int legacyId) {
            this.displayName = displayName;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        CropTooltipFortuneEntry(String displayName) {
            this(displayName, -1);
        }

        @Override
        public int getLegacyId() {
            return legacyId;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(
        name = "Total Crop Milestone",
        desc = "Show the progress bar till maxed crop milestone in the crop milestone inventory."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean cropMilestoneTotalProgress = true;
}
