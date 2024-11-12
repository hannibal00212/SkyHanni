package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.utils.LorenzColor;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class CakeTrackerConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Tracks which Cakes you have/need. §cWill not fully work with NEU Storage Overlay enabled.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigLink(owner = CakeTrackerConfig.class, field = "enabled")
    public Position cakeTrackerPosition = new Position(300, 300, false, true);

    @Expose
    public CakeTrackerDisplayType displayType = CakeTrackerDisplayType.MISSING_CAKES;

    public enum CakeTrackerDisplayType {
        MISSING_CAKES,
        OWNED_CAKES,
    }

    @Expose
    public CakeTrackerDisplayOrderType displayOrderType = CakeTrackerDisplayOrderType.OLDEST_FIRST;

    public enum CakeTrackerDisplayOrderType {
        OLDEST_FIRST,
        NEWEST_FIRST,
    }

    @Expose
    @ConfigOption(
        name = "Missing Color",
        desc = "The color that should be used to highlight unobtained cakes in the Auction House."
    )
    @ConfigEditorColour
    public String unobtainedAuctionHighlightColor = LorenzColor.RED.toConfigColor();

    @Expose
    @ConfigOption(
        name = "Owned Color",
        desc = "The color that should be used to highlight obtained cakes in the Auction House."
    )
    @ConfigEditorColour
    public String obtainedAuctionHighlightColor = LorenzColor.GREEN.toConfigColor();

    @Expose
    @ConfigOption(
        name = "Maximum Rows",
        desc = "The maximum number of rows to display in the tracker, before a cutoff is imposed."
    )
    @ConfigEditorSlider(minValue = 5, maxValue = 40, minStep = 1)
    public Property<Integer> maxDisplayRows = Property.of(20);
}
