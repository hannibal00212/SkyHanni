package at.hannibal2.skyhanni.features.gui.shtrack

import at.hannibal2.skyhanni.utils.CommandUtils
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.renderables.Renderable

// TODO remove
@Deprecated("Not needed anymore")
class ItemGroupElement(
    val group: CommandUtils.ItemGroup,
    override var current: Long,
    override val target: Long?,
    override val includeSack: Boolean,
) : ItemTrackingInterface() {

    override val name = group.name
    override val saveName = group.name

    override fun similarElement(other: TrackingElement<*>): Boolean {
        if (other !is ItemGroupElement) return false
        return other.group == this.group
    }

    override fun atRemove() {
        for (item in group.items.keys) {
            ShTrack.itemTrackers[item]?.remove(this)
        }
    }

    override fun atAdd() {
        for (item in group.items.keys) {
            ShTrack.itemTrackers.compute(item) { _, v ->
                v?.also { it.add(this) } ?: mutableListOf(this)
            }
        }
    }

    override fun internalUpdate(amount: Number) {
        current += amount.toLong()
        if (target != null && current >= target) {
            handleDone("${group.name} §adone")
        }
    }

    override fun generateLine() = listOf(
        Renderable.itemStack(group.icon.getItemStack()),
        Renderable.string(group.name),
        Renderable.string(current.toString() + ((target?.let { " / $it" }).orEmpty())),
    )

    override fun itemChange(item: PrimitiveItemStack) {
        val multiple = group.items[item.internalName] ?: throw IllegalStateException("You should not be here!")
        update(item.amount * multiple)
    }
}
