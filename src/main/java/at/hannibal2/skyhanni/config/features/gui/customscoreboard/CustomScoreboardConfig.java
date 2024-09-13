package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardElement;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.List;

public class CustomScoreboardConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show a custom scoreboard instead of the vanilla one."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(
        name = "Appearance",
        desc = "Drag text to change the appearance of the advanced scoreboard." // supporting both custom & advanced search
    )
    @ConfigEditorDraggableList()
    public List<ScoreboardElement> scoreboardEntries = new ArrayList<>(ScoreboardElement.defaultOption);

    @ConfigOption(name = "Reset Appearance", desc = "Reset the appearance of the advanced scoreboard.")
    @ConfigEditorButton(buttonText = "Reset")
    public Runnable reset = () -> {
        scoreboardEntries.clear();
        scoreboardEntries.addAll(ScoreboardElement.defaultOption);
    };

    @Expose
    @ConfigOption(name = "Display Options", desc = "")
    @Accordion
    public DisplayConfig display = new DisplayConfig();

    @Expose
    @ConfigOption(name = "Background Options", desc = "")
    @Accordion
    public BackgroundConfig background = new BackgroundConfig();

    @Expose
    @ConfigOption(name = "Information Filtering", desc = "")
    @Accordion
    public InformationFilteringConfig informationFiltering = new InformationFilteringConfig();

    @Expose
    @ConfigOption(name = "Unknown Lines warning", desc = "Give a chat warning when unknown lines are found in the scoreboard.")
    @ConfigEditorBoolean
    public boolean unknownLinesWarning = true;

    @Expose
    @ConfigLink(owner = CustomScoreboardConfig.class, field = "enabled")
    public Position position = new Position(10, 80, false, true);
}
