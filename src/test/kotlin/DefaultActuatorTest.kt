import org.junit.Before
import org.junit.Test
import taskmanager.policy.Actuator
import taskmanager.policy.DefaultActuator
import taskmanager.process.Priority
import taskmanager.process.Process
import java.util.*

class DefaultActuatorTest {

    var maxSize: Int = 0
    lateinit var actuator: Actuator
    lateinit var container: MutableList<Pair<Long, Process>>
    var timestamp: Long = 0

    @Before
    fun prepareTest() {
        maxSize = 15
        actuator = DefaultActuator()
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
    fun testAllElementsAreInsertedInOrder() {
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
    fun testNoMoreProcessAllowedWhenReachingMaxSize() {
        (0 until maxSize + 1).forEach { i ->
            actuator.add(Process(i, Priority.LOW), timestamp++, container, maxSize)
        }
        val youngestProcessId = maxSize
        assert(!container.map { it.second.pid }.contains(youngestProcessId)) {
            "expected: ${container.map { it.second.pid }}.contains($youngestProcessId) == false; got: true"
        }
    }
}