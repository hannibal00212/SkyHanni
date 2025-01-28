package at.hannibal2.skyhanni.utils.json

import at.hannibal2.skyhanni.SkyHanniMod
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/*
    Instead of crashing on a wrong value in the config we set the value to null and log a warning.
    This prevents user's config from resetting to default values.
    Which is especially important for when people downgrade their mod version, either on purpose or by accident.
    This does not always work, and can cause a crash later on, but the full config reset is avoided.
 */
object SkippingTypeAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
        return SafeTypeAdapter(gson.getDelegateAdapter(this, type))
    }

    private class SafeTypeAdapter<T>(val parent: TypeAdapter<T>) : TypeAdapter<T>() {
        override fun write(writer: JsonWriter, value: T) {
            parent.write(writer, value)
        }

        override fun read(reader: JsonReader): T? {
            return try {
                parent.read(reader)
            } catch (e: Exception) {
                val foundValue = reader.peek().toString()
                val message = "Skipping reading JSON from failed path '${reader.path}', found value of '$foundValue'."
                SkyHanniMod.logger.warn(message, e)
                if (!reader.hasNext()) return null
                // reader skip value seems to have an infinite loop if you dont have another element
                reader.skipValue()
                null
            }
        }
    }
}
