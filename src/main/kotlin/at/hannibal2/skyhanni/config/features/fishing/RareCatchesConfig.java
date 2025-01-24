package at.hannibal2.skyhanni.config.features.fishing;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class RareCatchesConfig {

    @Expose
    @ConfigOption(name = "Alert (Own Sea Creatures)", desc = "Show an alert on screen when you catch a rare sea creature.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean alertOwnCatches = true;

    @Expose
    @ConfigOption(name = "Alert (Other Sea Creatures)", desc = "Show an alert on screen when other players nearby catch a rare sea creature.")
    @ConfigEditorBoolean
    public boolean alertOtherCatches = false;

    @Expose
    @ConfigOption(name = "Creature Name", desc = "Say what creature was caught in the alert.")
    @ConfigEditorBoolean
    public boolean creatureName = true;

    @Expose
    @ConfigOption(name = "Play Sound Alert", desc = "Play a sound effect when rare sea creature alerts are displayed.")
    @ConfigEditorBoolean
    public boolean playSound = true;

    @Expose
    @ConfigOption(name = "Highlight", desc = "Highlight nearby rare sea creatures.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlight = false;

}
