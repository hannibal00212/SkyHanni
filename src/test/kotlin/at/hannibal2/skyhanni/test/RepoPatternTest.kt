package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.repopatterns.RepoPatternDump
import at.hannibal2.skyhanni.utils.repopatterns.RepoPatternManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

@Suppress("UNUSED_EXPRESSION")
object RepoPatternTest {

    private val abc = generateSequence('a') { it + 1 }.take(26).joinToString("")

    private var counter: Int = 0

    private fun nextKey(): String = "testonly.${getChar(counter++)}"
    private fun prevKey(n: Int = 0): String = "testonly.${getChar(counter - n - 1)}"

    private fun getChar(count: Int): String = (if (count > 26) getChar(count / 26) else "") + abc[count % 26]

    @Test
    fun testRemoteLoad() {
        val simpleLocalePattern = "I'm a test value"

        val simpleRepoPattern = RepoPattern.pattern(this.nextKey(), simpleLocalePattern)
        val simplePattern by simpleRepoPattern

        val remoteValue = "I'm remote."

        val isLocalWorking = simplePattern.pattern() == simpleLocalePattern

        assert(isLocalWorking)

        RepoPatternManager.loadPatternsFromDump(
            RepoPatternDump(
                regexes = mapOf(
                    simpleRepoPattern.key to remoteValue,
                ),
            ),
        )

        val isRemoteWorking = simplePattern.pattern() == remoteValue

        assert(isRemoteWorking)

    }

    @Test
    fun testRemoteLoadList() {
        val simpleLocalePattern1 = "I'm a test value"
        val simpleLocalePattern2 = "I'm a test value 2"

        val listPatterns = RepoPattern.list(this.nextKey(), simpleLocalePattern1, simpleLocalePattern2)
        val list by listPatterns

        val remoteValue1 = "I'm remote."
        val remoteValue2 = "I'm remote 2."
        val remoteValue3 = "I'm remote 3."
        val remoteValue4 = "I'm remote 4."

        val isLocalWorking = list[0].pattern() == simpleLocalePattern1 && list[1].pattern() == simpleLocalePattern2

        assert(isLocalWorking)

        RepoPatternManager.loadPatternsFromDump(
            RepoPatternDump(
                regexes = mapOf(
                    listPatterns.key + ".1" to remoteValue1,
                    listPatterns.key + ".2" to remoteValue2,
                ),
            ),
        )

        val isRemoteWorking = list[0].pattern() == remoteValue1 && list[1].pattern() == remoteValue2

        assert(isRemoteWorking)

        RepoPatternManager.loadPatternsFromDump(
            RepoPatternDump(
                regexes = mapOf(
                    listPatterns.key + ".1" to remoteValue3,
                ),
            ),
        )

        val isRemoteSingleWorking = list[0].pattern() == remoteValue3 && list.size == 1

        assert(isRemoteSingleWorking)

        RepoPatternManager.loadPatternsFromDump(
            RepoPatternDump(
                regexes = mapOf(
                    listPatterns.key to remoteValue4,
                ),
            ),
        )

        val isRemoteListToSingleWorking = list.isEmpty()

        assert(isRemoteListToSingleWorking)
    }

    @Test
    fun testRemoteLoadGroup() {
        val simpleLocalePattern1 = "I'm a test value"
        val simpleLocalePattern2 = "I'm a test value 2"

        val groupInfo = RepoPattern.exclusiveGroup(this.nextKey())
        val group by groupInfo
        val pattern1 by group.pattern("a", simpleLocalePattern1)
        val pattern2 by group.pattern("b", simpleLocalePattern2)

        val remoteValue1 = "I'm remote."
        val remoteValue2 = "I'm remote 2."
        val remoteValue3 = "I'm remote 3."
        val remoteValue4 = "I'm remote 4."

        val isLocalWorking =
            group.getUnusedPatterns().isEmpty() && pattern1.pattern() == simpleLocalePattern1 && pattern2.pattern() == simpleLocalePattern2

        assert(isLocalWorking)

        RepoPatternManager.loadPatternsFromDump(
            RepoPatternDump(
                regexes = mapOf(
                    groupInfo.prefix + ".a" to remoteValue1,
                    groupInfo.prefix + ".b" to remoteValue2,
                ),
            ),
        )

        val isRemoteWorking =
            group.getUnusedPatterns().isEmpty() && pattern1.pattern() == remoteValue1 && pattern2.pattern() == remoteValue2

        assert(isRemoteWorking)

        RepoPatternManager.loadPatternsFromDump(
            RepoPatternDump(
                regexes = mapOf(
                    groupInfo.prefix + ".a" to remoteValue3,
                    groupInfo.prefix + ".b" to remoteValue2,
                    groupInfo.prefix + ".c" to remoteValue4,
                ),
            ),
        )

        val unused = group.getUnusedPatterns()
        val isUnusedWorking =
            unused.size == 1 && unused[0].pattern() == remoteValue4 && pattern1.pattern() == remoteValue3 && pattern2.pattern() == remoteValue2

        assert(isUnusedWorking)
    }

    @Test
    fun testRemoteLoadGroupButNotFromList() {
        val simpleLocalePattern1 = "I'm a test value"
        val simpleLocalePattern2 = "I'm a test value 2"

        val groupInfo = RepoPattern.exclusiveGroup(this.nextKey())
        val group by groupInfo
        val list by group.list("a", simpleLocalePattern1, simpleLocalePattern2)

        val remoteValue1 = "I'm remote."
        val remoteValue2 = "I'm remote 2."
        val remoteValue3 = "I'm remote 3."
        val remoteValue4 = "I'm remote 4."

        val isLocalWorking =
            group.getUnusedPatterns().isEmpty() && list[0].pattern() == simpleLocalePattern1 && list[1].pattern() == simpleLocalePattern2

        assert(isLocalWorking)

        RepoPatternManager.loadPatternsFromDump(
            RepoPatternDump(
                regexes = mapOf(
                    groupInfo.prefix + ".a.1" to remoteValue1,
                    groupInfo.prefix + ".a.2" to remoteValue2,
                ),
            ),
        )

        val isRemoteWorking = group.getUnusedPatterns().isEmpty() && list[0].pattern() == remoteValue1 && list[1].pattern() == remoteValue2

        assert(isRemoteWorking)

        RepoPatternManager.loadPatternsFromDump(
            RepoPatternDump(
                regexes = mapOf(
                    groupInfo.prefix + ".a.1" to remoteValue3,
                    groupInfo.prefix + ".a.2" to remoteValue2,
                    groupInfo.prefix + ".b" to remoteValue4,
                ),
            ),
        )

        val unused = group.getUnusedPatterns()
        val isUnusedWorking =
            unused.size == 1 && unused[0].pattern() == remoteValue4 && list[0].pattern() == remoteValue3 && list[1].pattern() == remoteValue2

        assert(isUnusedWorking)
    }

    @Test
    fun testExclusivitySameKeyPattern() {
        RepoPatternManager.inTestDuplicateUsage = false

        assertThrows<RuntimeException> {
            val pattern1 by RepoPattern.pattern(this.nextKey(), "")
            val pattern2 by RepoPattern.pattern(this.prevKey(), "")
            pattern1
            pattern2
        }
        assertThrows<RuntimeException> {
            val pattern1 by RepoPattern.pattern(this.nextKey(), "")
            val list1 by RepoPattern.list(this.prevKey(), "")
            pattern1
            list1
        }
        assertThrows<RuntimeException> {
            val pattern1 by RepoPattern.pattern(this.nextKey(), "")
            val group1 by RepoPattern.exclusiveGroup(this.prevKey())
            pattern1
            group1
        }

        RepoPatternManager.inTestDuplicateUsage = true
    }

    @Test
    fun testExclusivitySameKeyList() {
        RepoPatternManager.inTestDuplicateUsage = false

        assertThrows<RuntimeException> {
            val list1 by RepoPattern.list(this.nextKey(), "")
            val pattern1 by RepoPattern.pattern(this.prevKey(), "")
            list1
            pattern1
        }
        assertThrows<RuntimeException> {
            val list1 by RepoPattern.list(this.nextKey(), "")
            val list2 by RepoPattern.list(this.prevKey(), "")
            list1
            list2
        }
        assertThrows<RuntimeException> {
            val list1 by RepoPattern.list(this.nextKey(), "")
            val group1 by RepoPattern.exclusiveGroup(this.prevKey())
            list1
            group1
        }

        RepoPatternManager.inTestDuplicateUsage = true
    }

    @Test
    fun testExclusivitySameKeyExclusiveGroup() {
        RepoPatternManager.inTestDuplicateUsage = false

        assertThrows<RuntimeException> {
            val group1 by RepoPattern.exclusiveGroup(this.nextKey())
            val pattern1 by RepoPattern.pattern(this.prevKey(), "")
            group1
            pattern1
        }
        assertThrows<RuntimeException> {
            val group1 by RepoPattern.exclusiveGroup(this.nextKey())
            val list2 by RepoPattern.list(this.prevKey(), "")
            group1
            list2
        }
        assertThrows<RuntimeException> {
            val group1 by RepoPattern.exclusiveGroup(this.nextKey())
            val group2 by RepoPattern.exclusiveGroup(this.prevKey())
            group1
            group2
        }

        RepoPatternManager.inTestDuplicateUsage = true
    }

    @Test
    fun testExclusivitySubSpaceList() {
        RepoPatternManager.inTestDuplicateUsage = false

        assertThrows<RuntimeException> {
            val pattern1 by RepoPattern.list(this.nextKey(), "")
            val pattern2 by RepoPattern.pattern(this.prevKey() + ".1", "")
            pattern1
            pattern2
        }

        assertThrows<RuntimeException> {
            val pattern2 by RepoPattern.pattern(this.nextKey() + ".1", "")
            val pattern1 by RepoPattern.list(this.prevKey(), "")
            pattern2
            pattern1
        }

        assertThrows<RuntimeException> {
            val pattern1 by RepoPattern.list(this.nextKey(), "")
            val pattern2 by RepoPattern.pattern(this.prevKey() + ".a", "")
            pattern1
            pattern2
        }

        assertThrows<RuntimeException> {
            val pattern2 by RepoPattern.pattern(this.nextKey() + ".a", "")
            val pattern1 by RepoPattern.list(this.prevKey(), "")
            pattern2
            pattern1
        }

        assertThrows<RuntimeException> {
            val pattern1 by RepoPattern.list(this.nextKey(), "")
            val pattern2 by RepoPattern.pattern(this.prevKey() + ".1.2", "")
            pattern1
            pattern2
        }

        assertThrows<RuntimeException> {
            val pattern2 by RepoPattern.pattern(this.nextKey() + ".1.2", "")
            val pattern1 by RepoPattern.list(this.prevKey(), "")
            pattern2
            pattern1
        }

        assertThrows<RuntimeException> {
            val pattern1 by RepoPattern.list(this.nextKey(), "")
            val pattern2 by RepoPattern.pattern(this.prevKey() + ".a.a", "")
            pattern1
            pattern2
        }

        assertThrows<RuntimeException> {
            val pattern2 by RepoPattern.pattern(this.nextKey() + ".a.a", "")
            val pattern1 by RepoPattern.list(this.prevKey(), "")
            pattern2
            pattern1
        }

        assertThrows<RuntimeException> {
            val pattern1 by RepoPattern.list(this.nextKey(), "")
            val pattern2 by RepoPattern.pattern(this.prevKey() + ".a.a.a", "")
            pattern1
            pattern2
        }

        assertThrows<RuntimeException> {
            val pattern2 by RepoPattern.pattern(this.nextKey() + ".a.a.a", "")
            val pattern1 by RepoPattern.list(this.prevKey(), "")
            pattern2
            pattern1
        }

        RepoPatternManager.inTestDuplicateUsage = true
    }

    @Test
    fun testExclusivityNoTrow() {
        RepoPatternManager.inTestDuplicateUsage = false

        assertDoesNotThrow {
            // Layer 0
            val pattern1 by RepoPattern.pattern(nextKey(), "")
            val pattern1a by RepoPattern.pattern(prevKey() + ".a", "")
            val pattern2 by RepoPattern.pattern(nextKey(), "")
            val pattern2a by RepoPattern.pattern(prevKey() + ".a", "")
            val list1 by RepoPattern.list(nextKey(), "")
            val list2 by RepoPattern.list(nextKey(), "")
            // Layer 1
            val group1 by RepoPattern.exclusiveGroup(nextKey())
            val group2 by RepoPattern.exclusiveGroup(nextKey())
            val pattern3 by group1.pattern("1", "")
            val pattern3a by group1.pattern("1.a", "")
            val pattern4 by group2.pattern("1", "")
            val pattern4a by group2.pattern("1.a", "")
            val list3 by group1.list("l1", "")
            val list4 by group2.pattern("l1", "")
            // Layer 2
            val group3 by group1.exclusiveGroup("g1")
            val group4 by group2.exclusiveGroup("g1")
            val pattern5 by group3.pattern("1", "")
            val pattern5a by group3.pattern("1.a", "")
            val pattern6 by group4.pattern("1", "")
            val pattern6a by group4.pattern("1.a", "")
            val list5 by group3.list("l1", "")
            val list6 by group4.pattern("l1", "")
            // Layer 3
            val group5 by group3.exclusiveGroup("g1")
            val group6 by group4.exclusiveGroup("g1")
            val pattern7 by group5.pattern("1", "")
            val pattern7a by group5.pattern("1.a", "")
            val pattern8 by group6.pattern("1", "")
            val pattern8a by group6.pattern("1.a", "")
            val list7 by group5.list("l1", "")
            val list8 by group6.pattern("l1", "")

            // Call Order mustn't matter
            pattern1
            pattern2
            pattern3
            pattern4
            pattern5
            pattern6
            pattern7
            pattern8
            list1
            list2
            list3
            list4
            list5
            list6
            list7
            list8
            group1
            group2
            group3
            group4
            group5
            group6
            pattern1a
            pattern2a
            pattern3a
            pattern4a
            pattern5a
            pattern6a
            pattern7a
            pattern8a
        }

        RepoPatternManager.inTestDuplicateUsage = true
    }

    @Test
    fun testExclusivitySuperSpaceExclusiveGroup() {
        RepoPatternManager.inTestDuplicateUsage = false

        assertDoesNotThrow {
            val group1 by RepoPattern.exclusiveGroup(nextKey() + ".a.a")
            val pattern1 by RepoPattern.pattern(prevKey() + ".a", "")
            group1
            pattern1
        }

        assertDoesNotThrow {
            val group1 by RepoPattern.exclusiveGroup(nextKey() + ".a.a.a")
            val pattern1 by RepoPattern.pattern(prevKey() + ".a", "")
            group1
            pattern1
        }

        RepoPatternManager.inTestDuplicateUsage = true
    }

    @Test
    fun testExclusivitySubSpaceExclusiveGroup() {
        RepoPatternManager.inTestDuplicateUsage = false

        assertThrows<RuntimeException> {
            val group1 by RepoPattern.exclusiveGroup(nextKey())
            val pattern1 by RepoPattern.pattern(prevKey() + ".a", "")
            group1
            pattern1
        }

        assertThrows<RuntimeException> {
            val group1 by RepoPattern.exclusiveGroup(nextKey())
            val pattern1 by group1.pattern("a", "")
            val pattern2 by RepoPattern.pattern(prevKey() + ".a.c", "")
            group1
            pattern1
            pattern2
        }

        assertThrows<RuntimeException> {
            val pattern2 by RepoPattern.pattern(nextKey() + ".a.c", "")
            pattern2
            val group1 by RepoPattern.exclusiveGroup(prevKey())
            group1
        }

        assertThrows<RuntimeException> {
            val group1 by RepoPattern.exclusiveGroup(nextKey())
            val group2 by group1.exclusiveGroup("1")
            val pattern1 by group1.pattern("1", "")
            group1
            group2
            pattern1
        }

        RepoPatternManager.inTestDuplicateUsage = true
    }

}
