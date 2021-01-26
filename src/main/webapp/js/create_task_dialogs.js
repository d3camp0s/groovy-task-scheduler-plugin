YAHOO.namespace("taskManager");
var taskpanels = new Object();
var descriptorPath = "/descriptor/com.tsoft.plugins.scheduler.config.GroovyTaskManager"

YAHOO.taskManager.showLog = function(baseUrl){
    this.url = baseUrl;
}
YAHOO.taskManager.showLog.prototype = {
    get : function(id) {
        var panel = createPanelIfNotExist("log", {
                fixedcenter: true,
                draggable: true,
                close: true,
                width: 800,
                height: 500,
                zindex: 999,
                body: '<img src="/images/spinner.gif" />'
        });

        FormChecker.sendRequest(`${this.url}?id=${encodeURIComponent(id)}`, {
            method : "post",
            onComplete : function(rsp) {
              panel.setHeader('log: '+rsp.responseJSON.scriptname);
              if(rsp.status==200){
                  panel.setBody(`<div class='auto'>
                                <span style='font-weight: bold'>Salida:</span>
                                <br/>
                                <pre>${rsp.responseJSON.fout}</pre>
                                <br/>
                                <span style='font-weight: bold'>Error:</span>
                                <pre>${rsp.responseJSON.ferr}</pre>
                                </div>`);
              }
              else{
                  panel.setBody(`<div class='auto'>
                                <span style='font-weight: bold'>Error: ${rsp.status}</span>
                                <pre>${rsp.responseText}</pre>
                                </div>`);
                  console.log(rsp.responseText);
                  notificationBar.show("ERROR!", notificationBar.ERROR);
              }
              panel.setFooter("");
              panel.show();
              layoutUpdateCallback.call();
            }
        });

    }
}

YAHOO.taskManager.runDelete = function(baseUrl){
    this.url = baseUrl;
}
YAHOO.taskManager.runDelete.prototype = {
    on : function(id,name){
        let opcion = confirm("Eliminar el script: '"+name+"'?");
        if (opcion == true) {
            FormChecker.sendRequest(`${this.url}?id=${encodeURIComponent(id)}`, {
                    method : "post",
                    onComplete : function(rsp) {
                      if(rsp.status==200){
                          notificationBar.show("SUCCESS", notificationBar.OK);
                      }
                      else{
                          console.log(rsp.responseText);
                          notificationBar.show("ERROR", notificationBar.ERROR);
                      }
                      layoutUpdateCallback.call();
                    }
                });
        }
    }
}

YAHOO.taskManager.runTask = function(baseUrl){
	this.url = baseUrl;
}
YAHOO.taskManager.runTask.prototype  = {
	execute : function(id) {
		// Initialize the temporary Panel to display while waiting for external content to load
		let waitPanel = createPanelIfNotExist("wait", {
                fixedcenter: true,
                close: true,
                draggable: false,
                zindex: 4,
                modal: true,
                width: 250,
                height: 90,
                visible: false,
                body: '<img src="/plugin/groovy-task-scheduler-plugin/images/rel_interstitial_loading.gif" />'
        });
        waitPanel.setHeader("Loading, please wait...");
		// Show the Panel
        waitPanel.show();

		FormChecker.sendRequest(`${this.url}?id=${encodeURIComponent(id)}`, {
            method : "post",
            onComplete : function(rsp) {
              if(rsp.status==200){
                  notificationBar.show("SUCCESS", notificationBar.OK);
              }
              else{
                  console.log(rsp.responseText);
                  YAHOO.taskManager.error(id,rsp.responseText);
              }
              waitPanel.hide();
              layoutUpdateCallback.call();
            }
        });
	},
	openTemplate : function(id){
	    FormChecker.sendRequest(`${descriptorPath}/loadTemplate?id=${encodeURIComponent(id)}`, {
            method : "post",
            onComplete : function(rsp) {
              if(rsp.status==200){
                  let panelTemplate
                  if( taskpanels['panelTemplate']!=null ){
                      panelTemplate = taskpanels['panelTemplate'];
                  }
                  else {
                      panelTemplate = new YAHOO.widget.Dialog("panelTemplate", {
                          width: "650px",
                          height: "300px",
                          effect:{
                              effect: YAHOO.widget.ContainerEffect.FADE,
                              duration: 0.15
                          },
                          fixedcenter: true,
                          modal: true,
                          visible: true,
                          draggable: true,
                          closable: true
                      });

                      taskpanels[panelTemplate.id] = panelTemplate;
                  }

                  let myButtons = [
                        { text: "Ejecutar", handler: function() {
                              let params = "";
                              jQuery3("#run_task_fromPanel_form :input").each(function(){
                                  let input = jQuery3(this); // This is the jquery object of the input, do what you will
                                  params += input[0].id +"=";
                                  params += encodeURIComponent(input[0].value) + "&";
                              });

                              params += `id=${id}`;

                              FormChecker.sendRequest(`${descriptorPath}/executeFromPanel?${params}`, {
                                  method : "post",
                                  onComplete : function(rsp) {
                                    if(rsp.status==200){
                                        notificationBar.show("SUCCESS", notificationBar.OK);
                                    }
                                    else {
                                        notificationBar.show(rsp.responseText, notificationBar.ERROR);
                                    }
                                    panelTemplate.hide();
                                    layoutUpdateCallback.call();
                                  }
                              });

                          }, isDefault: true
                        },
                        { text: "Cancelar", handler: function() { panelTemplate.hide(); } }
                  ];

                  panelTemplate.cfg.queueProperty("buttons", myButtons);
                  panelTemplate.setHeader(`Template: ${id}`);
                  panelTemplate.setBody(`<!DOCTYPE html>
                                          <html lang="en">
                                          <head>
                                              <meta charset="UTF-8">
                                              <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                              <title>Jenkins Panel</title>
                                              <link rel="stylesheet" href="/bootstrap/css/bootstrap.min.css">
                                          </head>
                                          <body>
                                              <!-- aca va el contenido del panel -->
                                              <form id='run_task_fromPanel_form'>${rsp.responseText}</form>
                                          </body>
                                          </html>`);
                  panelTemplate.render(document.body);
                  panelTemplate.show();
              }
              else{
                  console.log(rsp.responseText);
                  YAHOO.taskManager.error(id,rsp.responseText);
              }
              layoutUpdateCallback.call();
            }
        });
	}
}

YAHOO.taskManager.runSave = function(baseUrl){
    this.url = baseUrl;
}
YAHOO.taskManager.runSave.prototype  = {
	save : function(id,code,config) {
	    let waitPanel = createPanelIfNotExist("wait", {
                fixedcenter: true,
                close: true,
                draggable: false,
                zindex: 4,
                modal: true,
                width: 250,
                height: 90,
                visible: false,
                body: '<img src="/plugin/groovy-task-scheduler-plugin/images/rel_interstitial_loading.gif" />'
        });
        waitPanel.setHeader("Loading, please wait...");
        waitPanel.show();

		FormChecker.sendRequest(`${this.url}?scriptId=${encodeURIComponent(id)}&scriptCode=${encodeURIComponent(code)}&scriptYmlConfig=${encodeURIComponent(config)}`, {
            method : "post",
            onComplete : function(rsp) {
              if(rsp.status==200){
                  notificationBar.show("SUCCESS", notificationBar.OK);
              }
              else {
                  console.log(rsp.responseText);
                  YAHOO.taskManager.error(id,rsp.responseText);
              }
              waitPanel.hide();
              layoutUpdateCallback.call();
            }
        });
	}
}

YAHOO.taskManager.error = function(id,message){
    let error = createPanelIfNotExist(id, {
        fixedcenter:true,
        draggable:false,
        zindex:4,
        modal:true,
        visible:false,
        close: true,
        width: 300,
        height: 400,
        body: ''
    });

    // Show the Panel
    error.setHeader("Error:");
    error.setBody('<pre>'+message+'</pre>');
    error.show();
}

/* Se ejecuta al inicio */
YAHOO.util.Event.onDOMReady(function(){

    // Usando YUI 2.9
    YAHOO.taskManager.task = new YAHOO.taskManager.runTask(descriptorPath+'/execute');
    YAHOO.taskManager.log = new YAHOO.taskManager.showLog(descriptorPath+'/getLog');
    YAHOO.taskManager.delete = new YAHOO.taskManager.runDelete(descriptorPath+'/delete');
    YAHOO.taskManager.script = new YAHOO.taskManager.runSave(descriptorPath+'/saveScript');

    var addbtn = new YAHOO.widget.Button("add_script", { label:"Add" });
    addbtn.on("click", function(){
        var addNewPanel
        if( taskpanels['addNew']!=null ){
            addNewPanel = taskpanels['addNew'];
        }
        else {
            addNewPanel = new YAHOO.widget.Dialog("addNew", {
                width: "400px",
                effect:{
                    effect: YAHOO.widget.ContainerEffect.FADE,
                    duration: 0.25
                },
                fixedcenter: true,
                modal: true,
                visible: false,
                draggable: false
            });
            let dialogHtml = `<table id="tb_add_form" style="width: 100%;">
                                  <tr>
                                      <td style="width: 20%;"><span>Name: </span></td>
                                      <td style="width: 80%;"><input id="fscript" style="width: 100%;" type="text" name="script" /></td>
                                  </tr>
                               </table>`

            let myButtons = [
              { text: "Create", handler: function() {
                        let script = document.getElementById("fscript").value

                        FormChecker.sendRequest(`${descriptorPath}/create?folder=${encodeURIComponent(script)}&yamlfile=${encodeURIComponent(script)}&scriptfile=${encodeURIComponent(script)}`, {
                            method : "post",
                            onComplete : function(rsp) {
                              if(rsp.status==200){
                                  notificationBar.show("SUCCESS", notificationBar.OK);
                                  addNewPanel.hide();
                              }
                              else{
                                  console.log(rsp.responseText);
                                  notificationBar.show(rsp.responseText, notificationBar.ERROR);
                                  addNewPanel.hide();
                              }
                              layoutUpdateCallback.call();
                            }
                        });
                    }, isDefault: true
              },
              { text: "Cancel", handler: function() { addNewPanel.hide(); } }
            ];
            addNewPanel.cfg.queueProperty("buttons", myButtons);
            addNewPanel.setHeader("Please enter your script details here:");
            addNewPanel.setBody(dialogHtml);
            addNewPanel.render(document.body);
            taskpanels[addNewPanel.id] = addNewPanel;
        }

        addNewPanel.show();
    });

    var openbtn = new YAHOO.widget.Button("open_template", { label:"Open" });
    openbtn.on("click", function(){
        let id = YAHOO.util.Dom.get('open_template_openbtn').value;
        if(id!=''){ YAHOO.taskManager.task.openTemplate(id); }
        else { console.log('No se define propiedad value para open_template btn'); }
    });

    var runbtn = new YAHOO.widget.Button("run_script", { label:"Run" });
    runbtn.on("click", function(){
        let id = YAHOO.util.Dom.get('run_script_runbtn').value;
        if(id!=''){ YAHOO.taskManager.task.execute(id); }
    });


    var logbtn = new YAHOO.widget.Button("log_script", { label:"Log" });
    logbtn.on("click", function(){
        let id = YAHOO.util.Dom.get('log_script_logbtn').value;
        if(id!=''){ YAHOO.taskManager.log.get(id); }
    });

    var savebtn = new YAHOO.widget.Button("save_script", { label:"Save" });
    savebtn.on("click", function(){
        console.log("Guardando script...");
        let id = jQuery3('#scriptId')[0].value;
        let code = jQuery3('#scriptCodeId')[0].codemirrorObject.getValue();
        let config = jQuery3('#yamlCodeId')[0].codemirrorObject.getValue();
        if( id!='' ){ YAHOO.taskManager.script.save(id,code,config); }
    });
});

/* Peneles y contenedores */
function createPanelIfNotExist(panelName, panelOptions){
    let panel
    if(taskpanels[panelName]!=null){
        panel = taskpanels[panelName];
    }
    else {
        panel = new YAHOO.widget.Panel(panelName, {
            fixedcenter: panelOptions.fixedcenter? panelOptions.fixedcenter : false,
            draggable: panelOptions.draggable? panelOptions.draggable : false,
            close: panelOptions.close? panelOptions.close : false,
            modal: panelOptions.modal? panelOptions.modal : false,
            visible: panelOptions.visible? panelOptions.visible : false,
            width: `${panelOptions.width}px`,
            height: `${panelOptions.height}px`,
            zindex: panelOptions.zindex? panelOptions.zindex : 4,
            options: panelOptions
        });

        panel.setHeader(panelName);
        panel.setBody(panelOptions.body);
        panel.render('page-body');
        taskpanels[panel.id] = panel;

        let resize = new YAHOO.util.Resize(panelName, {
             handles: ['br'],
             autoRatio: false,
             minWidth: 300,
             minHeight: 200,
             status: false
       });

       resize.on('resize', function(args) {
          this.cfg.setProperty("width", args.width + "px");
          this.cfg.setProperty("height", args.height + "px");
       }, panel, true);

    }

   return panel
}
