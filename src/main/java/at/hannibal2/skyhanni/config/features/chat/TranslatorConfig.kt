package at.hannibal2.skyhanni.config.features.chat

import at.hannibal2.skyhanni.config.FeatureToggle
import at.hannibal2.skyhanni.features.chat.translation.TranslatableLanguage
import at.hannibal2.skyhanni.utils.OSUtils.openBrowser
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class TranslatorConfig {
    @Expose
    @ConfigOption(
        name = "Translate On Click",
        desc = "Click on a message to translate it to your language.\n" +
            "Use §e/shcopytranslation§7 to translate from English.\n" +
            "§cTranslation is not guaranteed to be 100% accurate."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    var translateOnClick: Boolean = false

    @ConfigOption(name = "Your Language", desc = "The language that messages should be translated to.")
    @Expose
    @ConfigEditorDropdown
    var languageName: Property<TranslatableLanguage> = Property.of(TranslatableLanguage.ENGLISH)

    @Expose
    @ConfigOption(
        name = "Language Code",
        desc = "If your language doesn't show in the dropdown, enter your language code here. " +
            "E.g. 'es' for Spanish or 'de' for German. Empty will use English."
    )
    @ConfigEditorText
    var languageCode: Property<String> = Property.of("en")

    @ConfigOption(name = "List of Language Codes", desc = "A list of Google Translate's supported language codes.")
    @ConfigEditorButton(buttonText = "Open")
    var langCodesURL: Runnable = Runnable {
        openBrowser(
            "https://cloud.google.com/translate/docs/languages#try-it-for-yourself"
        )
    }
}
