package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ArrowConfig {
    @Expose
    @ConfigOption(name = "Arrow Amount Display", desc = "Determine how the arrow amount is displayed.")
    @ConfigEditorDropdown
    public ArrowAmountDisplay arrowAmountDisplay = ArrowAmountDisplay.NUMBER;

    public enum ArrowAmountDisplay {
        NUMBER("Number"),
        PERCENTAGE("Percentage"),
        ;

        private final String displayName;

        ArrowAmountDisplay(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(name = "Color Arrow Amount", desc = "Color the arrow amount based on the percentage.")
    @ConfigEditorBoolean
    public boolean colorArrowAmount = false;
}
