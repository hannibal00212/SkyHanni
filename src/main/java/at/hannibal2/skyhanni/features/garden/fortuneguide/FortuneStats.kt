package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.utils.CollectionUtils.sumOfPair

enum class FortuneStats(private val label0: () -> String, private val tooltip0: () -> String) {
    BASE(
        "§2Universal Farming Fortune",
        "§7§2Farming fortune in that is\n§2applied to every crop\n§eNot the same as tab FF\n" + "§eSee on the grass block page"
    ),
    CROP_TOTAL("§6Crop Farming Fortune", "§7§2Farming fortune for this crop"),
    ACCESSORY("§2Talisman Bonus", "§7§2Fortune from your talisman\n§2You get 10☘ per talisman tier"),
    CROP_UPGRADE("§2Crop Upgrade", "§7§2Fortune from Desk crop upgrades\n§2You get 5☘ per level"),
    BASE_TOOL("§2Base tool fortune", "§7§2Crop specific fortune from your tool"),
    REFORGE("§2Tool reforge", "§7§2Fortune from reforging your tool"),
    GEMSTONE("§2Tool gemstone", "§7§2Fortune from gemstones on your tool"),
    FFD("§2Farming for Dummies", "§7§2Fortune for each applied book\n§2You get 1☘ per applied book"),
    COUNTER("§2Logarithmic Counter", "§7§2Fortune from increasing crop counter\n§2You get 16☘ per digit - 4"),
    COLLECTION("§2Collection Analyst", "§7§2Fortune from increasing crop collection\n§2You get 8☘ per digit - 4"),
    HARVESTING("§2Harvesting Enchantment", "§7§2Fortune for each enchantment level\n§2You get 12.5☘ per level"),
    SUNDER("§2Sunder Enchantment", "§7§2Fortune for each enchantment level\n§2You get 12.5☘ per level"),
    CULTIVATING("§2Cultivating Enchantment", "§7§2Fortune for each enchantment level\n§2You get 2☘ per level"),
    TURBO("§2Turbo-Crop Enchantment", "§7§2Fortune for each enchantment level\n§2You get 5☘ per level"),
    DEDICATION("§2Dedication Enchantment", "§7§2Fortune for each enchantment level\n§2and crop milestone"),
    CARROLYN(::carrolynLabel, {
        "§7§2Gain 12☘ from giving 3,000\n to Carrolyn in Scarleton!\n §eRun /shcarrolyn ${
            FFGuideGUI.currentCrop?.niceName
        } to toggle the stat"
    }),
    ;

    constructor(label: String, tooltip: String) : this({ label }, { tooltip })

    var current: Double = 0.0
    var max: Double = -1.0

    val label get() = label0()
    val tooltip get() = tooltip0()

    fun reset() {
        current = 0.0
        max = -1.0
    }

    fun set(value: Pair<Double, Double>) {
        current = value.first
        max = value.second
    }

    fun set(current: Double, max: Double) {
        this.current = current
        this.max = max
    }

    fun isActive() = max != -1.0

    companion object {

        fun getTotal(): Pair<Double, Double> = entries.filter { it.isActive() }.sumOfPair { it.current to it.max }

        fun reset() = entries.forEach { it.reset() }
    }
}

private fun carrolynLabel(): String =
    CarrolynTable.getByCrop(FFGuideGUI.currentCrop)?.label?.let { "§2$it" } ?: "§cError"
