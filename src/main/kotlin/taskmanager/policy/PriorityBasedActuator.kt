package taskmanager.policy

import taskmanager.process.Process

class PriorityBasedActuator : Actuator {
    override fun add(
        process: Process,
        timestamp: Long,
        container: MutableList<Pair<Long, Process>>,
        maxSize: Int
    ): Boolean {

        if (container.size < maxSize)
            return container.add(timestamp to process)

        val oldestLowestPriorityProcessIndex = container.withIndex().minWithOrNull { (_, ei), (_, ej) ->
            val (ts1, p1) = ei
            val (ts2, p2) = ej
            when {
                p1.priority < p2.priority -> -1
                p2.priority < p1.priority -> 1
                ts1 < ts2 -> -1 // when same priority, we get the older process
                ts2 < ts1 -> 1
                else -> 0
            }
        }
        val oldestLowestPriorityProcess = oldestLowestPriorityProcessIndex?.value?.second
        if (oldestLowestPriorityProcess == null || process.priority <= oldestLowestPriorityProcess.priority)
            return false

        // even if killing this process wasn't explicitly written in the requirements,
        // I am supposing that I still have to kill it
        oldestLowestPriorityProcess.kill()
        container[oldestLowestPriorityProcessIndex.index] = timestamp to process
        return true
    }
}