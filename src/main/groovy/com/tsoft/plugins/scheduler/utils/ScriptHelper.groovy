package com.tsoft.plugins.scheduler.utils

import static com.cloudbees.plugins.credentials.CredentialsMatchers.firstOrNull
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import org.jenkinsci.plugins.plaincredentials.StringCredentials
import com.tsoft.plugins.scheduler.config.GroovyTaskManager
import com.tsoft.plugins.scheduler.config.GroovyTaskPoolTrigger
import com.tsoft.plugins.scheduler.models.Event
import com.tsoft.plugins.scheduler.models.GroovyScript
import com.tsoft.plugins.scheduler.models.Task
import hudson.EnvVars
import hudson.FilePath
import hudson.model.Node
import hudson.security.ACL
import hudson.slaves.EnvironmentVariablesNodeProperty
import jenkins.model.Jenkins
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

import static groovy.io.FileType.FILES

class ScriptHelper {

    private final static Logger log = Logger.getLogger(ScriptHelper.class.getName())
    static final AtomicLong iota;
    static ClassLoader jenkinsClassloader
    static GroovyClassLoader groovyClassloader
    static CompilerConfiguration compilerConfiguration
    static ImportCustomizer ic

    static {
        iota = new AtomicLong()
        ic = new ImportCustomizer()
        ic.addStarImports(this.class.getPackage().getName())
        ic.addStarImports("jenkins", "jenkins.model", "hudson", "hudson.model", "com.tsoft.plugins.*")

        compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.addCompilationCustomizers(ic)

        jenkinsClassloader = Jenkins.getInstanceOrNull().getPluginManager().uberClassLoader// getClass().getClassLoader()
        groovyClassloader = new GroovyClassLoader(jenkinsClassloader)

        def classpath = []
        new File(Jenkins.get().getRootDir(),"plugins").traverse(type: FILES, nameFilter: ~/.+\.jar/) { file ->
            groovyClassloader.addClasspath(file.absolutePath)
            classpath.add(file.absolutePath)
        }
        compilerConfiguration.setClasspathList(classpath)

        Jenkins.getInstanceOrNull().getAuthorizationStrategy()
    }

    static long generateId() {
        return new Date().time + iota.getAndIncrement()
    }

    static synchronized void processEvent(String event, Map<String, Object> params) {
        try {
            def currentEvent = Event.fromString(event)
            GroovyTaskManager.get()
                    .getYmlGScripts()
                    .each { script ->
                        if (script.event != null && currentEvent == script.event && script.isScriptActive()) {
                            params.put("event", currentEvent)
                            GroovyTaskPoolTrigger.execute(new Task(script, params))
                        }
                    }
        } catch (Throwable t) {
            log.severe("Groovy Event Task: Caught unhandled exception! " + t.getMessage())
        }
    }

    static Map<Object, Object> getParams(){
        def jks = Jenkins.getInstanceOrNull()
        def envVars = jks?.globalNodeProperties?.get(EnvironmentVariablesNodeProperty)
        Map<Object, Object> params = new HashMap<Object, Object>()

        params.put("jenkins", jks)
        params.put("env", envVars==null? [:] : envVars )
        params.put("log", log)
        params.put("workspace", new FilePath(jks.getRootPath(), "groovy-task-plugin/workspace"))        // Set initial workspace for groovy script

        return params
    }

    static FilePath getFilePath(String node_name){
        if(node_name==null || node_name==""){
            node_name = "(master)"
        }
        Node node = Jenkins.get().getNode(node_name)
        return node.getRootPath()
    }

    static EnvVars getEnvVars(String node_name){
        if(node_name==null || node_name==""){
            node_name = "(master)"
        }
        Node node = Jenkins.get().getNode(node_name)
        return node.toComputer().getEnvironment()
    }

    /**
     * Procesa la propiedad 'trigger' del yaml y activa el Timer y/o Event
     * {@link java.util.concurrent.ScheduledFuture} / {@link Event}
     */
    static void registerTimerOrEvent(GroovyScript script){
        def ymld = script.getYmld()
        def keys = ymld.trigger.keySet()

        if ( keys.contains ( "active" ) )
            script.scriptActive = ( ymld.trigger.get( "active" ) == "true" )

        switch (script.getTriggerSpec()) {
            case "cron":
                script.setTrigger (
                    script.isScriptActive() ?
                        GroovyTaskPoolTrigger.schedule( new Task(script), ymld.trigger.get("cron")): null )
                break
            case "event":
                script.setEvent(Event.fromString(ymld.trigger.get("event")))
                break
            case "manual":
                script.scriptActive = (ymld.trigger.get("manual") == "true" && script.scriptActive)
                break
        }
    }

    static Map<String, String> getCredentialsMap(List credentialsId){
        def credentials = new HashMap<String, String>()
        credentialsId.each { String credId ->
            def creds = firstOrNull(lookupCredentials(
                    StandardUsernamePasswordCredentials.class,
                    Jenkins.getInstanceOrNull(),
                    ACL.SYSTEM,
                    Collections.emptyList()),
                    withId(credId))

            if (creds != null) {
                credentials.put(credId+"_user", creds.username)
                credentials.put(credId+"_pass", creds.password.plainText)
            }

            def token = firstOrNull(lookupCredentials(
                    StringCredentials.class,
                    Jenkins.getInstanceOrNull(),
                    ACL.SYSTEM,
                    Collections.emptyList()),
                    withId(credId))

            if (token != null) {
                credentials.put(credId+"_value", token.secret.plainText)
            }
        }

        return credentials
    }

    static GroovyShell getGroovyShellWithContext(HashMap<String, Object> params) {
        return new GroovyShell( groovyClassloader, new Binding(params), compilerConfiguration)
    }

    /**
     * Checks if the current user is anonymous.
     */
    static boolean isAnonymous() {
        return ACL.isAnonymous(Jenkins.getAuthentication())
    }

}