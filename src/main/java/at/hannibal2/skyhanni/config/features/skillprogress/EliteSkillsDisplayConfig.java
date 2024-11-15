package at.hannibal2.skyhanni.config.features.skillprogress;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class EliteSkillsDisplayConfig {
    @Expose
    @ConfigOption(name = "Display", desc = "Display your skill ranking on screen. " +
        "The calculation and API is provided by The Elite SkyBlock farmers. " +
        "See §celitebot.dev/info §7for more info.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean display = false;

    @Expose
    @ConfigLink(owner = EliteSkillsDisplayConfig.class, field = "display")
    public Position pos = new Position(10, 10, false, true);

    @Expose
    @ConfigOption(name = "Always Show", desc = "Always show, even when not collecting xp.")
    @ConfigEditorBoolean
    public boolean alwaysShow = true;

    @Expose
    @ConfigOption(name = "Cooldown", desc = "How long the display will stay after you've stopped collecting xp, in seconds.")
    @ConfigEditorSlider(minValue = 5, maxValue = 60, minStep = 5)
    public int alwaysShowTime = 30;

    @Expose
    @ConfigOption(name = "Only Show In Garden", desc = "Only show the skill display while on the garden island.")
    @ConfigEditorBoolean
    public boolean showInGarden = false;

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
    @ConfigOption(
        name = "Skill To Display",
        desc = "The skill to display on the tracker. Set to automatic to display last skill gained.")
    @ConfigEditorDropdown
    public Property<EliteSkillsDisplayConfig.SkillDisplay> skill = Property.of(EliteSkillsDisplayConfig.SkillDisplay.AUTO);

    public enum SkillDisplay {
        AUTO("Automatic", null),
        COMBAT("Combat", "combat"),
        MINING("Mining", "mining"),
        FORAGING("Foraging", "foraging"),
        FISHING("Fishing", "fishing"),
        ENCHANTING("Enchanting", "enchanting"),
        ALCHEMY("Alchemy", "alchemy"),
        TAMING("Taming", "taming"),
        CARPENTRY("Carpentry", "carpentry"),
        RUNECRAFTING("Runecrafting", "runecrafting"),
        SOCIAL("Social", "social"),
        FARMING("Farming", "farming"),
        ;

        private final String name;
        private final String skill;

        SkillDisplay(String name, String skill) {
            this.name = name;
            this.skill = skill;
        }

        public String getSkill() {
            return skill;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @Expose
    @ConfigOption(name = "Show Position", desc = "Show your current position next to the xp amount if below §b#5000§7.")
    @ConfigEditorBoolean
    public boolean showPosition = true;

    @Expose
    @ConfigOption(name = "Show Person To Beat", desc = "Show the person in front of you to be passed.")
    @ConfigEditorBoolean
    public boolean showPersonToBeat = true;
}
