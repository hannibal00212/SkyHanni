package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.utils.LorenzColor;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class CakeTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Track which Cakes you have/need. §cWill not fully work with NEU Storage Overlay.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigLink(owner = CakeTrackerConfig.class, field = "enabled")
    public Position cakeTrackerPosition = new Position(300, 300, false, true);

    @Expose
    @ConfigOption(name = "Display Mode", desc = "Which cakes the tracker should display.")
    @ConfigEditorDropdown
    public CakeTrackerDisplayType displayType = CakeTrackerDisplayType.MISSING_CAKES;

    public enum CakeTrackerDisplayType {
        MISSING_CAKES("§cMissing Cakes"),
        OWNED_CAKES("§aOwned Cakes")
        ;

        private final String name;

        CakeTrackerDisplayType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigOption(name = "Display Order", desc = "What order the tracker should display cakes in.")
    @ConfigEditorDropdown
    public CakeTrackerDisplayOrderType displayOrderType = CakeTrackerDisplayOrderType.OLDEST_FIRST;

    public enum CakeTrackerDisplayOrderType {

        OLDEST_FIRST("§cOldest Cakes First"),
        NEWEST_FIRST("§dNewest Cakes First")
        ;

        private final String name;

        CakeTrackerDisplayOrderType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Expose
    @ConfigOption(
        name = "Missing Auction Highlight Color",
        desc = "The color that should be used to highlight unobtained cakes in the auction house."
    )
    @ConfigEditorColour
    public String unobtainedAuctionHighlightColor = LorenzColor.RED.toConfigColor();

    @Expose
    @ConfigOption(
        name = "Obtained Auction Highlight Color",
        desc = "The color that should be used to highlight obtained cakes in the auction house."
    )
    @ConfigEditorColour
    public String obtainedAuctionHighlightColor = LorenzColor.GREEN.toConfigColor();

    @Expose
    @ConfigOption(
        name = "Maximum Rows to Display",
        desc = "The maximum number of rows to display in the tracker, before a cutoff is imposed."
    )
    @ConfigEditorSlider(minValue = 5, maxValue = 40, minStep = 1)
    public Property<Integer> maxDisplayRows = Property.of(20);
}
