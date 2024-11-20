package at.hannibal2.skyhanni.features.misc.reminders

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.TimeUtils.minutes
import at.hannibal2.skyhanni.utils.chat.Text
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.chat.Text.command
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.chat.Text.send
import at.hannibal2.skyhanni.utils.chat.Text.suggest
import at.hannibal2.skyhanni.utils.chat.Text.wrap
import net.minecraft.util.IChatComponent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ReminderManager {

    private const val REMINDERS_PER_PAGE = 10

    // Random numbers chosen, this will be used to delete the old list and action messages
    private const val REMINDERS_LIST_ID = -546745
    private const val REMINDERS_ACTION_ID = -546746
    private const val REMINDERS_MESSAGE_ID = -546747

    private val storage get() = SkyHanniMod.feature.storage.reminders
    private val config get() = SkyHanniMod.feature.misc.reminders

    private fun getSortedReminders() = storage.entries.sortedBy { it.value.remindAt }

    private fun sendMessage(message: String) = Text.join("§e[Reminder]", " ", message).send(REMINDERS_ACTION_ID)

    private fun parseDuration(text: String): Duration? = try {
        val duration = TimeUtils.getDuration(text)
        if (duration <= 1.seconds) null else duration
    } catch (e: Exception) {
        null
    }

    private fun listReminders(page: Int) {
        Text.displayPaginatedList(
            "SkyHanni Reminders",
            getSortedReminders(),
            chatLineId = REMINDERS_LIST_ID,
            emptyMessage = "No reminders found.",
            currentPage = page,
            maxPerPage = REMINDERS_PER_PAGE,
        ) { reminderEntry ->
            val id = reminderEntry.key
            val reminder = reminderEntry.value
            Text.join(
                "§c✕".asComponent {
                    hover = "§7Click to remove".asComponent()
                    command = "/shremind remove -l $id"
                }.wrap("§8[", "§8]"),
                " ",
                "§e✎".asComponent {
                    hover = "§7Click to start editing".asComponent()
                    suggest = "/shremind edit -l $id ${reminder.reason} "
                }.wrap("§8[", "§8]"),
                " ",
                "§6${reminder.formatShort()}".asComponent {
                    hover = "§7${reminder.formatFull()}".asComponent()
                }.wrap("§8[", "§8]"),
                " ",
                "§7${reminder.reason}",
            )
        }
    }

    private fun createReminder(args: Array<String>) {
        if (args.size < 2) return help()

        val time = parseDuration(args.first()) ?: return ChatUtils.userError("Invalid time format")
        val reminder = args.drop(1).joinToString(" ")
        val remindAt = SimpleTimeMark.now().plus(time)

        storage[StringUtils.generateRandomId()] = Reminder(reminder, remindAt)
        sendMessage("§6Reminder set for ${time.format()}")
    }

    private fun actionReminder(
        args: List<String>,
        command: String,
        vararg arguments: String,
        action: (List<String>, Reminder) -> String,
    ) {
        val argumentText = arguments.joinToString(" ")
        if (args.size < arguments.size) return ChatUtils.userError("/shremind $command $argumentText")

        if (args.first() == "-l") {
            if (args.size < arguments.size + 1) return ChatUtils.userError("/shremind $command -l $argumentText")
            if (storage[args.drop(1).first()] == null) return ChatUtils.userError("Reminder not found!")
            action(args.drop(2), storage[args.drop(1).first()]!!).apply {
                listReminders(1)
                sendMessage(this)
            }
        } else if (storage[args.first()] == null) {
            return ChatUtils.userError("Reminder not found!")
        } else {
            sendMessage(action(args.drop(1), storage[args.first()]!!))
        }
    }

    private fun removeReminder(args: List<String>) = actionReminder(
        args,
        "remove",
        "[id]",
    ) { _, reminder ->
        storage.values.remove(reminder)
        "§cReminder deleted."
    }

    private fun editReminder(args: List<String>) = actionReminder(
        args,
        "edit",
        "[id]",
        "[reminder]",
    ) { arguments, reminder ->
        reminder.reason = arguments.joinToString(" ")
        "§6Reminder edited."
    }

    private fun moveReminder(args: List<String>) = actionReminder(
        args,
        "move",
        "[id]",
        "[time]",
    ) { arguments, reminder ->
        val time = parseDuration(arguments.first()) ?: return@actionReminder "§cInvalid time format!"
        reminder.remindAt = SimpleTimeMark.now().plus(time)
        "§6Reminder moved to ${time.format()}"
    }

    private fun help() {
        Text.createDivider().send()
        "§6SkyHanni Reminder Commands:".asComponent().send()
        "§e/shremind <time> <reminder> - §bCreates a new reminder".asComponent().send()
        "§e/shremind list <page> - §bLists all reminders".asComponent().send()
        "§e/shremind remove <id> - §bRemoves a reminder".asComponent().send()
        "§e/shremind edit <id> <reminder> - §bEdits a reminder".asComponent().send()
        "§e/shremind move <id> <time> - §bMoves a reminder".asComponent().send()
        "§e/shremind help - §bShows this help message".asComponent().send()
        Text.createDivider().send()
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        val remindersToSend = mutableListOf<IChatComponent>()

        for ((id, reminder) in getSortedReminders()) {
            if (!reminder.shouldRemind(config.interval.minutes)) continue
            reminder.lastReminder = SimpleTimeMark.now()
            var actionsComponent: IChatComponent? = null

            if (!config.autoDeleteReminders) {
                actionsComponent = Text.join(
                    " ",
                    "§a✔".asComponent {
                        hover = "§7Click to dismiss".asComponent()
                        command = "/shremind remove $id"
                    }.wrap("§8[", "§8]"),
                    " ",
                    "§e§l⟳".asComponent {
                        hover = "§7Click to move".asComponent()
                        suggest = "/shremind move $id 1m"
                    }.wrap("§8[", "§8]"),
                )
            } else {
                storage.remove(id)
            }

            remindersToSend.add(
                Text.join(
                    "§e[Reminder]".asComponent {
                        hover = "§7Reminders by SkyHanni".asComponent()
                    },
                    actionsComponent,
                    " ",
                    "§6${reminder.reason}",
                ),
            )
        }

        if (remindersToSend.isNotEmpty()) {
            val id = if (config.autoDeleteReminders) 0 else REMINDERS_MESSAGE_ID
            Text.join(remindersToSend, separator = Text.NEWLINE).send(id)
        }
    }

    fun command(args: Array<String>) = when (args.firstOrNull()) {
        "list" -> listReminders(args.drop(1).firstOrNull()?.toIntOrNull() ?: 1)
        "remove", "delete" -> removeReminder(args.drop(1))
        "edit", "update" -> editReminder(args.drop(1))
        "move" -> moveReminder(args.drop(1))
        "help" -> help()
        else -> createReminder(args)
    }
}
