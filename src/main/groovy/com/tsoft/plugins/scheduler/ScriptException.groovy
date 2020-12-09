package com.tsoft.plugins.scheduler

class ScriptException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L

    ScriptException(String message) {
        super(message)
    }

    ScriptException(String message, Exception ex) {
        super(message, ex)
    }

}
