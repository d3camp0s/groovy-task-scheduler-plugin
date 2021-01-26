package com.tsoft.plugins.scheduler.models

class Yaml implements Serializable {

    private static final long serialVersionUID = 1L
    private String group
    private String name
    private Map<String, Object> trigger
    private String script
    private String template
    private Map<String, Object> options
    private String description
    private Map<String, Object> variables
    private List<String> credentials

    Yaml() {
        this.name = ""
        this.group = "*"
        this.trigger = new HashMap<>()
        this.script = ""
        this.template = ""
        this.description = "no description found"
        this.variables = new HashMap<>()
        this.options = ["autosave": false]
        this.credentials = new ArrayList<>()
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getTemplate() {
        return template
    }

    void setTemplate(String template) {
        this.template = template
    }

    String getGroup() {
        return group
    }

    void setGroup(String group) {
        this.group = group.toUpperCase()
    }

    Map<String, String> getTrigger() {
        return trigger
    }

    void setTrigger(Map<String, String> trigger) {
        this.trigger = trigger
    }

    String getScript() {
        return script
    }

    void setScript(String script) {
        this.script = script
    }

    String getDescription() {
        return description
    }

    void setDescription(String description) {
        this.description = description
    }

    Map<String, Object> getVariables() {
        return variables
    }

    void setVariables(Map<String, Object> variables) {
        this.variables = variables
    }

    Map<String, Object> getOptions() {
        return options
    }

    void setOptions(Map<String, Object> options) {
        this.options = options
    }

    List<String> getCredentials() {
        return credentials
    }

    void setCredentials(List<String> credentials) {
        this.credentials = credentials
    }

    @Override
    String toString() {
        return "Yaml {" +
                "name='" + name + '\'' +
                ", grupo='" + group + '\'' +
                ", trigger='" + trigger + '\'' +
                ", script='" + script + '\'' +
                ", description='" + description + '\'' +
                ", variables='" + variables + '\'' +
                ", options='" + options + '\''
                '}'
    }
}
