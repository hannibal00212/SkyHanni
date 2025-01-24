package at.hannibal2.skyhanni.features.inventory.wardrobe

class WardrobeSlot(
    val id: Int,
    val page: Int,
    val inventorySlot: Int,
    val helmetSlot: Int,
    val chestplateSlot: Int,
    val leggingsSlot: Int,
    val bootsSlot: Int,
) {
    fun getData() = WardrobeAPI.storage?.data?.getOrPut(id) {
        WardrobeAPI.WardrobeData(
            id,
            armor = WardrobeAPI.emptyArmor(),
            locked = true,
            favorite = false,
        )
    }

    var locked: Boolean
        get() = getData()?.locked ?: true
        set(value) {
            getData()?.locked = value
        }

    var favorite: Boolean
        get() = getData()?.favorite ?: false
        set(value) {
            getData()?.favorite = value
        }

    val armor get() = getData()?.armor ?: WardrobeAPI.emptyArmor()

    val inventorySlots = listOf(helmetSlot, chestplateSlot, leggingsSlot, bootsSlot)

    fun isEmpty(): Boolean = armor.all { it == null }

    fun isCurrentSlot() = getData()?.id == WardrobeAPI.currentSlot

    fun isInCurrentPage() = (WardrobeAPI.currentPage == null && page == 1) || (page == WardrobeAPI.currentPage)
}
