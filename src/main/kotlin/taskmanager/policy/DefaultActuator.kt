package taskmanager.policy

import taskmanager.process.Process

class DefaultActuator : Actuator {
    override fun add(process: Process, timestamp: Long, container: MutableList<Pair<Long, Process>>, maxSize: Int): Boolean =
        if (container.size < maxSize) container.add(timestamp to process) else false
}