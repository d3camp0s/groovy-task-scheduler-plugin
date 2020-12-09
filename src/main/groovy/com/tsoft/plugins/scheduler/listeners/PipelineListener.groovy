package com.tsoft.plugins.scheduler.listeners

import hudson.ExtensionList
import hudson.ExtensionPoint
import hudson.model.Run
import org.jvnet.tiger_types.Types
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.logging.Level
import java.util.logging.Logger

abstract class PipelineListener<R extends Run> implements ExtensionPoint {

    public final Class<R> targetType

    protected PipelineListener(Class<R> targetType) {
        this.targetType = targetType
    }

    protected PipelineListener() {
        Type type = Types.getBaseClass(getClass(), PipelineListener.class)
        if (type instanceof ParameterizedType)
            targetType = Types.erasure(Types.getTypeArgument(type,0))
        else
            throw new IllegalStateException(getClass()+" uses the raw type for extending PipelineListener")
    }

    /**
     * Called right before a pipeline is deployed.
     *
     * @param r The build.
     * @param d The build params.
     * @throws RuntimeException
     *      Any exception/error thrown from this method will be swallowed to prevent broken listeners
     *      from breaking all the builds.
     */
    public void onDeploy(R r, Map d){}
    
    /**
     * Fires the {@link #onDeploy} event.
     */

    static void fireDeploy(Run r) {
        this.fireDeploy(r, [:])
    }

    static void fireDeploy(Run r, Map data) {
        data = (data == null? [:] : data)
        for (PipelineListener l : all()) {
            if(l.targetType.isInstance(r))
                try {
                    l.onDeploy(r, data)
                } catch (Throwable e) {
                    report(e)
                }
        }
    }


    /**
    * Returns all the registered {@link PipelineListener}s.
    */
    static ExtensionList<PipelineListener> all() {
        return ExtensionList.lookup(PipelineListener.class)
    }

    private static void report(Throwable e) {
        LOGGER.log(Level.WARNING, "PipelineListener failed", e)
    }

    private static final Logger LOGGER = Logger.getLogger(PipelineListener.class.getName())
}
