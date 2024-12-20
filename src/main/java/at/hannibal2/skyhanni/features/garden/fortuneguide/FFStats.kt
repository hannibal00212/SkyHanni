package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.data.CropAccessoryData
import at.hannibal2.skyhanni.data.GardenCropUpgrades.getUpgradeLevel
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.FarmingFortuneDisplay
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getFarmingForDummiesCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPetItem
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getPetLevel
import net.minecraft.item.ItemStack
import kotlin.math.floor

object FFStats {

    private val mathCrops by lazy {
        listOf(CropType.WHEAT, CropType.CARROT, CropType.POTATO, CropType.SUGAR_CANE, CropType.NETHER_WART)
    }
    private val dicerCrops by lazy { listOf(CropType.PUMPKIN, CropType.MELON) }

    private val farmingBoots = arrayListOf("RANCHERS_BOOTS", "FARMER_BOOTS")

    var cakeExpireTime
        get() = GardenAPI.storage?.fortune?.cakeExpiring ?: SimpleTimeMark.farPast()
        set(value) {
            GardenAPI.storage?.fortune?.cakeExpiring = value
        }

    var equipmentTotalFF = mapOf<FFTypes, Double>()

    var armorTotalFF = mapOf<FFTypes, Double>()
    var usingSpeedBoots = false

    var currentPetItem = ""

    var baseFF = mapOf<FFTypes, Double>()

    var totalBaseFF = mapOf<FFTypes, Double>()

    fun loadFFData() {
        equipmentTotalFF = FarmingItems.equip.getFFData()

        armorTotalFF = FarmingItems.armor.getFFData()
        usingSpeedBoots = FarmingItems.BOOTS.getItem().getInternalName().asString() in farmingBoots

        baseFF = getGenericFF()

        getTotalFF()
    }

    fun getCropStats(crop: CropType, tool: ItemStack?) {
        FortuneStats.reset()

        FortuneStats.BASE.set(FFInfos.UNIVERSAL.current, FFInfos.UNIVERSAL.max)
        FortuneStats.CROP_UPGRADE.set((crop.getUpgradeLevel()?.toDouble() ?: 0.0) * 5.0, 45.0)
        FortuneStats.ACCESSORY.set(CropAccessoryData.cropAccessory.getFortune(crop), 30.0)
        FortuneStats.FFD.set((tool?.getFarmingForDummiesCount() ?: 0).toDouble(), 5.0)
        FortuneStats.TURBO.set(FarmingFortuneDisplay.getTurboCropFortune(tool, crop), 25.0)
        FortuneStats.DEDICATION.set(FarmingFortuneDisplay.getDedicationFortune(tool, crop), 92.0)
        FortuneStats.CULTIVATING.set(FarmingFortuneDisplay.getCultivatingFortune(tool), 20.0)

        FarmingFortuneDisplay.loadFortuneLineData(tool, 0.0)

        when (crop) {
            in mathCrops -> {
                FortuneStats.BASE_TOOL.set(FarmingFortuneDisplay.getToolFortune(tool), 50.0)
                FortuneStats.COUNTER.set(FarmingFortuneDisplay.getCounterFortune(tool), 96.0)
                FortuneStats.HARVESTING.set(FarmingFortuneDisplay.getHarvestingFortune(tool), 75.0)
                FortuneStats.COLLECTION.set(FarmingFortuneDisplay.getCollectionFortune(tool), 48.0)
                FortuneStats.REFORGE.set(FarmingFortuneDisplay.reforgeFortune, 20.0)
                FortuneStats.GEMSTONE.set(FarmingFortuneDisplay.gemstoneFortune, 30.0)
            }

            in dicerCrops -> {
                FortuneStats.SUNDER.set(FarmingFortuneDisplay.getSunderFortune(tool), 75.0)
                FortuneStats.REFORGE.set(FarmingFortuneDisplay.reforgeFortune, 20.0)
                FortuneStats.GEMSTONE.set(FarmingFortuneDisplay.gemstoneFortune, 30.0)
            }

            CropType.MUSHROOM -> {
                FortuneStats.BASE_TOOL.set(FarmingFortuneDisplay.getToolFortune(tool), 30.0)
                FortuneStats.HARVESTING.set(FarmingFortuneDisplay.getHarvestingFortune(tool), 75.0)
                FortuneStats.REFORGE.set(FarmingFortuneDisplay.reforgeFortune, 16.0)
                FortuneStats.GEMSTONE.set(FarmingFortuneDisplay.gemstoneFortune, 16.0)
            }

            CropType.COCOA_BEANS -> {
                FortuneStats.BASE_TOOL.set(FarmingFortuneDisplay.getToolFortune(tool), 20.0)
                FortuneStats.SUNDER.set(FarmingFortuneDisplay.getSunderFortune(tool), 75.0)
                FortuneStats.REFORGE.set(FarmingFortuneDisplay.reforgeFortune, 16.0)
                FortuneStats.GEMSTONE.set(FarmingFortuneDisplay.gemstoneFortune, 16.0)
            }

            CropType.CACTUS -> {
                FortuneStats.HARVESTING.set(FarmingFortuneDisplay.getHarvestingFortune(tool), 75.0)
                FortuneStats.REFORGE.set(FarmingFortuneDisplay.reforgeFortune, 16.0)
                FortuneStats.GEMSTONE.set(FarmingFortuneDisplay.gemstoneFortune, 16.0)
            }

            else -> {}
        }
        CarrolynTable.getByCrop(crop)?.let {
            val ff = if (it.get()) 12.0 else 0.0
            FortuneStats.CARROLYN.set(ff, 12.0)
        }

        FortuneStats.CROP_TOTAL.set(FortuneStats.getTotal())
    }

    fun getEquipmentFFData(item: ItemStack?): Map<FFTypes, Double> = buildMap {
        FarmingFortuneDisplay.loadFortuneLineData(item, 0.0)
        this[FFTypes.BASE] = FarmingFortuneDisplay.itemBaseFortune
        this[FFTypes.REFORGE] = FarmingFortuneDisplay.reforgeFortune
        this[FFTypes.ENCHANT] = FarmingFortuneDisplay.greenThumbFortune
        this[FFTypes.ABILITY] = FarmingFortuneDisplay.getAbilityFortune(item)
        this[FFTypes.TOTAL] = this.values.sum()
    }

    fun getArmorFFData(item: ItemStack?): Map<FFTypes, Double> = buildMap {
        FarmingFortuneDisplay.loadFortuneLineData(item, 0.0)
        this[FFTypes.BASE] = FarmingFortuneDisplay.itemBaseFortune
        this[FFTypes.REFORGE] = FarmingFortuneDisplay.reforgeFortune
        this[FFTypes.GEMSTONE] = FarmingFortuneDisplay.gemstoneFortune
        this[FFTypes.ENCHANT] = FarmingFortuneDisplay.pesterminatorFortune
        this[FFTypes.ABILITY] = FarmingFortuneDisplay.getAbilityFortune(item)
        this[FFTypes.TOTAL] = this.values.sum()
    }

    fun getPetFFData(item: ItemStack?): Map<FFTypes, Double> = buildMap {
        val gardenLvl = GardenAPI.getGardenLevel(overflow = false)
        this[FFTypes.BASE] = getPetFF(item)
        this[FFTypes.PET_ITEM] = when (item?.getPetItem()) {
            "GREEN_BANDANA" -> 4.0 * gardenLvl
            "YELLOW_BANDANA" -> 30.0
            "MINOS_RELIC" -> (this[FFTypes.BASE] ?: 0.0) * .33
            else -> 0.0
        }
        this[FFTypes.TOTAL] = this.values.sum()
    }

    private fun getGenericFF(): Map<FFTypes, Double> = buildMap {
        val storage = GardenAPI.storage?.fortune ?: return emptyMap()
        this[FFTypes.FARMING_LVL] = storage.farmingLevel.toDouble() * 4
        this[FFTypes.BESTIARY] = storage.bestiary
        this[FFTypes.PLOTS] = storage.plotsUnlocked.toDouble() * 3
        this[FFTypes.ANITA] = storage.anitaUpgrade.toDouble() * 4
        this[FFTypes.COMMUNITY_SHOP] = (ProfileStorageData.playerSpecific?.gardenCommunityUpgrade ?: -1).toDouble() * 4
        if (cakeExpireTime.isInFuture() || cakeExpireTime.isFarPast()) {
            this[FFTypes.CAKE] = 5.0
        } else {
            this[FFTypes.CAKE] = 0.0
        }
        this[FFTypes.TOTAL] = this.values.sum()
    }

    fun getTotalFF() {
        currentPetItem = FarmingItems.currentPet.getItem().getPetItem().toString()

        totalBaseFF = combineFFData(
            baseFF, armorTotalFF, equipmentTotalFF, FarmingItems.currentPet.getFFData(),
        )

        FFGuideGUI.updateDisplay()
    }

    fun List<FarmingItems>.getFFData(): Map<FFTypes, Double> = combineFFData(this.map { it.getFFData() })

    fun combineFFData(vararg value: Map<FFTypes, Double>) = combineFFData(value.toList())
    fun combineFFData(value: List<Map<FFTypes, Double>>) =
        value.map { it.toList() }.flatten().groupBy({ it.first }, { it.second })
            .mapValues { (_, values) -> values.sum() }

    private fun getPetFF(pet: ItemStack?): Double {
        if (pet == null) return 0.0
        val petLevel = pet.getPetLevel()
        val strength = (GardenAPI.storage?.fortune?.farmingStrength)
        if (strength != null) {
            val rawInternalName = pet.getInternalName()
            return when {
                rawInternalName.contains("ELEPHANT;4") -> 1.5 * petLevel
                rawInternalName.contains("MOOSHROOM_COW;4") -> {
                    (10 + petLevel).toDouble() + floor(floor(strength / (40 - petLevel * .2)) * .7)
                }

                rawInternalName.contains("MOOSHROOM") -> (10 + petLevel).toDouble()
                rawInternalName.contains("BEE;2") -> 0.2 * petLevel
                rawInternalName.contains("BEE;3") || rawInternalName.contains("BEE;4") -> 0.3 * petLevel
                rawInternalName.contains("SLUG;4") -> 1.0 * petLevel
                else -> 0.0
            }
        }
        return 0.0
    }

}
