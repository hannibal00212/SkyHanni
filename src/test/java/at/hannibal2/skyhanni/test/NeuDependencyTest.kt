package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.test.NeuDependent.test
import io.github.moulberry.notenoughupdates.util.SkyBlockTime
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test

object NeuDependent {
    fun mockMe(): Boolean {
        return false
    }

    fun SkyBlockTime.test(): Boolean {
        return true
    }
}

class NeuDependencyTest {
    @Test
    fun `test that we can have extensions of NEU on object classes`() {
        mockkObject(NeuDependent)
        every { NeuDependent.mockMe() } returns true
        assert(NeuDependent.mockMe())
        assert(SkyBlockTime(0).test())
    }
}
