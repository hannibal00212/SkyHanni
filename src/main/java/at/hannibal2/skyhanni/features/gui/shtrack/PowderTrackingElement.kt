package at.hannibal2.skyhanni.features.gui.shtrack

import at.hannibal2.skyhanni.api.HotmAPI
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.google.gson.JsonElement

class PowderTrackingElement(val type: HotmAPI.PowderType, override var current: Long, override val target: Long?) :
    TrackingElement<Long>() {

    override val name = "${type.displayNameWithColor} Powder"
    override val saveName = type.name

    override fun internalUpdate(amount: Number) {
        current += amount.toLong()
        if (target != null && current >= target) {
            handleDone("${type.displayName} §adone")
        }
    }

    override fun generateLine() = listOf(
        Renderable.itemStack(type.icon),
        Renderable.string(type.displayName),
        Renderable.string(current.toString() + ((target?.let { " / $it" }).orEmpty())),
    )

    override fun similarElement(other: TrackingElement<*>): Boolean {
        if (other !is PowderTrackingElement) return false
        return other.type == this.type
    }

    override fun atRemove() {
        ShTrack.powderTracker.remove(this)
    }

    override fun atAdd() {
        ShTrack.powderTracker.add(this)
    }

    companion object {
        fun fromJson(read: Map<String, JsonElement>): PowderTrackingElement =
            PowderTrackingElement(extractType(read), read["current"]?.asLong ?: 0, read["target"]?.asLong)

        private fun extractType(read: Map<String, JsonElement>) = HotmAPI.PowderType.getValue(read["name"]!!.asString)!!
    }

}
