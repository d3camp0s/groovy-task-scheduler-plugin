package com.tsoft.plugins.scheduler.utils

import com.tsoft.plugins.scheduler.TaskMonitor
import com.tsoft.plugins.scheduler.config.GroovyTaskManager
import com.tsoft.plugins.scheduler.models.GroovyScript
import com.tsoft.plugins.scheduler.models.Task
import com.tsoft.plugins.scheduler.models.Yaml

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class SubTaskExecutor {

    private final static Logger log = Logger.getLogger(SubTaskExecutor.class.getName())
    private String parent
    private HashMap<String, Object> inputs

    SubTaskExecutor(String parent, HashMap<String, Object> inputs){
        this.parent = parent
        this.inputs = inputs
    }

    void runTask(String nameTaskToRun){
        this.runTask(nameTaskToRun, [:])
    }

    void runTask(String nameTaskToRun, Map params){
        inputs.get("out").println()
        inputs.putAll(params)
        def exec = inputs.remove("exec")
        try {
            if (parent != nameTaskToRun) {
                log.info("Ejecutando el script: '${nameTaskToRun}' desde la tarea padre: ${parent}")
                def sp = GroovyTaskManager.get()
                        .getYmlGScripts()
                        .find { it.id == nameTaskToRun.hashCode().toLong() }

                if (sp != null) {
                    if (sp.triggerSpec == 'manual') {
                        if (sp.isScriptActive()) {
                            // Se ejecuta el script en un hilo actual
                            def latch = new CountDownLatch(1)
                            def task = new Task(sp, inputs).setLatch(latch).setRequireContext(false)
                            // log de ejecucion de subtarea
                            _addHeaderLog(nameTaskToRun, params, sp)
                            TaskMonitor.add(task)
                            task.run()

                            if (latch.await(120, TimeUnit.SECONDS)) {
                                log.info("SubTask: '${sp.name}' done successfully")
                            } else {
                                log.severe("Gave up waiting for tasks to finish running")
                            }
                            _addFooterLog(nameTaskToRun)
                        } else {
                            throw new ScriptException("El script: '${nameTaskToRun}' esta desactivado y no permite ejecucion automatica o manual")
                        }
                    } else {
                        throw new ScriptException("Solo se permite ejecutar sub-tareas con la configuracion: 'trigger: manual")
                    }
                } else {
                    throw new ScriptException("Script: '${nameTaskToRun}' ¡no encontrado!")
                }
            } else {
                throw new ScriptException("No es posible ejecutar el script: '${nameTaskToRun}' desde la tarea padre: ${parent}, posible loop infinito.")
            }
        }
        catch (ex){
            throw ex
        }
        finally {
            // se quita al inicio para evitar loop infinito y se agrega al final para pdoer seguir usandolo en el script principal
            inputs.put("exec", exec)
        }
    }

    private void _addHeaderLog(String name, Map params, GroovyScript sp){
        params.putAll(sp.ymld.variables)
        String header = """
################################################################################
  Name: ${name}
  Script: ${sp.ymld.script}
  Description: ${sp.ymld.description}
  Parameters:
        ${params}
--------------------------------------------------------------------------------
        """
        inputs.get("out").println(header)
    }

    private void _addFooterLog(String name){
        def endline = "--------------------------------------------------------------------------------"
        inputs.get("out").println(endline)
    }
}
