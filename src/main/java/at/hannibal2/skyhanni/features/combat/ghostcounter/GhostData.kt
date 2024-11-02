package at.hannibal2.skyhanni.features.combat.ghostcounter

import java.util.regex.Pattern
import kotlin.math.roundToInt

object GhostData {

    private var session = mutableMapOf(
        Option.KILLS to 0.0,
        Option.SORROWCOUNT to 0.0,
        Option.VOLTACOUNT to 0.0,
        Option.PLASMACOUNT to 0.0,
        Option.GHOSTLYBOOTS to 0.0,
        Option.BAGOFCASH to 0.0,
        Option.TOTALDROPS to 0.0,
        Option.SCAVENGERCOINS to 0.0,
        Option.MAXKILLCOMBO to 0.0,
        Option.SKILLXPGAINED to 0.0
    )

    // TODO repo
    val bestiaryData = mutableMapOf<Int, Int>().apply {
        for (i in 1..25) {
            this[i] = when (i) {
                1, 2, 3, 4, 5 -> 4
                6 -> 20
                7 -> 40
                8, 9 -> 60
                10 -> 100
                11 -> 300
                12 -> 600
                13 -> 800
                14, 15, 16, 17 -> 1_000
                18 -> 1_200
                19, 20 -> 1_400
                21 -> 10_000
                22, 23, 24, 25 -> 20_000
                else -> 0
            }
        }
    }

    enum class Option(val pattern: Pattern? = null) {
        KILLS,
        SORROWCOUNT("§6§lRARE DROP! §r§9Sorrow §r§b\\([+](?<mf>.*)% §r§b✯ Magic Find§r§b\\)".toPattern()),
        VOLTACOUNT("§6§lRARE DROP! §r§9Volta §r§b\\([+](?<mf>.*)% §r§b✯ Magic Find§r§b\\)".toPattern()),
        PLASMACOUNT("§6§lRARE DROP! §r§9Plasma §r§b\\([+](?<mf>.*)% §r§b✯ Magic Find§r§b\\)".toPattern()),
        GHOSTLYBOOTS("§6§lRARE DROP! §r§9Ghostly Boots §r§b\\([+](?<mf>.*)% §r§b✯ Magic Find§r§b\\)".toPattern()),
        BAGOFCASH("§eThe ghost's death materialized §r§61,000,000 coins §r§efrom the mists!".toPattern()),
        KILLCOMBOCOINS("[+]\\d+ Kill Combo [+](?<coin>.*) coins per kill".toPattern()),
        TOTALDROPS,
        GHOSTSINCESORROW,
        SCAVENGERCOINS,
        MAXKILLCOMBO,
        KILLCOMBO("[+]\\d+ Kill Combo [+](?<coin>.*) coins per kill".toPattern()),
        SKILLXPGAINED;

        fun add(i: Double, s: Boolean = false) {
            if (s)
                session[this] = session[this]?.plus(i) ?: i
            else
                GhostCounter.storage?.data?.set(this, GhostCounter.storage?.data?.get(this)?.plus(i) ?: i)
        }

        fun set(i: Double, s: Boolean = false) {
            if (s)
                session[this] = i
            else
                GhostCounter.storage?.data?.set(this, i)
        }

        fun getInt(s: Boolean = false): Int {
            return if (s)
                session[this]?.roundToInt() ?: 0
            else
                GhostCounter.storage?.data?.get(this)?.roundToInt() ?: 0
        }

        fun get(s: Boolean = false): Double {
            return if (s)
                session[this] ?: 0.0
            else
                GhostCounter.storage?.data?.get(this) ?: 0.0
        }
    }
}
