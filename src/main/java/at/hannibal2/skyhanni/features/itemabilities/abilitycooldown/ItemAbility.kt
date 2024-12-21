package at.hannibal2.skyhanni.features.itemabilities.abilitycooldown

import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import kotlin.math.floor

enum class ItemAbility(
    val abilityName: String,
    private val cooldownInSeconds: Int,
    vararg val itemNames: String,
    val alternativePosition: Boolean = false,
    var lastActivation: Long = 0L,
    var specialColor: LorenzColor? = null,
    var lastItemClick: Long = 0L,
    val actionBarDetection: Boolean = true,
    private val ignoreMageCooldownReduction: Boolean = false,
) {
    // TODO add into repo

    HYPERION(5, "SCYLLA", "VALKYRIE", "ASTRAEA", ignoreMageCooldownReduction = true),
    GYROKINETIC_WAND_LEFT(30, "GYROKINETIC_WAND", alternativePosition = true),
    GYROKINETIC_WAND_RIGHT(10, "GYROKINETIC_WAND"),
    GIANTS_SWORD(30),
    ICE_SPRAY_WAND(5),
    ATOMSPLIT_KATANA(4, "VORPAL_KATANA", "VOIDEDGE_KATANA", ignoreMageCooldownReduction = true),
    RAGNAROCK_AXE(20),
    WAND_OF_ATONEMENT(7, "WAND_OF_HEALING", "WAND_OF_MENDING", "WAND_OF_RESTORATION"),
    SOS_FLARE(10),
    ALERT_FLARE(20, "WARNING_FLARE"),

    GOLEM_SWORD(3),
    END_STONE_SWORD(5),
    SOUL_ESOWARD(20),
    PIGMAN_SWORD(5),
    EMBER_ROD(30),
    STAFF_OF_THE_VOLCANO(30),
    STARLIGHT_WAND(2),
    VOODOO_DOLL(5),
    WEIRD_TUBA(20),
    WEIRDER_TUBA(30),
    FIRE_FREEZE_STAFF(10),
    SWORD_OF_BAD_HEALTH(5),
    WITHER_CLOAK(10),
    HOLY_ICE(4),
    VOODOO_DOLL_WILTED(3),
    FIRE_FURY_STAFF(20),
    SHADOW_FURY(15, "STARRED_SHADOW_FURY"),
    ROYAL_PIGEON(5),
    WAND_OF_STRENGTH(10),
    TACTICAL_INSERTION(20),
    TOTEM_OF_CORRUPTION(20),
    ENRAGER(20),

    // doesn't have a sound
    ENDER_BOW("Ender Warp", 5, "Ender Bow"),
    LIVID_DAGGER("Throw", 5, "Livid Dagger"),
    FIRE_VEIL("Fire Veil", 5, "Fire Veil Wand"),
    INK_WAND("Ink Bomb", 30, "Ink Wand"),
    ROGUE_SWORD("Speed Boost", 30, "Rogue Sword", ignoreMageCooldownReduction = true),
    TALBOTS_THEODOLITE("Track", 10, "Talbot's Theodolite"),

    // doesn't have a consistent sound
    ECHO("Echo", 3, "Ancestral Spade");

    var newVariant = false
    var internalNames = mutableListOf<NEUInternalName>()

    constructor(
        cooldownInSeconds: Int,
        vararg alternateInternalNames: String,
        alternativePosition: Boolean = false,
        ignoreMageCooldownReduction: Boolean = false,
    ) : this(
        "no name",
        cooldownInSeconds,
        actionBarDetection = false,
        alternativePosition = alternativePosition,
        ignoreMageCooldownReduction = ignoreMageCooldownReduction,
    ) {
        newVariant = true
        alternateInternalNames.forEach {
            internalNames.add(it.toInternalName())
        }
        internalNames.add(name.toInternalName())
    }

    fun activate(color: LorenzColor? = null, customCooldown: Int = (cooldownInSeconds * 1000)) {
        specialColor = color
        lastActivation = System.currentTimeMillis() - ((cooldownInSeconds * 1000) - customCooldown)
    }

    fun isOnCooldown(): Boolean = lastActivation + getCooldown() > System.currentTimeMillis()

    fun getCooldown(): Long {
        // Some items aren't really a cooldown but an effect over time, so don't apply cooldown multipliers
        if (this == WAND_OF_ATONEMENT || this == RAGNAROCK_AXE) {
            return 1000L * cooldownInSeconds
        }

        return (1000L * cooldownInSeconds * getMultiplier()).toLong()
    }

    fun getDurationText(): String {
        var duration: Long = lastActivation + getCooldown() - System.currentTimeMillis()
        return if (duration < 1600) {
            duration /= 100
            var d = duration.toDouble()
            d /= 10.0
            d.roundTo(1).addSeparators()
        } else {
            duration /= 1000
            duration++
            duration.addSeparators()
        }
    }

    fun setItemClick() {
        lastItemClick = System.currentTimeMillis()
    }

    companion object {

        fun getByInternalName(internalName: NEUInternalName): ItemAbility? {
            return entries.firstOrNull { it.newVariant && internalName in it.internalNames }
        }

        fun ItemAbility.getMultiplier(): Double {
            return getMageCooldownReduction() ?: 1.0
        }

        private fun ItemAbility.getMageCooldownReduction(): Double? {
            if (ignoreMageCooldownReduction) return null
            if (!DungeonAPI.inDungeon()) return null
            if (DungeonAPI.playerClass != DungeonAPI.DungeonClass.MAGE) return null

            var abilityCooldownMultiplier = 1.0
            abilityCooldownMultiplier -= if (DungeonAPI.isUniqueClass) {
                0.5 // 50% base reduction at level 0
            } else {
                0.25 // 25% base reduction at level 0
            }

            // 1% ability reduction every other level
            abilityCooldownMultiplier -= 0.01 * floor(DungeonAPI.playerClassLevel / 2f)

            return abilityCooldownMultiplier
        }
    }
}
