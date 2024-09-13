package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PotionEffectsConfig {
    @Expose
    @ConfigOption(name = "Non God Pot Effects", desc = "Display the active potion effects that are not part of the God Pot.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean nonGodPotEffectDisplay = false;

    @Expose
    @ConfigOption(name = "Show Mixins", desc = "Include God Pot mixins in the Non God Pot Effects display.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean nonGodPotEffectShowMixins = false;

    @Expose
    @ConfigOption(name = "Expire Warning", desc = "Sends a title when one of the Non God Pot Effects is expiring.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean expireWarning = false;

    @Expose
    @ConfigOption(name = "Expire Sound", desc = "Makes a sound when one of the Non God Pot Effects is expiring.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean expireSound = false;

    @Expose
    @ConfigOption(
        name = "Expire Warning Time",
        desc = "Change the time in seconds before the potion expries to warn you.")
    @ConfigEditorSlider(
        minValue = 30,
        maxValue = 300,
        minStep = 5
    )
    public int expireWarnTime = 30;

    @Expose
    @ConfigLink(owner = PotionEffectsConfig.class, field = "nonGodPotEffectDisplay")
    // TODO rename position
    public Position nonGodPotEffectPos = new Position(10, 10, false, true);
}
