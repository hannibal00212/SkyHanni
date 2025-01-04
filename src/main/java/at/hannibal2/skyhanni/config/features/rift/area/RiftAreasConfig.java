package at.hannibal2.skyhanni.config.features.rift.area;

import at.hannibal2.skyhanni.config.features.rift.area.colosseum.ColosseumConfig;
import at.hannibal2.skyhanni.config.features.rift.area.dreadfarm.DreadfarmConfig;
import at.hannibal2.skyhanni.config.features.rift.area.livingcave.LivingCaveConfig;
import at.hannibal2.skyhanni.config.features.rift.area.mirrorverse.MirrorVerseConfig;
import at.hannibal2.skyhanni.config.features.rift.area.mountaintop.MountaintopConfig;
import at.hannibal2.skyhanni.config.features.rift.area.stillgorechateau.StillgoreChateauConfig;
import at.hannibal2.skyhanni.config.features.rift.area.westvillage.WestVillageConfig;
import at.hannibal2.skyhanni.config.features.rift.area.wyldwoods.WyldWoodsConfig;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class RiftAreasConfig {

    @ConfigOption(name = "Wyld Woods", desc = "")
    @Accordion
    @Expose
    public WyldWoodsConfig wyldWoods = new WyldWoodsConfig();

    @ConfigOption(name = "West Village", desc = "")
    @Accordion
    @Expose
    public WestVillageConfig westVillage = new WestVillageConfig();

    @Expose
    @ConfigOption(name = "Dreadfarm", desc = "")
    @Accordion
    public DreadfarmConfig dreadfarm = new DreadfarmConfig();

    @ConfigOption(name = "Mirrorverse", desc = "")
    @Accordion
    @Expose
    public MirrorVerseConfig mirrorverse = new MirrorVerseConfig();

//        @Expose
//        @ConfigOption(name = "Village Plaza", desc = "")
//        @Accordion
//        public VillagePlazaConfig villagePlaza = new VillagePlazaConfig();

    @Expose
    @ConfigOption(name = "Living Cave", desc = "")
    @Accordion
    public LivingCaveConfig livingCave = new LivingCaveConfig();

    @Expose
    @ConfigOption(name = "Colosseum", desc = "")
    @Accordion
    public ColosseumConfig colosseum = new ColosseumConfig();

    @Expose
    @ConfigOption(name = "Stillgore Chateau", desc = "")
    @Accordion
    public StillgoreChateauConfig stillgoreChateau = new StillgoreChateauConfig();

    @Expose
    @ConfigOption(name = "Mountaintop", desc = "")
    @Accordion
    public MountaintopConfig mountaintop = new MountaintopConfig();
}
