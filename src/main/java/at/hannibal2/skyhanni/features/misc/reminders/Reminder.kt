package at.hannibal2.skyhanni.features.misc.reminders

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import com.google.gson.annotations.Expose
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Duration

data class Reminder(
    @Expose var reason: String,
    @Expose var remindAt: SimpleTimeMark,
    @Expose var lastReminder: SimpleTimeMark = SimpleTimeMark.farPast(),
) {

    fun formatShort(): String {
        val time = getRemindTime()
        val date = time.toLocalDate()
        if (date.isEqual(LocalDate.now())) {
            return time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withDefaultLocale())
        }
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withDefaultLocale())
    }

    fun formatFull(): String = getRemindTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withDefaultLocale())

    fun shouldRemind(interval: Duration) = remindAt.isInPast() && lastReminder.passedSince() >= interval

    private fun getRemindTime(): ZonedDateTime = Instant.ofEpochMilli(remindAt.toMillis()).atZone(ZoneId.systemDefault())

    private fun DateTimeFormatter.withDefaultLocale(): DateTimeFormatter = withLocale(Locale.getDefault())
}

