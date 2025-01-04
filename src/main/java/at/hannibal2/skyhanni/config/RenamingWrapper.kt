package at.hannibal2.skyhanni.config

import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver
import io.github.notenoughupdates.moulconfig.processor.ConfigStructureReader
import java.lang.reflect.Field
import java.lang.reflect.Proxy

abstract class RenamingWrapper(private val wrappedProcessor: ConfigStructureReader) : ConfigStructureReader {
    override fun pushPath(fieldPath: String?) {
        wrappedProcessor.pushPath(fieldPath)
    }

    override fun popPath() {
        wrappedProcessor.popPath()
    }

    override fun beginConfig(
        configClass: Class<out Config?>?,
        driver: ConfigProcessorDriver?,
        configObject: Config?,
    ) {
        wrappedProcessor.beginConfig(configClass, driver, configObject)
    }

    override fun endConfig() {
        wrappedProcessor.endConfig()
    }

    override fun setCategoryParent(field: Field?) {
        wrappedProcessor.setCategoryParent(field)
    }

    private fun wrapOption(option: ConfigOption): ConfigOption {
        val originalHandler = Proxy.getInvocationHandler(option)
        return Proxy.newProxyInstance(
            option.javaClass.classLoader,
            arrayOf<Class<*>>(ConfigOption::class.java),
        ) { proxy, method, args ->
            var proxyResult = originalHandler.invoke(proxy, method, args)
            if (method.name == "name" || method.name == "desc") {
                proxyResult = mapText(proxyResult as String)
            }
            proxyResult
        } as ConfigOption
    }

    abstract fun mapText(original: String): String

    override fun beginCategory(
        baseObject: Any?,
        field: Field?,
        name: String,
        description: String,
    ) {
        wrappedProcessor.beginCategory(baseObject, field, mapText(name), mapText(description))
    }

    override fun endCategory() {
        wrappedProcessor.endCategory()
    }

    override fun beginAccordion(
        baseObject: Any?,
        field: Field?,
        option: ConfigOption,
        id: Int,
    ) {
        wrappedProcessor.beginAccordion(baseObject, field, wrapOption(option), id)
    }

    override fun endAccordion() {
        wrappedProcessor.endAccordion()
    }

    override fun emitOption(
        baseObject: Any?,
        field: Field?,
        option: ConfigOption,
    ) {
        wrappedProcessor.emitOption(baseObject, field, wrapOption(option))
    }
}
