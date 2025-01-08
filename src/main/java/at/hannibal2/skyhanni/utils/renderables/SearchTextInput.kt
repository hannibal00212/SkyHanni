package at.hannibal2.skyhanni.utils.renderables

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.model.TextInput
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils

class SearchTextInput : TextInput() {

    init {
        searchTextInputs.add(this)
    }

    @SkyHanniModule
    companion object {

        private val config get() = SkyHanniMod.feature.misc

        val searchTextInputs = mutableListOf<SearchTextInput>()

        @HandleEvent
        fun onInventoryClose(event: InventoryCloseEvent) {
            if (!isEnabled()) return

            for (input in searchTextInputs) {
                if (input.textBox != "") {
                    input.textBox = ""
                    input.update()
                }
            }
        }

        fun isEnabled() = LorenzUtils.inSkyBlock && config.resetSearchGuiOnClose
    }

}
