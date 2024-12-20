package at.hannibal2.skyhanni.features.misc.massconfiguration

import at.hannibal2.skyhanni.config.FeatureToggle
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import io.github.notenoughupdates.moulconfig.processor.ConfigStructureReader
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.util.Stack

class FeatureToggleProcessor : ConfigStructureReader {

    private var latestCategory: Category? = null
    private val pathStack = Stack<String>()
    private val accordionStack = Stack<String>()

    val allOptions = mutableListOf<FeatureToggleableOption>()
    val orderedOptions by lazy {
        allOptions.groupBy { it.category }
    }

    override fun beginCategory(baseObject: Any?, field: Field?, name: String, description: String) {
        latestCategory = Category(name, description)
    }

    @Suppress("EmptyFunctionBlock")
    override fun endCategory() {}

    override fun beginAccordion(baseObject: Any?, field: Field?, o: ConfigOption?, id: Int) {
        val option = o ?: return
        accordionStack.push(option.name)
    }

    override fun endAccordion() {
        accordionStack.pop()
    }

    override fun pushPath(fieldPath: String) {
        pathStack.push(fieldPath)
    }

    override fun popPath() {
        pathStack.pop()
    }

    override fun emitOption(baseObject: Any, field: Field, option: ConfigOption) {
        val featureToggle = field.getAnnotation(FeatureToggle::class.java) ?: return
        field.getAnnotation(ConfigEditorBoolean::class.java)
            ?: error("Feature toggle found without ConfigEditorBoolean: $field")
        val setter: (Boolean) -> Unit
        val value: Boolean
        when (field.type) {
            java.lang.Boolean.TYPE -> {
                setter = { field.setBoolean(baseObject, it) }
                value = field.getBoolean(baseObject)
            }

            Property::class.java -> {
                val genericType = field.genericType
                require(genericType is ParameterizedType)
                require((genericType.actualTypeArguments[0] as Class<*>) == (java.lang.Boolean::class.java))
                val prop = field.get(baseObject) as Property<Boolean>
                setter = { prop.set(it) }
                value = prop.get()
            }

            else -> error("Invalid FeatureToggle type: $field")
        }

        var name = option.name
        if ((name == "Enable" || name == "Enabled") && !accordionStack.empty()) {
            name = accordionStack.peek()
        }

        allOptions.add(
            FeatureToggleableOption(
                name,
                option.desc,
                value,
                featureToggle.trueIsEnabled,
                latestCategory!!,
                setter,
                pathStack.joinToString(".") + "." + field.name
            )
        )
    }

    @Suppress("EmptyFunctionBlock")
    override fun emitGuiOverlay(baseObject: Any?, field: Field?, option: ConfigOption?) {}
}
