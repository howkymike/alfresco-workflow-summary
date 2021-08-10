<@markup id="js">
<@script type="text/javascript" src="${page.url.context}/res/howkyReporting/js/processDashlet.js"/>
</@>
<!-- Simple Dialog -->
<@script type="text/javascript" src="${page.url.context}/res/modules/simple-dialog.js"></@script>
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
                        isAuthenticated: "${isAuthenticated?c}",
                        userSites: "${userSites}"});           
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
						  
						  
						  <div id="${el}-howkySiteNameDiv" class="form-field" hidden>
							<label for="${el}-howkySiteName">${msg('howkyProcessDashlet.siteName')}</label><br>
							<select id="${el}-howkySiteName" name="siteName" tabindex="0" onchange="${el?replace("-", "_")}_howkysiteNameSelectChange()" placeholder="${'howkyProcessDashlet.howkySiteNamePlaceholder'}" value="" style="width: 227px">	  
							</select>
						  </div>
						  
						  <div class="form-field">
							<label for="${el}-howkyDefinitionName">${msg('howkyProcessDashlet.processName')}</label><br>
							<select id="${el}-howkyDefinitionName" name="definitionName" tabindex="0"  placeholder="${'howkyProcessDashlet.definitioNamePlaceholder'}" value="" style="width: 227px">
						  		<option value="" selected="selected">${msg('howkyProcessDashlet.definitioNameAll')}</option>
							</select>
						  </div>
						  
						  <div id="${el}-end-date" class="form-field">
						    <label for="${el}-stopDate" class="dateLabel">${msg('howkyProcessDashlet.startedAfter')}</label>
						    <div class="relativeOver">
						      <input id="${el}-stopDateHidden" type="hidden" name="startedAfter" value="" />
						      <input id="${el}-stopDate"  type="date" class="howkyDate-entry" tabindex="0" style="width: 227px" onchange="dateInputUpdate('${el}-stopDate')"/>
						      <img src="${url.context}/res/components/form/images/calendar.png" style="vertical-align: text-top;" class="datepicker-icon" tabindex="-1" />
						    </div>
						  </div>
						  <div id="${el}-start-date" class="form-field">
						      <label for="${el}-startDate" class="dateLabel">${msg('howkyProcessDashlet.startedBefore')}</label>
						    <div class="relativeOver">  
						      <input id="${el}-startDateHidden" type="hidden" value="" name="startedBefore" />	
						      <input id="${el}-startDate"  type="date" class="howkyDate-entry" tabindex="0" style="width: 227px" onchange="dateInputUpdate('${el}-startDate')"/>
						      <img src="${url.context}/res/components/form/images/calendar.png" style="vertical-align: text-top;" class="datepicker-icon" tabindex="-1" />
						    </div>
						  </div>
<div class="form-field">
<script type="text/javascript">
Alfresco.constants.USERSITESHORTNAME = "${userSites}".split(",");
Alfresco.constants.USERSITESPREFIX = "${userSites}".toLowerCase().replaceAll(/[^a-z]/g, "").split(",");

// indexes in workflowDefinitionSitePrefix, workflowDefinitionListName, workflowDefinitionListTitle are corresponding to one, the same object
const PDworkflowDefinitionListName = ("${workflow_definition_list_name}").split(",");
const PDworkflowDefinitionListTitle = ("${workflow_definition_list_title}").split(",");
const PDworkflowDefinitionSitePrefix = ("${workflow_definition_list_sitePrefix}").split(",");
const PDsitePrefixSet = new Set(PDworkflowDefinitionSitePrefix.filter(function (el) {return el != null && el !== "";}));
if(PDsitePrefixSet.size > 0) {
    document.getElementById("${el}-howkySiteNameDiv").removeAttribute("hidden");
    const mySiteSelect = document.getElementById("${el}-howkySiteName");
    for(let prefix of PDsitePrefixSet) {
        const option = document.createElement("OPTION");
        option.value = prefix;
        option.text = prefix;
        mySiteSelect.appendChild(option);
    }
}


function ${el?replace("-", "_")}_howkysiteNameSelectChange() {
    let sitePrefix = document.getElementById("${el}-howkySiteName").value;
    const definitionNameSelect = document.getElementById("${el}-howkyDefinitionName");
    for (let i = definitionNameSelect.options.length-1; i > 0; i--) {  // remove all except "Wszystko"
        definitionNameSelect.options[i] = null;
    }

    for(let i = 0; i < PDworkflowDefinitionSitePrefix.length; i++) { // add options
        if(sitePrefix === "" || PDworkflowDefinitionSitePrefix[i] === sitePrefix) {
            let newOption = new Option(PDworkflowDefinitionListTitle[i], PDworkflowDefinitionListName[i]);
            definitionNameSelect.add(newOption, undefined);
        }
    }

}
${el?replace("-", "_")}_howkysiteNameSelectChange();


var howkyInitiatorBaseURL = Alfresco.constants.PROXY_URI + "api/people?startIndex=0&pageSize=20&filter=" ; //"api/forms/picker/authority/children?selectableType=cm:person&searchTerm=";
let howkyInitiatorIfHidden = true;

function showDropdown(dropdownId) {
	let dropdownElement = null;
	if(typeof dropdownId === 'object')
		dropdownElement = dropdownId;
	else
		dropdownElement = document.getElementById(dropdownId);
    dropdownElement.classList.toggle("show");

    if (dropdownElement.classList.contains("show")) {
    	dropdownElement.getElementsByTagName('INPUT')[0].focus();
    } 
}

function howkyInitiatorSearch() {

    const query = document.getElementById("howkyInitiatorinput").value;
    if(query.length < 3) {return;}

    const myselect = document.getElementById("${el}-howkyInitiatorDropdownContent");

    const mytestHttp = new XMLHttpRequest();
    const url = howkyInitiatorBaseURL + query;
    mytestHttp.open("GET", url);
    mytestHttp.send();
    mytestHttp.onreadystatechange = (e) => {
        if (mytestHttp.readyState === 4 && mytestHttp.status === 200) {
            const json = JSON.parse(mytestHttp.responseText);
            const arr = json["people"];
            while (myselect.children.length > 1) {
		        myselect.removeChild(myselect.lastChild);
		    }
            for (let i = 0; i < arr.length; i++) {
                const option = document.createElement("OPTION");
                option.value = arr[i]["userName"];
                option.text = arr[i]["firstName"] + " " + arr[i]["lastName"] + " (" + arr[i]["userName"] + ")";
                option.onclick = function () {
                    const howkyInitiator = document.getElementById("${el}-howkyInitiator");
                    howkyInitiator.value = this.value;
                    howkyInitiator.text = this.text;

                    showDropdown(myselect.parentNode);
                };
                myselect.appendChild(option);
            }
        }
    }
}


function makeDropdownEmpty(elementId) {
    const dropdownInput = document.getElementById(elementId);
    dropdownInput.value = "";
    dropdownInput.text = "${msg('howkyProcessDashlet.InitiatorAll')}";
    const dropdownElement = dropdownInput.parentNode.getElementsByClassName('dropdown-frame')[0];
    showDropdown(dropdownElement);
}
</script>


<label for="${el}-howkyInitiator">${msg('howkyProcessDashlet.initiator')}</label><br>
<input id="${el}-howkyInitiator" name="initiator" tabindex="0" class="dropbtn myselectclassid" readonly type="text" placeholder="${msg('howkyProcessDashlet.initiatorPlaceholder')}" value="" style="width: 227px" onclick='showDropdown("${el}-howkyInitiatorDropdownForm")'/>
<div id="${el}-howkyInitiatorDropdownForm" class="dropdown-frame">
	<input type="text" placeholder="Szukaj.." id="howkyInitiatorinput" onkeyup="howkyInitiatorSearch()">
    <div id="${el}-howkyInitiatorDropdownContent" class="dropdown-content">
      	<option value="" onclick="makeInitiatorEmpty()" style="color:grey;">-- ${msg('howkyProcessDashlet.InitiatorAll')} --</option>
	</div>
</div>
</div>
						  <div class="form-field">
						  	<label for="${el}-currTaskAuthority">${msg('howkyProcessDashlet.currTaskAuthority')}</label><br>
						    <input type="input" id="${el}-currTaskAuthority" name="currTaskAuthority" value="">
						  </div>
					      <#if msg('howkyProcessDashlet.additionalFieldPropTitle')?has_content>
						  <div class="form-field">
						  	<label for="${el}-additionalField">${msg('howkyProcessDashlet.additionalFieldPropTitle')}</label><br>
						    <input type="input" id="${el}-additionalField" name="additionalField" value="">
						  </div>
						  </#if>
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
						<div hidden id="howkyspinner"></div>
						<div id="reportingTabs" hidden>		
						 <div class="navTabs">			 
						  <button style="font-size: 12px" onclick="switchTab(event, 'tabs-table')" class="howkyTablinks" id="defaultOpenTab">${msg("tab.table")}</button>
						  <a class="downloadButton"><img style="height: 16px;" alt="Zapisz jako PDF" src="${page.url.context}/res/howkyReporting/images/download.png" onclick="createPDF()"></a>
						 </div>
						 <div id="tabs-table" class="tabsClass" style="margin-top: 1em;">
						  <table id="tabTable" class="relativeOver"></table>
						</div>
						<div id="tabs-chartLine" class="tabsClass">
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
        alert("${msg('howkyProcessDashlet.noPermission')}");
        return;
    }
	document.getElementById("howkyspinner").removeAttribute('hidden');
	document.getElementById("reportingTabs").setAttribute('hidden', '');
    document.getElementById("tabTable").innerHTML = '';

    let formData = new FormData(event.target);
    if (!formData.get("siteName")) {formData.delete("siteName");}
    if (!formData.get("initiator")) {formData.delete("initiator");}
    if (!formData.get("definitionName")) {formData.delete("definitionName");}
    if (!formData.get("startedAfter")) {formData.delete("startedAfter");}
    if (!formData.get("startedBefore")) {formData.delete("startedBefore");}
    const completed = (formData.get("state") === null) ? false : true;
    formData.set("state", (formData.get("state") === null) ? "ACTIVE" : "COMPLETED");

    const dataAsString = new URLSearchParams(formData).toString();
    fetch(Alfresco.constants.PROXY_URI + 'howkymike/processdashlet/get?' + dataAsString).then((resp) => {
        if(!resp.ok)
            throw new Error("Error retrieving json, status code: " + resp.status);
        return resp.json();
    }).then((body) => {
        let table = document.getElementById("tabTable");
        table.innerHTML = "";
        let headerData = ["${msg('table.header.name')}", "${msg('table.header.desc')}", "${msg('table.header.date')}",
            "${msg('table.header.daysProcess')}", "${msg('table.header.initiator')}"];	//, "${msg('table.header.progress')}"
        if(!completed)
            headerData.push("${msg('table.header.currentTaskName')}", "${msg('table.header.currentTaskPerson')}"
            , "${msg('table.header.currentTaskDays')}", "${msg('table.header.actions')}");
        generateTableHead(table, headerData);
        generateTable(table, body["data"], completed);
        document.getElementById("howkyspinner").setAttribute('hidden', '');
        document.getElementById("reportingTabs").removeAttribute('hidden');
    }).catch((error) => {
        console.error("cannot fetch howky process data");
        document.getElementById("howkyspinner").setAttribute('hidden', '');
    });
});

// Sorting logic
const howkysorting_getCellValue = (tr, idx) => tr.children[idx].innerText || tr.children[idx].textContent;
const howkysorting_comparer = (idx, asc) => (a, b) => ((v1, v2) => 
    v1 !== '' && v2 !== '' && !isNaN(v1) && !isNaN(v2) ? v1 - v2 : v1.toString().localeCompare(v2)
    )(howkysorting_getCellValue(asc ? a : b, idx), howkysorting_getCellValue(asc ? b : a, idx));


function generateTableHead(table, data) {
    let thead = table.createTHead();
    let row = thead.insertRow();
    for (let key of data) {
        let th = document.createElement("th");
		th.addEventListener('click', (() => {	// sorting logic
		    const table = th.closest('table');
		    Array.from(table.querySelectorAll('tr:nth-child(n+2)'))
		        .sort(howkysorting_comparer(Array.from(th.parentNode.children).indexOf(th), this.asc = !this.asc))
		        .forEach(tr => table.appendChild(tr) );
		    let headerText = th.textContent.substring(0, th.textContent.length - 1);
		    if(this.asc)
		    	headerText += '▲';
		    else
		    	headerText += '▼';
		    th.textContent = headerText;
		}));
		th.style.cursor = 'pointer';
        let text = document.createTextNode(key + " ▲");
        th.appendChild(text);
        row.appendChild(th);
    }
}



function generateTable(table, data, completed) {
    for (let element of data) {
        let row = table.insertRow();
        row.classList.add("howkyRowStyle");
        row.addEventListener("click", function(){
            window.open(window.location.origin + "/share/page/workflow-details?workflowId=" + element["instanceID"], "_blank");
        });

        let cellOrder = ["definitionName", "instanceTitle", "startDate", "daysCount", "initiator"];
        for (let i = 0; i < cellOrder.length; i++) {
            let cell = row.insertCell();
            let cellValue = "";
            switch(cellOrder[i]) {
                case "definitionName":
                case "instanceTitle":
                    cellValue = element[cellOrder[i]];
                    break;
                case "startDate":
                    cellValue = new Date(element["startDate"]).toISOString().split("T")[0];
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
	            if(element["initiator"])
		        cellValue = element["initiator"]["firstName"] + " " + element["initiator"]["lastName"];
                    break;
            }
            let text = document.createTextNode(cellValue);
            cell.appendChild(text);
        }

        if(!completed) { // taskDetailed
            let cellTaskOrder = ["currTaskTitle", "currTaskAuthority", "currTaskDays"];
            for (let i = 0; i < cellTaskOrder.length; i++) {
                let cell = row.insertCell();
                let cellValue = "";
                switch(cellTaskOrder[i]) {
                    case "currTaskTitle":
                        if(element["currentTask"])
                            cellValue = element["currentTask"]["taskTitle"];
                        break;
                    case "currTaskAuthority":
                        if(element["currentTask"])
                            cellValue = element["currentTask"]["taskAuthority"];
                        break;
                    case "currTaskDays":
                        if(element["currentTask"]) {
                            let startTaskDate = new Date(element["currentTask"]["taskStartDate"]).getTime();
                            let nowDate = Date.now();
                            cellValue = Math.round((nowDate-startTaskDate)/(1000*60*60*24));
                            if( cellValue < ${msg('table.currentProcessDaysLimit')}) {cell.style.color = "green";}
                            else {cell.style.color = "red";}
                        }
                        break;
                }
                let text = document.createTextNode(cellValue);
                cell.appendChild(text);
            }
            
            // create assignButton
            let assignButton = createReassignButton(element["currentTask"]["taskId"]);
			let cell = row.insertCell();
			cell.appendChild(assignButton);
        }
    }
}

function createReassignButton(taskId) {
	let assignButton = document.createElement("button");
	assignButton.innerHTML = "${msg('howkyProcessDashlet.reassignbutton')}";
	assignButton.dataset.taskId = taskId;
	assignButton.addEventListener ("click", processDashlet_onReassignActionClick);
	assignButton.classList.add("reassignbutton");
	
	let assignButtonSpanFirst = document.createElement('span');
	assignButtonSpanFirst.classList.add("first-child");
	assignButtonSpanFirst.appendChild(assignButton);
	let assignButtonSpan = document.createElement('span');
	assignButtonSpan.classList.add("yui-button", "yui-push-button");
	assignButtonSpan.appendChild(assignButtonSpanFirst);
	return assignButtonSpan;
}

function dateInputUpdate(id) {
    hidden = document.getElementById(id + "Hidden");
    hidden.value = new Date(document.getElementById(id).value).toJSON();
}

function howky_setDefaultDateValues() {
	let configDefaultStartedAfterDays = parseInt(${msg('howkyProcessDashlet.defaultStartedAfterProcessDays')});
	if(!isNaN(configDefaultStartedAfterDays) && configDefaultStartedAfterDays > 0) {
		let stopDateDefaultValue=new Date(new Date().setDate(new Date().getDate() - configDefaultStartedAfterDays));
		const stopDateElement = document.getElementById("${el}-stopDate");
		stopDateElement.valueAsDate=stopDateDefaultValue;
		stopDateElement.onchange();
	}

	let configDefaultStartedBeforeDays = parseInt(${msg('howkyProcessDashlet.defaultStartedBeforeProcessDays')});
	if(!isNaN(configDefaultStartedBeforeDays) && configDefaultStartedBeforeDays > 0) {
		let startDateDefaultValue=new Date(new Date().setDate(new Date().getDate() - configDefaultStartedBeforeDays));
		const startDateElement = document.getElementById("${el}-startDate");
		startDateElement.valueAsDate=startDateDefaultValue;
		startDateElement.onchange();
		
	}
}

window.addEventListener('load', function () {
//document.getElementsByClassName("sticky-footer")[0].style.marginTop="auto";
    if(document.getElementsByClassName("sticky-wrapper")[0].children.length === 4) {    // footer fix
        document.getElementById("Share").appendChild(document.getElementsByClassName("sticky-wrapper")[0].children[1]);
        document.getElementById("Share").appendChild(document.getElementsByClassName("sticky-wrapper")[0].children[1]);
    }
    
    howky_setDefaultDateValues();
})


function createPDF() {
    // CREATE A WINDOW OBJECT.
    var win = window.open('', '', 'height=700,width=700');
    
    var processTable = document.getElementById('tabs-table').innerHTML;

    var style = "<style>";
    style += "table {width: 100%;font: 17px Calibri;border-collapse: collapse;}";
    style += "th {padding-top: 12px;padding-bottom: 12px;background-color: #1e88e5;color: white;}";
    style += "tr, th {border: 1px solid #ddd; padding: 8px; cursor: pointer;text-align: center;}";
    style += "tr:nth-child(even){background-color: #f2f2f2;}";
    style += "</style>";

    win.document.write('<html lang="pl"><head>');
    win.document.write('<title>Raport obiegów</title>');   // PDF HEADER.
    win.document.write(style);          // ADD STYLE
    win.document.write('</head>');
    win.document.write('<body>');
    win.document.write(processTable);
    win.document.write('</body></html>');

    win.document.close();
    win.onafterprint = win.close;
    win.print();
}
						</script>
                    
                    
              </div>
		</@>
</@>
