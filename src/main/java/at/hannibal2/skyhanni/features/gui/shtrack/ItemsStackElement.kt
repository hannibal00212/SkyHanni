package at.hannibal2.skyhanni.features.gui.shtrack

import at.hannibal2.skyhanni.api.CollectionAPI.getMultipleMap
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.google.gson.JsonElement

class ItemsStackElement(
    val main: NEUInternalName,
    override var current: Long,
    override val target: Long?,
    override val includeSack: Boolean,
) : ItemTrackingInterface() {

    override val name get() = main.itemName
    override val saveName = main.asString()

    val map = NEUItems.getPrimitiveMultiplier(main).internalName.getMultipleMap()

    private val mappedCurrent get() = map[main]?.let { current.div(it) } ?: current

    override fun similarElement(other: TrackingElement<*>): Boolean {
        if (other !is ItemsStackElement) return false
        return other.main == this.main
    }

    override fun atRemove() {
        for (item in map.keys) {
            ShTrack.itemTrackers[item]?.remove(this)
        }
    }

    override fun atAdd() {
        for (item in map.keys) {
            ShTrack.itemTrackers.compute(item) { _, v ->
                v?.also { it.add(this) } ?: mutableListOf(this)
            }
        }
    }

    override fun internalUpdate(amount: Number) {
        current += amount.toLong()
        if (target != null && mappedCurrent >= target) {
            handleDone("$name §adone")
        }
    }

    override fun generateLine() = listOf(
        Renderable.itemStack(main.getItemStack()),
        Renderable.string(main.itemName),

        Renderable.string(mappedCurrent.toString() + ((target?.let { " / $it" }).orEmpty())),
    )

    override fun itemChange(item: PrimitiveItemStack) {
        val multiple = map[item.internalName] ?: throw IllegalStateException("You should not be here!")
        update(item.amount * multiple)
    }

    companion object {

        fun fromJson(read: Map<String, JsonElement>): ItemsStackElement = ItemsStackElement(
            read["name"]!!.asString.toInternalName(),
            read["current"]?.asLong ?: 0,
            read["target"]?.asLong,
            read["sack"]?.asBoolean ?: false,
        )
    }
}
