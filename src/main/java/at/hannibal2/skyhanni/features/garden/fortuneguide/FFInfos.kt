package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.utils.GuiRenderUtils

internal enum class FFInfos(
    val sumTo: FFInfos?,
    private val currentF: () -> Number,
    private val maxF: (FFInfos) -> Number,
) {
    UNIVERSAL(
        null, { FFStats.totalBaseFF }, FFTypes.TOTAL,
        {
            val backupArmor = FarmingItems.currentArmor
            val backupEquip = FarmingItems.currentEquip
            FarmingItems.currentArmor = null
            FarmingItems.currentEquip = null
            val total = maxSumToThis(it)
            FarmingItems.currentArmor = backupArmor
            FarmingItems.currentEquip = backupEquip
            total
        },
    ),
    FARMING_LEVEL(UNIVERSAL, { FFStats.baseFF }, FFTypes.FARMING_LVL, 240),
    BESTIARY(UNIVERSAL, { FFStats.baseFF }, FFTypes.BESTIARY, 60),
    GARDEN_PLOTS(UNIVERSAL, { FFStats.baseFF }, FFTypes.PLOTS, 72),
    ANITA_BUFF(UNIVERSAL, { FFStats.baseFF }, FFTypes.ANITA, 60),
    COMMUNITY_SHOP(UNIVERSAL, { FFStats.baseFF }, FFTypes.COMMUNITY_SHOP, 40),
    CAKE_BUFF(UNIVERSAL, { FFStats.baseFF }, FFTypes.CAKE, 5),
    TOTAL_ARMOR(UNIVERSAL, { FarmingItems.currentArmor?.getFFData() ?: FFStats.armorTotalFF }, FFTypes.TOTAL),
    BASE_ARMOR(
        TOTAL_ARMOR, { FarmingItems.currentArmor?.getFFData() ?: FFStats.armorTotalFF }, FFTypes.BASE,
        {
            when (FarmingItems.currentArmor) {
                FarmingItems.HELMET -> 30
                FarmingItems.CHESTPLATE, FarmingItems.LEGGINGS -> 35
                FarmingItems.BOOTS -> if (FFStats.usingSpeedBoots) 60 else 30
                else -> if (FFStats.usingSpeedBoots) 160 else 130
            }
        },
    ),
    ABILITY_ARMOR(
        TOTAL_ARMOR, { FarmingItems.currentArmor?.getFFData() ?: FFStats.armorTotalFF }, FFTypes.ABILITY,
        {
            when (FarmingItems.currentArmor) {
                FarmingItems.HELMET, FarmingItems.CHESTPLATE, FarmingItems.LEGGINGS -> if (FFStats.usingSpeedBoots) 16.667 else 18.75
                FarmingItems.BOOTS -> if (FFStats.usingSpeedBoots) 0 else 18.75
                else -> if (FFStats.usingSpeedBoots) 50 else 75
            }
        },
    ),
    REFORGE_ARMOR(
        TOTAL_ARMOR, { FarmingItems.currentArmor?.getFFData() ?: FFStats.armorTotalFF }, FFTypes.REFORGE,
        {
            when (FarmingItems.currentArmor) {
                FarmingItems.HELMET, FarmingItems.CHESTPLATE, FarmingItems.LEGGINGS -> 30
                FarmingItems.BOOTS -> if (FFStats.usingSpeedBoots) 25 else 30
                else -> if (FFStats.usingSpeedBoots) 115 else 120
            }
        },
    ),
    ENCHANT_ARMOR(
        sumTo = TOTAL_ARMOR,
        from = { FarmingItems.currentArmor?.getFFData() ?: FFStats.armorTotalFF },
        what = FFTypes.ENCHANT,
        x4 = { FarmingItems.currentArmor == null },
        max = 5,
    ),
    GEMSTONE_ARMOR(
        TOTAL_ARMOR, { FarmingItems.currentArmor?.getFFData() ?: FFStats.armorTotalFF }, FFTypes.GEMSTONE,
        {
            when (FarmingItems.currentArmor) {
                FarmingItems.HELMET, FarmingItems.CHESTPLATE, FarmingItems.LEGGINGS -> 20
                FarmingItems.BOOTS -> if (FFStats.usingSpeedBoots) 16 else 20
                else -> if (FFStats.usingSpeedBoots) 76 else 80
            }
        },
    ),
    TOTAL_PET(UNIVERSAL, { FarmingItems.currentPet.getFFData() }, FFTypes.TOTAL),
    PET_BASE(
        TOTAL_PET, { FarmingItems.currentPet.getFFData() }, FFTypes.BASE,
        {
            when (FarmingItems.currentPet) {
                FarmingItems.ELEPHANT -> 150
                FarmingItems.MOOSHROOM_COW -> 158
                FarmingItems.BEE -> 30
                FarmingItems.SLUG -> 100
                else -> 0
            }
        },
    ),
    PET_ITEM(TOTAL_PET, { FarmingItems.currentPet.getFFData() }, FFTypes.PET_ITEM, 60),
    TOTAL_EQUIP(
        sumTo = UNIVERSAL,
        from = { FarmingItems.currentEquip?.getFFData() ?: FFStats.equipmentTotalFF },
        what = FFTypes.TOTAL,
    ),
    BASE_EQUIP(
        sumTo = TOTAL_EQUIP,
        from = { FarmingItems.currentEquip?.getFFData() ?: FFStats.equipmentTotalFF },
        what = FFTypes.BASE,
        x4 = { FarmingItems.currentEquip == null },
        max = 5.0,
    ),
    ABILITY_EQUIP(
        sumTo = TOTAL_EQUIP,
        from = { FarmingItems.currentEquip?.getFFData() ?: FFStats.equipmentTotalFF },
        what = FFTypes.ABILITY,
        x4 = { FarmingItems.currentEquip == null },
        max = 15.0,
    ),
    REFORGE_EQUIP(
        sumTo = TOTAL_EQUIP,
        from = { FarmingItems.currentEquip?.getFFData() ?: FFStats.equipmentTotalFF },
        what = FFTypes.REFORGE,
        x4 = { FarmingItems.currentEquip == null },
        max = 15.0,
    ),
    ENCHANT_EQUIP(
        sumTo = TOTAL_EQUIP,
        from = { FarmingItems.currentEquip?.getFFData() ?: FFStats.equipmentTotalFF },
        what = FFTypes.ENCHANT,
        x4 = { FarmingItems.currentEquip == null },
        max = { at.hannibal2.skyhanni.features.garden.GardenAPI.totalAmountVisitorsExisting.toDouble() / 4.0 },
    ),
    ;

    val current get() = currentF().toDouble()
    val max get() = maxF(this).toDouble()

    fun bar(label: String, tooltip: String, width: Int = 90) =
        GuiRenderUtils.getFarmingBar(label, tooltip, current, max, width)

    constructor(
        sumTo: FFInfos?,
        current: () -> Number,
        max: Number,
    ) : this(sumTo, current, { max })

    constructor(
        sumTo: FFInfos?,
        from: () -> Map<FFTypes, Double>,
        what: FFTypes,
        max: Number,
    ) : this(sumTo, { from()[what] ?: 0.0 }, { max })

    constructor(
        sumTo: FFInfos?,
        from: () -> Map<FFTypes, Double>,
        what: FFTypes,
        x4: () -> Boolean,
        max: Number,
    ) : this(sumTo, { from()[what] ?: 0.0 }, { if (x4()) max.toDouble() * 4 else max })

    constructor(
        sumTo: FFInfos?,
        from: () -> Map<FFTypes, Double>,
        what: FFTypes,
        x4: () -> Boolean,
        max: () -> Number,
    ) : this(sumTo, { from()[what] ?: 0.0 }, { if (x4()) max().toDouble() * 4 else max() })

    constructor(
        sumTo: FFInfos?,
        from: () -> Map<FFTypes, Double>,
        what: FFTypes,
        max: (FFInfos) -> Number,
    ) : this(sumTo, { from()[what] ?: 0.0 }, max)

    constructor(
        sumTo: FFInfos?,
        from: () -> Map<FFTypes, Double>,
        what: FFTypes,
    ) : this(sumTo, { from()[what] ?: 0.0 }, ::maxSumToThis)
}

private fun maxSumToThis(self: FFInfos): Double = FFInfos.entries.filter { it.sumTo == self }.sumOf { it.max }
