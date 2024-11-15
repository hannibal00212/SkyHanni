package at.hannibal2.skyhanni.config.features.skillprogress;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import at.hannibal2.skyhanni.utils.RenderUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class SkillProgressConfig {

    @Expose
    @ConfigOption(name = "Elite Bot ranking display", desc = "")
    @Accordion
    public EliteSkillsDisplayConfig rankDisplay = new EliteSkillsDisplayConfig();

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show the Skill Progress Display.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Text Alignment", desc = "Align the display text with the progress bar.")
    @ConfigEditorDropdown
    public Property<TextAlignment> textAlignmentProperty = Property.of(TextAlignment.CENTERED);

    public enum TextAlignment {
        NONE("None", null),
        CENTERED("Centered", RenderUtils.HorizontalAlignment.CENTER),
        LEFT("Left", RenderUtils.HorizontalAlignment.LEFT),
        RIGHT("Right", RenderUtils.HorizontalAlignment.RIGHT),
        ;

        private final String str;
        private final RenderUtils.HorizontalAlignment alignment;

        TextAlignment(String str, RenderUtils.HorizontalAlignment alignment) {
            this.str = str;
            this.alignment = alignment;
        }

        public RenderUtils.HorizontalAlignment getAlignment() {
            return alignment;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Hide In Action Bar", desc = "Hide the skill progress in the Hypixel action bar.")
    @ConfigEditorBoolean
    public boolean hideInActionBar = false;

    @Expose
    @ConfigOption(name = "Always Show", desc = "Always show the skill progress.")
    @ConfigEditorBoolean
    public Property<Boolean> alwaysShow = Property.of(false);

    @Expose
    @ConfigOption(name = "Show Action left", desc = "Show action left until you reach the next level.")
    @ConfigEditorBoolean
    public Property<Boolean> showActionLeft = Property.of(false);

    @Expose
    @ConfigOption(name = "Use percentage", desc = "Use percentage instead of XP.")
    @ConfigEditorBoolean
    public Property<Boolean> usePercentage = Property.of(false);

    @Expose
    @ConfigOption(name = "Use Icon", desc = "Show the skill icon in the display.")
    @ConfigEditorBoolean
    public Property<Boolean> useIcon = Property.of(true);

    @Expose
    @ConfigOption(name = "Use Skill Name", desc = "Show the skill name in the display.")
    @ConfigEditorBoolean
    public Property<Boolean> useSkillName = Property.of(false);

    @Expose
    @ConfigOption(name = "Show Level", desc = "Show your current level in the display.")
    @ConfigEditorBoolean
    public Property<Boolean> showLevel = Property.of(true);

    @Expose
    @Category(name = "Progress Bar", desc = "Progress Bar Config.")
    public SkillProgressBarConfig skillProgressBarConfig = new SkillProgressBarConfig();

    @Expose
    @Category(name = "Overflow", desc = "Overflow Config.")
    public SkillOverflowConfig overflowConfig = new SkillOverflowConfig();

    @Expose
    @Category(name = "Custom Goal", desc = "Define a custom goal for each skills.")
    public CustomGoalConfig customGoalConfig = new CustomGoalConfig();

    @Expose
    @Category(name = "All Skill Display", desc = "All Skill Display Config.")
    public AllSkillDisplayConfig allSkillDisplayConfig = new AllSkillDisplayConfig();

    @Expose
    @Category(name = "ETA Display", desc = "ETA Display Config.")
    public SkillETADisplayConfig skillETADisplayConfig = new SkillETADisplayConfig();

    @Expose
    @ConfigLink(owner = SkillProgressConfig.class, field = "enabled")
    public Position displayPosition = new Position(384, -105, false, true);

    @Expose
    @ConfigLink(owner = SkillProgressBarConfig.class, field = "enabled")
    public Position barPosition = new Position(384, -87, false, true);

    @Expose
    @ConfigLink(owner = AllSkillDisplayConfig.class, field = "enabled")
    public Position allSkillPosition = new Position(5, 209, false, true);

    @Expose
    @ConfigLink(owner = SkillETADisplayConfig.class, field = "enabled")
    public Position etaPosition = new Position(5, 155, false, true);
}
