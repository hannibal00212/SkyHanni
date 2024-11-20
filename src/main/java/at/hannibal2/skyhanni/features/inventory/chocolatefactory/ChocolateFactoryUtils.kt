package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats
import at.hannibal2.skyhanni.utils.CollectionUtils.getOrNull
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

object ChocolateFactoryUtils {
    /** Calculates the upgrade Cost for the upgrade at [slotIndex] with [level].
     *
     * @param slotIndex the slot index of the upgrade
     * @param level the level of the upgrade
     *
     * @return the cost of the upgrade || null if the upgrade is maxed.
     */
    fun getUpgradeCost(slotIndex: Int, level: Int): Long? {
        var price: Long? = null
        val maxLevel = ChocolateFactoryAPI.maxUpgradeLevelPerPrestige[slotIndex]?.getOrNull(ChocolateFactoryAPI.currentPrestige - 1) ?: 0
        val dif: Int = maxLevel - level

        if (dif > 0 || -dif < HoppityCollectionStats.getDivineRabbitCount()) {

            val prestigeMultiplier = 1 + (ChocolateFactoryAPI.upgradeCostFormulaConstants[slotIndex]?.get("prestige") ?: 0.0) *
                (ChocolateFactoryAPI.currentPrestige - 1)
            val otherMultipliers = if (HoppityCollectionStats.hasFoundRabbit("Zeta")) 0.99 else 1.0

            // Use upgrade cost per level if it exists, otherwise use the formula.
            if ((ChocolateFactoryAPI.upgradeCostPerLevel[slotIndex]?.size ?: 0) > level) {
                val nextRaw = ChocolateFactoryAPI.upgradeCostPerLevel[slotIndex]?.get(level) ?: 0

                price = floor(nextRaw * prestigeMultiplier * otherMultipliers).toLong()
            } else {
                val base = ChocolateFactoryAPI.upgradeCostFormulaConstants[slotIndex]?.get("base") ?: 0.0
                val multiplier = ChocolateFactoryAPI.upgradeCostFormulaConstants[slotIndex]?.get("exp") ?: 0.0

                price = (floor(round(base * multiplier.pow(level)) * prestigeMultiplier) * otherMultipliers).toLong()
            }
        }
        return price
    }

    /** Constructs the rabbit with level = 0 after [upgrade] if upgrade is a rabbit.
     *  Does not recalculate [ChocolateFactoryUpgrade.effectiveCost] and [ChocolateFactoryUpgrade.extraPerSecond] but
     *  instead just copies it from [upgrade]
     *
     * @param upgrade the current upgrade
     */
    fun getNextUnlockedRabbitWorker(upgrade: ChocolateFactoryUpgrade): ChocolateFactoryUpgrade? {
        val nextSlot = upgrade.slotIndex + 1
        if (nextSlot !in ChocolateFactoryAPI.rabbitSlots) return null

        return ChocolateFactoryUpgrade(
            slotIndex = nextSlot,
            level = 0,
            price = getUpgradeCost(nextSlot, 0),
            extraPerSecond = 0.0,
            effectiveCost = 0.0,
            isRabbit = true,
        )
    }

    /** Constructs the next upgrade after [upgrade].
     *  Does not recalculate [ChocolateFactoryUpgrade.effectiveCost] and [ChocolateFactoryUpgrade.extraPerSecond] but
     *  instead just copies it from [upgrade]
     *
     * @param upgrade the current upgrade
     */
    fun getNextUpgrade(upgrade: ChocolateFactoryUpgrade): ChocolateFactoryUpgrade =
        upgrade.copy(level = upgrade.level + 1, price = getUpgradeCost(upgrade.slotIndex, upgrade.level + 1))
}
