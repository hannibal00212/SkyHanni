package at.hannibal2.skyhanni.config.features.garden.pests;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SprayConfig {

    @Expose
    @ConfigOption(
        name = "Pest Spray Selector",
        desc = "Show the pests that are attracted when changing the selected material of the §aSprayonator§7."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean pestWhenSelector = true;

    @Expose
    @ConfigOption(name = "Draw Plot Border", desc = "Draw plots border when holding the Sprayonator.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean drawPlotsBorderWhenInHands = true;

    @Expose
    @ConfigLink(owner = SprayConfig.class, field = "pestWhenSelector")
    public Position position = new Position(315, -200, 2.3f);

    @Expose
    @ConfigOption(
        name = "Spray Display",
        desc = "Show the active spray and duration for your current plot."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean displayEnabled = true;

    @Expose
    @ConfigOption(
        name = "Show If Not Sprayed",
        desc = "Also show if current plot is not sprayed."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showNotSprayed = false;

    @Expose
    @ConfigOption(
        name = "Spray Expiration Notice",
        desc = "Show a notification in chat when a spray runs out in any plot. Only active in Garden."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean expiryNotification = true;

    @Expose
    @ConfigOption(
        name = "New Spray Notice",
        desc = "Send a message in chat if a new spray is detected when entering a plot."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean newSprayNotification = false;

    @Expose
    @ConfigLink(owner = SprayConfig.class, field = "displayEnabled")
    public Position displayPosition = new Position(390, 75, false, true);
}
