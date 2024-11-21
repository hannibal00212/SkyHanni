package at.hannibal2.skyhanni.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class DateFormat(pattern: String) {
    US_SLASH_MMDDYYYY("MM/dd/yyyy"),
    US_DASH_MMDDYYYY("MM-dd-yyyy"),
    UK_DDMMYYYY("dd/MM/yyyy"),
    UK_DASH_DDMMYYYY("dd-MM-yyyy"),
    ISO_YYYYMMDD("yyyy-MM-dd"),
    FULL_MONTH_DAY_YEAR("MMMM dd, yyyy"),
    SHORT_MONTH_DAY_YEAR("MMM dd, yyyy"),
    YEAR_MONTH_DAY("yyyy/MM/dd"),
    YEAR_DAY_MONTH("yyyy/dd/MM"),
    COMPACT_YYYYMMDD("yyyyMMdd"),
    DAY_MONTH_YEAR("dd MMMM yyyy"),
    DAY_MONTH_YEAR_SHORT("dd MMM yyyy");

    private val formatter = DateTimeFormatter.ofPattern(pattern)

    override fun toString() = LocalDate.now().format(formatter)
}

