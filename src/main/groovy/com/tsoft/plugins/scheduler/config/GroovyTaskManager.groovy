package com.tsoft.plugins.scheduler.config

import com.tsoft.plugins.scheduler.TaskMonitor
import com.tsoft.plugins.scheduler.models.GroovyScript
import com.tsoft.plugins.scheduler.models.Task
import com.tsoft.plugins.scheduler.utils.Consts
import com.tsoft.plugins.scheduler.utils.ScriptHelper
import groovy.json.JsonOutput
import hudson.Extension
import hudson.model.Describable
import hudson.model.Descriptor
import hudson.model.ManagementLink
import hudson.model.Saveable
import hudson.util.HttpResponses
import jenkins.model.Jenkins
import org.apache.commons.io.FilenameUtils
import org.kohsuke.accmod.Restricted
import org.kohsuke.accmod.restrictions.NoExternalUse
import org.kohsuke.stapler.HttpResponse
import org.kohsuke.stapler.StaplerProxy
import org.kohsuke.stapler.StaplerRequest
import org.kohsuke.stapler.StaplerResponse
import org.kohsuke.stapler.interceptor.RequirePOST

import javax.servlet.ServletException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES

@Extension
class GroovyTaskManager extends ManagementLink implements StaplerProxy, Describable<GroovyTaskManager>, Saveable {

    private final static Logger log = Logger.getLogger(GroovyTaskManager.class.getName())
    private Set<GroovyScript> scripts = null

    GroovyTaskManager() throws IOException {
        // directorio se scripts
        if( !Consts.ROOT_SCRIPTS_DIR.exists() )
            Consts.ROOT_SCRIPTS_DIR.mkdirs()

        if( !Consts.DEFAULT_LOGDIR_GTASKS.exists() )
            Consts.DEFAULT_LOGDIR_GTASKS.mkdirs()

        if( !Consts.DEFAULT_TMP_DIR.exists() )
            Consts.DEFAULT_TMP_DIR.mkdirs()

        if( !Consts.DEFAULT_WORKSPACE.exists() )
            Consts.DEFAULT_WORKSPACE.mkdirs()

        // Load scripts
        load()

        log.info("Groovy Task Manager is fully up and running")
    }

    @Override
    Descriptor<GroovyTaskManager> getDescriptor() {
        return Jenkins.get().getDescriptorByType(DescriptorImpl.class)
    }

    @Override
    String getIconFileName() {
        return Consts.PLUGIN_IMAGES_URL + "48x48/groovy_task_manager.png"
    }

    @Override
    String getDisplayName() {
        return Consts.TASK_MANAGER_DISPLAY_NAME
    }

    @Override
    String getUrlName() {
        return Consts.TASK_MANAGER_URL_NAME
    }

    @Override
    String getDescription() {
        return Consts.PLUGIN_MANAGER_DESCRIPTION
    }

    @Override
    void save() throws IOException {
        //TODO: No Implementar metodo de gurdado!
    }

    void load() throws IOException {
        if( scripts==null ) {
            scripts = new HashSet<>()
            loadScripts()
        }
        log.info("Scripts loaded from: ${Consts.ROOT_SCRIPTS_DIR}")
    }

    @Override
    Object getTarget() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER)
        return this
    }

    static GroovyTaskManager get() {
        return ManagementLink.all().get(GroovyTaskManager.class)
    }


    @Extension
    static final class DescriptorImpl extends Descriptor<GroovyTaskManager> {

        @Override
        String getDisplayName() {
            return Consts.TASK_MANAGER_DISPLAY_NAME
        }

        @Restricted(NoExternalUse.class)
        void doInterrupt(StaplerRequest req, StaplerResponse rsp) throws IOException {
            try {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                req.setCharacterEncoding("UTF-8")
                long threadId = req.getParameter("id").toLong()
                TaskMonitor.interrupt(threadId)
                rsp.setStatus(200)
            }
            catch (ex){
                rsp.setStatus(500)
                throw ex
            }
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        void doGetScriptPreview(StaplerRequest req, StaplerResponse rsp) throws IOException {
            try {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                req.setCharacterEncoding("UTF-8")
                rsp.setContentType("application/json")

                String value = req.getParameter("value")
                if(value==null) {
                    rsp.setStatus(404)
                    rsp.getWriter().println("Label can not be empty!")
                }
                else {
                    GroovyScript sp = GroovyTaskManager.get().getYmlGScripts().find { it.id==Double.parseDouble(value) }
                    if( sp!=null ) {
                        rsp.setStatus(200)
                        rsp.getWriter().println(sp.toJSON())
                    }
                    else {
                        rsp.setStatus(404)
                        rsp.getWriter().println("SCRIPT NO ENCONTRADO!")
                    }
                }
            }
            catch (Exception e){
                rsp.setStatus(500)
                rsp.getWriter().println(e.toString())
            }
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        void doLoadTemplate(StaplerRequest req, StaplerResponse rsp) throws IOException {
            try {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                req.setCharacterEncoding("UTF-8")
                rsp.setContentType("application/text")

                String value = req.getParameter("id")
                if(value==null) {
                    rsp.setStatus(404)
                    rsp.getWriter().println("id can not be empty!")
                }
                else {
                    GroovyScript sp = GroovyTaskManager.get().getYmlGScripts().find { it.id==Double.parseDouble(value) }
                    if( sp!=null && sp.ymld.template!='') {
                        File template = new File(Consts.ROOT_SCRIPTS_DIR, sp.folder.name + "/" + sp.ymld.template)
                        if (template.exists()){
                            rsp.setStatus(200)
                            rsp.getWriter().println(template.getText("utf-8"))
                        }
                        else {
                            rsp.setStatus(404)
                            rsp.getWriter().println("Error al cargar el template: Not FOUND!")
                        }
                    }
                    else {
                        rsp.setStatus(400)
                        rsp.getWriter().println("Error al cargar el template del script, variable ymld.template no puede estar vacio.")
                    }
                }
            }
            catch (Exception e){
                rsp.setStatus(500)
                rsp.getWriter().println(e.toString())
            }
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        void doGetLog(StaplerRequest req, StaplerResponse rsp) throws IOException {
            def output = [:]
            try {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                req.setCharacterEncoding("UTF-8")
                rsp.setContentType("application/json")

                String value = req.getParameter("id")
                if(value==null) {
                    rsp.setStatus(500)
                    output.fout = ""
                    output.ferr = "Script id can not be empty"
                }
                else {
                    GroovyScript sp = GroovyTaskManager.get().getYmlGScripts().find { it.id==Double.parseDouble(value) }
                    if( sp!=null ) {
                        def logfile = new File(Consts.DEFAULT_LOGDIR_GTASKS,sp.getLogfile()+".out")
                        def errfile = new File(Consts.DEFAULT_LOGDIR_GTASKS,sp.getLogfile()+".err")
                        if( logfile.exists() && errfile.exists() ) {
                            rsp.setStatus(200)
                            output.scriptname = sp.getName()
                            output.fout = logfile.text
                            output.ferr = errfile.text
                        }
                        else {
                            rsp.setStatus(404)
                            output.fout = ""
                            output.ferr = "LogFile not Found"
                        }
                    }
                    else {
                        rsp.setStatus(404)
                        output.fout = ""
                        output.ferr = "Script not Found"
                    }
                }

                rsp.getWriter().println(JsonOutput.toJson(output))
            }
            catch (Exception e){
                log.severe(e.toString())
                rsp.setStatus(500)
                output.fout = ""
                output.ferr = e.toString()
                rsp.getWriter().println(JsonOutput.toJson(output))
            }
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        void doExecute(StaplerRequest req, StaplerResponse rsp) throws IOException {
            synchronized (this) {
                try {
                    Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                    req.setCharacterEncoding("UTF-8")
                    rsp.setContentType("application/text")

                    String value = req.getParameter("id")

                    if (value == null) {
                        rsp.setStatus(500)
                        rsp.getWriter().println("Script id can not be empty!")
                    } else {
                        GroovyScript sp = GroovyTaskManager.get().getYmlGScripts().find {
                            it.id == Double.parseDouble(value)
                        }
                        if (sp != null) {
                            if (sp.triggerSpec == 'manual') {
                                if (sp.isScriptActive()) {
                                    // Se debe cancelar la ejecucion anterior en curso
                                    if (sp.trigger != null) {
                                        sp.trigger.cancel(true)
                                    }

                                    // Se ejecuta el script en un hilo actual
                                    def latch = new CountDownLatch(1)
                                    def task = new Task(sp).setLatch(latch)
                                    TaskMonitor.add(task)
                                    task.run()

                                    // TODO: Sera necesario el latch para esperar al hilo responder?
                                    if ( latch.await(Consts.TASK_MAX_WAIT_TIME_SECONDS, TimeUnit.SECONDS) ) {
                                        log.info("Task: '${sp.name}' done successfully")
                                    }
                                    else {
                                        log.severe("Gave up waiting for tasks to finish running")
                                    }

                                    task.finalize()
                                    // Se registra el evento anterior
                                    ScriptHelper.registerTimerOrEvent(sp)

                                    rsp.setStatus(200)
                                    rsp.getWriter().println("Ejecucion de script: " + sp.getName() + " encolada")
                                } else {
                                    rsp.setStatus(403)
                                    rsp.getWriter().println("Script desactivado no permite la ejecucion manual.")
                                }
                            } else {
                                rsp.setStatus(403)
                                rsp.getWriter().println("Script debe ser configurado como 'manual:true' para permitir ejecucion manual.")
                            }
                        } else {
                            rsp.setStatus(404)
                            rsp.getWriter().println("Script no encontrado!")
                        }
                    }
                }
                catch (Exception e) {
                    log.severe(e.toString())
                    rsp.setStatus(500)
                    rsp.getWriter().println(e.toString())
                }
            }
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        void doExecuteFromPanel(StaplerRequest req, StaplerResponse rsp) throws IOException {
            synchronized (this) {
                try {
                    Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                    req.setCharacterEncoding("UTF-8")
                    rsp.setContentType("application/text")

                    String id = req.getParameter("id")

                    if (id == null || req.getParameterMap().size()==0) {
                        rsp.setStatus(500)
                        rsp.getWriter().println("Form inputs cannot be empty!")
                    } else {
                        GroovyScript sp = GroovyTaskManager.get().getYmlGScripts().find {
                            it.id == Double.parseDouble(id)
                        }
                        if (sp != null) {
                            if (sp.triggerSpec == 'manual') {
                                if (sp.isScriptActive()) {

                                    // genera map de parametros
                                    def params = req.getParameterMap().inject([:]){ map, it ->
                                        map[it.key] = it.value[0]
                                        map
                                    }

                                    // Se debe cancelar la ejecucion anterior en curso
                                    if (sp.trigger != null) {
                                        sp.trigger.cancel(true)
                                    }

                                    // Se ejecuta el script en un hilo actual
                                    def latch = new CountDownLatch(1)
                                    def task = new Task(sp, params, true).setLatch(latch)
                                    TaskMonitor.add(task)
                                    task.run()

                                    // TODO: Sera necesario el latch para esperar al hilo responder?
                                    if ( latch.await(Consts.TASK_MAX_WAIT_TIME_SECONDS, TimeUnit.SECONDS) ) {
                                        log.info("Task: '${sp.name}' done successfully")
                                    }
                                    else {
                                        log.severe("Gave up waiting for tasks to finish running")
                                    }

                                    task.finalize()
                                    // Se registra el evento anterior
                                    ScriptHelper.registerTimerOrEvent(sp)

                                    rsp.setStatus(200)
                                    rsp.getWriter().println("Ejecucion de script: " + sp.getName() + " encolada")
                                } else {
                                    rsp.setStatus(403)
                                    rsp.getWriter().println("Script desactivado no permite la ejecucion manual.")
                                }
                            } else {
                                rsp.setStatus(403)
                                rsp.getWriter().println("Script debe ser configurado como 'manual:true' para permitir ejecucion manual.")
                            }
                        } else {
                            rsp.setStatus(404)
                            rsp.getWriter().println("Script no encontrado!")
                        }
                    }
                }
                catch (Exception e) {
                    log.severe(e.toString())
                    rsp.setStatus(500)
                    rsp.getWriter().println(e.toString())
                }
            }
        }

        @RequirePOST
        void doCreate(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            try {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                def validProjectFolder = ~/[0-9A-Za-z]+/
                req.setCharacterEncoding("UTF-8")
                rsp.setContentType("application/text")

                String folder = req.getParameter("folder")
                String yaml = req.getParameter("yamlfile")
                String script = req.getParameter("scriptfile")

                //Se agrega extension a archivos yml y groovy
                if( !(yaml.endsWith(".yml") || yaml.endsWith(".yaml")) )
                    yaml = yaml+".yml"

                if( !script.endsWith(".groovy") )
                    script = script+".groovy"

                def validFolder = (folder =~ validProjectFolder).matches()
                if( validFolder ){
                    def new_folder = new File(Consts.ROOT_SCRIPTS_DIR, folder)
                    if (!new_folder.exists()) {
                        new_folder.mkdir()
                        def yamlfile = new File(new_folder, yaml)
                        if( !yamlfile.exists() ){
                            yamlfile.createNewFile()
                            yamlfile.text = "---\nname: ${folder.toLowerCase()}\ntrigger:\n  cron: '* * * * * ?'\n  active: false\nscript: ${script}\ndescription: no description found\nvariables:\n  key: 'value'\noptions:\n  opt1: 'val1'"
                        }
                        def scriptfile = new File(new_folder, script)
                        if( !scriptfile.exists() ){
                            scriptfile.createNewFile()
                            scriptfile.text = "log.info('Debe completar el codigo del script aqui')"
                        }
                        GroovyScript sp = new GroovyScript().setFolder(new_folder)
                                                .setName(FilenameUtils.removeExtension(new_folder.name))
                                                .setYaml(yamlfile)
                                                .setScriptFile(scriptfile)
                        if(sp.isValid())
                            ScriptHelper.registerTimerOrEvent(sp)

                        GroovyTaskManager.get().getYmlGScripts().add(sp)

                        rsp.setStatus(200)
                        rsp.getWriter().println("Creado con exito!")
                    } else {
                        rsp.setStatus(500)
                        rsp.getWriter().println("Ya existe un directorio con el nombre: "+folder)
                    }
                }
                else {
                    rsp.setStatus(500)
                    rsp.getWriter().println("El nombre del directorio no debe contener caracteres especiales: "+folder)
                }
            }
            catch (ex){
                rsp.setStatus(500)
                rsp.getWriter().println(ex.toString())
                log.severe("Error creando nuevo script: "+ex.toString())
            }
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        void doDelete(StaplerRequest req, StaplerResponse rsp) throws IOException {
            try {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                req.setCharacterEncoding("UTF-8")
                rsp.setContentType("application/text")

                String value = req.getParameter("id")

                if(value==null) {
                    rsp.setStatus(500)
                    rsp.getWriter().println("Script id can not be empty!")
                }
                else {
                    GroovyScript sp = GroovyTaskManager.get().getYmlGScripts().find { it.id==Double.parseDouble(value) }
                    if( sp!=null ) {
                        // Se debe cancelar la ejecucion anterior en curso
                        if(sp.trigger!=null){ sp.trigger.cancel(true) }

                        // delete any file on folder
                        sp.folder.listFiles().each { file->
                            file.delete()
                        }
                        // delete root folder
                        sp.folder.delete()

                        // delete scrip from memory
                        GroovyTaskManager.get().deleteScript(sp)

                        rsp.setStatus(200)
                        rsp.getWriter().println("Script: "+sp.getName() +" deleted from list")
                    }
                    else {
                        rsp.setStatus(404)
                        rsp.getWriter().println("Script not found!")
                    }
                }
            }
            catch (Exception e){
                log.severe(e.toString())
                rsp.setStatus(500)
                rsp.getWriter().println(e.toString())
            }
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        void doSaveScript(StaplerRequest req, StaplerResponse rsp) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER)
            req.setCharacterEncoding("UTF-8")
            rsp.setContentType("application/text")

            String scriptId = req.getParameter("scriptId")
            String scriptCode = req.getParameter("scriptCode")
            String scriptYmlConfig = req.getParameter("scriptYmlConfig")

            GroovyScript sp = GroovyTaskManager.get().getYmlGScripts().find { it.id==Double.parseDouble(scriptId) }
            try {
                if( sp!=null ){
                    // Se elimina la ejecucion periodica anterior
                    if( sp.trigger!=null ){ sp.trigger.cancel(false) }

                    //Se limpia historial de errores
                    if( sp.errores!=null && sp.errores.size()>0 ){ sp.errores.clear() }

                    // Se elimina la configuracion del evento
                    if( sp.event!=null ){ sp.event = null }

                    def ymlfile = new File(sp.getYaml().path)
                    ymlfile.text = scriptYmlConfig
                    sp.setYaml(ymlfile)

                    def scriptfile = new File(sp.getScriptFile().path)
                    scriptfile.text = scriptCode
                    sp.setScriptFile(scriptfile)

                    // Si ambos archivos son validos (yml, groovy) se registra el timer o el evento segun corresponda
                    if( sp.isValid() )
                        ScriptHelper.registerTimerOrEvent(sp)

                    GroovyTaskManager.get().getYmlGScripts().add(sp)
                    rsp.setStatus(200)
                    rsp.getWriter().println("Save completed successfully")
                }
            } catch (Throwable t) {
                sp.errores.add(t.toString())
                rsp.setStatus(500)
                rsp.getWriter().println("An exception was caught.\n\n"+t.getMessage())
                log.severe(t.printStackTrace())
            }
        }

        @RequirePOST
        @Restricted(NoExternalUse.class)
        void doGetCurrentTasks(StaplerRequest req, StaplerResponse rsp) throws IOException {
            req.setCharacterEncoding("UTF-8")
            rsp.setContentType("application/json")
            try {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER)
                rsp.setStatus(200)
                rsp.getWriter().println(JsonOutput.toJson(TaskMonitor.getTaskList()))
            }
            catch (ex){
                log.severe(ex.toString())
                rsp.setStatus(500)
                rsp.getWriter().println(JsonOutput.toJson(['error':ex.toString()]))
            }
        }
    }

    /* Used in sidepanel_manager.jelly */
    List<ManagementLink> getManagementLinks(){
        return Jenkins.get().getManagementLinks()
    }
    Set<Thread> getAllThreads(){
        return TaskMonitor.getAllThread()
    }

    // Detiene la ejecucion de los script y limpia la lista en memoria
    private void cleanAndRemoveScripts(){
        scripts.each { script->
            if( script.trigger!=null )
                script.trigger.cancel(true)
        }
        scripts.clear()
    }

    // Busca y carga en memoria los scripts del workspace
    private void loadScripts(){
        try {
            // recupera informacion de ultimas ejecuciones
            def logs = [:]
            Consts.DEFAULT_LOGDIR_GTASKS.listFiles().sort { it.lastModified() }.each { file ->
                if (file.name.contains("_")) {
                    def name = file.name.split("_")
                    logs.put(name[0], file)
                }
            }

            def filter = ~/.*(\.groovy|\.yml|\.yaml)$/
            Consts.ROOT_SCRIPTS_DIR.traverse(type: DIRECTORIES, maxDepth: 0) { dir ->
                GroovyScript sp = new GroovyScript().setFolder(dir)
                dir.traverse(type: FILES, maxDepth: 0, nameFilter: filter) { file ->
                    if (file.name.endsWith(".yml") || file.name.endsWith(".yaml")) {
                        sp.setYaml(file)
                        sp.setName(sp.getYmld().name) // Se obtiene el nombre del yml
                        //sp.setName(FilenameUtils.removeExtension(file.name))
                    } else if (file.name.endsWith(".groovy") ) {
                        sp.setScriptFile(file)
                    }
                }

                // Si ambos archivos son validos (yml, groovy) se le pide al Objecto crear un Script con el classpath del pluginmanager
                if (sp.isValid()) {
                    try {
                        ScriptHelper.registerTimerOrEvent(sp)
                        File logfile = logs.get(sp.getLogname())
                        if (logfile != null) {
                            sp.setLogfile(FilenameUtils.removeExtension(logfile.name))
                            sp.setLastExecution(new Date(logfile.lastModified()))
                        }
                    }
                    catch (t){
                        log.warning(t.toString())
                        sp.errores.add(t.toString())
                    }
                }
                // se valida si existe el script con el mismo nombre
                if( scripts.any { it.name == sp.name} )
                    log.info("Se ignora el script: ${sp.toString()} por duplicidad de nombre y carpeta contenedora")
                else
                    scripts.add(sp)
            }
        }
        catch (ex){
            log.severe(ex.toString())
            throw ex
        }
    }

    HashSet<GroovyScript> getYmlGScripts(){
        return this.scripts
    }

    private boolean deleteScript(GroovyScript sp){
        return this.scripts.remove(sp)
    }

    @Restricted(NoExternalUse.class)
    HashSet<GroovyScript> getAllTasks(){
        if(scripts==null)
            loadScripts()
        return scripts.toSorted { a -> a.group }
    }

    @Restricted(NoExternalUse.class)
    Map<String, GroovyScript> getGroupedTasks(){
        return getAllTasks().groupBy{ it.group }.toSorted { it.key }
    }

    @RequirePOST
    @Restricted(NoExternalUse.class)
    HttpResponse doReloadScripts(StaplerRequest request) throws IOException, ServletException, InterruptedException {
        try {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER)
            cleanAndRemoveScripts()
            loadScripts()
        }
        catch (ex){
            log.severe("Error recargando los scripts: "+ex.toString())
        }

        // go back to the Run console page
        return HttpResponses.redirectTo("../admin-scripts")
    }



}
