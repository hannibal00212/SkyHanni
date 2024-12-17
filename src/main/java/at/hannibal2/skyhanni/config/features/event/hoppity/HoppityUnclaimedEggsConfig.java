package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HoppityUnclaimedEggsConfig {
    @Expose
    @ConfigOption(name = "Show Unclaimed Eggs", desc = "Display which eggs haven't been found in the last SkyBlock day.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Unclaimed Eggs Order", desc = "Order in which to display unclaimed eggs.")
    @ConfigEditorDropdown
    public UnclaimedEggsOrder displayOrder = UnclaimedEggsOrder.SOONEST_FIRST;

    public enum UnclaimedEggsOrder {
        SOONEST_FIRST("Soonest First"),
        MEAL_ORDER("Meal Order"),
        ;

        private final String name;

        UnclaimedEggsOrder(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigOption(
        name = "Show Collected Locations", desc = "Show the number of found egg locations on this island.\n" +
        "§eThis is not retroactive and may not be fully synced with Hypixel's count."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showCollectedLocationCount = false;

    @Expose
    @ConfigOption(name = "Show While Busy", desc = "Show while \"busy\" (in a farming contest, doing Kuudra, in the rift, etc).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showWhileBusy = false;

    @Expose
    @ConfigOption(name = "Show Outside SkyBlock", desc = "Show on Hypixel even when not playing SkyBlock.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showOutsideSkyblock = false;

    @Expose
    @ConfigOption(name = "Warn When Unclaimed", desc = "Warn when all six eggs are ready to be found.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean warningsEnabled = false;

    @Expose
    @ConfigOption(name = "Warn While Busy", desc = "Warn while \"busy\" (in a farming contest, doing Kuudra, in the rift, etc).")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean warnWhileBusy = false;

    @Expose
    @ConfigOption(name = "Click to Warp", desc = "Make the eggs ready chat message & unclaimed timer display clickable to warp you to an island.")
    @ConfigEditorBoolean
    public boolean warpClickEnabled = false;

    @Expose
    @ConfigOption(name = "Warp Destination", desc = "A custom island to warp to in the above option.")
    @ConfigEditorText
    public String warpClickDestination = "nucleus";
}
