package at.hannibal2.skyhanni.features.mining.fossilexcavator.solver

data class FossilTile(val x: Int, val y: Int) {

    constructor(slotIndex: Int) : this(slotIndex % 9, slotIndex / 9)

    fun toSlotIndex() = x + y * 9
}
