package at.hannibal2.skyhanni.config.features.inventory.chocolatefactory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HoppityCollectionStatsConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show info about your Hoppity rabbit collection."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigLink(owner = HoppityCollectionStatsConfig.class, field = "enabled")
    public Position position = new Position(163, 160, false, true);

    @Expose
    @ConfigOption(name = "Highlight Found Rabbits", desc = "Highlight rabbits that have already been found.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightFoundRabbits = false;

    @Expose
    @ConfigOption(name = "Highlight Rabbits", desc = "Highlight specific rabbit types in Hoppity's Collection.")
    @ConfigEditorDraggableList
    public List<HoppityCollectionStats.HighlightRabbitTypes> highlightRabbits = new ArrayList<>(Arrays.asList(
        HoppityCollectionStats.HighlightRabbitTypes.ABI,
        HoppityCollectionStats.HighlightRabbitTypes.FACTORY,
        HoppityCollectionStats.HighlightRabbitTypes.MET,
        HoppityCollectionStats.HighlightRabbitTypes.NOT_MET,
        HoppityCollectionStats.HighlightRabbitTypes.SHOP,
        HoppityCollectionStats.HighlightRabbitTypes.STRAYS
    ));

    @Expose
    @ConfigOption(name = "Re-color Missing Rabbit Dyes", desc = "Replace the gray dye in Hoppity's Collection with a color for the rarity of the rabbit.")
    @ConfigEditorBoolean
    public boolean rarityDyeRecolor = true;

    @Expose
    @ConfigOption(
        name = "Missing Location Rabbits",
        desc = "Show the locations you have yet to find enough egg locations for in order to unlock the rabbit for that location."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showLocationRequirementsRabbits = false;

    @Expose
    @ConfigOption(
        name = "Missing Resident Rabbits",
        desc = "Show the islands that you have the most missing resident rabbits om."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showResidentSummary = false;

    @Expose
    @ConfigOption(
        name = "Show Missing Hotspot Rabbits",
        desc = "Show the islands that have the most hotspot rabbits that you are missing."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showHotspotSummary = false;
}
