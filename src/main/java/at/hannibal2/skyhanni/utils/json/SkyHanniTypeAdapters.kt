package at.hannibal2.skyhanni.utils.json

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.pests.PestType
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.item.ItemStack
import java.time.LocalDate
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object SkyHanniTypeAdapters {

    val NEU_ITEMSTACK: TypeAdapter<ItemStack> = SimpleStringTypeAdapter(NEUItems::saveNBTData, NEUItems::loadNBTData)

    val UUID: TypeAdapter<UUID> = SimpleStringTypeAdapter(
        { this.toString() },
        { java.util.UUID.fromString(this) },
    )

    val INTERNAL_NAME: TypeAdapter<NEUInternalName> = SimpleStringTypeAdapter(
        { this.asString() },
        { this.asInternalName() },
    )

    val VEC_STRING: TypeAdapter<LorenzVec> = SimpleStringTypeAdapter(
        { "$x:$y:$z" },
        { LorenzVec.decodeFromString(this) },
    )

    val TROPHY_RARITY: TypeAdapter<TrophyRarity> = SimpleStringTypeAdapter(
        { name },
        { TrophyRarity.getByName(this) ?: error("Could not parse TrophyRarity from '$this'") },
    )

    val TIME_MARK: TypeAdapter<SimpleTimeMark> = object : TypeAdapter<SimpleTimeMark>() {
        override fun write(out: JsonWriter, value: SimpleTimeMark) {
            out.value(value.toMillis())
        }

        override fun read(reader: JsonReader): SimpleTimeMark {
            return reader.nextString().toLong().asTimeMark()
        }
    }

    val DURATION: TypeAdapter<Duration> = object : TypeAdapter<Duration>() {
        override fun write(out: JsonWriter, value: Duration) {
            out.value(value.inWholeMilliseconds)
        }

        override fun read(reader: JsonReader): Duration {
            return reader.nextString().toLong().milliseconds
        }
    }

    val CROP_TYPE: TypeAdapter<CropType> = SimpleStringTypeAdapter(
        { name },
        { CropType.getByName(this) },
    )

    val PEST_TYPE: TypeAdapter<PestType> = SimpleStringTypeAdapter(
        { name },
        { PestType.getByName(this) },
    )

    val SKYBLOCK_STAT: TypeAdapter<SkyblockStat> = SimpleStringTypeAdapter(
        { name.lowercase() },
        { SkyblockStat.getValue(this.uppercase()) },
    )

    val TRACKER_DISPLAY_MODE = SimpleStringTypeAdapter.forEnum<SkyHanniTracker.DefaultDisplayMode>()
    val ISLAND_TYPE = SimpleStringTypeAdapter.forEnum<IslandType>()
    val RARITY = SimpleStringTypeAdapter.forEnum<LorenzRarity>()

    val LOCALE_DATE = object : TypeAdapter<LocalDate>() {
        override fun write(out: JsonWriter, value: LocalDate) {
            out.value(value.toString())
        }

        override fun read(reader: JsonReader): LocalDate {
            return LocalDate.parse(reader.nextString())
        }
    }

    inline fun <reified T> GsonBuilder.registerTypeAdapter(
        crossinline write: (JsonWriter, T) -> Unit,
        crossinline read: (JsonReader) -> T,
    ): GsonBuilder {
        this.registerTypeAdapter(
            T::class.java,
            object : TypeAdapter<T>() {
                override fun write(out: JsonWriter, value: T) = write(out, value)
                override fun read(reader: JsonReader) = read(reader)
            }.nullSafe(),
        )
        return this
    }
}
