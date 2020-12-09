package com.tsoft.plugins.scheduler.utils

import com.cloudbees.plugins.credentials.CredentialsConfidentialKey
import com.cloudbees.plugins.credentials.CredentialsMatcher
import com.cloudbees.plugins.credentials.CredentialsMatchers
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import jenkins.model.Jenkins

/**
 * Created by D3CAMP0S on 09/05/2020.
 */
class Consts {
    public static final int MAX_POOL_SIZE = 10
    public static final int TASK_MAX_WAIT_TIME_SECONDS = 300
    public static final int TASK_MAX_WAIT_TIME_MINUTES = 5
    public static final String PLUGIN_URL = "/plugin/groovy-task-scheduler-plugin/"
    public static final String PLUGIN_IMAGES_URL = PLUGIN_URL + "images/"
    public static final String PLUGIN_JS_URL = PLUGIN_URL + "js/"

    public static final String TASK_MANAGER_DISPLAY_NAME = "Configure Groovy Scripts"
    public static final String TASK_MANAGER_URL_NAME = "admin-scripts"
    public static final String PLUGIN_MANAGER_DESCRIPTION = "Programa eventos, ejecuta tareas manualmente y carga scripts desde el workspace de jenkins"

    public static final File ROOT_SCRIPTS_DIR = new File(Jenkins.get().getRootDir(),"groovy-task-plugin/scripts")
    public static final File DEFAULT_LOGDIR_GTASKS = new File(Jenkins.get().getRootDir(),"groovy-task-plugin/logs")
    public static final File DEFAULT_TMP_DIR = new File(Jenkins.get().getRootDir(),"groovy-task-plugin/tmp")
    public static final File DEFAULT_WORKSPACE = new File(Jenkins.get().getRootDir(),"groovy-task-plugin/workspace")

    public static final CredentialsMatcher CREDENTIALS_MATCHER = CredentialsMatchers.anyOf(
            CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class),
            CredentialsMatchers.instanceOf(CredentialsConfidentialKey.class)
    )
}
