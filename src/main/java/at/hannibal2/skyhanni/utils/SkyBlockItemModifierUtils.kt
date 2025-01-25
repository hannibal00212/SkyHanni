package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.PetApi
import at.hannibal2.skyhanni.mixins.hooks.ItemStackCachedData
import at.hannibal2.skyhanni.utils.ItemUtils.extraAttributes
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.getStringList
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.isPositive
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import com.google.gson.JsonObject
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.Constants
import java.util.Locale

object SkyBlockItemModifierUtils {

    private val drillPartTypes = listOf("drill_part_upgrade_module", "drill_part_engine", "drill_part_fuel_tank")

    fun ItemStack.getHotPotatoCount() = getAttributeInt("hot_potato_count")

    fun ItemStack.getFarmingForDummiesCount() = getAttributeInt("farming_for_dummies_count")

    fun ItemStack.getPolarvoidBookCount() = getAttributeInt("polarvoid")

    fun ItemStack.getBookwormBookCount() = getAttributeInt("bookworm_books")

    fun ItemStack.getCultivatingCounter() = getAttributeLong("farmed_cultivating")

    fun ItemStack.getHoeCounter() = getAttributeLong("mined_crops")

    fun ItemStack.getSilexCount() = getEnchantments()?.get("efficiency")?.let {
        it - 5 - getBaseSilexCount()
    }?.takeIf { it > 0 }

    fun ItemStack.getMithrilInfusion(): Boolean = getAttributeByte("mithril_infusion") == 1.toByte()

    private fun ItemStack.getBaseSilexCount() = when (getInternalName().asString()) {
        "STONK_PICKAXE" -> 1
        "PROMISING_SPADE" -> 5

        else -> 0
    }

    fun ItemStack.getTransmissionTunerCount() = getAttributeInt("tuned_transmission")

    fun ItemStack.getManaDisintegrators() = getAttributeInt("mana_disintegrator_count")

    fun ItemStack.getDungeonStarCount() = if (isDungeonItem()) {
        getStarCount() ?: getAttributeInt("dungeon_item_level")
    } else null

    fun ItemStack.getStarCount() = getAttributeInt("upgrade_level")

    private fun ItemStack.isDungeonItem() = getLore().any { it.contains("DUNGEON ") }

    fun ItemStack.getPetExp() = getPetInfo()?.get("exp")?.asDouble

    fun ItemStack.getPetCandyUsed(): Int? {
        val data = cachedData
        if (data.petCandies == -1) {
            data.petCandies = getPetInfo()?.get("candyUsed")?.asInt
        }
        return data.petCandies
    }

    // TODO use NeuInternalName here
    fun ItemStack.getPetItem(): String? {
        val data = cachedData
        if (data.heldItem == "") {
            data.heldItem = getPetInfo()?.get("heldItem")?.asString
        }
        return data.heldItem
    }

    fun ItemStack.isRiftTransferable(): Boolean {
        val data = cachedData
        return data.riftTransferable
            ?: UtilsPatterns.riftTransferablePattern.anyMatches(getLore())
                .also { data.riftTransferable = it }
    }

    fun ItemStack.isRiftExportable(): Boolean {
        val data = cachedData
        return data.riftExportable
            ?: UtilsPatterns.riftExportablePattern.anyMatches(getLore())
                .also { data.riftExportable = it }
    }

    fun ItemStack.wasRiftTransferred(): Boolean = getAttributeBoolean("rift_transferred")

    private fun ItemStack.getPetInfo() =
        ConfigManager.gson.fromJson(getExtraAttributes()?.getString("petInfo"), JsonObject::class.java)

    @Suppress("CAST_NEVER_SUCCEEDS")
    inline val ItemStack.cachedData get() = (this as ItemStackCachedData).skyhanni_cachedData

    fun ItemStack.getPetLevel(): Int = PetApi.getPetLevel(displayName) ?: 0

    fun ItemStack.getMaxPetLevel() = if (this.getInternalName() == "GOLDEN_DRAGON;4".toInternalName()) 200 else 100

    fun ItemStack.getDrillUpgrades() = getExtraAttributes()?.let {
        val list = mutableListOf<NeuInternalName>()
        for (attributes in it.keySet) {
            if (attributes in drillPartTypes) {
                val upgradeItem = it.getString(attributes)
                list.add(upgradeItem.uppercase().toInternalName())
            }
        }
        list
    }

    fun ItemStack.getPowerScroll() = getAttributeString("power_ability_scroll")?.toInternalName()

    fun ItemStack.getEnrichment() = getAttributeString("talisman_enrichment")

    fun ItemStack.getHelmetSkin() = getAttributeString("skin")?.toInternalName()

    fun ItemStack.getArmorDye() = getAttributeString("dye_item")?.toInternalName()

    fun ItemStack.getFungiCutterMode() = getAttributeString("fungi_cutter_mode")

    fun ItemStack.getRanchersSpeed() = getAttributeInt("ranchers_speed")

    fun ItemStack.getRune(): NeuInternalName? {
        val runesMap = getExtraAttributes()?.getCompoundTag("runes") ?: return null
        val runesList = runesMap.keySet.associateWith { runesMap.getInteger(it) }.toList()
        if (runesList.isEmpty()) return null
        val (name, tier) = runesList.first()
        return "${name.uppercase()}_RUNE;$tier".toInternalName()
    }

    fun ItemStack.getAbilityScrolls() = getExtraAttributes()?.let { compound ->
        val ultimateWitherScroll = "ULTIMATE_WITHER_SCROLL".toInternalName()
        val implosion = "IMPLOSION_SCROLL".toInternalName()
        val witherShield = "WITHER_SHIELD_SCROLL".toInternalName()
        val shadowWarp = "SHADOW_WARP_SCROLL".toInternalName()

        val scrolls = mutableSetOf<NeuInternalName>()

        for (scroll in compound.getStringList("ability_scroll").map { it.toInternalName() }) {
            if (scroll == ultimateWitherScroll) {
                scrolls.add(implosion)
                scrolls.add(witherShield)
                scrolls.add(shadowWarp)
                continue
            }
            scrolls.add(scroll)
        }

        scrolls.toList()
    }

    fun ItemStack.getAttributes() = getExtraAttributes()
        ?.takeIf { it.hasKey("attributes", Constants.NBT.TAG_COMPOUND) }
        ?.getCompoundTag("attributes")
        ?.let { attr ->
            attr.keySet.map {
                it.uppercase() to attr.getInteger(it)
            }.sortedBy { it.first }
        }

    fun ItemStack.hasAttributes() = getAttributes() != null

    fun ItemStack.getReforgeName() = getAttributeString("modifier")?.let {
        when {
            it == "pitchin" -> "pitchin_koi"
            it == "warped" && name.removeColor().startsWith("Hyper ") -> "endstone_geode"

            else -> it
        }
    }

    fun ItemStack.isRecombobulated() = getAttributeInt("rarity_upgrades").isPositive()

    fun ItemStack.hasJalapenoBook() = getAttributeInt("jalapeno_count").isPositive()

    fun ItemStack.hasEtherwarp() = getAttributeBoolean("ethermerge")

    fun ItemStack.hasWoodSingularity() = getAttributeInt("wood_singularity_count").isPositive()

    fun ItemStack.hasDivanPowderCoating() = getAttributeBoolean("divan_powder_coating")

    fun ItemStack.hasArtOfWar() = getAttributeInt("art_of_war_count").isPositive()

    fun ItemStack.hasBookOfStats() = getAttributeInt("stats_book") != null

    fun ItemStack.hasArtOfPeace() = getAttributeBoolean("artOfPeaceApplied")

    fun ItemStack.isMuseumDonated() = getAttributeBoolean("donated_museum")

    fun ItemStack.getLivingMetalProgress() = getAttributeInt("lm_evo")

    fun ItemStack.getSecondsHeld() = when (getItemId()) {
        "NEW_BOTTLE_OF_JYRRE" -> getAttributeInt("bottle_of_jyrre_seconds")
        "DARK_CACAO_TRUFFLE" -> getAttributeInt("seconds_held")
        "DISCRITE" -> getAttributeInt("rift_discrite_seconds")
        else -> null
    }

    fun ItemStack.getEdition() = getAttributeInt("edition")

    fun ItemStack.getNewYearCake() = getAttributeInt("new_years_cake")

    fun ItemStack.getPersonalCompactorActive() = getAttributeByte("PERSONAL_DELETOR_ACTIVE") == 1.toByte()

    fun ItemStack.getEnchantments(): Map<String, Int>? = getExtraAttributes()
        ?.takeIf { it.hasKey("enchantments") }
        ?.run {
            val enchantments = this.getCompoundTag("enchantments")
            enchantments.keySet.associateWith { enchantments.getInteger(it) }
        }

    fun ItemStack.getAppliedPocketSackInASack(): Int? {
        val data = cachedData
        if (data.sackInASack == -1) {
            data.sackInASack = getAttributeInt("sack_pss")
        }
        return data.sackInASack
    }

    fun ItemStack.getRecipientName() = getAttributeString("recipient_name")

    fun ItemStack.getItemUuid() = getAttributeString("uuid")

    fun ItemStack.getItemId() = getAttributeString("id")

    fun ItemStack.getMinecraftId() = Item.itemRegistry.getNameForObject(item) as ResourceLocation

    fun ItemStack.getGemstones() = getExtraAttributes()?.let {
        val list = mutableListOf<GemstoneSlot>()
        for (attributes in it.keySet) {
            if (attributes != "gems") continue
            val gemstones = it.getCompoundTag(attributes)
            for (key in gemstones.keySet) {
                if (key.endsWith("_gem")) continue
                if (key == "unlocked_slots") continue
                var value = gemstones.getString(key)
                if (value == "") {
                    val tag = gemstones.getCompoundTag(key)
                    value = tag.getString("quality")
                    if (value == "") continue
                }

                val rawType = key.split("_")[0]
                val type = GemstoneType.getByName(rawType)

                val quality = GemstoneQuality.getByName(value)
                if (quality == null) {
                    ChatUtils.debug("Gemstone quality is null for item $name§7: ('$key' = '$value')")
                    continue
                }
                if (type != null) {
                    list.add(GemstoneSlot(type, quality))
                } else {
                    val newKey = gemstones.getString(key + "_gem")
                    val newType = GemstoneType.getByName(newKey)
                    if (newType == null) {
                        ChatUtils.debug("Gemstone type is null for item $name§7: ('$newKey' with '$key' = '$value')")
                        continue
                    }
                    list.add(GemstoneSlot(newType, quality))
                }
            }
        }
        list
    }

    fun ItemStack.getAttributeString(label: String) =
        getExtraAttributes()?.getString(label)?.takeUnless { it.isBlank() }

    private fun ItemStack.getAttributeInt(label: String) =
        getExtraAttributes()?.getInteger(label)?.takeUnless { it == 0 }

    private fun ItemStack.getAttributeLong(label: String) =
        getExtraAttributes()?.getLong(label)?.takeUnless { it == 0L }

    private fun ItemStack.getAttributeBoolean(label: String) =
        getExtraAttributes()?.getBoolean(label) ?: false

    private fun ItemStack.getAttributeByte(label: String) =
        getExtraAttributes()?.getByte(label) ?: 0

    fun ItemStack.getExtraAttributes() = tagCompound?.extraAttributes

    class GemstoneSlot(val type: GemstoneType, val quality: GemstoneQuality) {

        fun getInternalName() = "${quality}_${type}_GEM".toInternalName()
    }

    enum class GemstoneQuality(val displayName: String) {
        ROUGH("Rough"),
        FLAWED("Flawed"),
        FINE("Fine"),
        FLAWLESS("Flawless"),
        PERFECT("Perfect"),
        ;

        companion object {

            fun getByName(name: String) = entries.firstOrNull { it.name == name }
        }
    }

    enum class GemstoneType(val displayName: String) {
        JADE("Jade"),
        AMBER("Amber"),
        TOPAZ("Topaz"),
        SAPPHIRE("Sapphire"),
        AMETHYST("Amethyst"),
        JASPER("Jasper"),
        RUBY("Ruby"),
        OPAL("Opal"),
        ONYX("Onyx"),
        AQUAMARINE("Aquamarine"),
        CITRINE("Citrine"),
        PERIDOT("Peridot"),
        ;

        companion object {

            fun getByName(name: String) = entries.firstOrNull { it.name == name }
        }
    }

    enum class GemstoneSlotType(val colorCode: Char) {
        JADE('a'),
        AMBER('6'),
        TOPAZ('e'),
        SAPPHIRE('b'),
        AMETHYST('5'),
        JASPER('d'),
        RUBY('c'),
        OPAL('f'),
        ONYX('8'),
        AQUAMARINE('3'),
        CITRINE('4'),
        PERIDOT('2'),
        COMBAT('4'),
        DEFENSIVE('a'),
        MINING('5'),
        UNIVERSAL('f'),
        ;

        companion object {

            fun getByName(name: String): GemstoneSlotType =
                entries.firstOrNull { name.uppercase(Locale.ENGLISH).contains(it.name) }
                    ?: error("Unknown GemstoneSlotType: '$name'")

            fun getColorCode(name: String) = getByName(name).colorCode
        }
    }
}
