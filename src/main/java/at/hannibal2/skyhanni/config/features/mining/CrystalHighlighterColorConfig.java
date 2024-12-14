package at.hannibal2.skyhanni.config.features.mining;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class CrystalHighlighterColorConfig {

    @Expose
    @ConfigOption(name = "Amber", desc = "§7Default: §#§c§b§6§4§0§0§/#CB6400")
    @ConfigEditorColour
    public Property<String> amber = Property.of("0:127:203:100:0");

    @Expose
    @ConfigOption(name = "Amethyst", desc = "§7Default: §#§a§8§0§b§c§b§/#A80BCB")
    @ConfigEditorColour
    public Property<String> amethyst = Property.of("0:127:168:11:203");

    @Expose
    @ConfigOption(name = "Topaz", desc = "§7Default: §#§c§d§c§4§0§0§/#CDC400")
    @ConfigEditorColour
    public Property<String> topaz = Property.of("0:127:205:196:0");

    @Expose
    @ConfigOption(name = "Jade", desc = "§7Default: §#§6§8§c§b§0§0§/#68CB00")
    @ConfigEditorColour
    public Property<String> jade = Property.of("0:85:104:203:0");

    @Expose
    @ConfigOption(name = "Sapphire", desc = "§7Default: §#§2§9§6§4§c§b§/#2964CB")
    @ConfigEditorColour
    public Property<String> sapphire = Property.of("0:127:41:100:203");
}
