package at.hannibal2.skyhanni.utils

import java.lang.reflect.Field

class OtherModsSettings(val modConfigPath: String) {

    companion object {
        fun patcher(): OtherModsSettings = getModPath("club.sk1er.patcher.config.PatcherConfig")

        private fun getModPath(modConfigPath: String): OtherModsSettings = OtherModsSettings(modConfigPath)
    }

    fun getBoolean(optionPath: String): Boolean = getOption(optionPath)?.get(null) as? Boolean ?: false

    fun setBoolean(optionPath: String, value: Boolean) {
        getOption(optionPath)?.set(null, value)
    }

    private fun getOption(optionPath: String): Field? =
        try {
            Class.forName(modConfigPath).getField(optionPath)
        } catch (e: Throwable) {
            null
        }
}
