<@markup id="js">
<@script type="text/javascript" src="${page.url.context}/res/howkyReporting/js/processDashlet.js"/>
</@>
<#-- Stylesheet Dependencies-->
<@markup id="css">
	<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/form/form.css"/>
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/howkyReporting/css/reporting.css"/>
</@>
<#-- Widget creation -->
<@markup id="widgets">
		<@createWidgets group="dashlets"/>
</@>
<@markup id="html">
		<@uniqueIdDiv>
				<#assign el = args.htmlid?html>
				<#assign dashboardconfig=config.scoped['Dashboard']['dashboard']>
              <div class="dashlet">
                <script type="text/javascript">//<![CDATA[  
                    new HOWKY.dashlet.Reporting("${el}").setMessages(${messages}).setOptions({
                        workflow_definition_list_name: "${workflow_definition_list_name}" ,
                        workflow_definition_list_title: "${workflow_definition_list_title}" ,
                        isAuthenticated: "${isAuthenticated?c}"});           
                    new Alfresco.widget.DashletResizer("${el}");
                    new Alfresco.widget.DashletTitleBarActions("${el}").setOptions({
                        "actions": [{
                            "bubbleOnClick": {
                            "message": "${msg('dashlet.help')}"}, 
                            "cssClass": "help", 
                            "tooltip": "${msg('dashlet.help.tooltip')}"
                        }]
                    });
                //]]></script>
                    <div class="title">${msg("dashlet.name")} </div>
                    <div id="${el}-body" class="body howkyDashletbody">
                        <div id="${el}-global-container" class="howkyContainer">
						  <form id="procesInstancesForm" name="processForm" action="/share/proxy/alfresco/api/workflow-instances" method="get">
						  
						  <div class="form-field">
<script type="text/javascript">
    let howkyDefinitionNameBaseURL = Alfresco.constants.PROXY_URI + "api/workflow-definitions";
    let howkyDefinitionNameIfHidden = true;
	let howkyDefinitionNameHttp = new XMLHttpRequest();
    howkyDefinitionNameHttp.open("GET", howkyDefinitionNameBaseURL);
    howkyDefinitionNameHttp.send();
    howkyDefinitionNameHttp.onreadystatechange = (e) => {
        if (howkyDefinitionNameHttp.readyState === 4 && howkyDefinitionNameHttp.status === 200) {
            const json = JSON.parse(howkyDefinitionNameHttp.responseText);
            const arr = json["data"];
            const myselect = document.getElementById("${el}-howkyDefinitionName");
            const excludeNames = ["activiti$activitiAdhoc", "activiti$activitiInvitationModerated", "activiti$activitiInvitationNominated", "activiti$activitiInvitationNominatedAddDirect", "activiti$activitiParallelGroupReview"
            , "activiti$activitiParallelReview", "activiti$activitiReview", "activiti$activitiReviewPooled", "activiti$resetPassword"]
            for (let i = 0; i < arr.length; i++) {
            	if (excludeNames.includes(arr[i]["name"])) {
            	continue;
            	}
                const option = document.createElement("OPTION");
                option.value = arr[i]["name"];
				option.text = arr[i]["title"];   
                myselect.appendChild(option);
            }
       }
    }			
</script>	    
<label for="${el}-howkyDefinitionName">${msg('howkyProcessDashlet.processName')}</label><br>
<select id="${el}-howkyDefinitionName" name="definitionName" tabindex="0"  placeholder="${'howkyProcessDashlet.definitioNamePlaceholder'}" value="" style="width: 227px">
  <option value="" selected="selected">${msg('howkyProcessDashlet.definitioNameAll')}</option>
</select>
						    
						    
						  </div>
						  <div id="${el}-start-date" class="form-field">
						      <label for="${el}-startDate" class="dateLabel">${msg('howkyProcessDashlet.startedBefore')}</label>
						    <div class="relativeOver">  
						      <input id="${el}-startDateHidden" type="hidden" value="" name="startedBefore" />	
						      <input id="${el}-startDate"  type="date" class="howkyDate-entry" tabindex="0" style="width: 227px" onchange="dateInputUpdate('${el}-startDate')"/>
						      <img src="${url.context}/res/components/form/images/calendar.png" style="vertical-align: text-top;" class="datepicker-icon" tabindex="-1" />
						    </div>
						  </div>
						  <div id="${el}-end-date" class="form-field">
						    <label for="${el}-stopDate" class="dateLabel">${msg('howkyProcessDashlet.startedAfter')}</label>
						    <div class="relativeOver">
						      <input id="${el}-stopDateHidden" type="hidden" name="startedAfter" value="" />
						      <input id="${el}-stopDate"  type="date" class="howkyDate-entry" tabindex="0" style="width: 227px" onchange="dateInputUpdate('${el}-stopDate')"/>
						      <img src="${url.context}/res/components/form/images/calendar.png" style="vertical-align: text-top;" class="datepicker-icon" tabindex="-1" />
						    </div>
						  </div>
<div class="form-field">
<script type="text/javascript">
    var howkyInitiatorBaseURL = Alfresco.constants.PROXY_URI + "api/forms/picker/authority/children?selectableType=cm:person&searchTerm=";
    let howkyInitiatorIfHidden = true;

    function showDropdown() {
        document.getElementById("${el}-howkyInitiatorDropdownForm").classList.toggle("show");

        if (howkyInitiatorIfHidden) {
            document.getElementById("howkyInitiatorinput").focus();
            howkyInitiatorIfHidden = false;
        } else {
            howkyInitiatorIfHidden = true;
        }
    }
    
    function howkyInitiatorSearch() {

        const query = document.getElementById("howkyInitiatorinput").value;
        if(query.length < 3) {return;}

        const myselect = document.getElementById("${el}-howkyInitiatorDropdownContent");
        while (myselect.children.length > 1) {
            myselect.removeChild(myselect.lastChild);
        }

        const mytestHttp = new XMLHttpRequest();
        const url = howkyInitiatorBaseURL + query + "&size=1000";
        mytestHttp.open("GET", url);
        mytestHttp.send();
        mytestHttp.onreadystatechange = (e) => {
            if (mytestHttp.readyState === 4 && mytestHttp.status === 200) {
                const json = JSON.parse(mytestHttp.responseText);
                const arr = json["data"]["items"];
                const patt = /\([^)]+\)/g;
                for (let i = 0; i < arr.length; i++) {
                    const option = document.createElement("OPTION");
                    option.value = arr[i]["name"];
					option.text = arr[i]["name"];   
                    option.onclick = function () {
                        const howkyInitiator = document.getElementById("${el}-howkyInitiator");
                        let res = this.value.match(patt);
                        howkyInitiator.value = res[0].substring(1, res[0].length-1);
                        howkyInitiator.text = this.value;
                        
                        showDropdown();
                    };
                    myselect.appendChild(option);
                }
            }
        }
    }

function makeInitiatorEmpty() {
	let initiatorInput = document.getElementById("${el}-howkyInitiator");
    initiatorInput.value = "";            
    initiatorInput.text = "${msg('howkyProcessDashlet.InitiatorAll')}";       
    showDropdown();
}
</script>
<label for="${el}-howkyInitiator">${msg('howkyProcessDashlet.initiator')}</label><br>
<input id="${el}-howkyInitiator" name="initiator" tabindex="0" class="dropbtn myselectclassid" readonly type="text" placeholder="${msg('howkyProcessDashlet.initiatorPlaceholder')}" value="" style="width: 227px"/>
<div id="${el}-howkyInitiatorDropdownForm" class="dropdown-frame">
	<input type="text" placeholder="Szukaj.." id="howkyInitiatorinput" onkeyup="howkyInitiatorSearch()">
    <div id="${el}-howkyInitiatorDropdownContent" class="dropdown-content">
      	<option value="" onclick="makeInitiatorEmpty()" style="color:grey;">-- ${msg('howkyProcessDashlet.InitiatorAll')} --</option>
	</div>
</div>
</div>
						  <div class="form-field">
						    <input type="checkbox" id="${el}-status" name="state" value="COMPLETED">
						    <label for="${el}-status">${msg('howkyProcessDashlet.state')}</label><br>
						  </div>
						<input type="hidden" name="exclude" value="jbpm$wcmwf:*,jbpm$wf:articleapproval,activiti$publishWebContent,jbpm$publishWebContent,jbpm$inwf:invitation-nominated,jbpm$imwf:invitation-moderated,activiti$activitiInvitationModerated,activiti$activitiInvitationNominated,activiti$activitiInvitationNominatedAddDirect,activiti$resetPassword">
						<div class="yui-button yui-submit-button alf-primary-button">
						  <button type="submit" style="margin-top: 0.5em;">${msg('howkyProcessDashlet.submit')}</button>
						</div>
						</form>
						</div>
									
						<hr id="${el}-hr" class="hrDivider hidden" />
						<div id="reportingTabs" class="hidden">		
						 <div class="navTabs" style="display:none;">			 
						  <button style="font-size: 12px" onclick="switchTab(event, 'tabs-table')" class="howkyTablinks" id="defaultOpenTab">${msg("tab.table")}</button>
						  <button style="font-size: 12px" onclick="switchTab(event, 'tabs-chart')" class="howkyTablinks">${msg("tab.chart")}</button>
						  <a class="downloadButton" style="display:none;"><img style="height: 16px;" alt="Zapisz jako Excel" src="${page.url.context}/res/howkyReporting/images/download.png"></a>
						 </div>
						 
						 <div id="tabs-table" class="tabsClass" style="margin-top: 1em;">
						  <table id="tabTable" class="relativeOver"></table>
						</div>
						<div id="tabs-chart" class="tabsClass">
						  <canvas id="howkyReportingChart" class="relativeOver"></canvas>
						</div>
						</div>
						<script type="text/javascript">
						
						 function switchTab(evt, tabName) {
						   var i, tabcontent, tablinks;
						   tabcontent = document.getElementsByClassName("tabsClass");
						   for (i = 0; i < tabcontent.length; i++) {
						     tabcontent[i].style.display = "none";
						   }
						   tablinks = document.getElementsByClassName("howkyTablinks");
						   for (i = 0; i < tablinks.length; i++) {
						     tablinks[i].classList.remove("active");
						   }
						   document.getElementById(tabName).style.display = "block";
						   evt.currentTarget.classList.add("active");
						 }
						 document.getElementById("defaultOpenTab").click();
						
						 document.forms['procesInstancesForm'].addEventListener('submit', (event) => {
						   event.preventDefault();
						   
						   if(!("${isAuthenticated?c}" === "true")) {
						   	alert("Nie masz uprawnień!");
						   	return;
						   }
						   
							document.getElementById("reportingTabs").classList.remove("hidden");
							document.getElementById("tabTable").innerHTML = '';
						
						   let formData = new FormData(event.target);
						   if (!formData.get("initiator") ||  0 === formData.get("initiator")) {formData.delete("initiator");}
						   if (!formData.get("definitionName") || 0 === formData.get("definitionName")) {formData.delete("definitionName");}
						   if (!formData.get("startedAfter") || 0 === formData.get("startedAfter")) {formData.delete("startedAfter");}
						   if (!formData.get("startedBefore") || 0 === formData.get("startedBefore")) {formData.delete("startedBefore");}
						
						   const dataAsString = new URLSearchParams(formData).toString();
						
						   fetch(Alfresco.constants.PROXY_URI + 'api/workflow-instances?' + dataAsString).then((resp) => {
											        return resp.json();
						               }).then((body) => {
										 let table = document.getElementById("tabTable");
						                 let headerData = ["${msg('table.header.name')}", "${msg('table.header.desc')}", "${msg('table.header.date')}",
						                 	 "${msg('table.header.daysProcess')}", "${msg('table.header.initiator')}", "${msg('table.header.currentTaskName')}", 
						                 	 "${msg('table.header.currentTaskPerson')}", "${msg('table.header.currentTaskDays')}"];	//, "${msg('table.header.progress')}"
						                 generateTableHead(table, headerData);
						                 generateTable(table, body["data"]);
						               }).catch((error) => {
											console.error("cannot fetch howky process data");
						               });
						             });
						
						 function generateTableHead(table, data) {
						  let thead = table.createTHead();
						  let row = thead.insertRow();
						  for (let key of data) {
						    let th = document.createElement("th");
						    let text = document.createTextNode(key);
						    th.appendChild(text);
						    row.appendChild(th);
						  }
						}
						
						function generateTable(table, data) {
						  for (let element of data) {
						    let row = table.insertRow();
						    row.classList.add("howkyRowStyle");
						    row.addEventListener("click", function(){
						    		window.open(window.location.origin + "/share/page/workflow-details?workflowId=" + element["id"], "_blank");
									});
						    let cellOrder = ["title", "message", "startDate", "daysCount", "initiator"];
						    for (let i = 0; i < cellOrder.length; i++) {
							  let cell = row.insertCell();
						      let cellValue = element[cellOrder[i]];
						      switch(cellOrder[i]) {
						        case "startDate":
						        cellValue = new Date(element[cellOrder[i]]).toLocaleString();
						        break;
						        case "daysCount":
						        let t1 = new Date(element["startDate"]).getTime();
						        if(!element["endDate"]) {	// not finished
						    	 let t2 = Date.now();
						         cellValue = Math.round((t2-t1)/(1000*60*60*24));
						         if( cellValue < ${msg('table.currentProcessDaysLimit')}) {cell.style.color = "green";}
						         else {cell.style.color = "red";}
						        }
						        else {	// finished
									let endDate = new Date(element["endDate"]).getTime();
									cellValue = Math.ceil((endDate-t1)/(1000*60*60*24));
						        }
						        break;
						        case "initiator":
						        cellValue = element[cellOrder[i]]["firstName"] + " " + element[cellOrder[i]]["lastName"];
						        break;
						      }
						      let text = document.createTextNode(cellValue);
						      cell.appendChild(text);
						      
						      
						      
						    }
						    
						    // fetch more details
						   fetch(Alfresco.constants.PROXY_URI + 'api/workflow-instances/' + element["id"] + '?includeTasks=true').then((resp) => {
						   	return resp.json();
						   }).then((body) => {

						   	let currentTaskName;
						   	let currentTaskPerson;
						   	let currentTaskDays;
						   	var uniqueTaskSet = new Set();
						   	for (let j = 0; j < body["data"]["tasks"].length; j++) {
						   		uniqueTaskSet.add(body["data"]["tasks"][j]["name"]);
						   	
						   		let task = body["data"]["tasks"][j];
						   		if(task["state"] === "IN_PROGRESS") {
						   			currentTaskName = task["title"];
						   			if(task["owner"]) { currentTaskPerson = task["owner"]["firstName"] + " " + task["owner"]["lastName"];}
						   			else {currentTaskPerson = "-";}
						   			let startTaskDate = new Date(task["properties"]["bpm_startDate"]).getTime();
						   			let nowDate = Date.now();
						   			currentTaskDays = Math.round((nowDate-startTaskDate)/(1000*60*60*24));
						   			
						   		}
						   	}
							let currentTaskNameCell = row.insertCell();
						   	if(currentTaskName) {currentTaskNameCell.appendChild(document.createTextNode(currentTaskName));}
						   	else {currentTaskNameCell.appendChild(document.createTextNode("-"));}
						   	

						   	let currentTaskPersonCell = row.insertCell();
						   	if(currentTaskPerson) {currentTaskPersonCell.appendChild(document.createTextNode(currentTaskPerson));}
						   	else {currentTaskPersonCell.appendChild(document.createTextNode("-"));}

						   	let currentTaskDaysCell = row.insertCell();
						   	if (currentTaskDays) {
						   		currentTaskDaysCell.appendChild(document.createTextNode(currentTaskDays));
						   		if( currentTaskDays < ${msg('table.currentTaskDaysLimit')}) {currentTaskDaysCell.style.color = "green";}
						   		else {currentTaskDaysCell.style.color = "red";}
						   	}
						   	else {currentTaskDaysCell.appendChild(document.createTextNode("-"));}

						   	//let createdTasksCount = uniqueTaskSet.size;	percent columnt is deleted for now
						   	//let tasksCount = body["data"]["definition"]["taskDefinitions"].length; 
						   	//let progress = Math.round((createdTasksCount/tasksCount)*100);
						   	//let progressCell = row.insertCell();
						   	//progressCell.appendChild(document.createTextNode(progress+" %"));
						   }).catch((error) => {
						   	console.error("cannot fetch howky task data");
						   });
						    
						  }
						}
						
						function dateInputUpdate(id) {
							hidden = document.getElementById(id + "Hidden");
							hidden.value = new Date(document.getElementById(id).value).toJSON();
						}
						
window.addEventListener('load', function () {
//document.getElementsByClassName("sticky-footer")[0].style.marginTop="auto";
    document.getElementById("${el}-howkyInitiator").addEventListener("click", showDropdown);
    if(document.getElementsByClassName("sticky-wrapper")[0].children.length == 4) {
      document.getElementById("Share").appendChild(document.getElementsByClassName("sticky-wrapper")[0].children[1]);
      document.getElementById("Share").appendChild(document.getElementsByClassName("sticky-wrapper")[0].children[1]);
    }
})
						</script>
                    
                    
              </div>
		</@>
</@>