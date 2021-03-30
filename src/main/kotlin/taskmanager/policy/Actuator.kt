package taskmanager.policy

import taskmanager.process.Process

interface Actuator {
    fun add(process: Process, timestamp: Long, container: MutableList<Pair<Long, Process>>, maxSize: Int): Boolean
}