package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import java.util.TreeMap

object CommandUtils {

    class ItemGroup(val name: String, vararg items: Pair<String, Int>, val collection: String = "") {

        val icon = items.first().first.asInternalName()

        val items = items.associate { it.first.asInternalName() to it.second }

        init {
            entries[name] = this
        }

        companion object {

            private val entries = TreeMap<String, ItemGroup>()

            fun groupStartingWith(start: String): Collection<ItemGroup> = StringUtils.subMapOfStringsStartingWith(start, entries).values

            fun groupNameStartingWith(start: String): List<String> = groupStartingWith(start).map { it.name }

            fun findGroup(string: String): ItemGroup? {
                val search = string.replace(" ", "_").uppercase()
                return entries[search]
            }
        }
    }

    private enum class NameSource {
        INTERNAL_NAME,
        ITEM_NAME,
        GROUP;
    }

    private val namePattern = "^(?i)(name:)(.*)".toRegex()
    private val internalPattern = "^(?i)(internal:)(.*)".toRegex()
    private val groupPattern = "(?i)^(group:|collection:)(.*)".toRegex()

    fun itemCheck(args: Iterable<String>, context: CommandContextAwareObject): Pair<Int, Any?> {
        @Suppress("ReplaceSizeZeroCheckWithIsEmpty") // A bug since the replacement does not work for iterable interface.
        if (args.count() == 0) {
            context.errorMessage = "No item specified"
            return 0 to null
        }
        val first = args.first()

        val expected = when {
            namePattern.matches(first) -> NameSource.ITEM_NAME
            internalPattern.matches(first) -> NameSource.INTERNAL_NAME
            groupPattern.matches(first) -> NameSource.GROUP
            else -> null
        }

        val grabbed = args.takeWhile { "[a-zA-Z:_\"';]+([:-;]\\d+)?".toPattern().matches(it) }

        val collected = grabbed.joinToString(" ").replace("[\"']".toRegex(), "")

        val item: Any? = when (expected) {
            NameSource.INTERNAL_NAME -> collected.replace(internalPattern, "$2").replace(" ", "_").asInternalName()
            NameSource.ITEM_NAME -> NEUInternalName.fromItemNameOrNull(collected.replace(namePattern, "$2").replace("_", " "))
            NameSource.GROUP -> ItemGroup.findGroup(collected.replace(groupPattern, "$2"))
            null -> {
                val fromItemName = NEUInternalName.fromItemNameOrNull(collected.replace("_", " "))
                if (fromItemName?.getItemStackOrNull() != null) {
                    fromItemName
                } else {
                    val internalName = collected.replace(" ", "_").asInternalName()
                    if (internalName.getItemStackOrNull() != null) {
                        internalName
                    } else {
                        ItemGroup.findGroup(collected)
                    }
                }
            }
        }

        if ((item as? NEUInternalName)?.getItemStackOrNull() == null && (item as? ItemGroup) == null) {
            context.errorMessage = "Could not find a valid item for: '$collected'"
        }

        return grabbed.size to item
    }

    fun itemTabComplete(start: String): List<String> = buildList {
        if (start.isEmpty()) return@buildList
        val expected = when {
            namePattern.matches(start) -> NameSource.ITEM_NAME
            internalPattern.matches(start) -> NameSource.INTERNAL_NAME
            groupPattern.matches(start) -> NameSource.GROUP
            else -> null
        }

        val uppercaseStart = start.uppercase().replace(" ", "_")
        val lowercaseStart = start.lowercase().replace("_", " ")

        fun MutableList<String>.resultAdd(pattern: Regex, start: String, transformedStart: String, f: (String) -> Collection<String>) {
            val prefix = start.replace(pattern, "$1")
            val withoutPrefix = transformedStart.replace(pattern, "$2")
            if (withoutPrefix.isEmpty()) return
            val lastSpaceIndex = start.replace(pattern, "$2").indexOfLast { it == ' ' } + 1
            this@resultAdd.addAll(
                f(withoutPrefix).map { r ->
                    if (lastSpaceIndex == 0) {
                        prefix + r
                    } else {
                        r.substring(lastSpaceIndex)
                    }
                },
            )
        }

        when (expected) {
            NameSource.INTERNAL_NAME -> resultAdd(internalPattern, start, uppercaseStart, NEUItems::findInternalNameStartingWithWithoutNPCs)
            NameSource.ITEM_NAME -> resultAdd(namePattern, start, lowercaseStart, NEUItems::findItemNameStartingWithWithoutNPCs)
            NameSource.GROUP -> resultAdd(groupPattern, start, uppercaseStart, ItemGroup::groupNameStartingWith)
            null -> {
                val lastSpaceIndex = start.indexOfLast { it == ' ' } + 1
                addAll(ItemGroup.groupStartingWith(uppercaseStart).map { it.name.substring(lastSpaceIndex) })
                addAll(NEUItems.findInternalNameStartingWithWithoutNPCs(uppercaseStart).map { it.substring(lastSpaceIndex) })
                if (size < 200) {
                    addAll(
                        NEUItems.findItemNameStartingWithWithoutNPCs(lowercaseStart).map { r ->
                            r.substring(lastSpaceIndex).replace(" ", "_")
                        },
                    )
                }
            }
        }
    }

    fun <T : CommandContextAwareObject> numberCalculate(args: Iterable<String>, context: T, use: (T, Long) -> Unit): Int {
        NEUCalculator.calculateOrNull(args.firstOrNull())?.toLong()?.let { use(context, it) } ?: {
            context.errorMessage = "Invalid number/calculation: '${args.firstOrNull()}'"
        }
        return args.firstOrNull()?.let { 1 } ?: 0
    }
}

interface CommandContextAwareObject {
    /** Setting this to none null will print the [errorMessage] as a user error and terminates the command handling */
    var errorMessage: String?

    /** Function that is executed after all arguments have been handled */
    fun post()
}

data class CommandArgument<T : CommandContextAwareObject>(
    val documentation: String,
    val prefix: String = "",
    /** -1 = invalid, -2 last element else index of the position of defaults */
    val defaultPosition: Int = -1,
    val validity: (T) -> Boolean = { true },
    val tabComplete: (String) -> Collection<String> = { emptyList() },
    val handler: (Iterable<String>, T) -> Int,
) {

    constructor(
        documentation: String,
        prefix: String = "",
        /** -1 = invalid, -2 last element else index of the position of defaults */
        defaultPosition: Int = -1,
        validity: (T) -> Boolean = { true },
        tabComplete: (String) -> Collection<String> = { emptyList() },
        noDocumentationFor: List<MutableSet<CommandArgument<T>>> = emptyList(),
        handler: (Iterable<String>, T) -> Int,
    ) : this(documentation, prefix, defaultPosition, validity, tabComplete, handler) {
        noDocumentationFor.forEach { it.add(this) }
    }

    override fun toString(): String = documentation

    fun getResult(
        args: Array<String>,
        lookup: Int,
        index: Int,
        context: T,
    ) = this.handler(args.slice((lookup + index + 1)..<args.size), context)

    companion object {
        private fun <A : CommandArgument<O>, O : CommandContextAwareObject> Collection<A>.findSpecifier(
            current: String,
            context: O,
            amountNoPrefixArguments: Int,
            amountNoPrefixArgumentsIncrement: () -> Unit,
        ): Pair<A?, Int> = (firstOrNull { it.prefix == current && it.validity(context) }?.let { it to 0 }
            ?: firstOrNull { it.defaultPosition == amountNoPrefixArguments && it.validity(context) }
                ?.let {
                    amountNoPrefixArgumentsIncrement()
                    it to -1
                }
            ?: firstOrNull { it.defaultPosition == -2 && it.validity(context) }?.let {
                amountNoPrefixArgumentsIncrement()
                it to -1
            } ?: (null to 0))

        fun <A : CommandArgument<O>, O : CommandContextAwareObject> Collection<A>.findSpecifierAndGetResult(
            args: Array<String>,
            index: Int,
            context: O,
            amountNoPrefixArguments: Int,
            amountNoPrefixArgumentsIncrement: () -> Unit,
        ): Int {
            val (spec, lookup) = findSpecifier(args[index], context, amountNoPrefixArguments, amountNoPrefixArgumentsIncrement)
            val result = spec?.getResult(args, lookup, index, context)
            return if (result == null) {
                context.errorMessage = "Unknown argument: '${args[index]}'"
                lookup + 1
            } else {
                lookup + 1 + result
            }
        }
    }
}
