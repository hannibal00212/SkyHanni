package at.hannibal2.skyhanni.features.garden.farming.keybinds

import at.hannibal2.skyhanni.config.features.garden.keybinds.KeyBindLayout
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.GardenCustomKeybinds.mcSettings
import net.minecraft.client.settings.KeyBinding

enum class KeyBindLayouts(
    val displayName: String,
    val layoutProvider: () -> KeyBindLayout,
    val map: MutableMap<KeyBinding, Int>,
) {
    LAYOUT_1("Layout 1", { GardenAPI.config.keyBind.layout1 }, mutableMapOf()),
    LAYOUT_2("Layout 2", { GardenAPI.config.keyBind.layout2 }, mutableMapOf()),
    LAYOUT_3("Layout 3", { GardenAPI.config.keyBind.layout3 }, mutableMapOf()),
    LAYOUT_4("Layout 4", { GardenAPI.config.keyBind.layout4 }, mutableMapOf()),
    LAYOUT_5("Layout 5", { GardenAPI.config.keyBind.layout5 }, mutableMapOf());

    val layout: KeyBindLayout
        get() = layoutProvider()

    fun buildKeybindLayoutMap() {
        val keyBindings = with(mcSettings) {
            listOf(
                keyBindAttack, keyBindUseItem, keyBindLeft, keyBindRight,
                keyBindForward, keyBindBack, keyBindJump, keyBindSneak
            )
        }

        map.clear()
        keyBindings.zip(
            layout.allKeybindings
        ).forEach { (keyBinding, setKeyProperty) ->
            map[keyBinding] = setKeyProperty.get() // Update map directly
        }
    }

    companion object {
        fun update() {
            entries.forEach { it.buildKeybindLayoutMap() }
        }

        fun getLayoutByDisplayName(displayName: String): KeyBindLayouts {
            val result = entries.find { it.displayName == displayName }
//             if (result == null) {
//                 TODO: Implement ErrorManager
//                 ErrorManager.logErrorWithData("KeyBindLayouts.getLayoutByDisplayName: Layout not found: $displayName")
//             }
            return result?: LAYOUT_1
        }
    }
}
