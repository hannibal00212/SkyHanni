package at.hannibal2.skyhanni.config.features.inventory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import org.lwjgl.input.Keyboard;

public class JacobFarmingContestConfig {
    @Expose
    @ConfigOption(name = "Unclaimed Rewards", desc = "Highlight contests with unclaimed rewards in the Jacob inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean highlightRewards = true;

    @Expose
    @ConfigOption(name = "Contest Time", desc = "Add the real time format to the Contest description.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean realTime = true;

    @Expose
    @ConfigOption(name = "Open On Elite", desc = "Open the contest on §eelitebot.dev§7 when pressing this key in Jacob's menu or the calendar.")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int openOnElite = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(name = "Medal Icon", desc = "Add a symbol that shows what medal you received in this Contest. " +
        "§eIf you use a texture pack this may cause conflicting icons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean medalIcon = true;

    @Expose
    @ConfigOption(name = "Finnegan Icon", desc = "Use a different indicator for when the Contest happens during Mayor Finnegan.")
    @ConfigEditorBoolean
    public boolean finneganIcon = true;
}
