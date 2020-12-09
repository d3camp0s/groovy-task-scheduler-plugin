package com.tsoft.plugins.scheduler.events

import com.tsoft.plugins.scheduler.models.Event
import com.tsoft.plugins.scheduler.utils.ScriptHelper
import hudson.Extension
import hudson.model.Computer
import hudson.model.TaskListener
import hudson.slaves.ComputerListener
import hudson.slaves.OfflineCause
import java.util.logging.Logger

@Extension
class ComputerEventListener extends ComputerListener {

    protected static Logger log = Logger.getLogger(ComputerEventListener.class.getName())

    ComputerEventListener() {
        log.info(">>> Computer Event Listener Initialised")
    }

    @Override
    void onLaunchFailure(final Computer computer, final TaskListener listener) {
        ScriptHelper.processEvent(Event.NODE_LAUNCH_FAILURE, ["computer":computer, "listener":listener])
    }

    @Override
    void onOnline(final Computer computer, final TaskListener listener) {
        ScriptHelper.processEvent(Event.NODE_ONLINE, ["computer":computer, "listener":listener])
    }

    @Override
    void onOffline(final Computer computer, final OfflineCause cause) {
        ScriptHelper.processEvent(Event.NODE_OFFLINE, ["computer":computer, "cause":cause])
    }

    @Override
    void onTemporarilyOnline(final Computer computer) {
        ScriptHelper.processEvent(Event.NODE_TEMP_ONLINE, ["computer":computer])
    }

    @Override
    void onTemporarilyOffline(final Computer computer, final OfflineCause cause) {
        ScriptHelper.processEvent(Event.NODE_TEMP_OFFLINE, ["computer":computer, "cause":cause])
    }
}