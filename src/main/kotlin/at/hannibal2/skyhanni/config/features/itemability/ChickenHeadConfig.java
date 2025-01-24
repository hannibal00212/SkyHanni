package at.hannibal2.skyhanni.config.features.itemability;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ChickenHeadConfig {

    @Expose
    @ConfigOption(name = "Chicken Head Timer", desc = "Show the cooldown until the next time you can lay an egg with the Chicken Head.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean displayTimer = false;

    @Expose
    @ConfigLink(owner = ChickenHeadConfig.class, field = "displayTimer")
    public Position position = new Position(-372, 73, false, true);

    @Expose
    @ConfigOption(name = "Hide Chat", desc = "Hide the 'You laid an egg!' chat message.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean hideChat = true;
}
