package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.test.NEUDependent.test
import io.github.moulberry.notenoughupdates.util.SkyBlockTime
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test

object NEUDependent {
    fun mockMe(): Boolean {
        return false
    }

    fun SkyBlockTime.test(): Boolean {
        return true
    }
}

class NEUDependencyTest {
    @Test
    fun `test that we can have extensions of NEU on object classes`() {
        mockkObject(NEUDependent)
        every { NEUDependent.mockMe() } returns true
        assert(NEUDependent.mockMe())
        assert(SkyBlockTime(0).test())
    }
}