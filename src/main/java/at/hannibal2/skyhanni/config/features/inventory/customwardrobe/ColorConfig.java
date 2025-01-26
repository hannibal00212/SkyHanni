package at.hannibal2.skyhanni.config.features.inventory.customwardrobe;

import at.hannibal2.skyhanni.features.inventory.wardrobe.CustomWardrobeReset;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ColorConfig {

    @ConfigOption(name = "Reset to Default", desc = "Reset all custom wardrobe color settings to the default.")
    @ConfigEditorButton(buttonText = "Reset")
    public Runnable resetColor = CustomWardrobeReset::resetColor;

    @Expose
    @ConfigOption(name = "Background", desc = "Color of the GUI background.")
    @ConfigEditorColour
    public String backgroundColor = "0:127:0:0:0";

    @Expose
    @ConfigOption(name = "Equipped", desc = "Color of the currently equipped wardrobe slot.")
    @ConfigEditorColour
    public String equippedColor = "0:127:85:255:85";

    @Expose
    @ConfigOption(name = "Favorite", desc = "Color of the wardrobe slots that have been added as favorites.")
    @ConfigEditorColour
    public String favoriteColor = "0:127:255:85:85";

    @Expose
    @ConfigOption(name = "Same Page", desc = "Color of wardrobe slots in the same page.")
    @ConfigEditorColour
    public String samePageColor = "0:127:94:108:255";

    @Expose
    @ConfigOption(name = "Other Page", desc = "Color of wardrobe slots in another page.")
    @ConfigEditorColour
    public String otherPageColor = "0:127:0:0:0";

    @Expose
    @ConfigOption(name = "Top Outline", desc = "Color of the top of the outline when hovered.")
    @ConfigEditorColour
    public String topBorderColor = "0:255:255:200:0";

    @Expose
    @ConfigOption(name = "Bottom Outline", desc = "Color of the bottom of the outline when hovered.")
    @ConfigEditorColour
    public String bottomBorderColor = "0:255:255:0:0";

}
