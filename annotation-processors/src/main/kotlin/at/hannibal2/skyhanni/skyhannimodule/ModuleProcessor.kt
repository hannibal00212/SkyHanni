package at.hannibal2.skyhanni.skyhannimodule

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

class ModuleProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    // TODO remove once all events are migrated to SkyHanniEvent
    private var skyHanniEvent: KSType? = null
    private var minecraftForgeEvent: KSType? = null
    private val warnings = mutableListOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {

        skyHanniEvent =
            resolver.getClassDeclarationByName("at.hannibal2.skyhanni.api.event.SkyHanniEvent")?.asStarProjectedType()

        minecraftForgeEvent = resolver.getClassDeclarationByName("net.minecraftforge.fml.common.eventhandler.Event")
            ?.asStarProjectedType()
                ?: return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(SkyHanniModule::class.qualifiedName!!).toList()
        val validSymbols = symbols.mapNotNull { validateSymbol(it) }

        if (validSymbols.isNotEmpty()) {
            generateFile(validSymbols)
        }

        return emptyList()
    }

    private fun validateSymbol(symbol: KSAnnotated): KSClassDeclaration? {
        if (!symbol.validate()) {
            logger.warn("Symbol is not valid: $symbol")
            return null
        }

        if (symbol !is KSClassDeclaration) {
            logger.error("@SkyHanniModule is only valid on class declarations", symbol)
            return null
        }

        if (symbol.classKind != ClassKind.OBJECT) {
            logger.error("@SkyHanniModule is only valid on kotlin objects", symbol)
            return null
        }

        // TODO remove once all events are migrated to SkyHanniEvent
        val className = symbol.qualifiedName?.asString() ?: "unknown"

        for (function in symbol.getDeclaredFunctions()) {
            if (function.annotations.any { it.shortName.asString() == "SubscribeEvent" }) {
                val firstParameter = function.parameters.firstOrNull()?.type?.resolve()!!
                if (!minecraftForgeEvent!!.isAssignableFrom(firstParameter)) {
                    warnings.add("Function in $className must have an event assignable from $minecraftForgeEvent because it is annotated with @SubscribeEvent")
                }
            }

            if (function.annotations.any { it.shortName.asString() == "HandleEvent" }) {
                val firstParameter = function.parameters.firstOrNull()?.type?.resolve()!!
                if (!skyHanniEvent!!.isAssignableFrom(firstParameter)) {
                    warnings.add("Function in $className must have an event assignable from $skyHanniEvent because it is annotated with @HandleEvent")
                }
            }
        }

        return symbol
    }

    //TODO remove when KMixins added as it contains KSP annotation helpers.
    private fun isDevAnnotation(klass: KSClassDeclaration): Boolean {
        val annotation = klass.annotations.find { it.shortName.asString() == "SkyHanniModule" } ?: return false
        return annotation.arguments.find { it.name?.asString() == "devOnly" }?.value as? Boolean ?: false
    }

    private fun isNEUAnnotation(klass: KSClassDeclaration): Boolean {
        val annotation = klass.annotations.find { it.shortName.asString() == "SkyHanniModule" } ?: return false
        return annotation.arguments.find { it.name?.asString() == "neuRequired" }?.value as? Boolean ?: false
    }

    // TODO use Kotlin Poet once KMixins is merged
    private fun generateFile(symbols: List<KSClassDeclaration>) {

        if (warnings.isNotEmpty()) {
            warnings.forEach { logger.warn(it) }
            error("${warnings.size} errors related to event annotations found, please fix them before continuing. Click on the kspKotlin build log for more information.")
        }

        val sources = symbols.mapNotNull { it.containingFile }.toTypedArray()
        val dependencies = Dependencies(true, *sources)

        val file = codeGenerator.createNewFile(dependencies, "at.hannibal2.skyhanni.skyhannimodule", "LoadedModules")

        OutputStreamWriter(file).use {
            it.write("package at.hannibal2.skyhanni.skyhannimodule\n\n")
            it.write("object LoadedModules {\n")
            it.write("    val isDev: Boolean = at.hannibal2.skyhanni.utils.system.PlatformUtils.isDevEnvironment\n")
            it.write("    val hasNeu: Boolean get() = at.hannibal2.skyhanni.utils.system.PlatformUtils.isNeuLoaded()\n")
            it.write("    val modules: List<Any> = buildList {\n")

            symbols.forEach { symbol ->
                if (isDevAnnotation(symbol)) {
                    it.write("        if (isDev) add(${symbol.qualifiedName!!.asString()})\n")
                } else if (isNEUAnnotation(symbol)) {
                    it.write("        if (hasNeu) add(${symbol.qualifiedName!!.asString()})\n")
                } else {
                    it.write("        add(${symbol.qualifiedName!!.asString()})\n")
                }
            }

            it.write("    }\n")
            it.write("}\n")
        }

        logger.warn("Generated LoadedModules file with ${symbols.size} modules")
    }
}
