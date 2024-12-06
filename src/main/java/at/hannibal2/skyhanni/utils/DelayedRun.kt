package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.drainTo
import net.minecraft.client.Minecraft
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import kotlin.time.Duration

object DelayedRun {

    private val tasks = mutableListOf<Pair<() -> Any, SimpleTimeMark>>()
    private val futureTasks = ConcurrentLinkedQueue<Pair<() -> Any, SimpleTimeMark>>()

    fun runDelayed(duration: Duration, run: () -> Unit): SimpleTimeMark {
        val time = SimpleTimeMark.now() + duration
        futureTasks.add(Pair(run, time))
        return time
    }

    /** Runs in the next full Tick so the delay is between 50ms to 100ms**/
    fun runNextTick(run: () -> Unit) {
        futureTasks.add(Pair(run, SimpleTimeMark.farPast()))
    }

    fun checkRuns() {
        tasks.removeIf { (runnable, time) ->
            val inPast = time.isInPast()
            if (inPast) {
                try {
                    runnable()
                } catch (e: Exception) {
                    ErrorManager.logErrorWithData(e, "DelayedRun task crashed while executing")
                }
            }
            inPast
        }
        futureTasks.drainTo(tasks)
    }

    @JvmField
    val onThread = Executor {
        val mc = Minecraft.getMinecraft()
        if (mc.isCallingFromMinecraftThread) {
            it.run()
        } else {
            mc.addScheduledTask(it)
        }
    }
}
