package at.hannibal2.skyhanni.config.features.itemability;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ItemAbilityConfig {

    @Expose
    @ConfigOption(name = "Ability Cooldown", desc = "Show the cooldown of item abilities.")
    @ConfigEditorBoolean
    @FeatureToggle
    // TODO rename to "enabled"
    public boolean itemAbilityCooldown = false;

    @Expose
    @ConfigOption(name = "Ability Cooldown Background", desc = "Show the cooldown color of item abilities in the background.")
    @ConfigEditorBoolean
    // TODO rename to "background"
    public boolean itemAbilityCooldownBackground = false;

    @Expose
    @ConfigOption(name = "Show When Ready", desc = "Show the R and background (if enabled) when the ability is ready.")
    @ConfigEditorBoolean
    public boolean itemAbilityShowWhenReady = true;

    @Expose
    @ConfigOption(name = "Fire Veil", desc = "")
    @Accordion
    public FireVeilWandConfig fireVeilWands = new FireVeilWandConfig();

    @ConfigOption(name = "Chicken Head", desc = "")
    @Accordion
    @Expose
    public ChickenHeadConfig chickenHead = new ChickenHeadConfig();

    @ConfigOption(name = "Crown of Avarice", desc = "")
    @Accordion
    @Expose
    public CrownOfAvariceConfig crownOfAvarice = new CrownOfAvariceConfig();

    @Expose
    @ConfigOption(name = "Depleted Bonzo's Masks",
        desc = "Highlight used Bonzo's Masks and Spirit Masks with a background.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean depletedBonzosMasks = false;
}
