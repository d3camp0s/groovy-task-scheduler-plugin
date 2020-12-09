package com.tsoft.plugins.scheduler.events;

import com.tsoft.plugins.scheduler.models.Event;
import com.tsoft.plugins.scheduler.utils.ScriptHelper;
import hudson.Extension;
import hudson.model.Queue.BlockedItem;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;
import jenkins.model.Jenkins;

import java.util.HashMap;
import java.util.logging.Logger;

@Extension
public class QueueEventListener extends QueueListener {

    protected static Logger log = Logger.getLogger(QueueEventListener.class.getName());

    /**
     * This class is lazy loaded (as required).
     */
    public QueueEventListener() {
        log.info(">>> Queue Event Listener Initialised");
    }

    @Override
    public void onEnterWaiting(final WaitingItem item) {
        ScriptHelper.processEvent(Event.QUEUE_WAITING, new HashMap<String, Object>() {{
            put("item", item);
        }});
    }

    @Override
    public void onEnterBlocked(final BlockedItem item) {
        ScriptHelper.processEvent(Event.QUEUE_BLOCKED, new HashMap<String, Object>() {{
            put("item", item);
        }});
    }

    @Override
    public void onEnterBuildable(final BuildableItem item) {
        ScriptHelper.processEvent(Event.QUEUE_BUILDABLE, new HashMap<String, Object>() {{
            put("item", item);
        }});
    }

    @Override
    public void onLeft(final LeftItem item) {
        ScriptHelper.processEvent(Event.QUEUE_LEFT, new HashMap<String, Object>() {{
            put("item", item);
        }});
    }

}
