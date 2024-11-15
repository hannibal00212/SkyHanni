package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.garden.CropType;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class EliteFarmingCollectionConfig {
    @Expose
    @ConfigOption(name = "Display", desc = "Display your farming collection on screen. " +
        "The calculation and API is provided by The Elite SkyBlock farmers. " +
        "See §celitebot.dev/info §7for more info.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = true;

    @Expose
    @ConfigLink(owner = EliteFarmingCollectionConfig.class, field = "display")
    public Position pos = new Position(10, 60, false, true);

    @Expose
    @ConfigOption(name = "Show Time Until Reached", desc = "Show the time until you pass the person in front, or " +
        "time until §b#5000§7.")
    @ConfigEditorBoolean
    public boolean showTimeUntilReached = true;

    @Expose
    @ConfigOption(name = "Show Time Until Refresh", desc = "Show the time until the leaderboard updates.")
    @ConfigEditorBoolean
    public boolean showTimeUntilRefresh = false;

    @Expose
    @ConfigOption(name = "Estimate Collection", desc = "Estimates how many crops you have broken between leaderboard refreshes. " +
        "Only works in the garden.")
    @ConfigEditorBoolean
    public boolean estimateCollected = true;

    @Expose
    @ConfigOption(name = "Show Outside Garden", desc = "Show the farming collection outside of the garden.")
    @ConfigEditorBoolean
    public boolean showOutsideGarden = false;

    @Expose
    @ConfigOption(
        name = "Crop To Display",
        desc = "The crop to display on the tracker. Set to automatic to display last broken crop.")
    @ConfigEditorDropdown
    public Property<CropDisplay> crop = Property.of(CropDisplay.AUTO);

    public enum CropDisplay {
        AUTO("Automatic", null),
        WHEAT("Wheat", CropType.WHEAT),
        CARROT("Carrot", CropType.CARROT),
        POTATO("Potato", CropType.POTATO),
        NETHER_WART("Nether Wart", CropType.NETHER_WART),
        PUMPKIN("Pumpkin", CropType.PUMPKIN),
        MELON("Melon", CropType.MELON),
        COCOA_BEANS("Cocoa Beans", CropType.COCOA_BEANS),
        SUGAR_CANE("Sugar Cane", CropType.SUGAR_CANE),
        CACTUS("Cactus", CropType.CACTUS),
        MUSHROOM("Mushroom", CropType.MUSHROOM),
        ;

        private final String name;
        private final CropType crop;

        CropDisplay(String name, CropType crop) {
            this.name = name;
            this.crop = crop;
        }

        public CropType getCrop() {
            return crop;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @Expose
    @ConfigOption(name = "Show Position", desc = "Show your current position next to the collection amount if below §b#5000§7.")
    @ConfigEditorBoolean
    public boolean showPosition = true;

    @Expose
    @ConfigOption(name = "Show Person To Beat", desc = "Show the person in front of you to be passed.")
    @ConfigEditorBoolean
    public boolean showPersonToBeat = true;
}
