package com.tsoft.plugins.scheduler.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.commons.lang.time.DateFormatUtils
import groovy.json.JsonOutput
import java.util.concurrent.ScheduledFuture
import com.tsoft.plugins.scheduler.utils.ScriptHelper

class GroovyScript implements Serializable {

    private static final long serialVersionUID = 1L
    private long id
    private String name
    private String group
    private File folder
    private File yaml
    private Yaml ymld

    private File scriptFile
    private Date lastExecution
    private String description

    private ObjectMapper mapper
    private Boolean validYmlFile = false

    private String triggerSpec // event or cron
    private ScheduledFuture trigger
    private boolean scriptActive = false

    private String event
    private List<String> errores

    private String lastExecutionStatus
    private String logfile
    private String logname

    GroovyScript(){
        this.mapper = new ObjectMapper(new YAMLFactory())
        this.errores = new ArrayList<>()
    }

    GroovyScript(File folder, File yaml, File script) {
        this.folder = folder
        this.errores = new ArrayList<>()
        this.mapper = new ObjectMapper(new YAMLFactory())
        this.description = "no description found"
        setYaml(yaml)
        setScriptFile(script)
    }

    GroovyScript setYaml(File yaml) {
        this.yaml = yaml
        try {
            this.ymld = mapper.readValue(yaml, Yaml.class)
            this.name = ymld.name
            this.group = ymld.group
            this.description = ymld.description
            this.triggerSpec = (ymld.trigger.get("cron")!=null? "cron" : (ymld.trigger.get("event")!=null? "event" : "manual"))

            this.logname = triggerSpec+"-"+name.toLowerCase()
            this.id = name.hashCode().toLong()
            this.validYmlFile = true
        }
        catch (ex){
            // Se omite el exception
            this.errores.add(ex.toString())
            this.validYmlFile = false
        }
        return this
    }

    File getYaml() {
        return yaml
    }

    String getName() {
        return name
    }

    GroovyScript setName(String name) {
        this.name = name
        return this
    }

    String getGroup() {
        return group
    }

    GroovyScript setGroup(String group) {
        this.group = group
        return this
    }

    long getId(){
        return id
    }

    GroovyScript setId(long id){
        this.id = id
        return this
    }

    File getFolder() {
        return folder
    }

    GroovyScript setFolder(File folder) {
        this.folder = folder
        return this
    }

    File getScriptFile() {
        return scriptFile
    }

    GroovyScript setScriptFile(File scriptFile) {
        this.scriptFile = scriptFile
        return this
    }

    String getTriggerSpec() {
        return triggerSpec
    }

    GroovyScript setTriggerSpec(String spec) {
        this.triggerSpec = spec
        return this
    }

    ScheduledFuture getTrigger() {
        return trigger
    }

    GroovyScript setTrigger(ScheduledFuture trigger) {
        this.trigger = trigger
        return this
    }

    String getEvent() {
        return event
    }

    GroovyScript setEvent(String event) {
        this.event = event
        return this
    }

    String getLastExecution() {
        return lastExecution==null? "0000-00-00 00:00:00": DateFormatUtils.format(lastExecution, "yyyy-MM-dd HH:mm:ss")
    }

    GroovyScript setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution
        return this
    }

    String getDescription() {
        return description
    }

    String getShortDescription(){
        return description.length()>50? description.substring(0, 47)+"..." : description
    }

    GroovyScript setDescription(String description) {
        this.description = description
        return this
    }

    Yaml getYmld() {
        return ymld
    }

    List<String> getErrores() {
        return errores
    }

    String getLogfile() {
        return logfile
    }

    GroovyScript setLogfile(String logfile) {
        this.logfile = logfile
        return this
    }

    String getLogname() {
        return logname
    }

    GroovyScript setLogname(String logname) {
        this.logname = logname
        return this
    }

    String getLastExecutionStatus() {
        return lastExecutionStatus
    }

    GroovyScript setLastExecutionStatus(String lastExecutionStatus) {
        this.lastExecutionStatus = lastExecutionStatus
        return this
    }

    boolean isScriptActive() {
        return scriptActive
    }

    GroovyScript setScriptActive(boolean scriptActive) {
        this.scriptActive = scriptActive
        return this
    }

    String getType(){
        return scriptFile.name.endsWith(".groovy")? "groovy" : "otro"
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        GroovyScript that = (GroovyScript) o

        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return (int) (id ^ (id >>> 32))
    }

    String toJSON(){
        def result = [:]
        result.id = this.id
        result.type = getType()
        result.yaml = yaml.text
        result.script = scriptFile.text
        result.error = (errores!=null && errores.size()>0)? errores.join("\n"):"0"
        return JsonOutput.toJson(result)
    }

    Boolean isValid() {
        return (yaml!=null && folder!=null && name!=null && validYmlFile )
    }

    @Override
    String toString() {
        return "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ymld=" + ymld +
                ", lastExecution=" + lastExecution +
                ", description='" + description + '\'' +
                ", isValidYmlFile=" + validYmlFile +
                ", triggerSpec='" + triggerSpec + '\'' +
                ", trigger=" + trigger +
                ", Active=" + scriptActive +
                ", event=" + event +
                ", error='" + errores + '\'' +
                ", lastExecutionStatus='" + lastExecutionStatus + '\'' +
                '}'
    }

    void saveYml(){
        synchronized (this) {
            try {
                mapper.writeValue(yaml, ymld)
            }
            catch (ex) {
                throw ex
            }
        }
    }

}