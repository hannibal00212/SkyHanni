package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import net.minecraft.item.ItemStack

class RenderItemTipEvent(
    val stack: ItemStack,
    var renderObjects: MutableList<RenderObject>,
) : SkyHanniEvent() {

    var stackTip = ""
        set(value) {
            if (value.isEmpty()) return
            renderObjects.add(RenderObject(value, 0, 0))
        }
}

class RenderObject(val text: String, var offsetX: Int = 0, var offsetY: Int = 0)
