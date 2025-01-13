package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.NEUInternalName
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ItemsJson(
    @Expose @SerializedName("crimson_armors") val crimsonArmors: List<String>,
    @Expose @SerializedName("crimson_tiers") val crimsonTiers: Map<String, Int>,
    @Expose @SerializedName("enchant_multiplier") val enchantMultiplier: Map<String, Float>,
    @Expose @SerializedName("lava_fishing_rods") val lavaFishingRods: List<NEUInternalName>,
    @Expose @SerializedName("water_fishing_rods") val waterFishingRods: List<NEUInternalName>,
    @Expose @SerializedName("dungeon_secret_items") val dungeonSecretItems: Set<NEUInternalName>,
    @Expose @SerializedName("book_bundle_amount") val bookBundleAmount: Map<String, Int>,
)
