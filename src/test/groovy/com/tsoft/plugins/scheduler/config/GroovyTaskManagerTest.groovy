package com.tsoft.plugins.scheduler.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.tsoft.plugins.scheduler.TaskMonitor
import com.tsoft.plugins.scheduler.models.GroovyScript
import com.tsoft.plugins.scheduler.models.Task
import com.tsoft.plugins.scheduler.models.Yaml
import com.tsoft.plugins.scheduler.utils.Consts
import groovy.json.JsonOutput
import org.apache.commons.jexl.Expression
import org.apache.commons.jexl.ExpressionFactory
import org.apache.commons.jexl.JexlContext
import org.apache.commons.jexl.JexlHelper
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.time.DateFormatUtils

import java.util.concurrent.CountDownLatch

class GroovyTaskManagerTest extends GroovyTestCase {

    def BasePath = new File("/Users/diego.campos/projects/gradle-projects/groovy-task-scheduler-plugin/work/groovy-task-plugin/")

    void testYmlWorkspace() {
        def ymlfile = getClass().getClassLoader().getResource("deleteWorkspace.yml").getFile()
        def mapper = new ObjectMapper(new YAMLFactory())
        Yaml yml = mapper.readValue(new File(ymlfile), Yaml.class);

        println yml
    }

    void testYmlHelloWorld() {
        def ymlfile = getClass().getClassLoader().getResource("HelloWorld.yml").getFile()
        def mapper = new ObjectMapper(new YAMLFactory())
        Yaml yml = mapper.readValue(new File(ymlfile), Yaml.class);

        println yml
    }

    void testTraverseFiles(){
        new File("/Users/diego.campos/projects/gradle-projects/groovy-task-scheduler-plugin")
                .listFiles().sort{ it.lastModified() }.each {
            println(it.name +" : "+ DateFormatUtils.format(new Date(it.lastModified()), "yyyyMMdd_HHmmSS"))
        }
    }

    void testTeRegex(){
        def validProjectFolder = ~/[0-9A-Za-z]+/

        def folderName = "carpetaConFormatoCorrecto"
        def matcher = ( folderName =~ validProjectFolder)
        println matcher.matches()

        def folderName2 = "algoEsta_Mal12"
        def matcher2 = folderName2 =~ validProjectFolder
        println matcher2.matches()
    }

    void testJsonTasks(){
        File folder = BasePath
        File yaml = new File(BasePath, "scripts/queuetest/queuetest.yml")
        File script = new File(BasePath, "scripts/queuetest/queuetest.groovy")
        GroovyScript sp = new GroovyScript()
                                    .setFolder(folder)
                                    .setYaml(yaml)
                                    .setScriptFile(script)

        def latch = new CountDownLatch(0)
        def task1 = new Task(sp).setLatch(latch)

        TaskMonitor.add(task1)

        println(JsonOutput.toJson(TaskMonitor.getTaskList()))
    }


    void testInject(){

        def mapInicial = [item1: 1, item2: 2, item3: 3]
        def map = mapInicial.inject([:]) { params, it ->
            params[it.key] = it.value*2
            params
        }
        println map

    }

}


