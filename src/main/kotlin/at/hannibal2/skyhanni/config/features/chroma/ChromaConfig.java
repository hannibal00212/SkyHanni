package at.hannibal2.skyhanni.config.features.chroma;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.HasLegacyId;
import at.hannibal2.skyhanni.features.chroma.ChromaManager;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class ChromaConfig {

    @Expose
    @ConfigOption(name = "Chroma Preview", desc = "§fPlease star SkyHanni on GitHub!")
    @ConfigEditorInfoText(infoTitle = "Only in SkyBlock")
    public boolean chromaPreview = false;

    @Expose
    @ConfigOption(name = "Enabled", desc = "Toggle SkyHanni's chroma.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Chroma Size", desc = "Change the size of each color in the chroma.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 100f, minStep = 1f)
    public float chromaSize = 30f;

    @Expose
    @ConfigOption(name = "Chroma Speed", desc = "Change how fast the chroma animation moves.")
    @ConfigEditorSlider(minValue = 0.5f, maxValue = 20f, minStep = 0.5f)
    public float chromaSpeed = 6f;

    @Expose
    @ConfigOption(name = "Chroma Saturation", desc = "Change the saturation of the chroma.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 1f, minStep = 0.01f)
    public float chromaSaturation = 0.75f;

    @Expose
    @ConfigOption(name = "Chroma Direction", desc = "Change the slant and direction of the chroma.")
    @ConfigEditorDropdown
    public Direction chromaDirection = Direction.FORWARD_RIGHT;

    public enum Direction implements HasLegacyId {
        FORWARD_RIGHT("Forward + Right", 0),
        FORWARD_LEFT("Forward + Left", 1),
        BACKWARD_RIGHT("Backward + Right", 2),
        BACKWARD_LEFT("Backward + Left", 3);

        private final String str;
        private final int legacyId;

        Direction(String str, int legacyId) {
            this.str = str;
            this.legacyId = legacyId;
        }

        // Constructor if new enum elements are added post-migration
        Direction(String str) {
            this(str, -1);
        }

        @Override
        public int getLegacyId() {
            return legacyId;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    @ConfigOption(name = "Reset to Default", desc = "Reset all chroma settings to the default.")
    @ConfigEditorButton(buttonText = "Reset")
    public Runnable resetSettings = ChromaManager::resetChromaSettings;

    @Expose
    @ConfigOption(name = "Everything Chroma", desc = "Render §4§l§oALL §r§7text in chroma. §e(Disables Patcher's Optimized Font Renderer while enabled)")
    @ConfigEditorBoolean
    public boolean allChroma = false;

    @Expose
    @ConfigOption(name = "Ignore Chat", desc = "Prevent Everything Chroma from applying to the chat (if you unironically use that feature...)")
    @ConfigEditorBoolean
    public boolean ignoreChat = false;

}
