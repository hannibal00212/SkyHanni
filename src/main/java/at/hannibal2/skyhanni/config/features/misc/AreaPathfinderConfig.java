package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class AreaPathfinderConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "While in your invenotry, show all areas of the island. Click on an area to display the path to this area.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Show Always", desc = "Show the list always, not only while inside the inventory.")
    @ConfigEditorBoolean
    public boolean showAlways = false;

    @Expose
    @ConfigOption(name = "Current Area", desc = "Show the name of the current area at the top of the list")
    @ConfigEditorBoolean
    public Property<Boolean> includeCurrentArea = Property.of(false);

    @Expose
    @ConfigOption(
        name = "Path Color",
        desc = "Change the color of the path."
    )
    @ConfigEditorColour
    public Property<String> color = Property.of("0:245:85:255:85");

    @Expose
    @ConfigLink(owner = AreaPathfinderConfig.class, field = "enabled")
    public Position position = new Position(-350, 100);
}
