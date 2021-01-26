package com.tsoft.plugins.scheduler.config

import com.tsoft.plugins.scheduler.utils.Consts
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.logging.Logger

class GroovyTaskPoolTrigger implements Closeable {

    private final static Logger log = Logger.getLogger(GroovyTaskPoolTrigger.class.getName())
    private static ThreadPoolTaskScheduler poolTaskScheduler
    private static ThreadPoolTaskExecutor taskExecutor
    private static Map<String, ScheduledFuture> scheduledTasks

    static {
        _createTaskPoolTrigger()
    }

    protected static void _createTaskPoolTrigger(){

        //stop first
        stopTaskPool()
        stopTaskExecutor()


        poolTaskScheduler = new ThreadPoolTaskScheduler()
        poolTaskScheduler.setPoolSize(Consts.MAX_POOL_SIZE)
        poolTaskScheduler.setThreadNamePrefix("groovy-task-scheduler-")
        poolTaskScheduler.initialize()

        taskExecutor = new ThreadPoolTaskExecutor()
        taskExecutor.setMaxPoolSize(Consts.MAX_POOL_SIZE)
        taskExecutor.setThreadNamePrefix("groovy-task-executor-")
        taskExecutor.initialize()

        scheduledTasks = new ConcurrentHashMap<>()
    }

    protected static boolean stopTaskPool(){
        try {
            if (poolTaskScheduler!=null && poolTaskScheduler.waitForTasksToCompleteOnShutdown) {
                poolTaskScheduler.getScheduledExecutor().shutdown()
                return true
            }
        }
        catch (ex){
            return false
        }
    }

    protected static boolean stopTaskExecutor(){
        try {
            if (taskExecutor!=null && taskExecutor.waitForTasksToCompleteOnShutdown) {
                taskExecutor.shutdown()
                return true
            }
        }
        catch (ex){
            return false
        }
    }

    static ScheduledFuture schedule(Runnable task, String cronSpec) {
        log.finer("Nueva tarea registrada: "+ task.toString())
        return poolTaskScheduler.schedule(task, new CronTrigger(cronSpec))
    }

    static void execute(Runnable task) {
        log.finer("Se ejecuta la tarea: "+ task.toString())
        taskExecutor.execute(task)
    }

    @Override
    void close() throws IOException {
        stopTaskExecutor()
        stopTaskPool()
    }
}
