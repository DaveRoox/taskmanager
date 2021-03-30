import org.junit.Before
import org.junit.Test
import taskmanager.TaskManager
import taskmanager.policy.Policy
import taskmanager.process.Priority
import taskmanager.process.Process
import kotlin.random.Random

class TaskManagerTest {

    var maxSize: Int = 0
    lateinit var taskManager: TaskManager

    private fun randomProcess() = Process(
        Random.nextInt(10000),
        when (Random.nextInt(3)) {
            0 -> Priority.LOW
            1 -> Priority.MEDIUM
            else -> Priority.HIGH
        }
    )

    @Before
    fun prepareTest() {
        maxSize = 15
        taskManager = TaskManager.Builder().withMaxSize(maxSize).withPolicy(Policy.PRIORITY_BASED).build()
    }

    @Test
    fun testKill() {
        (0 until maxSize).forEach { i -> taskManager.add(Process(i, Priority.LOW)) }
        val processPidToKill = 3
        taskManager.kill(processPidToKill)
        assert(!taskManager.list().map { it.pid }.contains(processPidToKill)) {
            "expected: ${taskManager.list().map { it.pid }}.contains(${processPidToKill}) == false; got: true"
        }
    }

    @Test
    fun testKillAll() {
        (0 until maxSize).forEach { i -> taskManager.add(Process(i, Priority.LOW)) }
        taskManager.killAll()
        assert(taskManager.list().isEmpty()) {
            "expected: ${taskManager.list()}.isEmpty() == true; got: false"
        }
    }

    @Test
    fun testKillGroup() {
        (0 until maxSize / 3).forEach { i -> taskManager.add(Process(i, Priority.LOW)) }
        (maxSize / 3 until 2 * maxSize / 3).forEach { i -> taskManager.add(Process(i, Priority.MEDIUM)) }
        (2 * maxSize / 3 until maxSize).forEach { i -> taskManager.add(Process(i, Priority.HIGH)) }
        taskManager.killGroup(Priority.MEDIUM)
        assert(taskManager.list().map { it.priority }.none { it == Priority.MEDIUM }) {
            "expected: ${taskManager.list().map { it.priority }}.none{it == Priority.MEDIUM} == true; got: false"
        }
    }

    @Test
    fun testEmptyList() {
        val processesToInsert = listOf<Process>()
        processesToInsert.forEach { taskManager.add(it) }
        assert(taskManager.list().isEmpty()) {
            "expected: ${taskManager.list()}.isEmpty() == true; got: false"
        }
    }

    @Test
    fun testListSortedByPID() {
        (0 until maxSize).forEach { _ -> taskManager.add(randomProcess()) }
        val list = taskManager.listSortedBy(TaskManager.SortingCriteria.ID)
        val a = list.dropLast(1)
        val b = list.drop(1)
        assert(a.zip(b).all { (aa, bb) -> aa.pid < bb.pid }) {
            "expected: $a.zip($b).all { it.first.pid < it.second.pid } == true; got: false"
        }
    }

    @Test
    fun testListSortedByDescendingPID() {
        (0 until maxSize).forEach { _ -> taskManager.add(randomProcess()) }
        val list = taskManager.listSortedByDescending(TaskManager.SortingCriteria.ID)
        val a = list.dropLast(1)
        val b = list.drop(1)
        assert(a.zip(b).all { (aa, bb) -> aa.pid > bb.pid }) {
            "expected: $a.zip($b).all { it.first.pid > it.second.pid } == true; got: false"
        }
    }

    @Test
    fun testListSortedByPriority() {
        (0 until maxSize).forEach { _ -> taskManager.add(randomProcess()) }
        val list = taskManager.listSortedBy(TaskManager.SortingCriteria.PRIORITY)
        val a = list.dropLast(1)
        val b = list.drop(1)
        assert(a.zip(b).all { (aa, bb) -> aa.priority <= bb.priority }) {
            "expected: $a.zip($b).all { it.first.pid <= it.second.pid } == true; got: false"
        }
    }

    @Test
    fun testListSortedByDescendingPriority() {
        (0 until maxSize).forEach { _ -> taskManager.add(randomProcess()) }
        val list = taskManager.listSortedByDescending(TaskManager.SortingCriteria.PRIORITY)
        val a = list.dropLast(1)
        val b = list.drop(1)
        assert(a.zip(b).all { (aa, bb) -> aa.priority >= bb.priority }) {
            "expected: $a.zip($b).all { it.first.pid >= it.second.pid } == true; got: false"
        }
    }

    @Test
    fun testListSortedByCreationTime() {
        // the index-based ordering in this array is equivalent to
        // the creation time-based ordering in taskManager's list of processes
        val processes = (0 until maxSize * 5).map { randomProcess() }
        processes.forEach { p -> taskManager.add(p) }
        val list = taskManager.listSortedBy(TaskManager.SortingCriteria.CREATION_TIME)
        var i = 0
        var j = 0
        while (j < list.size - 1) {
            while (i < processes.size && processes[i] != list[j])
                i++
            if (i == processes.size)
                break
            j++
            i++
        }
        val isOrdered = j == list.size - 1
        assert(isOrdered) {
            "expected: isOrdered == true; got: false"
        }
    }

    @Test
    fun testListSortedByDescendingCreationTime() {
        // the index-based ordering in this array is equivalent to
        // the creation time-based ordering in taskManager's list of processes
        val processes = (0 until maxSize * 5).map { randomProcess() }
        processes.forEach { p -> taskManager.add(p) }
        val list = taskManager.listSortedByDescending(TaskManager.SortingCriteria.CREATION_TIME)
        var i = 0
        var j = list.size - 1
        while (j >= 0) {
            while (i < processes.size && processes[i] != list[j])
                i++
            if (i == processes.size)
                break
            j--
            i++
        }
        val isOrdered = j == -1
        assert(isOrdered) {
            "expected: isOrdered == true; got: false"
        }
    }
}