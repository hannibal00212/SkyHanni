package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.hypixel.chat.event.SystemMessageEvent
import at.hannibal2.skyhanni.mixins.transformers.AccessorChatComponentText
import at.hannibal2.skyhanni.utils.ColorUtils.getFirstColorCode
import at.hannibal2.skyhanni.utils.GuiRenderUtils.darkenColor
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.findAll
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiUtilRenderComponents
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import java.util.Base64
import java.util.NavigableMap
import java.util.UUID
import java.util.function.Predicate
import java.util.regex.Matcher

object StringUtils {
    private val whiteSpaceResetPattern = "^(?:\\s|§r)*|(?:\\s|§r)*$".toPattern()
    private val whiteSpacePattern = "^\\s*|\\s*$".toPattern()
    private val resetPattern = "(?i)§R".toPattern()
    private val sFormattingPattern = "(?i)§S".toPattern()
    private val stringColorPattern = "§[0123456789abcdef].*".toPattern()
    private val asciiPattern = "[^\\x00-\\x7F]".toPattern()
    private val minecraftColorCodesPattern = "(?i)(§[0-9a-fklmnor])+".toPattern()
    private val lettersAndNumbersPattern = "(§.)|[^a-zA-Z0-9 ]".toPattern()
    fun String.removeAllNonLettersAndNumbers(): String = lettersAndNumbersPattern.matcher(this).replaceAll("")
    fun String.cleanString(): String = removeAllNonLettersAndNumbers().trimWhiteSpaceAndResets().lowercase()

    fun String.trimWhiteSpaceAndResets(): String = whiteSpaceResetPattern.matcher(this).replaceAll("")
    fun String.trimWhiteSpace(): String = whiteSpacePattern.matcher(this).replaceAll("")
    fun String.removeResets(): String = resetPattern.matcher(this).replaceAll("")
    fun String.removeSFormattingCode(): String = sFormattingPattern.matcher(this).replaceAll("")
    fun String.removeNonAscii(): String = asciiPattern.matcher(this).replaceAll("")

    fun String.firstLetterUppercase(): String {
        if (isEmpty()) return this

        val lowercase = lowercase()
        val first = lowercase[0].uppercase()
        return first + lowercase.substring(1)
    }

    private val formattingChars = "kmolnrKMOLNR".toSet()
    private val colorChars = "abcdefABCDEF0123456789".toSet()

    /**
     * Removes color and optionally formatting codes from the given string, leaving plain text.
     *
     * @param keepFormatting Boolean indicating whether to retain non-color formatting codes (default: false).
     * @return A string with color codes removed (and optionally formatting codes if specified).
     */
    fun CharSequence.removeColor(keepFormatting: Boolean = false): String {
        // Glossary:
        // Formatting indicator: The '§' character indicating the beginning of a formatting sequence
        // Formatting code: The character following a formatting indicator which specifies what color or text style this sequence corresponds to
        // Formatting sequence: The combination of a formatting indicator and code that changes the color or format of a string

        // Flag for whether there is a text style (non-color and non-reset formatting code) currently being applied
        var isFormatted = false

        // Find the first formatting indicator
        var nextFormattingSequence = indexOf('§')

        // If this string does not contain any formatting indicators, just return this string directly
        if (nextFormattingSequence < 0) return this.toString()

        // Let's create a new string, and pre-allocate enough space to store this entire string
        val cleanedString = StringBuilder(this.length)

        // Read index stores the position in `this` which we have written up until now
        // a/k/a where we need to start reading from
        var readIndex = 0

        // As long as there still is a formatting indicator left in our string
        while (nextFormattingSequence >= 0) {

            // Write everything from the read index up to the next formatting indicator into our clean string
            cleanedString.append(this, readIndex, nextFormattingSequence)

            // Get the formatting code (note: this may not be a valid formatting code)
            val formattingCode = this.getOrNull(nextFormattingSequence + 1)

            // If the next formatting sequence's code indicates a non-color format and we should keep those
            if (keepFormatting && formattingCode in formattingChars) {
                // Update formatted flag based on whether this is a reset or a style format code
                isFormatted = formattingCode?.lowercaseChar() != 'r'

                // Set the readIndex to the formatting indicator, so that the next loop will start writing from that paragraph symbol
                readIndex = nextFormattingSequence
                // Find the next § symbol after the formatting sequence
                nextFormattingSequence = indexOf('§', startIndex = readIndex + 1)
            } else {
                // If this formatting sequence should be skipped (either a color code, or !keepFormatting or an incomplete formatting sequence without a code)

                // If being formatted and color code encountered, reset the current formatting code
                if (isFormatted && formattingCode in colorChars) {
                    cleanedString.append("§r")
                    isFormatted = false
                }

                // Set the readIndex to after this formatting sequence, so that the next loop will skip over it before writing the string
                readIndex = nextFormattingSequence + 2
                // Find the next § symbol after the formatting sequence
                nextFormattingSequence = indexOf('§', startIndex = readIndex)

                // If the next read would be out of bound, reset the readIndex to the very end of the string, resulting in a "" string to be appended
                readIndex = readIndex.coerceAtMost(this.length)
            }
        }
        // Finally, after the last formatting sequence was processed, copy over the last sequence of the string
        cleanedString.append(this, readIndex, this.length)

        // And turn the string builder into a string
        return cleanedString.toString()
    }

    /**
     * From https://stackoverflow.com/questions/10711494/get-values-in-treemap-whose-string-keys-start-with-a-pattern
     */
    fun <T> subMapOfStringsStartingWith(prefix: String, map: NavigableMap<String, T>): NavigableMap<String, T> {
        if ("" == prefix) return map
        val lastKey = nextLexicographicallyStringWithSameLength(prefix)
        return map.subMap(prefix, true, lastKey, false)
    }

    fun nextLexicographicallyStringWithSameLength(input: String): String {
        val lastCharPosition = input.length - 1
        val inputWithoutLastChar = input.substring(0, lastCharPosition)
        val lastChar = input[lastCharPosition]
        val incrementedLastChar = (lastChar.code + 1).toChar()
        return inputWithoutLastChar + incrementedLastChar
    }

    fun UUID.toDashlessUUID(): String = toString().replace("-", "")

    private fun String.internalCleanPlayerName(): String {
        val split = trim().split(" ")
        return if (split.size > 1) {
            split[1].removeColor()
        } else {
            split[0].removeColor()
        }
    }

    fun String.cleanPlayerName(displayName: Boolean = false): String {
        return if (displayName) {
            if (SkyHanniMod.feature.chat.playerMessage.playerRankHider) {
                // TODO custom color
                "§b" + internalCleanPlayerName()
            } else this
        } else {
            internalCleanPlayerName()
        }
    }

    fun getColor(string: String, default: Int, darker: Boolean = true): Int {
        val matcher = stringColorPattern.matcher(string)
        if (matcher.matches()) {
            val colorInt = Minecraft.getMinecraft().fontRendererObj.getColorCode(string[1])
            return if (darker) {
                colorInt.darkenColor()
            } else {
                "ff${Integer.toHexString(colorInt)}".toLong(radix = 16).toInt()
            }
        }
        return default
    }

    fun String.substringBeforeLastOrNull(needle: String): String? {
        val index = this.lastIndexOf(needle)
        if (index < 0) return null
        return this.substring(0, index)
    }

    fun encodeBase64(input: String) = Base64.getEncoder().encodeToString(input.toByteArray())

    fun decodeBase64(input: String) = Base64.getDecoder().decode(input).decodeToString()

    fun addFormat(text: String, format: String): String {
        if (text.length < 2) return text

        val rawText = text.substring(2)
        return if (rawText == text.removeColor()) {
            val originalColor = text.substring(0, 2)
            "$originalColor$format$rawText"
        } else {
            "$format$text"
        }
    }

    fun String.removeWordsAtEnd(i: Int) = split(" ").dropLast(i).joinToString(" ")
    fun Double.removeUnusedDecimal() = if (this % 1 == 0.0) this.toInt().toString() else this.toString()

    fun String.splitLines(width: Int): String = GuiUtilRenderComponents.splitText(
        ChatComponentText(this),
        width,
        Minecraft.getMinecraft().fontRendererObj,
        false,
        false,
    ).joinToString("\n") {
        val text = it.formattedText
        val formatCode = Regex("(?:§[a-f0-9l-or]|\\s)*")
        formatCode.matchAt(text, 0)?.let { matcher ->
            val codes = matcher.value.replace("\\s".toRegex(), "")
            codes + text.removeRange(matcher.range)
        } ?: text
    }

    /**
     * Creates a comma-separated list using natural formatting (a, b, and c).
     * @param list - the list of strings to join into a string, containing 0 or more elements.
     * @param delimiterColor - the color code of the delimiter, inserted before each delimiter (commas and "and").
     * @return a string representing the list joined with the Oxford comma and the word "and".
     */
    fun List<String>.createCommaSeparatedList(delimiterColor: String = ""): String {
        if (this.isEmpty()) return ""
        if (this.size == 1) return this[0]
        if (this.size == 2) return "${this[0]}$delimiterColor and ${this[1]}"
        val lastIndex = this.size - 1
        val allButLast = this.subList(0, lastIndex).joinToString("$delimiterColor, ")
        return "$allButLast$delimiterColor, and ${this[lastIndex]}"
    }

    fun pluralize(number: Int, singular: String, plural: String? = null, withNumber: Boolean = false): String {
        val pluralForm = plural ?: "${singular}s"
        var str = if (number == 1 || number == -1) singular else pluralForm
        if (withNumber) str = "${number.addSeparators()} $str"
        return str
    }

    fun progressBar(percentage: Double, steps: Int = 24): Any {
        // '§5§o§2§l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §f§l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §l§m §r §e348,144.3§6/§e936k'
        val prefix = "§5§o§2"
        val step = "§l§m "
        val missing = "§f"
        val end = "§r"

        val builder = StringBuilder()
        var inMissingArea = false
        builder.append(prefix)
        for (i in 0..steps) {
            val toDouble = i.toDouble()
            val stepPercentage = toDouble / steps
            if (stepPercentage >= percentage && !inMissingArea) {
                builder.append(missing)
                inMissingArea = true
            }
            builder.append(step)
        }
        builder.append(end)
        return builder.toString()
    }

    fun String.capAtMinecraftLength(limit: Int) =
        capAtLength(limit) { Minecraft.getMinecraft().fontRendererObj.getCharWidth(it) }

    private fun String.capAtLength(limit: Int, lengthJudger: (Char) -> Int): String {
        var i = 0
        return takeWhile {
            i += lengthJudger(it)
            i < limit
        }
    }

    // recursively goes through the chat component until an action is completed
    fun modifyFirstChatComponent(chatComponent: IChatComponent, action: Predicate<IChatComponent>): Boolean {
        if (action.test(chatComponent)) {
            return true
        }
        for (sibling in chatComponent.siblings) {
            if (modifyFirstChatComponent(sibling, action)) {
                return true
            }
        }
        return false
    }

    // replaces a word without breaking any chat components
    fun replaceFirstChatText(chatComponent: IChatComponent, toReplace: String, replacement: String): IChatComponent {
        modifyFirstChatComponent(chatComponent) { component ->
            if (component is ChatComponentText) {
                component as AccessorChatComponentText
                val componentText = component.text_skyhanni()
                if (componentText.contains(toReplace)) {
                    component.setText_skyhanni(componentText.replace(toReplace, replacement))
                    return@modifyFirstChatComponent true
                }
                return@modifyFirstChatComponent false
            }
            return@modifyFirstChatComponent false
        }
        return chatComponent
    }

    fun String.getPlayerNameFromChatMessage(): String? = matchPlayerChatMessage(this)?.group("username")

    fun String.getPlayerNameAndRankFromChatMessage(): String? = matchPlayerChatMessage(this)?.group("rankedName")

    private fun matchPlayerChatMessage(string: String): Matcher? {
        var username = ""
        var matcher = UtilsPatterns.playerChatPattern.matcher(string)
        if (matcher.matches()) {
            username = matcher.group("important").removeResets()
        }
        if (username == "") return null

        if (username.contains("[NPC]")) {
            return null
        }

        if (username.contains(">")) {
            username = username.substring(username.indexOf('>') + 1).trim()
        }

        username = username.removePrefix("§dFrom ")
        username = username.removePrefix("§dTo ")

        matcher = UtilsPatterns.chatUsernamePattern.matcher(username)
        return if (matcher.matches()) matcher else null
    }

    fun String.convertToFormatted(): String = this.replace("&&", "§")

    fun String.allLettersFirstUppercase() = split("_").joinToString(" ") { it.firstLetterUppercase() }

    fun String?.equalsIgnoreColor(string: String?) = this?.let { it.removeColor() == string?.removeColor() } ?: false

    fun String.isRoman(): Boolean = UtilsPatterns.isRomanPattern.matches(this)

    fun isEmpty(message: String): Boolean = message.removeColor().trimWhiteSpaceAndResets().isEmpty()

    fun generateRandomId() = UUID.randomUUID().toString()

    fun String.insert(pos: Int, chars: CharSequence): String = this.substring(0, pos) + chars + this.substring(pos)

    fun String.insert(pos: Int, char: Char): String = this.substring(0, pos) + char + this.substring(pos)

    fun replaceIfNeeded(
        original: ChatComponentText,
        newText: String,
    ): ChatComponentText? {
        return replaceIfNeeded(original, ChatComponentText(newText))
    }

    private val colorMap = EnumChatFormatting.entries.associateBy { it.toString()[1] }
    fun enumChatFormattingByCode(char: Char): EnumChatFormatting? {
        return colorMap[char]
    }

    fun doLookTheSame(left: IChatComponent, right: IChatComponent): Boolean {
        class ChatIterator(var component: IChatComponent) {
            var queue = mutableListOf<IChatComponent>()
            var idx = 0
            var colorOverride = ChatStyle()
            fun next(): Pair<Char, ChatStyle>? {
                while (true) {
                    while (idx >= component.unformattedTextForChat.length) {
                        queue.addAll(0, component.siblings)
                        colorOverride = ChatStyle()
                        component = queue.removeFirstOrNull() ?: return null
                    }
                    val char = component.unformattedTextForChat[idx++]
                    if (char == '§' && idx < component.unformattedTextForChat.length) {
                        val formattingChar = component.unformattedTextForChat[idx++]
                        val formatting = enumChatFormattingByCode(formattingChar) ?: continue
                        when (formatting) {
                            EnumChatFormatting.OBFUSCATED -> {
                                colorOverride.setObfuscated(true)
                            }

                            EnumChatFormatting.BOLD -> {
                                colorOverride.setBold(true)
                            }

                            EnumChatFormatting.STRIKETHROUGH -> {
                                colorOverride.setStrikethrough(true)
                            }

                            EnumChatFormatting.UNDERLINE -> {
                                colorOverride.setUnderlined(true)
                            }

                            EnumChatFormatting.ITALIC -> {
                                colorOverride.setItalic(true)
                            }

                            else -> {
                                colorOverride = ChatStyle().setColor(formatting)
                            }
                        }
                    } else {
                        return Pair(char, colorOverride.setParentStyle(component.chatStyle))
                    }
                }
            }
        }

        val leftIt = ChatIterator(left)
        val rightIt = ChatIterator(right)
        while (true) {
            val leftChar = leftIt.next()
            val rightChar = rightIt.next()
            if (leftChar == null && rightChar == null) return true
            if (leftChar != rightChar) return false
        }
    }

    fun <T : IChatComponent> replaceIfNeeded(
        original: T,
        newText: T,
    ): T? {
        if (doLookTheSame(original, newText)) return null
        return newText
    }

    private fun addComponent(foundCommands: MutableList<IChatComponent>, message: IChatComponent) {
        val clickEvent = message.chatStyle.chatClickEvent
        if (clickEvent != null) {
            if (foundCommands.size == 1 && foundCommands[0].chatStyle.chatClickEvent?.value == clickEvent.value) {
                return
            }
            foundCommands.add(message)
        }
    }

    /**
     * Applies a transformation on the message of a SystemMessageEvent if possible.
     */
    fun SystemMessageEvent.applyIfPossible(transform: (String) -> String) {
        val original = chatComponent.formattedText
        val new = transform(original)
        if (new == original) return

        val clickEvents = mutableListOf<ClickEvent>()
        val hoverEvents = mutableListOf<HoverEvent>()
        chatComponent.findAllEvents(clickEvents, hoverEvents)

        if (clickEvents.size > 1 || hoverEvents.size > 1) return

        chatComponent = ChatComponentText(new)
        if (clickEvents.size == 1) chatComponent.chatStyle.chatClickEvent = clickEvents.first()
        if (hoverEvents.size == 1) chatComponent.chatStyle.chatHoverEvent = hoverEvents.first()
    }

    private fun IChatComponent.findAllEvents(
        clickEvents: MutableList<ClickEvent>,
        hoverEvents: MutableList<HoverEvent>,
    ) {
        siblings.forEach { it.findAllEvents(clickEvents, hoverEvents) }

        val clickEvent = chatStyle.chatClickEvent
        val hoverEvent = chatStyle.chatHoverEvent

        if (clickEvent?.action != null && clickEvents.none { it.value == clickEvent.value }) {
            clickEvents.add(clickEvent)
        }
        if (hoverEvent?.action != null && hoverEvents.none { it.value == hoverEvent.value }) {
            hoverEvents.add(hoverEvent)
        }
    }

    fun String.replaceAll(oldValue: String, newValue: String, ignoreCase: Boolean = false): String {
        var text = this
        while (true) {
            val newText = text.replace(oldValue, newValue, ignoreCase = ignoreCase)
            if (newText == text) {
                return text
            }
            text = newText
        }
    }

    /**
     * Removes starting and ending reset formattings that don't sever a benefit at all.
     */
    fun String.stripHypixelMessage(): String {
        var message = this

        while (message.startsWith("§r")) {
            message = message.substring(2)
        }
        while (message.endsWith("§r")) {
            message = message.substring(0, message.length - 2)
        }
        return message
    }

    fun String.applyFormattingFrom(original: ComponentSpan): IChatComponent {
        val text = ChatComponentText(this)
        text.chatStyle = original.sampleStyleAtStart()
        return text
    }

    fun String.applyFormattingFrom(original: IChatComponent): IChatComponent {
        val text = ChatComponentText(this)
        text.chatStyle = original.chatStyle
        return text
    }

    fun String.toCleanChatComponent(): IChatComponent = ChatComponentText(this)

    fun IChatComponent.contains(string: String): Boolean = formattedText.contains(string)

    fun String.width(): Int {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(this)
    }

    private val vowels = "aeiouAEIOU".toSet()

    fun Char.isVowel(): Boolean = this in vowels

    fun String.lastColorCode(): String? = minecraftColorCodesPattern.findAll(this).lastOrNull()

    fun String.isValidUuid(): Boolean {
        return try {
            UUID.fromString(this)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun optionalAn(string: String): String {
        if (string.isEmpty()) return ""
        return if (string[0] in "aeiou") "an" else "a"
    }

    fun String.addStrikethorugh(strikethorugh: Boolean = true): String {
        if (!strikethorugh) return this

        val firstColor = getFirstColorCode()
        val clean = removeColor()
        return "§$firstColor§m$clean"
    }
}
