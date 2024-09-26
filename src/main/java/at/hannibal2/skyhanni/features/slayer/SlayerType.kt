package at.hannibal2.skyhanni.features.slayer

import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityWolf

enum class SlayerType(val displayName: String, val rngName: String, val clazz: Class<*>) {
    REVENANT("Revenant Horror", "revenant", EntityZombie::class.java),
    TARANTULA("Tarantula Broodfather", "tarantula", EntitySpider::class.java),
    SVEN("Sven Packmaster", "sven", EntityWolf::class.java),
    VOID("Voidgloom Seraph", "voidgloom", EntityEnderman::class.java),
    INFERNO("Inferno Demonlord", "inferno", EntityBlaze::class.java),
    VAMPIRE("Bloodfiend", "vampire", EntityZombie::class.java) // previously called "Riftstalker Bloodfiend"
    ;

    companion object {
        fun getByName(name: String): SlayerType? = entries.firstOrNull {name.contains(it.displayName)}
    }
}
