package at.hannibal2.skyhanni.utils.chat

import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils

object ChatClickActionManager {

    private val actions = mutableMapOf<String, ClickableAction>()

    fun createAction(onClick: () -> Any, expiresAt: SimpleTimeMark, oneTime: Boolean = true): String {
        val token = StringUtils.generateRandomId()
        actions[token] = ClickableAction(onClick, oneTime, expiresAt)
        return token
    }

    fun onCommand(args: Array<String>) {
        if (args.size == 1) {
            actions[args.first()]?.apply {
                if (expiresAt.isInPast()) {
                    actions.remove(args.first())
                    return
                }
                onClick()
                if (oneTime) {
                    actions.remove(args.first())
                }
            }
        }
    }

    class ClickableAction(
        val onClick: () -> Any,
        val oneTime: Boolean = true,
        val expiresAt: SimpleTimeMark = SimpleTimeMark.farFuture(),
    )
}
