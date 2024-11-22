package at.hannibal2.skyhanni.features.slayer

import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityWolf

enum class SlayerType(
    val displayName: String,
    val rngName: String,
    val clazz: Class<*>,
    val miniBossType: SlayerMiniBossFeatures.SlayerMiniBossType? = null,
) {
    REVENANT(
        "Revenant Horror",
        "revenant",
        EntityZombie::class.java,
        SlayerMiniBossFeatures.SlayerMiniBossType.REVENANT,
    ),
    TARANTULA(
        "Tarantula Broodfather",
        "tarantula",
        EntitySpider::class.java,
        SlayerMiniBossFeatures.SlayerMiniBossType.TARANTULA,
    ),
    SVEN(
        "Sven Packmaster",
        "sven",
        EntityWolf::class.java,
        SlayerMiniBossFeatures.SlayerMiniBossType.SVEN,
    ),
    VOID(
        "Voidgloom Seraph",
        "voidgloom",
        EntityEnderman::class.java,
        SlayerMiniBossFeatures.SlayerMiniBossType.VOIDLING,
    ),
    INFERNO(
        "Inferno Demonlord",
        "inferno",
        EntityBlaze::class.java,
        SlayerMiniBossFeatures.SlayerMiniBossType.INFERNAL,
    ),
    VAMPIRE(
        "Bloodfiend",
        "vampire",
        EntityZombie::class.java,
    ) // previously called "Riftstalker Bloodfiend"
    ;

    companion object {
        fun getByName(name: String): SlayerType? = entries.firstOrNull { name.contains(it.displayName) }
    }
}
