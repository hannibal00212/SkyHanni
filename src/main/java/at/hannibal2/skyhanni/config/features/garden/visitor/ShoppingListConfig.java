package at.hannibal2.skyhanni.config.features.garden.visitor;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ShoppingListConfig {
    @Expose
    @ConfigOption(name = "Enable", desc = "Show all items required for the visitors.")
    @ConfigEditorBoolean
    @FeatureToggle
    // TODO rename "enabled"
    public boolean display = true;

    @Expose
    // TODO rename "position"
    @ConfigLink(owner = ShoppingListConfig.class, field = "display")
    public Position pos = new Position(180, 170, false, true);

    @Expose
    @ConfigOption(name = "Only when Close", desc = "Only show the shopping list when close to the visitors.")
    @ConfigEditorBoolean
    public boolean onlyWhenClose = false;

    @Expose
    @ConfigOption(name = "Bazaar Alley", desc = "Show the Visitor Items List while inside the Bazaar Alley in the Hub.\n" +
        "§eHelps in buying the correct amount when not having a §6Booster Cookie §ebuff active.")
    @ConfigEditorBoolean
    public boolean inBazaarAlley = true;

    @Expose
    @ConfigOption(name = "Farming Areas", desc = "Show the Visitor Shopping List while on the Farming Islands or inside the Farm in the Hub.\n" +
        "§eHelps in farming the correct amount, especially when in the early game.")
    @ConfigEditorBoolean
    public boolean inFarmingAreas = false;

    @Expose
    @ConfigOption(name = "Show Price", desc = "Show the coin price in the shopping list.")
    @ConfigEditorBoolean
    public boolean showPrice = true;

    @Expose
    @ConfigOption(name = "Show Sack Count", desc = "Show the amount of this item that you already have in your sacks.\n" +
        "§eOnly updates on sack change messages.")
    @ConfigEditorBoolean
    public boolean showSackCount = true;

    @Expose
    @ConfigOption(name = "Show Super Craft", desc = "Show super craft button if there are enough materials to make in the sack.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showSuperCraft = false;

    @Expose
    @ConfigOption(name = "Item Preview", desc = "Show the base type for the required items next to new visitors.\n" +
        "§cNote that some visitors may require any crop.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean itemPreview = true;

    @Expose
    @ConfigOption(name = "Ignore Spaceman", desc = "Exclude crops requested by Spaceman from the shopping list.")
    @ConfigEditorBoolean
    public boolean ignoreSpaceman = false;

}
