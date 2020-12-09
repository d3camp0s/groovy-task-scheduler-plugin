package com.tsoft.plugins.scheduler.models

import javax.annotation.Nonnull
/**
 * Contains constants for all available event names.
 */
final class Event {

    private Event() {}

    public static final String JOB_DELETED = "Job.onDeleted"
    public static final String JOB_STARTED = "Job.onStarted"
    public static final String JOB_FINALIZED = "Job.onFinalized"
    public static final String JOB_COMPLETED = "Job.onCompleted"

    public static final String NODE_LAUNCH_FAILURE = "Node.onLaunchFailure"
    public static final String NODE_ONLINE = "Node.onOnline"
    public static final String NODE_OFFLINE = "Node.onOffline"
    public static final String NODE_TEMP_ONLINE = "Node.onTemporarilyOnline"
    public static final String NODE_TEMP_OFFLINE = "Node.onTemporarilyOffline"

    public static final String QUEUE_WAITING = "Queue.onEnterWaiting"
    public static final String QUEUE_BLOCKED = "Queue.onEnterBlocked"
    public static final String QUEUE_BUILDABLE = "Queue.onEnterBuildable"
    public static final String QUEUE_LEFT = "Queue.onLeft"

    public static final String ITEM_UPDATED = "Item.onUpdated"
    public static final String ITEM_LOCATION_CHANGED = "Item.onLocationChanged"
    public static final String ITEM_RENAMED = "Item.onRenamed"
    public static final String ITEM_DELETED = "Item.onDeleted"
    public static final String ITEM_COPIED = "Item.onCopied"
    public static final String ITEM_CREATED = "Item.onCreated"

    public static final String FLOW_CREATED = "Flow.onCreated"
    public static final String DEPLOY_FINALIZED = "Pipe.onDeployFinalized"


    public static final String NOT_FOUND = "Event.NOT_FOUND"

    static List<Event> getAll() {
        return [JOB_DELETED,
                JOB_STARTED,
                JOB_FINALIZED,
                JOB_COMPLETED,
                NODE_LAUNCH_FAILURE,
                NODE_ONLINE,
                NODE_OFFLINE,
                NODE_TEMP_ONLINE,
                NODE_TEMP_OFFLINE,
                QUEUE_WAITING,
                QUEUE_BLOCKED,
                QUEUE_BUILDABLE,
                QUEUE_LEFT,
                ITEM_CREATED,
                ITEM_COPIED,
                ITEM_DELETED,
                ITEM_RENAMED,
                ITEM_LOCATION_CHANGED,
                ITEM_UPDATED,
                FLOW_CREATED,
                DEPLOY_FINALIZED
            ]
    }

    @Nonnull
    static String fromString(@Nonnull String s) {
        return getAll().contains(s)? s : NOT_FOUND
    }
}