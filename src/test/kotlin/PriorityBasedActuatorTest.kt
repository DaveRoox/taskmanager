import org.junit.Before
import org.junit.Test
import taskmanager.policy.Actuator
import taskmanager.policy.PriorityBasedActuator
import taskmanager.process.Priority
import taskmanager.process.Process
import java.util.*

class PriorityBasedActuatorTest {

    var maxSize: Int = 0
    lateinit var actuator: Actuator
    lateinit var container: MutableList<Pair<Long, Process>>
    var timestamp: Long = 0

    @Before
    fun prepareTest() {
        maxSize = 15
        actuator = PriorityBasedActuator()
        container = LinkedList()
        timestamp = 1
    }

    @Test
    fun testNoMoreThanMaxSize() {
        (0 until maxSize * 3).forEach { i ->
            actuator.add(Process(i, Priority.LOW), timestamp++, container, maxSize)
        }
        assert(container.size <= maxSize) {
            "expected: $container.size <= $maxSize; got: false"
        }
    }

    @Test
    fun testAllElementsAreInsertedInOrderBeforeReachingMaxSize() {
        (0 until maxSize).forEach { i ->
            actuator.add(Process(i, Priority.LOW), timestamp++, container, maxSize)
        }
        val a = container.dropLast(1)
        val b = container.drop(1)
        assert(a.map { it.first }.zip(b.map { it.first }).all { (aa, bb) -> aa < bb }) {
            "expected: ${a.map { it.first }}.zip(${b.map { it.first }}).all { (aa, bb) -> aa < bb } == true; got: false"
        }
    }

    @Test
    fun testNoProcessInsertionAfterMaxSizeWithAllHigherPriorityProcesses() {
        (0 until maxSize).forEach { i ->
            actuator.add(Process(i, Priority.MEDIUM), timestamp++, container, maxSize)
        }
        val newProcess = Process(maxSize, Priority.LOW) // its priority < than any other
        actuator.add(newProcess, timestamp++, container, maxSize)
        assert(!container.map { it.second.pid }.contains(newProcess.pid)) {
            "expected: ${container.map { it.second.pid }}.contains(${newProcess.pid}) == false; got: true"
        }
    }

    @Test
    fun testNoProcessInsertionAfterMaxSizeWithNoLowerPriorityProcesses() {
        (0 until maxSize).forEach { i ->
            actuator.add(
                Process(i, if (i % 2 == 0) Priority.MEDIUM else Priority.HIGH),
                timestamp++,
                container,
                maxSize
            )
        }
        val newProcess = Process(maxSize, Priority.MEDIUM) // its priority <= than any other
        actuator.add(newProcess, timestamp++, container, maxSize)
        assert(!container.map { it.second.pid }.contains(newProcess.pid)) {
            "expected: ${container.map { it.second.pid }}.contains(${newProcess.pid}) == false; got: true"
        }
    }

    @Test
    fun testProcessInsertionAfterMaxSizeWithAtLeastOneLowerPriorityProcess() {
        (0 until maxSize).forEach { i -> actuator.add(Process(i, Priority.MEDIUM), timestamp++, container, maxSize) }
        val newProcess = Process(maxSize, Priority.HIGH) // its priority > than at least one process
        actuator.add(newProcess, timestamp++, container, maxSize)
        assert(container.map { it.second.pid }.contains(newProcess.pid)) {
            "expected: ${container.map { it.second.pid }}.contains(${newProcess.pid}) == true; got: false"
        }
    }

    @Test
    fun testTheOldestProcessIsRemovedWhenInsertingAfterMaxSize() {
        (0 until maxSize).forEach { i -> actuator.add(Process(i, Priority.MEDIUM), timestamp++, container, maxSize) }
        val oldestProcess = container.minByOrNull { it.first }!!.second // the first process is the oldest one
        val newProcess = Process(maxSize, Priority.HIGH) // its priority > than at least one process
        actuator.add(newProcess, timestamp++, container, maxSize)
        assert(!container.map { it.second.pid }.contains(oldestProcess.pid)) {
            "expected: ${container.map { it.second.pid }}.contains(${oldestProcess.pid}) == false; got: true"
        }
    }

    @Test
    fun testTheLowestPriorityProcessIsRemovedWhenInsertingAfterMaxSize() {
        (0 until maxSize - 1).forEach { i ->
            actuator.add(
                Process(i, Priority.MEDIUM),
                timestamp++,
                container,
                maxSize
            )
        }
        val lowestPriorityProcess = Process(maxSize - 1, Priority.LOW)
        actuator.add(lowestPriorityProcess, timestamp++, container, maxSize)
        val newProcess = Process(maxSize, Priority.HIGH) // its priority > than at least one process
        actuator.add(newProcess, timestamp++, container, maxSize)
        assert(!container.map { it.second.pid }.contains(lowestPriorityProcess.pid)) {
            "expected: ${container.map { it.second.pid }}.contains(${lowestPriorityProcess.pid}) == false; got: true"
        }
    }

    @Test
    fun testTheOldestAndLowestPriorityProcessIsRemovedWhenInsertingAfterMaxSize() {
        (0 until maxSize).forEach { i ->
            actuator.add(
                Process(i, if (i % 2 == 0) Priority.MEDIUM else Priority.LOW),
                timestamp++,
                container,
                maxSize
            )
        }
        val oldestAndLowestPriorityProcess = container
            .filter { it.second.priority == Priority.LOW }
            .minByOrNull { it.first }!!
            .second
        val newProcess = Process(maxSize, Priority.HIGH) // its priority > than at least one process
        actuator.add(newProcess, timestamp++, container, maxSize)
        assert(!container.map { it.second.pid }.contains(oldestAndLowestPriorityProcess.pid)) {
            "expected: ${
                container.map { it.second.pid }
            }.contains(${oldestAndLowestPriorityProcess.pid}) == false; got: true"
        }
    }
}