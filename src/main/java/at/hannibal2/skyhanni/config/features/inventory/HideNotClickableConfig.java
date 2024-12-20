package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HideNotClickableConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Hide items that are not clickable in the current inventory: ah, bz, accessory bag, etc.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean items = false;

    @Expose
    @ConfigOption(name = "Block Clicks", desc = "Block the clicks on these items.")
    @ConfigEditorBoolean
    public boolean itemsBlockClicks = true;

    @Expose
    @ConfigOption(
        name = "Opacity",
        desc = "How strong should the items be grayed out?"
    )
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 255,
        minStep = 5
    )
    public int opacity = 180;
    @Expose
    @ConfigOption(name = "Bypass With Key", desc = "Add the ability to bypass not clickable items when holding the control/command key.")
    @ConfigEditorBoolean
    public boolean itemsBypass = true;

    @Expose
    @ConfigOption(name = "Green Line", desc = "Add green line around items that are clickable.")
    @ConfigEditorBoolean
    public boolean itemsGreenLine = true;

}
