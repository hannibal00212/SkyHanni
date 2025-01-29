package at.hannibal2.skyhanni.config.features.dungeon;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HighlightClickedBlocksConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Highlight levers, chests, and wither essence when clicked in Dungeons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Chest Color", desc = "Color of clicked chests.")
    @ConfigEditorColour
    public String chestColor = "0:178:85:255:85";

    @Expose
    @ConfigOption(name = "Trapped Chest Color", desc = "Color of clicked trapped chests.")
    @ConfigEditorColour
    public String trappedChestColor = "0:178:0:170:0";

    @Expose
    @ConfigOption(name = "Locked Chest Color", desc = "Color of clicked locked chests.")
    @ConfigEditorColour
    public String lockedChestColor = "0:178:255:85:85";

    @Expose
    @ConfigOption(name = "Wither Essence Color", desc = "Color of clicked wither essence.")
    @ConfigEditorColour
    public String witherEssenceColor = "0:178:255:85:255";

    @Expose
    @ConfigOption(name = "Lever Color", desc = "Color of clicked levers.")
    @ConfigEditorColour
    public String leverColor = "0:178:255:255:85";

    @Expose
    @ConfigOption(name = "Show Text", desc = "Shows a text saying what you clicked with the highlight.")
    @ConfigEditorBoolean
    public boolean showText = true;

    @Expose
    @ConfigOption(name = "Random Color", desc = "If enabled makes the colors random.")
    @ConfigEditorBoolean
    public boolean randomColor = false;

    @ConfigOption(name = "Reset Colors", desc = "Resets the colors of the highlights to default ones.")
    @ConfigEditorButton(buttonText = "Reset")
    public Runnable reset = () -> {
        chestColor = "0:178:85:255:85";
        trappedChestColor = "0:178:0:170:0";
        lockedChestColor = "0:178:255:85:85";
        witherEssenceColor = "0:178:255:85:255";
        leverColor = "0:178:255:255:85";
    };
}
