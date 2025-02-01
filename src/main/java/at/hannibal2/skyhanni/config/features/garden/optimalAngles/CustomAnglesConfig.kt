package at.hannibal2.skyhanni.config.features.garden.optimalAngles

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class CustomAnglesConfig {

    // Cactus
    @Expose
    @ConfigOption(name = "Cactus Yaw", desc = "Set Yaw for cactus farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var cactusYaw: Property<Float> = Property.of(-90f)

    @Expose
    @ConfigOption(name = "Cactus Pitch", desc = "Set Pitch for cactus farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var cactusPitch: Property<Float> = Property.of(0f)

    // Carrot
    @Expose
    @ConfigOption(name = "Carrot Yaw", desc = "Set Yaw for carrot farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var carrotYaw: Property<Float> = Property.of(-90f)

    @Expose
    @ConfigOption(name = "Carrot Pitch", desc = "Set Pitch for carrot farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var carrotPitch: Property<Float> = Property.of(2.8f)

    // Cocoa Beans
    @Expose
    @ConfigOption(name = "Cocoa Beans Yaw", desc = "Set Yaw for cocoa bean farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var cocoaBeansYaw: Property<Float> = Property.of(180f)

    @Expose
    @ConfigOption(name = "Cocoa Beans Pitch", desc = "Set Pitch for cocoa bean farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var cocoaBeansPitch: Property<Float> = Property.of(-45f)

    // Melon
    @Expose
    @ConfigOption(name = "Melon Yaw", desc = "Set Yaw for melon farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var melonYaw: Property<Float> = Property.of(90f)

    @Expose
    @ConfigOption(name = "Melon Pitch", desc = "Set Pitch for melon farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var melonPitch: Property<Float> = Property.of(-58.5f)

    // Mushroom
    @Expose
    @ConfigOption(name = "Mushroom Yaw", desc = "Set Yaw for mushroom farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var mushroomYaw: Property<Float> = Property.of(116.5f)

    @Expose
    @ConfigOption(name = "Mushroom Pitch", desc = "Set Pitch for mushroom farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var mushroomPitch: Property<Float> = Property.of(0f)

    // Nether Wart
    @Expose
    @ConfigOption(name = "Nether Wart Yaw", desc = "Set Yaw for nether wart farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var netherWartYaw: Property<Float> = Property.of(90f)

    @Expose
    @ConfigOption(name = "Nether Wart Pitch", desc = "Set Pitch for nether wart farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var netherWartPitch: Property<Float> = Property.of(0f)

    // Potato
    @Expose
    @ConfigOption(name = "Potato Yaw", desc = "Set Yaw for potato farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var potatoYaw: Property<Float> = Property.of(-90f)

    @Expose
    @ConfigOption(name = "Potato Pitch", desc = "Set Pitch for potato farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var potatoPitch: Property<Float> = Property.of(2.8f)

    // Pumpkin
    @Expose
    @ConfigOption(name = "Pumpkin Yaw", desc = "Set Yaw for pumpkin farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var pumpkinYaw: Property<Float> = Property.of(90f)

    @Expose
    @ConfigOption(name = "Pumpkin Pitch", desc = "Set Pitch for pumpkin farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var pumpkinPitch: Property<Float> = Property.of(-58.5f)

    // Sugar Cane
    @Expose
    @ConfigOption(name = "Sugar Cane Yaw", desc = "Set Yaw for sugar cane farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var sugarCaneYaw: Property<Float> = Property.of(-135f)

    @Expose
    @ConfigOption(name = "Sugar Cane Pitch", desc = "Set Pitch for sugar cane farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var sugarCanePitch: Property<Float> = Property.of(0f)

    // Wheat
    @Expose
    @ConfigOption(name = "Wheat Yaw", desc = "Set Yaw for wheat farming.")
    @ConfigEditorSlider(minValue = -180f, maxValue = 180f, minStep = 0.1f)
    var wheatYaw: Property<Float> = Property.of(90f)

    @Expose
    @ConfigOption(name = "Wheat Pitch", desc = "Set Pitch for wheat farming.")
    @ConfigEditorSlider(minValue = -90f, maxValue = 90f, minStep = 0.1f)
    var wheatPitch: Property<Float> = Property.of(0f)
}
