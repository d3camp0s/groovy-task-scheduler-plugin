package com.tsoft.plugins.scheduler.events

import com.tsoft.plugins.scheduler.models.Event
import com.tsoft.plugins.scheduler.utils.ScriptHelper
import org.jenkinsci.plugins.workflow.flow.FlowExecution
import org.jenkinsci.plugins.workflow.flow.FlowExecutionListener
import javax.annotation.Nonnull
import java.util.logging.Logger
import hudson.Extension

@Extension
class StageEventListener extends FlowExecutionListener {

    protected static Logger log = Logger.getLogger(StageEventListener.class.getName())

    StageEventListener() {
        log.info(">>> FlowNode Event Listener Initialised")
    }

    @Override
    void onCreated(@Nonnull FlowExecution execution) {
        ScriptHelper.processEvent(Event.FLOW_CREATED, ["execution":execution])
    }

    /*
    @Override
    void onRunning(@Nonnull FlowExecution execution) {
        log.info("Event Fired: FlowExecution onRunning")
    }

    @Override
    void onResumed(@Nonnull FlowExecution execution) {
        log.info("Event Fired: FlowExecution onResumed")
    }

    @Override
    void onCompleted(@Nonnull FlowExecution execution) {
        log.info("Event Fired: FlowExecution onCompleted")
    }

 */
}
