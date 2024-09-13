package at.hannibal2.skyhanni.config.features.chat;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.features.chat.translation.TranslatableLanguage;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class TranslatorConfig {

    @Expose
    @ConfigOption(
        name = "Translate On Click",
        desc = "Click on a message to translate it to English.\n" +
            "Use §e/shcopytranslation§7 to translate from English.\n" +
            "§cTranslation is not guaranteed to be 100% accurate."
    )
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean translateOnClick = false;

    @ConfigOption(name = "Language Name", desc = "The name of the language selected below. Note that languages marked as unknown might still be supported.")
    @Expose
    @ConfigEditorDropdown
    public Property<TranslatableLanguage> languageName = Property.of(TranslatableLanguage.ENGLISH);

    @Expose
    @ConfigOption(
        name = "Language Code",
        desc = "Enter a language code here to translate on chat click into another language. " +
            "E.g. `es` for spanish or 'de' for german. Empty for english.")
    @ConfigEditorText
    public Property<String> languageCode = Property.of("en");
}
