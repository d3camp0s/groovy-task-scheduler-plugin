package com.tsoft.plugins.scheduler

import com.tsoft.plugins.scheduler.models.Task
import java.util.logging.Logger

class TaskMonitor {

    private final static Logger log = Logger.getLogger(TaskMonitor.class.getName())
    private static List<Task> taskList = null

    static {
        if( taskList==null )
            taskList = Collections.synchronizedList(new ArrayList<Task>())
    }

    static boolean add(Task task){
        return taskList.add(task)
    }

    static boolean remove(Task task){
        return taskList.remove(task)
    }

    static List getTaskList() {
        return taskList.inject([]) { list, task ->
            list.add(task.toMap())
            list
        }
    }

    static Set<Thread> getAllThread(){
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
        return threadSet
    }

    static void interrupt(long id){
        Thread.getAllStackTraces().keySet().find { it.id == id }.interrupt()
    }
}
