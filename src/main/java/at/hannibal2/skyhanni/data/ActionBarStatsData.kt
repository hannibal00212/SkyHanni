package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.ActionBarValueUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import org.intellij.lang.annotations.Language

enum class ActionBarStatsData(@Language("RegExp") rawPattern: String) {
    HEALTH(
        // language=RegExp
        "§[c6](?<health>[\\d,]+)/[\\d,]+❤.*"
    ),
    DEFENSE(
        // language=RegExp
        ".*§a(?<defense>[\\d,]+)§a❈.*"
    ),
    MANA(
        // language=RegExp
        ".*§b(?<mana>[\\d,]+)/[\\d,]+✎.*"
    ),
    RIFT_TIME(
        // language=RegExp
        "§[a7](?<riftTime>[\\dms ]+)ф.*"
    ),
    SKYBLOCK_XP(
        // language=RegExp
        ".*(§b\\+\\d+ SkyBlock XP §.\\([^()]+\\)§b \\(\\d+/\\d+\\)).*"
    ),
    ;

    private val repoKey = name.replace("_", ".").lowercase()

    internal val pattern by RepoPattern.pattern("actionbar.$repoKey", rawPattern)
    var value: String = ""
        private set

    @SkyHanniModule
    companion object {

        init {
            entries.forEach { it.pattern }
        }

        @HandleEvent(onlyOnSkyblock = true)
        fun onActionBarUpdate(event: ActionBarUpdateEvent) {

            entries.mapNotNull { data ->
                data.pattern.matchMatcher(event.actionBar) {
                    val newValue = group(1)
                    if (data.value != newValue) {
                        data.value = newValue
                        ActionBarValueUpdateEvent(data)
                    } else null
                }
            }.forEach { it.post() }
        }
    }
}
