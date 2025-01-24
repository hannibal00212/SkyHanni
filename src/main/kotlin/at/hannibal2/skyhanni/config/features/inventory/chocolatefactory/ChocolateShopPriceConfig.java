package at.hannibal2.skyhanni.config.features.inventory.chocolatefactory;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ChocolateShopPriceConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show chocolate to coin prices inside the Chocolate Shop inventory.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @ConfigLink(owner = ChocolateFactoryConfig.class, field = "chocolateShopPrice")
    public Position position = new Position(200, 150, false, true);

    @Expose
    @ConfigOption(name = "Item Scale", desc = "Change the size of the items.")
    @ConfigEditorSlider(minValue = 0.3f, maxValue = 3, minStep = 0.1f)
    public double itemScale = 0.6;
}
