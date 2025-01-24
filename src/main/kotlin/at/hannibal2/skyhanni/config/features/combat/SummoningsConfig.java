package at.hannibal2.skyhanni.config.features.combat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SummoningsConfig {

    @Expose
    @ConfigOption(name = "Summoning Soul Display", desc = "Show the name of dropped Summoning Souls laying on the ground.\n" +
        "§cNot working in Dungeons if Skytils' 'Hide Non-Starred Mobs Nametags' feature is enabled!")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean summoningSoulDisplay = false;

    @Expose
    @ConfigOption(name = "Summoning Mob Display", desc = "Show the health of your spawned summons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean summoningMobDisplay = false;

    @Expose
    @ConfigLink(owner = SummoningsConfig.class, field = "summoningMobDisplay")
    public Position summoningMobDisplayPos = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Summoning Mob Nametag", desc = "Hide the nametag of your spawned summons.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean summoningMobHideNametag = false;

    @Expose
    @ConfigOption(name = "Summoning Mob Color", desc = "Mark own summons green.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean summoningMobColored = false;

    @Expose
    @ConfigOption(name = "Summon Chat Messages", desc = "Sends a chat message when a summon dies and hides other summon related messages.")
    @ConfigEditorBoolean
    public boolean summonMessages = false;
}
