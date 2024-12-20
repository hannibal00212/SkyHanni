package at.hannibal2.skyhanni.utils.json

import at.hannibal2.skyhanni.config.ConfigManager
import com.google.gson.JsonElement
import java.lang.reflect.Field

// Copied from NEU
class Shimmy private constructor(
    val source: Any,
    val field: Field,
) {
    companion object {
        private fun shimmy(source: Any?, fieldName: String): Any? {
            if (source == null) return null
            return try {
                val declaredField = source.javaClass.getDeclaredField(fieldName)
                declaredField.isAccessible = true
                declaredField.get(source)
            } catch (e: NoSuchFieldException) {
                null
            }
        }

        @JvmStatic
        fun makeShimmy(source: Any?, path: List<String>): Shimmy? {
            if (path.isEmpty())
                return null
            var source = source
            for (part in path.dropLast(1)) {
                source = shimmy(source, part)
            }
            if (source == null) return null
            val lastName = path.last()
            return try {
                val field = source.javaClass.getDeclaredField(lastName)
                field.isAccessible = true
                Shimmy(
                    source,
                    field,
                )
            } catch (e: NoSuchFieldException) {
                null
            }
        }

    }

    val clazz: Class<*> = field.type
    fun get(): Any? {
        return field.get(source)
    }

    fun set(value: Any?) {
        field.set(source, value)
    }

    fun getJson(): JsonElement {
        return ConfigManager.gson.toJsonTree(get())
    }

    fun setJson(element: JsonElement) {
        set(ConfigManager.gson.fromJson(element, clazz))
    }
}
