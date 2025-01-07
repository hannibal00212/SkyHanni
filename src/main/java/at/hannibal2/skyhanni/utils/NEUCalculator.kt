package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.system.PlatformUtils
import io.github.moulberry.notenoughupdates.util.Calculator
import java.math.BigDecimal

object NEUCalculator {

    fun calculateOrNull(input: String?): BigDecimal? {
        if (input.isNullOrEmpty() || !PlatformUtils.isNeuLoaded()) return null
        return runCatching { Calculator.calculate(input) }.getOrNull()
    }
}
