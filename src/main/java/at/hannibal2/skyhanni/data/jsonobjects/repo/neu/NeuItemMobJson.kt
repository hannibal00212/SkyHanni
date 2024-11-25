package at.hannibal2.skyhanni.data.jsonobjects.repo.neu;

import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

data class NeuItemMobJson(
    @Expose @SerializedName("itemid") val itemId: String = "",
    @Expose @SerializedName("displayname") val displayName: String = "",
    @Expose @SerializedName("nbttag") val nbtTag: String = "",
    @Expose val damage: Int = 0,
    @Expose val lore: List<String> = listOf(),
    @Expose @SerializedName("internalname") val internalName: String = "",
    @Expose @SerializedName("clickcommand") val clickCommand: String = "",
    @Expose @SerializedName("modver") val modVersion: String = "",
    @Expose val infoType: String = "",
    @Expose val info: List<String> = listOf(),
    @Expose @SerializedName("crafttext") val craftText: String = "",
    @Expose val recipes: List<NeuRecipeJson>
)

data class NeuRecipeJson(
    @Expose val level: Int = 0,
    @Expose val coins: Long = 0L,
    @Expose val xp: Long = 0L,
    @Expose val panorama: String = "",
    @Expose val render: String = "",
    @Expose val name: String = "",
    @Expose val type: String = "",
    @Expose val drops: List<NeuDropsJson>
)

data class NeuDropsJson(
    @Expose val id: String = "",
    @Expose val extra: List<String> = listOf(),
    @Expose val chance: String = "",
) {
    // Format structure is;
    //
    // {internal_name}:{amount}
    // ENCHANTED_HUGE_MUSHROOM_2:3
    //
    fun toInternalName() = id.split(":").first().toInternalName()
}
