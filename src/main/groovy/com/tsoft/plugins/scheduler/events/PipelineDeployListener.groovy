package com.tsoft.plugins.scheduler.events

import com.tsoft.plugins.scheduler.listeners.PipelineListener
import com.tsoft.plugins.scheduler.models.Event
import com.tsoft.plugins.scheduler.utils.ScriptHelper
import hudson.Extension
import hudson.model.Run
import java.util.logging.Logger

@Extension
class PipelineDeployListener extends PipelineListener<Run> {

    protected static Logger log = Logger.getLogger(PipelineDeployListener.class.getName())

    PipelineDeployListener() {
        log.info(">>> Pipeline Event Listener Initialised")
    }

    @Override
    void onDeploy(Run run, Map d) {
        ScriptHelper.processEvent(Event.DEPLOY_FINALIZED, ["run":run, "params":d])
    }
}
