package at.hannibal2.skyhanni.utils.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class SimpleStringTypeAdapter<T>(
    val serializer: T.() -> String,
    val deserializer: String.() -> T
) : TypeAdapter<T>() {

    override fun write(writer: JsonWriter, value: T) {
        writer.value(serializer(value))
    }

    override fun read(reader: JsonReader): T {
        return deserializer(reader.nextString())
    }

    companion object {

        inline fun <reified T : Enum<T>> forEnum(): SimpleStringTypeAdapter<T> {
            return SimpleStringTypeAdapter(
                { name },
                { enumValueOf(this.replace(" ", "_").uppercase()) }
            )
        }
    }
}
