package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.gui.GuiScreen
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack

enum class FarmingItems(
    val itemCategory: ItemCategory,
    private val ffCalculation: (ItemStack?) -> Map<FFTypes, Double> = { emptyMap() },
) {
    WHEAT(ItemCategory.HOE),
    SEEDS(ItemCategory.HOE),
    CARROT(ItemCategory.HOE),
    POTATO(ItemCategory.HOE),
    NETHER_WART(ItemCategory.HOE),
    PUMPKIN(ItemCategory.AXE),
    MELON(ItemCategory.AXE),
    COCOA_BEANS(ItemCategory.AXE),
    SUGAR_CANE(ItemCategory.HOE),
    CACTUS(ItemCategory.HOE),
    MUSHROOM(ItemCategory.HOE),
    HELMET(ItemCategory.HELMET, FFStats::getArmorFFData),
    CHESTPLATE(ItemCategory.CHESTPLATE, FFStats::getArmorFFData),
    LEGGINGS(ItemCategory.LEGGINGS, FFStats::getArmorFFData),
    BOOTS(ItemCategory.BOOTS, FFStats::getArmorFFData),
    NECKLACE(ItemCategory.NECKLACE, FFStats::getEquipmentFFData),
    CLOAK(ItemCategory.CLOAK, FFStats::getEquipmentFFData),
    BELT(ItemCategory.BELT, FFStats::getEquipmentFFData),
    BRACELET(ItemCategory.BRACELET, FFStats::getEquipmentFFData),
    ELEPHANT(ItemCategory.PET, FFStats::getPetFFData),
    MOOSHROOM_COW(ItemCategory.PET, FFStats::getPetFFData),
    RABBIT(ItemCategory.PET, FFStats::getPetFFData),
    BEE(ItemCategory.PET, FFStats::getPetFFData),
    SLUG(ItemCategory.PET, FFStats::getPetFFData),
    ;

    var selectedState = false

    fun getItem() = getItemOrNull() ?: fallbackItem

    private val fallbackItem: ItemStack by lazy {
        val name = "§cNo saved ${name.lowercase().replace("_", " ")}"
        ItemStack(Blocks.barrier).setStackDisplayName(name)
    }

    fun getItemOrNull() = ProfileStorageData.profileSpecific?.garden?.fortune?.farmingItems?.get(this)
    fun setItem(value: ItemStack) = ProfileStorageData.profileSpecific?.garden?.fortune?.farmingItems?.set(this, value)

    private fun onClick(): () -> Unit = when (this) {
        in armor -> {
            {
                SoundUtils.playClickSound()
                currentArmor = if (selectedState) null else this
                armor.forEach {
                    it.selectedState = it == currentArmor
                }
                FFGuideGUI.updateDisplay()
            }
        }

        in equip -> {
            {
                SoundUtils.playClickSound()
                currentEquip = if (selectedState) null else this
                equip.forEach {
                    it.selectedState = it == currentEquip
                }
                FFGuideGUI.updateDisplay()
            }
        }

        in pets -> {
            {
                val prev = currentPet
                currentPet = if (selectedState) lastEquippedPet else this
                if (prev != currentPet) {
                    SoundUtils.playClickSound()
                }
                pets.forEach {
                    it.selectedState = it == currentPet
                }
                FFStats.getTotalFF()
            }
        }

        else -> {
            {}
        }
    }

    fun getDisplay(clickEnabled: Boolean = false) = object : Renderable {

        val content = Renderable.clickable(
            Renderable.itemStackWithTip(
                getItem(), 1.0, 0, 0, false,
            ),
            onClick = onClick(),
            condition = { clickEnabled },
        )

        override val width = content.width
        override val height = content.height
        override val horizontalAlign = RenderUtils.HorizontalAlignment.CENTER
        override val verticalAlign = RenderUtils.VerticalAlignment.CENTER

        override fun render(posX: Int, posY: Int) {
            GuiScreen.drawRect(
                0,
                0,
                width,
                height,
                if (selectedState) 0xFFB3FFB3.toInt() else 0xFF43464B.toInt(),
            )
            content.render(posX, posY)
        }
    }

    private var ffData: Map<FFTypes, Double>? = null

    fun getFFData() = ffData ?: run {
        val data = ffCalculation(getItemOrNull())
        ffData = data
        data
    }

    companion object {

        // TODO
        var lastEquippedPet = ELEPHANT

        var currentPet: FarmingItems = lastEquippedPet
        var currentArmor: FarmingItems? = null
        var currentEquip: FarmingItems? = null

        val armor = listOf(HELMET, CHESTPLATE, LEGGINGS, BOOTS)
        val equip = listOf(NECKLACE, CLOAK, BELT, BRACELET)
        val pets = listOf(ELEPHANT, MOOSHROOM_COW, RABBIT, BEE, SLUG)

        fun getArmorDisplay(clickEnabled: Boolean = false): List<Renderable> = armor.map { it.getDisplay(clickEnabled) }

        fun getEquipmentDisplay(clickEnabled: Boolean = false): List<Renderable> = equip.map { it.getDisplay(clickEnabled) }

        fun getPetsDisplay(clickEnabled: Boolean = false): List<Renderable> = pets.map { it.getDisplay(clickEnabled) }

        fun resetClickState() {
            entries.filterNot { pets.contains(it) }.forEach { it.selectedState = false }
        }

        fun resetFFData() {
            entries.forEach { it.ffData = null }
        }

        fun setDefaultPet(): FarmingItems {
            currentPet = lastEquippedPet
            pets.forEach {
                it.selectedState = it == currentPet
            }
            return lastEquippedPet
        }

        fun getFromItemCategory(category: ItemCategory) = entries.filter { it.itemCategory == category }
        fun getFromItemCategoryOne(category: ItemCategory) = entries.firstOrNull { it.itemCategory == category }
    }
}
