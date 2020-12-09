package com.tsoft.plugins.scheduler.models

import com.tsoft.plugins.scheduler.utils.*
import hudson.FilePath
import hudson.model.User
import org.apache.commons.lang.time.DateFormatUtils
import java.util.concurrent.CountDownLatch

class Task implements Runnable, Serializable {

    private static final long serialVersionUID = 1L
    private GroovyScript script
    private HashMap<String, Object> inputs
    private CountDownLatch latch = null
    private current_date = ""
    private boolean requireContext = true

    Task(GroovyScript scp) {
        this(scp, new HashMap<>())
    }

    Task(GroovyScript scp, HashMap params) {
        this.script = scp
        this.inputs = params
        this.current_date = DateFormatUtils.format(new Date(), "yyyyMMdd_HHmmSS")
    }

    Task setLatch(CountDownLatch latch) {
        this.latch = latch
        return this
    }

    Task setRequireContext(boolean requireContext) {
        this.requireContext = requireContext
        return this
    }

    @Override
    void run() {

        PrintStream bout = null
        PrintStream berr = null
        FilePath tmpDir = null

        try {
            if (isRequireContext())
                configureContext(bout, berr, tmpDir)

            // fetch variables from de yaml config file
            def variables = script.getYmld().variables != null ? script.getYmld().variables : [:]
            inputs.putAll(variables)                     // variables del yml de configuracion del script

            // Se añaden al contexto las variables de las credenciales informadas
            def credentials = script.getYmld().credentials
            inputs.putAll( credentials.size()>0? ScriptHelper.getCredentialsMap(credentials) : [:] )

            GroovyShell gs = ScriptHelper.getGroovyShellWithContext(inputs)
            gs.evaluate(script.getScriptFile())

            if( script.getYmld().options.get("autosave")==true )
                script.saveYml()
        }
        catch (ex) {
            inputs.get("err").println(ex.toString())
            throw ex
        }
        finally {
            if (bout != null) {
                bout.close()
            }
            if (berr != null) {
                berr.close()
            }
            if (latch != null) {
                latch.countDown()
            }
            if( tmpDir!=null && tmpDir.exists() ) {
                def deleteWorkspace = script.getYmld().options.get("deleteWorkspace")
                if( deleteWorkspace==null || deleteWorkspace == true )
                    tmpDir.deleteRecursive()
            }
        }
    }

    void configureContext(PrintStream bout, PrintStream berr, FilePath tmpDir){
        def user = User.current()
        inputs.put("username", user==null? "jenkins" : user.id)
        inputs.putAll(ScriptHelper.getParams())      // variables del contexto

        // Crea directorio de trabajo temporal para el script de groovy
        tmpDir = ((FilePath)inputs.get("workspace")).createTempDir(script.folder.name,"")
        tmpDir.mkdirs()
        inputs.put("workspace", tmpDir )

        // Crea y almacena el log del script
        String logFileName = (new StringBuffer())
                .append(script.logname)
                .append("_")
                .append(latch != null ? "manual" : "auto")
                .append("-")
                .append(user != null ? user.id : "jenkins")
                .append("-")
                .append(current_date)

        inputs.put("logname", logFileName)
        File fout = new File(Consts.DEFAULT_LOGDIR_GTASKS, logFileName + ".out")
        File ferr = new File(Consts.DEFAULT_LOGDIR_GTASKS, logFileName + ".err")

        bout = new PrintStream(new FileOutputStream(fout), true, "UTF-8")
        berr = new PrintStream(new FileOutputStream(ferr), true, "UTF-8")

        inputs.put("out", bout)
        inputs.put("err", berr)
        inputs.put("yml", script.getYmld())         // Objeto yml de configuracion

        def jobDsl = new JobDsl(inputs)
        def gitClient = new GitClient(inputs)
        def restClient = new RESTClient()

        inputs.put("jobDsl", jobDsl)                // add JobDSL object in context
        inputs.put("git", gitClient)                // add git client in context
        inputs.put("RESTClient", restClient)        // add REST client in context

        // add JobExec to context (permite la ejecucion de subtareas dentro de una ejecucion de script)
        def jobExec = new SubTaskExecutor(script.name, inputs)
        inputs.put("exec", jobExec)

        script.setLogfile(logFileName)
        script.setLastExecution(new Date())
    }

    @Override
    String toString() {
        return "{" +
                "id:" + script.id +
                ", name:" + script.name +
                ", latchCount:" + (latch!=null? latch.count : 0) +
                '}';
    }

    def toMap() {
        return ['id': script.id,
                'name': script.name,
                'status': Thread.currentThread().getState(),
                'threadId': Thread.currentThread().getId(),
                'threadName': Thread.currentThread().getName(),
                'latchCount': (latch!=null? latch.count : 0)]
    }

    boolean isRequireContext() {
        return requireContext
    }
}

