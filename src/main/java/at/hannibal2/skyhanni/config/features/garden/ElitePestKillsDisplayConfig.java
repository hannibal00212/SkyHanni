package at.hannibal2.skyhanni.config.features.garden;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.garden.pests.PestType;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class ElitePestKillsDisplayConfig {
    @Expose
    @ConfigOption(name = "Display", desc = "Display your pest kills on screen. " +
        "The calculation and API is provided by The Elite SkyBlock farmers. " +
        "See §celitebot.dev/info §7for more info.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = true;

    @Expose
    @ConfigLink(owner = ElitePestKillsDisplayConfig.class, field = "display")
    public Position pos = new Position(10, 110, false, true);

    @Expose
    @ConfigOption(name = "Show Time Until Refresh", desc = "Show the time until the leaderboard updates.")
    @ConfigEditorBoolean
    public boolean showTimeUntilRefresh = false;

    @Expose
    @ConfigOption(name = "Show Outside Garden", desc = "Show the pest kill display outside of the garden.")
    @ConfigEditorBoolean
    public boolean showOutsideGarden = false;

    @Expose
    @ConfigOption(
        name = "Pest To Display",
        desc = "The pest to display on the tracker. Set to automatic to display last killed pest.")
    @ConfigEditorDropdown
    public Property<PestDisplay> pest = Property.of(PestDisplay.AUTO);

    public enum PestDisplay {
        AUTO("Automatic", null),
        BEETLE("Beetle", PestType.BEETLE),
        CRICKET("Cricket", PestType.CRICKET),
        EARTHWORM("Earthworm", PestType.EARTHWORM),
        FLY("Fly", PestType.FLY),
        LOCUST("Locust", PestType.LOCUST),
        MITE("Mite", PestType.MITE),
        MOSQUITO("Mosquito", PestType.MOSQUITO),
        MOTH("Moth", PestType.MOTH),
        RAT("Rat", PestType.RAT),
        SLUG("Slug", PestType.SLUG),
        ;

        private final String name;
        private final PestType pest;

        PestDisplay(String name, PestType pest) {
            this.name = name;
            this.pest = pest;
        }

        public PestType getPest() {
            return pest;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @Expose
    @ConfigOption(name = "Show Position", desc = "Show your current position next to the collection amount if below §b#5000§7.")
    @ConfigEditorBoolean
    public boolean showPosition = false;

    @Expose
    @ConfigOption(name = "Show Person To Beat", desc = "Show the person in front of you to be passed§7.")
    @ConfigEditorBoolean
    public boolean showPersonToBeat = true;
}
