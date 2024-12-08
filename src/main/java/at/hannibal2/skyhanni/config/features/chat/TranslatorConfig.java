package at.hannibal2.skyhanni.config.features.chat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.chat.translation.TranslatableLanguage;
import at.hannibal2.skyhanni.utils.OSUtils;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class TranslatorConfig {

    @Expose
    @ConfigOption(
        name = "Translate On Click",
        desc = "Click on a message to translate it to your language.\n" +
            "Use §e/shcopytranslation§7 to translate from English.\n" +
            "§cTranslation is not guaranteed to be 100% accurate."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean translateOnClick = false;

    @ConfigOption(name = "Your Language", desc = "The language that messages should be translated to.")
    @Expose
    @ConfigEditorDropdown
    public Property<TranslatableLanguage> languageName = Property.of(TranslatableLanguage.ENGLISH);

    @Expose
    @ConfigOption(
        name = "Language Code",
        desc = "If your language doesn't show in the dropdown, enter your language code here. " +
            "E.g. 'es' for Spanish or 'de' for German. Empty will use English."
    )
    @ConfigEditorText
    public Property<String> languageCode = Property.of("en");

    @ConfigOption(
        name = "List of Language Codes",
        desc = "A list of Google Translate's suppored language codes."
    )
    @ConfigEditorButton(buttonText = "Open")
    public Runnable langCodesURL = () -> OSUtils.openBrowser(
        "https://cloud.google.com/translate/docs/languages#try-it-for-yourself"
    );
}
