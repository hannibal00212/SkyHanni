package at.hannibal2.skyhanni.config.features.garden.keybinds;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class KeyBindConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Use custom keybinds while holding a farming tool or Daedalus Axe in the hand.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = false;

    @Expose
    @ConfigOption(name = "Exclude Barn", desc = "Disable this feature while on the barn plot.")
    @ConfigEditorBoolean
    public boolean excludeBarn = false;

    public enum Layouts {
        LAYOUT_1("Layout 1"),
        LAYOUT_2("Layout 2"),
        LAYOUT_3("Layout 3"),
        LAYOUT_4("Layout 4"),
        LAYOUT_5("Layout 5"),
        ;

        private final String str;

        Layouts(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @Expose
    @ConfigOption(name = "Profile Selection For Crops", desc = "")
    @Accordion
    public LayoutSelection cropLayoutSelection = new LayoutSelection();

    public static class LayoutSelection {
        @Expose
        @ConfigOption(name = "Layout Wheat", desc = "Select the keybind layout for wheat.")
        @ConfigEditorDropdown
        public Layouts wheat = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Carrot", desc = "Select the keybind layout for carrot.")
        @ConfigEditorDropdown
        public Layouts carrot = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Potato", desc = "Select the keybind layout for potato.")
        @ConfigEditorDropdown
        public Layouts potato = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Nether Wart", desc = "Select the keybind layout for nether wart.")
        @ConfigEditorDropdown
        public Layouts netherWart = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Pumpkin", desc = "Select the keybind layout for pumpkin.")
        @ConfigEditorDropdown
        public Layouts pumpkin = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Melon", desc = "Select the keybind layout for melon.")
        @ConfigEditorDropdown
        public Layouts melon = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Cocoa Beans", desc = "Select the keybind layout for cocoa beans.")
        @ConfigEditorDropdown
        public Layouts cocoaBeans = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Sugar Cane", desc = "Select the keybind layout for sugar cane.")
        @ConfigEditorDropdown
        public Layouts sugarCane = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Cactus", desc = "Select the keybind layout for cactus.")
        @ConfigEditorDropdown
        public Layouts cactus = Layouts.LAYOUT_1;

        @Expose
        @ConfigOption(name = "Layout Mushroom", desc = "Select the keybind layout for mushroom.")
        @ConfigEditorDropdown
        public Layouts mushroom = Layouts.LAYOUT_1;
    }

    @Expose
    @ConfigOption(name = "Layout 1", desc = "")
    @Accordion
    public KeyBindLayout layout1 = new KeyBindLayout();

    @Expose
    @ConfigOption(name = "Layout 2", desc = "")
    @Accordion
    public KeyBindLayout layout2 = new KeyBindLayout();

    @Expose
    @ConfigOption(name = "Layout 3", desc = "")
    @Accordion
    public KeyBindLayout layout3 = new KeyBindLayout();

    @Expose
    @ConfigOption(name = "Layout 4", desc = "")
    @Accordion
    public KeyBindLayout layout4 = new KeyBindLayout();

    @Expose
    @ConfigOption(name = "Layout 5", desc = "")
    @Accordion
    public KeyBindLayout layout5 = new KeyBindLayout();
}
