package taskmanager

import taskmanager.policy.*
import taskmanager.process.Priority
import taskmanager.process.Process
import java.util.*

class TaskManager private constructor(private val maxSize: Int, private val policyActuator: Actuator) {

    enum class SortingCriteria {
        CREATION_TIME,
        PRIORITY,
        ID
    }

    private val list: MutableList<Pair<Long, Process>> = LinkedList()

    // any monotonically increasing value is fine to represent the creation time, so I am choosing a simple counter
    private var counter: Long = 0

    init {
        if (maxSize <= 0) throw IllegalArgumentException("Max size must be greater than 0. Received: $maxSize")
    }

    fun add(process: Process): Boolean =
        if (this.policyActuator.add(process, counter + 1, this.list, this.maxSize)) {
            counter++
            true
        } else false

    fun list(): List<Process> = list.map { it.second }.toList()
    fun listSortedBy(sortingCriteria: SortingCriteria) = listSortedBy(sortingCriteria, false)
    fun listSortedByDescending(sortingCriteria: SortingCriteria) = listSortedBy(sortingCriteria, true)

    fun kill(byPID: Int) = kill(byPID) { it.pid }
    fun killGroup(byPriority: Priority) = kill(byPriority) { it.priority }
    fun killAll() = kill(true) { true }

    private fun <Key : Comparable<Key>> kill(keyValue: Key, keyFunc: (Process) -> Key): Boolean {
        list.map { it.second }.filter { keyFunc(it) == keyValue }.forEach { it.kill() }
        return list.removeIf { keyFunc(it.second) == keyValue }
    }

    private fun <Key : Comparable<Key>> list(
        descending: Boolean,
        keyFunc: (Pair<Long, Process>) -> Key
    ): List<Process> =
        (if (!descending) list.sortedBy(keyFunc) else list.sortedByDescending(keyFunc)).map { it.second }

    private fun listSortedBy(sortingCriteria: SortingCriteria, descending: Boolean) = when (sortingCriteria) {
        SortingCriteria.CREATION_TIME -> list(descending) { it.first }
        SortingCriteria.PRIORITY -> list(descending) { it.second.priority }
        SortingCriteria.ID -> list(descending) { it.second.pid }
    }

    class Builder(private var maxSize: Int = 0, private var policy: Policy = Policy.DEFAULT) {
        fun withMaxSize(maxSize: Int) = apply { this.maxSize = maxSize }
        fun withPolicy(policy: Policy) = apply { this.policy = policy }
        fun build() = TaskManager(
            this.maxSize,
            when (this.policy) {
                Policy.FIFO -> FIFOActuator()
                Policy.PRIORITY_BASED -> PriorityBasedActuator()
                else -> DefaultActuator()
            }
        )
    }

}