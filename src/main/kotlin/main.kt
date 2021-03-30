package main

import taskmanager.TaskManager
import taskmanager.policy.Policy
import taskmanager.process.*

fun main() {
    TaskManager
        .Builder()
        .withMaxSize(10)
        .withPolicy(Policy.PRIORITY_BASED)
        .build()
        .apply {
            add(Process(0, Priority.HIGH))
            add(Process(1, Priority.HIGH))
            add(Process(2, Priority.LOW))
            add(Process(3, Priority.MEDIUM))
            add(Process(4, Priority.HIGH))
            add(Process(5, Priority.LOW))
            add(Process(6, Priority.HIGH))
            add(Process(7, Priority.MEDIUM))
            add(Process(8, Priority.HIGH))
            add(Process(9, Priority.LOW))
            add(Process(10, Priority.MEDIUM))
            add(Process(11, Priority.HIGH))
            add(Process(12, Priority.LOW))
            add(Process(13, Priority.HIGH))
            add(Process(14, Priority.MEDIUM))
            add(Process(20, Priority.HIGH))

            val chainElements = { list: List<Process> ->
                if (list.isNotEmpty())
                    list.joinToString("\n") { "\t" + it.toString() }
                else
                    "\t[empty list]"
            }

            println(
                "List:\n${
                    chainElements(list())
                }\n"
            )
            println(
                "List sorted by pid:\n${
                    chainElements(listSortedBy(TaskManager.SortingCriteria.ID))
                }\n"
            )
            println(
                "List sorted by priority:\n${
                    chainElements(listSortedBy(TaskManager.SortingCriteria.PRIORITY))
                }\n"
            )
            println(
                "List sorted by creation time:\n${
                    chainElements(listSortedBy(TaskManager.SortingCriteria.CREATION_TIME))
                }\n"
            )

            kill(byPID = 0)
            println("List:\n${chainElements(list())}\n")

            println("Adding a low-priority process with pid 16...")
            add(Process(16, Priority.LOW))
            println(
                "List sorted by descending creation time:\n${
                    chainElements(listSortedByDescending(TaskManager.SortingCriteria.CREATION_TIME))
                }\n"
            )
            println(
                "List sorted by descending priority:\n${
                    chainElements(listSortedByDescending(TaskManager.SortingCriteria.PRIORITY))
                }\n"
            )
            println(
                "List sorted by descending pid:\n${
                    chainElements(listSortedByDescending(TaskManager.SortingCriteria.ID))
                }\n"
            )

            println("Killing all high-priority processes...")
            killGroup(byPriority = Priority.HIGH)
            println("List:\n${chainElements(list())}\n")

            println("Killing all processes...")
            killAll()
            println("List:\n${chainElements(list())}\n")
        }
}