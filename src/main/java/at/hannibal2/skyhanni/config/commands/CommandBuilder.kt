package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.utils.CommandArgument
import at.hannibal2.skyhanni.utils.CommandContextAwareObject
import at.hannibal2.skyhanni.utils.ComplexCommand

class CommandBuilder(name: String) : CommandBuilderBase(name) {
    private var autoComplete: ((Array<String>) -> List<String>) = { listOf() }
    private var callback: (Array<String>) -> Unit = {}

    fun callback(callback: (Array<String>) -> Unit) {
        this.callback = callback
    }

    fun autoComplete(autoComplete: (Array<String>) -> List<String>) {
        this.autoComplete = autoComplete
    }
}

open class CommandBuilderBase(val name: String) {
    var description: String = ""
    var category: CommandCategory = CommandCategory.MAIN
    var aliases: List<String> = emptyList()
    private var autoComplete: ((Array<String>) -> List<String>) = { listOf() }
    private var callback: (Array<String>) -> Unit = {}

    fun toSimpleCommand() = SimpleCommand(name.lowercase(), aliases, callback, autoComplete)
}

class ComplexCommandBuilder<O : CommandContextAwareObject, A : CommandArgument<O>>(name: String) : CommandBuilderBase(name){
    lateinit var specifiers: Collection<A>
    var excludedSpecifiersFromDescription: Set<A> = emptySet()
    lateinit var context: () -> O

    lateinit var complexCommand: ComplexCommand<O>
        private set

    fun construct(){
        complexCommand = ComplexCommand(name, specifiers, context)
        complexCommand.constructHelp(description,excludedSpecifiersFromDescription)
    }
}

