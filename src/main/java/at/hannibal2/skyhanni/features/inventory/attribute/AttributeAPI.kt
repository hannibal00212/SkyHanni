package at.hannibal2.skyhanni.features.inventory.attribute

import at.hannibal2.skyhanni.data.jsonobjects.repo.AttributeGoodRollsJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.equalsIgnoreOrder
import at.hannibal2.skyhanni.utils.CollectionUtils.toPair
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getAttributes
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

@SkyHanniModule
object AttributeAPI {

    private var goodRolls = listOf<GoodRollItem>()

    enum class AttributeType(val displayName: String, val internalName: String, val shortName: String) {
        ARACHNO("Arachno", "arachno", "AR"),
        ATTACK_SPEED("Attack Speed", "attack_speed", "AS"),
        BLAZING("Blazing", "blazing", "BL"),
        COMBO("Combo", "combo", "CO"),
        ELITE("Elite", "elite", "EL"),
        ENDER("Ender", "ender", "EN"),
        IGNITION("Ignition", "ignition", "IG"),
        LIFE_RECOVERY("Life Recovery", "life_recovery", "LR"),
        MANA_STEAL("Mana Steal", "mana_steal", "MS"),
        MIDAS_TOUCH("Midas Touch", "midas_touch", "MT"),
        UNDEAD("Undead", "undead", "UN"),
        WARRIOR("Warrior", "warrior", "WA"),
        DEADEYE("Deadeye", "deadeye", "DE"),
        ARACHNO_RESISTANCE("Arachno Resistance", "arachno_resistance", "AR"),
        BLAZING_RESISTANCE("Blazing Resistance", "blazing_resistance", "BR"),
        BREEZE("Breeze", "breeze", "BZ"),
        DOMINANCE("Dominance", "dominance", "DO"),
        ENDER_RESISTANCE("Ender Resistance", "ender_resistance", "ER"),
        EXPERIENCE("Experience", "experience", "EX"),
        FORTITUDE("Fortitude", "fortitude", "FO"),
        LIFE_REGENERATION("Life Regeneration", "life_regeneration", "LR"),
        LIFELINE("Lifeline", "lifeline", "LL"),
        MAGIC_FIND("Magic Find", "magic_find", "MF"),
        MANA_POOL("Mana Pool", "mana_pool", "MP"),
        MANA_REGENERATION("Mana Regeneration", "mana_regeneration", "MR"),
        VITALITY("Vitality", "mending", "VI"),
        SPEED("Speed", "speed", "SP"),
        UNDEAD_RESISTANCE("Undead Resistance", "undead_resistance", "UR"),
        VETERAN("Veteran", "veteran", "VE"),
        BLAZING_FORTUNE("Blazing Fortune", "blazing_fortune", "BF"),
        FISHING_EXPERIENCE("Fishing Experience", "fishing_experience", "FE"),
        INFECTION("Infection", "infection", "IN"),
        DOUBLE_HOOK("Double Hook", "double_hook", "DH"),
        FISHERMAN("Fisherman", "fisherman", "FH"),
        FISHING_SPEED("Fishing Speed", "fishing_speed", "FS"),
        HUNTER("Hunter", "hunter", "HU"),
        TROPHY_HUNTER("Trophy Hunter", "trophy_hunter", "TH"),
        UNKNOWN("Unknown", "unknown", "??"),
        ;

        override fun toString() = "§b$displayName"

        companion object {

            fun getByInternalNameOrNull(internalName: String) = entries.find { it.internalName == internalName }

            fun getByInternalName(internalName: String) = getByInternalNameOrNull(internalName) ?: UNKNOWN
        }
    }

    data class Attribute(val type: AttributeType, val level: Int)

    private data class GoodRollItem(val regex: Pattern, val attributes: List<Pair<AttributeType, AttributeType>>)

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<AttributeGoodRollsJson>("AttributeGoodRolls")
        goodRolls = data.goodRolls.values.map {
            val regex = it.regex.toPattern()
            val list = it.list.map { combo ->
                val first = AttributeType.getByInternalName(combo[0])
                val second = AttributeType.getByInternalName(combo[1])
                first to second
            }
            GoodRollItem(regex, list)
        }
    }

    fun ItemStack.getAttributesLevels(): Pair<Attribute, Attribute>? =
        getAttributes()?.takeIf { it.isNotEmpty() }?.mapNotNull { (name, level) ->
            AttributeType.getByInternalNameOrNull(name.lowercase())?.let { Attribute(it, level) }
        }?.toPair()

    fun Pair<Attribute, Attribute>.isGoodRoll(internalName: NEUInternalName): Boolean =
        goodRolls.firstOrNull { it.regex.matches(internalName.asString()) }?.let { goodRoll ->
            val attributes = first.type to second.type
            goodRoll.attributes.any { it.equalsIgnoreOrder(attributes) }
        } ?: false

}
