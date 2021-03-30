package taskmanager.process

data class Process(val pid: Int, val priority: Priority) {
    fun kill() {
        // Doing very complex stuff...
        println("process $pid has been killed")
    }
}