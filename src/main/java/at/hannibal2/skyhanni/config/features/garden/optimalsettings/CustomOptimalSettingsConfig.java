package at.hannibal2.skyhanni.config.features.garden.optimalsettings;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class CustomOptimalSettingsConfig {

    @Expose
    @ConfigOption(name = "Wheat", desc = "")
    @Accordion
    public WheatConfig wheat = new WheatConfig();

    @Expose
    @ConfigOption(name = "Carrot", desc = "")
    @Accordion
    public CarrotConfig carrot = new CarrotConfig();

    @Expose
    @ConfigOption(name = "Potato", desc = "")
    @Accordion
    public PotatoConfig potato = new PotatoConfig();

    @Expose
    @ConfigOption(name = "Nether Wart", desc = "")
    @Accordion
    public NetherWartConfig netherWart = new NetherWartConfig();

    @Expose
    @ConfigOption(name = "Pumpkin", desc = "")
    @Accordion
    public PumpkinConfig pumpkin = new PumpkinConfig();

    @Expose
    @ConfigOption(name = "Melon", desc = "")
    @Accordion
    public MelonConfig melon = new MelonConfig();

    @Expose
    @ConfigOption(name = "Cocoa Beans", desc = "")
    @Accordion
    public CocoaBeansConfig cocoaBeans = new CocoaBeansConfig();

    @Expose
    @ConfigOption(name = "Sugar Cane", desc = "")
    @Accordion
    public SugarCaneConfig sugarCane = new SugarCaneConfig();

    @Expose
    @ConfigOption(name = "Cactus", desc = "")
    @Accordion
    public CactusConfig cactus = new CactusConfig();

    @Expose
    @ConfigOption(name = "Mushroom", desc = "")
    @Accordion
    public MushroomConfig mushroom = new MushroomConfig();


    public static class WheatConfig {

        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§e5 Blocks§7: §f✦ 93 speed\n" +
            "§e4 Blocks§7: §f✦ 116 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(93f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f0.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(0f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f0.0°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(0f);
    }

    public static class CarrotConfig {

        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§e5 Blocks§7: §f✦ 93 speed\n" +
            "§e4 Blocks§7: §f✦ 116 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(93f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f0.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(0f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f0.0°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(0f);
    }

    public static class PotatoConfig {

        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§e5 Blocks§7: §f✦ 93 speed\n" +
            "§e4 Blocks§7: §f✦ 116 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(93f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f0.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(0f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f0.0°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(0f);
    }

    public static class NetherWartConfig {

        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§e5 Blocks§7: §f✦ 93 speed\n" +
            "§e4 Blocks§7: §f✦ 116 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(93f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f0.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(0f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f0.0°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(0f);
    }

    public static class PumpkinConfig {

        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§e3 Blocks§7: §f✦ 155 speed\n" +
            "§e2 Blocks§7: §f✦ 265 §7or §f400 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(155f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f0.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(0f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f-58.5°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(-58.5f);
    }

    public static class MelonConfig {

        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§e3 Blocks§7: §f✦ 155 speed\n" +
            "§e2 Blocks§7: §f✦ 265 or 400 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(155f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f0.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(0f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f-58.5°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(-58.5f);
    }

    public static class CocoaBeansConfig {

        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§e3 Blocks§7: §f✦ 155 speed\n" +
            "§e4 Blocks§7: §f✦ 116 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(155f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f0.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(0f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f45.0°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(45f);
    }

    public static class SugarCaneConfig {

        // TODO do other speed settings exist?
        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§f✦ 328 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(328f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f45.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(45f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f0.0°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(0f);
    }

    public static class CactusConfig {

        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed: §f✦ 464 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 500, minStep = 1)
        public Property<Float> speed = Property.of(464f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f90.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(90f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f0.0°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(0f);
    }

    public static class MushroomConfig {

        // TODO do other speed settings exist?
        @Expose
        @ConfigOption(name = "Speed", desc = "Suggested farm speed:\n" +
            "§f✦ 233 speed")
        @ConfigEditorSlider(minValue = 1, maxValue = 400, minStep = 1)
        public Property<Float> speed = Property.of(233f);

        @Expose
        @ConfigOption(name = "Yaw", desc = "Suggested farm yaw: §f60.0°")
        @ConfigEditorSlider(minValue = -180, maxValue = 180, minStep = 1)
        public Property<Float> yaw = Property.of(60f);

        @Expose
        @ConfigOption(name = "Pitch", desc = "Suggested farm pitch: §f0.0°")
        @ConfigEditorSlider(minValue = -90, maxValue = 90, minStep = 1)
        public Property<Float> pitch = Property.of(0f);
    }
}
