package com.tsoft.plugins.scheduler.utils

import hudson.FilePath
import javaposse.jobdsl.dsl.*
import javaposse.jobdsl.plugin.*
import jenkins.model.Jenkins

class JobDsl {

    private HashMap<String, Object> binding
    private FilePath workspace
    private String scriptText = ""
    private String targets = ""
    private String additionalClasspath = ""
    private boolean isUsingScriptText = true
    private boolean ignoreExisting = false
    private boolean isIgnoreMissingFiles = false
    private boolean failOnMissingPlugin = true
    private boolean unstableOnDeprecation = true

    JobDsl(HashMap<String, Object> binding) {
        this.binding = binding
        this.workspace = (FilePath)binding.get("workspace")
    }
    
    def execute(){
        def envVars = Jenkins.getInstanceOrNull().getComputer('(master)').getEnvironment()
        JenkinsJobManagement jenkinsJobManagement = new JenkinsJobManagement(
                (PrintStream) binding.get("out"),
                binding,
                null,
                workspace,
                LookupStrategy.JENKINS_ROOT
        )

        jenkinsJobManagement.setFailOnMissingPlugin(failOnMissingPlugin)
        jenkinsJobManagement.setFailOnSeedCollision(false)
        jenkinsJobManagement.setUnstableOnDeprecation(unstableOnDeprecation)

        JobManagement jobManagement = new InterruptibleJobManagement(jenkinsJobManagement)

        try {
            ScriptRequestGenerator generator = new ScriptRequestGenerator(workspace, envVars)
            Set<ScriptRequest> scriptRequests = generator.getScriptRequests(
                    targets,
                    isUsingScriptText,
                    scriptText,
                    ignoreExisting,
                    isIgnoreMissingFiles,
                    additionalClasspath
            )

            JenkinsDslScriptLoader dslScriptLoader = new JenkinsDslScriptLoader(jobManagement)

            GeneratedItems generatedItems = dslScriptLoader.runScripts(scriptRequests)
            Set<GeneratedJob> freshJobs = generatedItems.getJobs()
            Set<GeneratedView> freshViews = generatedItems.getViews()
            Set<GeneratedConfigFile> freshConfigFiles = generatedItems.getConfigFiles()
            Set<GeneratedUserContent> freshUserContents = generatedItems.getUserContents()

            println freshJobs
            println freshViews
            println freshConfigFiles
            println freshUserContents
        }
        catch(ex){
            throw ex
        }
    }

    HashMap getBinding() {
        return binding
    }

    JobDsl setBinding(HashMap binding) {
        this.binding = binding
        return this
    }

    FilePath getWorkspace() {
        return workspace
    }

    JobDsl setWorkspace(FilePath workspace) {
        this.workspace = workspace
        return this
    }

    JobDsl setScriptText(String scriptText) {
        this.scriptText = scriptText
        this.isUsingScriptText = true
        return this
    }

    boolean getIsUsingScriptText() {
        return isUsingScriptText
    }

    String getTargets() {
        return targets
    }

    JobDsl setTargets(String targets) {
        this.targets = targets
        this.isUsingScriptText = false
        return this
    }

    boolean getIgnoreExisting() {
        return ignoreExisting
    }


    JobDsl setIgnoreExisting(boolean ignoreExisting) {
        this.ignoreExisting = ignoreExisting
        return this
    }

    boolean getIsIgnoreMissingFiles() {
        return isIgnoreMissingFiles
    }


    JobDsl setIsIgnoreMissingFiles(boolean isIgnoreMissingFiles) {
        this.isIgnoreMissingFiles = isIgnoreMissingFiles
        return this
    }

    String getAdditionalClasspath() {
        return additionalClasspath
    }


    JobDsl setAdditionalClasspath(String additionalClasspath) {
        this.additionalClasspath = additionalClasspath
        return this
    }
}
