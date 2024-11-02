package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SkyBlockXPAPI {

    private val storage get() = ProfileStorageData.profileSpecific?.skyblockXP

    var xp: Int?
        get() = storage?.xp
        set(value) {
            storage?.xp = value
        }

    var level: Int?
        get() = storage?.level
        set(value) {
            storage?.level = value
        }


    @SubscribeEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.SB_LEVEL)) return

        event.widget.pattern.firstMatcher(event.lines) {
            level = group("level")?.toIntOrNull()
            xp = group("xp")?.toIntOrNull()
        }
    }

}
