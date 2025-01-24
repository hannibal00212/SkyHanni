package at.hannibal2.skyhanni.utils.json

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.utils.ReflectionUtils.getDeclaredFieldOrNull
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.bind.JsonTreeReader
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Field
import java.lang.reflect.Type
import com.google.gson.internal.`$Gson$Types` as InternalGsonTypes

object FeatureTogglesByDefaultAdapter : TypeAdapterFactory {
    fun <T> getType(typeToken: TypeToken<T>, field: Field): Type {
        return InternalGsonTypes.resolve(typeToken.type, typeToken.rawType, field.genericType)
    }

    class Adapter<T>(
        val originalWrite: TypeAdapter<T>,
        val clazz: Class<T>,
        val gson: Gson,
        val type: TypeToken<T>,
    ) : TypeAdapter<T>() {
        override fun write(out: JsonWriter, value: T) {
            // Delegate the original config write, since that one is unchanged
            originalWrite.write(out, value)
        }

        override fun read(reader: JsonReader): T {
            reader.beginObject()
            // Create a default initialized instance
            val obj = clazz.newInstance()

            // Overwrite the default with true (or false) for feature toggles
            for (field in clazz.fields) {
                val featureToggle = field.getAnnotation(FeatureToggle::class.java)
                val adapt = gson.getAdapter(TypeToken.get(getType(type, field)))
                if (featureToggle != null)
                    field.set(obj, adapt.read(JsonTreeReader(JsonPrimitive(featureToggle.trueIsEnabled))))
                if (adapt is Adapter) {
                    field.set(obj, adapt.read(JsonTreeReader(JsonObject())))
                }
            }

            // Read the actual JSON Object
            while (reader.peek() != JsonToken.END_OBJECT) {
                // IllegalStateException: Expected NAME but was BOOLEAN
                if (reader.peek() != JsonToken.NAME) {
                    reader.skipValue()
                    continue
                }
                val name = reader.nextName()
                val field = clazz.getDeclaredFieldOrNull(name)
                if (field == null) {
                    println("field is in config file, but not in object file: $name")
                    continue
                }
                val fieldType = gson.getAdapter(TypeToken.get(getType(type, field)))
                // Read the field data
                val data = fieldType.read(reader)
                // Set the field or override the feature toggle with the saved data, leaving only the unset feature toggles to deviate from their defaults
                field.set(obj, data)
            }

            reader.endObject()
            return obj
        }
    }

    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>): TypeAdapter<T>? {
        gson!!
        val t = type.rawType

        // Check if this object has any feature toggles present
        if (t.fields.none {
                it.isAnnotationPresent(FeatureToggle::class.java) ||
                    gson.getAdapter(TypeToken.get(getType(type, it))) is Adapter
            }
        ) return null

        val originalWrite = gson.getDelegateAdapter(this, type)
        return Adapter(originalWrite, t as Class<T>, gson, type)
    }
}
