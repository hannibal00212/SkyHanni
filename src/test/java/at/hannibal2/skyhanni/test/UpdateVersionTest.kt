package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.system.ModVersion
import org.junit.jupiter.api.Test

class UpdateVersionTest {

    private val versionA = ModVersion.fromString("1.0.0")
    private val versionB = ModVersion.fromString("1.0.1")
    private val versionC = ModVersion.fromString("1.0.2")
    private val versionD = ModVersion.fromString("1.1.0")
    private val versionE = ModVersion.fromString("1.2.0")
    private val versionF = ModVersion.fromString("2.0.0")

    /**
     * These tests assume you are on the beta updater branch (so you always want the most recent beta
     */
    @Test
    fun `test versions are correctly compared`() {
        assert(versionA < versionB)
        assert(versionB < versionC)

        assert(versionC < versionD)
        assert(versionD < versionE)

        assert(versionE < versionF)
    }
}
