/**
 * @author Linnea Gräf
 */
package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.findStyledMatcher
import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.intoSpan
import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.matchStyledMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import java.util.Stack
import java.util.regex.Matcher
import java.util.regex.Pattern

object ComponentMatcherUtils {

    /**
     * Convert an [IChatComponent] into a [ComponentSpan], which allows taking substrings of the given component,
     * while preserving chat style.
     */
    fun IChatComponent.intoSpan(): ComponentSpan {
        val text = this.unformattedText
        return ComponentSpan(
            this,
            text,
            0,
            text.length
        )
    }

    /**
     * Create a styled matcher, analogous to [Pattern.matcher], but while preserving [ChatStyle].
     */
    fun Pattern.styledMatcher(span: ComponentSpan): ComponentMatcher {
        val matcher = matcher(span.getText())
        return ComponentMatcher(matcher, span)
    }

    /**
     * Equivalent to [matchMatcher], but while preserving [ChatStyle]
     */
    inline fun <T> Pattern.matchStyledMatcher(chat: IChatComponent, consumer: ComponentMatcher.() -> T) =
        matchStyledMatcher(chat.intoSpan(), consumer)

    /**
     * Equivalent to [matchMatcher], but while preserving [ChatStyle]
     */
    inline fun <T> Pattern.matchStyledMatcher(span: ComponentSpan, consumer: ComponentMatcher.() -> T) =
        styledMatcher(span).let { if (it.matches()) consumer(it) else null }

    /**
     * Equivalent to [findMatcher], but while preserving [ChatStyle]
     */
    inline fun <T> Pattern.findStyledMatcher(chat: IChatComponent, consumer: ComponentMatcher.() -> T) =
        findStyledMatcher(chat.intoSpan(), consumer)

    /**
     * Equivalent to [findMatcher], but while preserving [ChatStyle]
     */
    inline fun <T> Pattern.findStyledMatcher(span: ComponentSpan, consumer: ComponentMatcher.() -> T) =
        styledMatcher(span).let { if (it.find()) consumer(it) else null }
}

/**
 * This is an analogue for [Matcher], but for [ComponentSpan], therefore it is stateful, with [matches] and [find]
 * mutating this state. Direct usage of this class is recommended against in favor of using
 * [ComponentMatcherUtils.matchStyledMatcher] and [ComponentMatcherUtils.findStyledMatcher]
 */
class ComponentMatcher internal constructor(
    val matcher: Matcher,
    val span: ComponentSpan,
) {
    /**
     * Try to match the entire input component against the stored regex.
     */
    fun matches(): Boolean {
        return matcher.matches()
    }

    /**
     * Try to find the next match in the input component of the stored regex.
     */
    fun find(): Boolean {
        return matcher.find()
    }

    /**
     * Return a span equivalent to the entire match found by [matches] or [find]
     */
    fun group(): ComponentSpan {
        return this.span.slice(matcher.start(), matcher.end())
    }

    /**
     * Return a span equivalent to the group with the given index found by [matches] or [find]
     */
    fun group(index: Int): ComponentSpan? {
        val start = matcher.start(index)
        if (start < 0) return null
        return this.span.slice(start, matcher.end(index))
    }

    /**
     * Return a span equivalent to the group with the given name found by [matches] or [find]
     */
    fun group(name: String): ComponentSpan? {
        val start = matcher.start(name)
        if (start < 0) return null
        return this.span.slice(start, matcher.end(name))
    }

    /**
     * Return a span equivalent to the group with the given name found by [matches] or [find]
     */
    fun component(name: String): IChatComponent? {
        return group(name)?.intoComponent()
    }

    /**
     * Return a span equivalent to the group with the given name found by [matches] or [find].
     * Returns not nullable object, or throws an error.
     */
    fun groupOrThrow(name: String): ComponentSpan {
        return group(name) ?: error("group '$name' not found in ComponentSpan!")
    }

    /**
     * Return a IChatComponent equivalent to the group with the given name found by [matches] or [find].
     * Returns not nullable object, or throws an error.
     */
    fun componentOrThrow(name: String): IChatComponent {
        return groupOrThrow(name).intoComponent()
    }
}

/**
 * Represents a substring of a [IChatComponent], preserving the [chat style][IChatComponent.getChatStyle].
 * This class always deals in what the chat component APIs call [unformatted text][IChatComponent.getUnformattedText].
 * This text may contain formatting codes, but will not have additional formatting codes inserted based on the chat
 * style. Specifically, it will look at the [IChatComponent.getUnformattedTextForChat], which *excludes* the text from
 * the siblings/children of a chat component. To make sure that internal cached states are upheld, use
 * [ComponentMatcherUtils.intoSpan] instead of the constructor.
 */
class ComponentSpan internal constructor(
    val textComponent: IChatComponent,
    private val cachedText: String,
    val start: Int, val end: Int,
) {
    init {
        require(start <= end)
        require(0 <= start)
        require(end <= cachedText.length)
    }

    /**
     * Returns the text length of this span.
     */
    val length = end - start

    /**
     * Slice this component span. This is equivalent to the [String.substring] operation on the [text][getText].
     */
    fun slice(start: Int = 0, end: Int = length): ComponentSpan {
        require(0 <= start) { "start is bigger than 0: start=$start, cachedText=$cachedText" }
        require(start <= end) { "start is bigger than end: start=$start, end=$end, cachedText=$cachedText" }
        require(end <= length) { "end is bigger than length: end=$end, length=$length, cachedText=$cachedText" }
        return ComponentSpan(textComponent, cachedText, this.start + start, this.start + end)
    }

    /**
     * Returns the text (without any chat style formatting applied). May still contain formatting codes.
     */
    fun getText() = cachedText.substring(start, end)

    /**
     * Sample the chat style at the start of the span.
     */
    fun sampleStyleAtStart(): ChatStyle? = sampleAtStart().chatStyle

    /**
     * Sample all the components that intersect with this span. Note that some of the returned components may contain
     * children/siblings that are not intersecting this span, and that some of the returned components may only
     * partially intersect with this span.
     */
    fun sampleComponents(): List<IChatComponent> {
        return sampleSlicedComponents().map { it.first }
    }

    /**
     * Sample all the components that intersect with this span, with their respective indices. This behaves like
     * [sampleComponents], but it will also return indices indicating which part of the
     * [IChatComponent.getUnformattedTextForChat] is actually intersecting with this span.
     *
     * ```
     * val firstComponent = this.sampleSlicedComponents().first()
     * firstComponent.first.getUnformattedTextForChat().substring(firstComponent.second, firstComponent.third)
     * ```
     *
     * @see intoComponent
     */
    fun sampleSlicedComponents(): List<Triple<IChatComponent, Int, Int>> {
        var index = start
        val workStack = Stack<IChatComponent>()
        workStack.push(textComponent)
        var lastComponent = textComponent
        val listBuilder = mutableListOf<Triple<IChatComponent, Int, Int>>()
        while (workStack.isNotEmpty()) {
            val currentComponent = workStack.pop()
            if (index + length <= 0) {
                break
            }
            for (sibling in currentComponent.siblings.reversed()) {
                workStack.push(sibling)
            }
            val rawText = currentComponent.unformattedTextForChat
            index -= rawText.length
            if (index < 0) {
                listBuilder.add(
                    Triple(
                        currentComponent,
                        (rawText.length + index).coerceAtLeast(0),
                        (rawText.length + index + length).coerceAtMost(rawText.length)
                    )
                )
            }
            lastComponent = currentComponent
        }
        if (listBuilder.isEmpty())
            listBuilder.add(Triple(lastComponent, 0, 0))
        return listBuilder
    }

    /**
     * Returns the first [chat component][IChatComponent] that intersects with this span.
     */
    fun sampleAtStart(): IChatComponent {
        return sampleComponents().first()
    }

    /**
     * Create an [IChatComponent] that looks identical to this slice, including hover events and such.
     * This new chat component will be structurally different (flat) and not therefore not have the same property
     * inheritances as the old [textComponent]. Be therefore careful when modifying styles. This new component will also
     * only use [ChatComponentText], converting any other [IChatComponent] in the process.
     */
    fun intoComponent(): IChatComponent {
        val parent = ChatComponentText("")
        parent.chatStyle = ChatStyle()
        for ((component, start, end) in sampleSlicedComponents()) {
            val copy = ChatComponentText(component.unformattedTextForChat.substring(start, end))
            copy.chatStyle = component.chatStyle.createDeepCopy()
            parent.appendSibling(copy)
        }
        return parent
    }

    /**
     * Returns a list of all the styles that intersect with this span.
     */
    fun sampleStyles(): List<ChatStyle> {
        return sampleComponents().map { it.chatStyle }
    }

    /**
     * Strip extra `§r` color codes from the beginning / end of this span.
     */
    fun stripHypixelMessage(): ComponentSpan {
        var start = 0
        var newString = getText()
        var end = this.length
        while (newString.startsWith("§r")) {
            start += 2
            newString = newString.substring(2)
        }
        while (newString.endsWith("§r")) {
            newString = newString.substring(0, newString.length - 2)
            end -= 2
        }
        return slice(start, end)
    }

    /**
     * Remove a prefix from this span if it exists. Otherwise, return this unchanged.
     */
    fun removePrefix(prefix: String): ComponentSpan {
        if (!getText().startsWith(prefix)) return this
        return slice(prefix.length)
    }

    /**
     * Remove a suffix from this span if it exists. Otherwise, return this unchanged.
     */
    fun removeSuffix(suffix: String): ComponentSpan {
        if (!getText().endsWith(suffix)) return this
        return slice(end = length - suffix.length)
    }

    /**
     * Append another [ComponentSpan] to this one to create a new one. This will [flatten][intoComponent] the hierarchy
     * of the underlying [IChatComponent] but preserve formatting.
     */
    operator fun plus(other: ComponentSpan): ComponentSpan {
        val left = this.intoComponent()
        val right = other.intoComponent()
        left.appendSibling(right)
        return left.intoSpan()
    }

    companion object {
        fun empty(): ComponentSpan {
            return ComponentSpan(ChatComponentText(""), "", 0, 0)
        }
    }

}
