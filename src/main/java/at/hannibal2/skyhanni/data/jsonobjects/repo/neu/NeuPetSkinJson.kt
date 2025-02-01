package at.hannibal2.skyhanni.data.jsonobjects.repo.neu

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import java.io.ByteArrayInputStream
import java.util.Base64

data class NeuPetSkinJson(
    @Expose @SerializedName("itemid") val itemId: String,
    @Expose @SerializedName("displayname") val displayName: String,
    @Expose @SerializedName("nbttag") val nbtTagString: String,
    @Expose val damage: Int,
    @Expose val lore: List<String>,
    @Expose @SerializedName("internalname") val internalName: String,
    @Expose @SerializedName("crafttext") val craftText: String,
    @Expose @SerializedName("clickcommand") val clickCommand: String,
    @Expose @SerializedName("modver") val modVersion: String,
    @Expose val infoType: String,
    @Expose val info: List<String>
) {
    /**
     * Parses the NBT tag from the JSON into an NBTTagCompound.
     * @return Parsed NBTTagCompound object.
     * @throws IllegalArgumentException if the NBT parsing fails.
     */
    private fun getParsedNBT(): NBTTagCompound {
        return try {
            val decodedBytes = Base64.getDecoder().decode(nbtTagString.toByteArray(Charsets.UTF_8))
            val inputStream = ByteArrayInputStream(decodedBytes)
            CompressedStreamTools.readCompressed(inputStream)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse NBT tag: $nbtTagString", e)
        }
    }

    val nbtTag get() = getParsedNBT()
}
