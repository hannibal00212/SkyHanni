package at.hannibal2.skyhanni.features.gui.shtrack

import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import com.google.gson.stream.JsonWriter

abstract class ItemTrackingInterface() : TrackingElement<Long>() {

    abstract fun itemChange(item: PrimitiveItemStack)

    abstract val includeSack: Boolean

    override fun toJson(out: JsonWriter) {
        super.toJson(out)
        out.name("sack").value(includeSack)
    }
}
