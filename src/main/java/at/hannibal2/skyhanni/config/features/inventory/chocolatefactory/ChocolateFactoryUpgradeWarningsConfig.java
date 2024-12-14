package at.hannibal2.skyhanni.config.features.inventory.chocolatefactory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class ChocolateFactoryUpgradeWarningsConfig {
    @Expose
    @ConfigOption(name = "Upgrade Warning", desc = "Chat notification when you have a chocolate factory upgrade available to purchase.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean upgradeWarning = false;

    @Expose
    @ConfigOption(name = "Upgrade Warning Sound", desc = "Also play a sound when an upgrade is available.\n" +
        "§eUpgrade warning must be turned on.")
    @ConfigEditorBoolean
    public boolean upgradeWarningSound = false;

    @Expose
    @ConfigOption(
        name = "Upgrade Warning Interval",
        desc = "How often the warning an upgrade is available is repeated in minutes."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 0.25f)
    public float timeBetweenWarnings = 1;

    @Expose
    @ConfigOption(name = "Include Time Tower", desc = "Include Time Tower in the list of upgrades to be considered 'next best'.")
    @ConfigEditorBoolean
    public Property<Boolean> upgradeWarningTimeTower = Property.of(false);
}
