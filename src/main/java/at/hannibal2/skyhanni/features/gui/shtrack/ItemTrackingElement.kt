package at.hannibal2.skyhanni.features.gui.shtrack

import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.google.gson.JsonElement

class ItemTrackingElement(
    val item: NEUInternalName,
    override var current: Long,
    override val target: Long?,
    override val includeSack: Boolean,
) : ItemTrackingInterface() {

    override val name get() = item.itemName
    override val saveName = item.asString()

    override fun similarElement(other: TrackingElement<*>): Boolean {
        if (other !is ItemTrackingElement) return false
        return other.item == this.item
    }

    override fun atRemove() {
        ShTrack.itemTrackers[item]?.remove(this)
    }

    override fun atAdd() {
        ShTrack.itemTrackers.compute(item) { _, v ->
            v?.also { it.add(this) } ?: mutableListOf(this)
        }
    }

    override fun internalUpdate(amount: Number) {
        current += amount.toLong()
        if (target != null && current >= target) {
            handleDone()
        }
    }

    override val icon: Renderable get() = Renderable.itemStack(item.getItemStack())

    override fun itemChange(item: PrimitiveItemStack) {
        update(item.amount.toLong())
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
