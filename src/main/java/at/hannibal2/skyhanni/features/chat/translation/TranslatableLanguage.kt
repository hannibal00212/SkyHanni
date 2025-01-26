package at.hannibal2.skyhanni.features.chat.translation

enum class TranslatableLanguage(private val englishName: String, private val nativeName: String, val languageCode: String) {

    // 1. First Language - The primary language of the application.
    ENGLISH("English", "", "en"),

    // 2. Well Supported - Major languages commonly used in Europe and North America.
    SPANISH("Spanish", "Español", "es"), // Major language in Spain and Latin America
    GERMAN("German", "Deutsch", "de"), // Important language in Germany, Austria, and Switzerland
    FRENCH("French", "Français", "fr"), // Significant language in France, Canada, and parts of Africa
    DUTCH("Dutch", "Nederlands", "nl"), // Spoken in the Netherlands and Belgium
    RUSSIAN("Russian", "Русский", "ru"), // Major language in Russia and other parts of Eastern Europe and Central Asia
    POLISH("Polish", "Polski", "pl"), // Spoken primarily in Poland
    ITALIAN("Italian", "Italiano", "it"), // Important language in Italy and parts of Switzerland
    UKRAINIAN("Ukrainian", "Українська", "uk"), // Spoken in Ukraine
    PORTUGUESE("Portuguese", "Português", "pt"), // Spoken in Portugal and Brazil
    TURKISH("Turkish", "Türkçe", "tr"), // Significant in Turkey and Central Asia
    SWEDISH("Swedish", "Svenska", "sv"), // Relevant in Northern Europe

    // 3. Global Languages - Widely spoken languages with significant global presence.
    CHINESE("Chinese", "中文", "zh"), // Major language in China and other parts of East Asia
    ARABIC("Arabic", "العربية", "ar"), // Significant language in the Middle East and North Africa
    JAPANESE("Japanese", "日本語", "ja"), // Major language in Japan
    HINDI("Hindi", "हिन्दी", "hi"), // Major language in India
    BENGALI("Bengali", "বাংলা", "bn"), // Widely spoken in India and Bangladesh
    KOREAN("Korean", "한국어", "ko"), // Important for East Asia
    VIETNAMESE("Vietnamese", "Tiếng Việt", "vi"), // Major language in Vietnam
    INDONESIAN("Indonesian", "Bahasa Indonesia", "id"), // Key language in Southeast Asia
    THAI("Thai", "ภาษาไทย", "th"), // Important in Thailand

    // 4. Other Supported Languages
    PERSIAN("Persian", "فارسی", "fa"), // Spoken in Iran and other parts of the Middle East
    TAGALOG("Tagalog", "Tagalog", "tl"), // Major language in the Philippines
    PUNJABI("Punjabi", "ਪੰਜਾਬੀ", "pa"), // Significant in India and Pakistan

    // 5. Other Language
    UNKNOWN("Other", "", ""),
    ;

    // Limit to 20 characters so that the text is not too small in the config
    private val displayName: String = if (nativeName.isBlank()) englishName else "$englishName/$nativeName".take(20)

    override fun toString(): String = displayName
}
