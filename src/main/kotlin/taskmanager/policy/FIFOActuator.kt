package taskmanager.policy

import taskmanager.process.Process

class FIFOActuator : Actuator {
    override fun add(process: Process, timestamp: Long, container: MutableList<Pair<Long, Process>>, maxSize: Int): Boolean {
        while (container.size >= maxSize) {
            container.firstOrNull()?.second?.kill()
            container.removeFirst()
        }
        return container.add(timestamp to process)
    }
}