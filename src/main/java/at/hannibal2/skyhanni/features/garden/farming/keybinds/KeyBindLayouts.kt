package at.hannibal2.skyhanni.features.garden.farming.keybinds

import at.hannibal2.skyhanni.config.features.garden.keybinds.KeyBindLayout
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenCustomKeybinds.mcSettings
import net.minecraft.client.settings.KeyBinding

enum class KeyBindLayouts(
    val displayName: String,
    val layoutProvider: () -> KeyBindLayout,
    var map: Map<KeyBinding, Int>,
) {
    LAYOUT_1("Layout 1", { GardenAPI.config.keyBind.layout1 }, mapOf()),
    LAYOUT_2("Layout 2", { GardenAPI.config.keyBind.layout2 }, mapOf()),
    LAYOUT_3("Layout 3", { GardenAPI.config.keyBind.layout3 }, mapOf()),
    LAYOUT_4("Layout 4", { GardenAPI.config.keyBind.layout4 }, mapOf()),
    LAYOUT_5("Layout 5", { GardenAPI.config.keyBind.layout5 }, mapOf());

    val layout: KeyBindLayout
        get() = layoutProvider()

    fun buildKeybindLayoutMap() {
        val keyBindings = with(mcSettings) {
            listOf(
                keyBindAttack, keyBindUseItem, keyBindLeft, keyBindRight,
                keyBindForward, keyBindBack, keyBindJump, keyBindSneak
            )
        }

        val zipped = keyBindings.zip(layout.allKeybindingFields)

        map = zipped.associate { (keyBinding, keybind) ->
            keyBinding to keybind.get()
        }
    }

    override fun toString(): String {
        return displayName
    }

    companion object {
        fun update() {
            entries.forEach { it.buildKeybindLayoutMap() }
        }
    }
}
