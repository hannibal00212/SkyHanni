package at.hannibal2.skyhanni.config.features.combat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class QuiverConfig {
    @Expose
    @ConfigOption(name = "Quiver Display", desc = "")
    @Accordion
    public QuiverDisplayConfig quiverDisplay = new QuiverDisplayConfig();

    @Expose
    @ConfigOption(
        name = "Low Quiver Alert",
        desc = "Notifies you when your quiver reaches a set amount of arrows."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean lowQuiverNotification = true;

    @Expose
    @ConfigOption(
        name = "Reminder After Run",
        desc = "Reminds you to buy arrows after a Dungeons/Kuudra run if you're low."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean reminderAfterRun = true;

    @Expose
    @ConfigOption(name = "Low Quiver Amount", desc = "Amount at which to notify you.")
    @ConfigEditorSlider(minValue = 50, maxValue = 500, minStep = 50)
    public int lowQuiverAmount = 100;
}
