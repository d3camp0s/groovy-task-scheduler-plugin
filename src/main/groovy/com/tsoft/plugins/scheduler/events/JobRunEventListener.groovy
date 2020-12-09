package com.tsoft.plugins.scheduler.events

import com.tsoft.plugins.scheduler.models.Event
import com.tsoft.plugins.scheduler.utils.ScriptHelper
import hudson.Extension
import hudson.model.Run
import hudson.model.TaskListener
import hudson.model.listeners.RunListener
import javax.annotation.Nonnull
import java.util.logging.Logger

@Extension
class JobRunEventListener extends RunListener<Run> {

    protected static Logger log = Logger.getLogger(JobRunEventListener.class.getName())

    /**
     * This class is lazy loaded (as required).
     */
    JobRunEventListener() {
        log.info(">>> JOB Event Listener Initialised")
    }

    @Override
    void onDeleted(final Run run) {
        ScriptHelper.processEvent(Event.JOB_DELETED, ["run":run])
    }

    @Override
    void onStarted(final Run run, final TaskListener listener) {
        ScriptHelper.processEvent(Event.JOB_STARTED, ["run":run, "listener":listener])
    }

    @Override
    void onFinalized(final Run run) {
        ScriptHelper.processEvent(Event.JOB_FINALIZED, ["run":run])
    }

    @Override
    void onCompleted(final Run run, final @Nonnull TaskListener listener) {
        ScriptHelper.processEvent(Event.JOB_COMPLETED, ["run":run, "listener":listener])
    }

}