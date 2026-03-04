package info.sorbon.hskvocabulary.domain.model

enum class Language(
    val code: String,
    val flag: String,
    val nameInEnglish: String,
    val nameInLanguage: String
) {
    English("en", "\uD83C\uDDEC\uD83C\uDDE7", "English", "English"),
    Russian("ru", "\uD83C\uDDF7\uD83C\uDDFA", "Russian", "\u0420\u0443\u0441\u0441\u043A\u0438\u0439"),
    Tajik("tj", "\uD83C\uDDF9\uD83C\uDDEF", "Tajik", "\u0422\u043E\u04B7\u0438\u043A\u04E3"),
    German("de", "\uD83C\uDDE9\uD83C\uDDEA", "German", "Deutsch"),
    French("fr", "\uD83C\uDDEB\uD83C\uDDF7", "French", "Fran\u00E7ais"),
    Japanese("jp", "\uD83C\uDDEF\uD83C\uDDF5", "Japanese", "\u65E5\u672C\u8A9E"),
    Arabic("ar", "\uD83C\uDDF8\uD83C\uDDE6", "Arabic", "\u0627\u0644\u0639\u0631\u0628\u064A\u0629"),
    Spanish("es", "\uD83C\uDDEA\uD83C\uDDF8", "Spanish", "Espa\u00F1ol"),
    Italian("it", "\uD83C\uDDEE\uD83C\uDDF9", "Italian", "Italiano"),
    Khmer("km", "\uD83C\uDDF0\uD83C\uDDED", "Khmer", "\u1781\u17D2\u1798\u17C2\u179A"),
    Korean("ko", "\uD83C\uDDF0\uD83C\uDDF7", "Korean", "\uD55C\uAD6D\uC5B4"),
    Portuguese("pt", "\uD83C\uDDE7\uD83C\uDDF7", "Portuguese", "Portugu\u00EAs"),
    Thai("th", "\uD83C\uDDF9\uD83C\uDDED", "Thai", "\u0E44\u0E17\u0E22"),
    Vietnamese("vi", "\uD83C\uDDFB\uD83C\uDDF3", "Vietnamese", "Ti\u1EBFng Vi\u1EC7t");

    /**
     * Room column key matching iOS coreDataKey.
     * English uses "definition"; others use "definition_<code>".
     */
    val roomColumnKey: String
        get() = when (this) {
            English -> "definition"
            else -> "definition_${code}"
        }

    companion object {
        val available: List<Language> = entries.toList()

        fun fromCode(code: String): Language? = entries.find { it.code == code }

        fun fromDeviceLocale(localeTag: String): Language? {
            val langCode = localeTag.substringBefore("-").substringBefore("_").lowercase()
            return when (langCode) {
                "ja" -> Japanese
                "tg" -> Tajik
                else -> fromCode(langCode)
            }
        }
    }
}
