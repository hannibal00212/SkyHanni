package at.hannibal2.skyhanni.config.features.combat.broodmother;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.features.combat.BroodmotherFeatures.StageEntry;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BroodmotherConfig {

    @Expose
    @ConfigOption(name = "Countdown", desc = "Display a countdown until the Broodmother will spawn.\n" +
        "§cCountdown will not show unless the time until spawn has been established, and may be off by a few seconds.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean countdown = true;

    @Expose
    @ConfigOption(name = "Spawn Alert", desc = "Send a chat message, title and sound when the Broodmother spawns.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean alertOnSpawn = false;

    @Expose
    @ConfigOption(name = "Alert Settings", desc = "")
    @Accordion
    public BroodmotherSpawnAlertConfig spawnAlert = new BroodmotherSpawnAlertConfig();

    @Expose
    @ConfigOption(name = "Imminent Warning", desc = "Warns you when the Broodmother is 1 minute away from spawning.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean imminentWarning = false;

    @Expose
    @ConfigOption(name = "Chat Messages", desc = "Send a chat message when the Broodmother enters these stages.\n" +
        "§cThe 'Alive!' and 'Imminent' stages are overridden by the \"Spawn Alert\" and \"Imminent Warning\" features.")
    @ConfigEditorDraggableList
    public List<StageEntry> stages = new ArrayList<>(Arrays.asList(
        StageEntry.SLAIN,
        StageEntry.ALIVE
    ));

    @Expose
    @ConfigOption(name = "Stage on Server Join", desc = "Send a chat message with the Broodmother's current stage upon joining the Spider's Den.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean stageOnJoin = false;

    @Expose
    @ConfigOption(name = "Hide own kills", desc = "Disable the chat message for the §eSlain §rstage if at the Spider Mound.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideSlainWhenNearby = false;

    @Expose
    @ConfigLink(owner = BroodmotherConfig.class, field = "countdown")
    public Position countdownPosition = new Position(10, 10, false, true);

}
