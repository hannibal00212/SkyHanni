package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.utils.CommandArgument
import at.hannibal2.skyhanni.utils.CommandContextAwareObject
import net.minecraft.command.ICommand

class CommandBuilder(name: String) : CommandBuilderBase(name) {
    private var autoComplete: ((Array<String>) -> List<String>) = { listOf() }
    private var callback: (Array<String>) -> Unit = {}

    fun callback(callback: (Array<String>) -> Unit) {
        this.callback = callback
    }

    fun autoComplete(autoComplete: (Array<String>) -> List<String>) {
        this.autoComplete = autoComplete
    }

    override fun toCommand() = SimpleCommand(name.lowercase(), aliases, callback, autoComplete)
}

abstract class CommandBuilderBase(val name: String) {
    var description: String = ""
    var category: CommandCategory = CommandCategory.MAIN
    var aliases: List<String> = emptyList()

    abstract fun toCommand(): ICommand

    open val descriptor: String get() = description
}

class ComplexCommandBuilder<O : CommandContextAwareObject, A : CommandArgument<O>>(name: String) : CommandBuilderBase(name) {
    lateinit var specifiers: Collection<A>
    var excludedSpecifiersFromDescription: Set<A> = emptySet()
    lateinit var context: () -> O

    private var realDescription: String = ""

    override fun toCommand() = ComplexCommand(name.lowercase(), specifiers, context, aliases).also {
        realDescription = it.constructHelp(description, excludedSpecifiersFromDescription)
    }

    override val descriptor get() = realDescription
}

