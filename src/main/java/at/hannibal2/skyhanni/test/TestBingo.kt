package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils

@SkyHanniModule
object TestBingo {

    var testBingo = false

    fun toggle() {
        testBingo = !testBingo
        ChatUtils.chat("Test Bingo " + (if (testBingo) "enabled" else "disabled"))
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Bingo Test")
        if (testBingo) {
            event.addData {
                add("debug active!")
            }
        } else {
            event.addIrrelevant("not active.")
        }
    }
}
