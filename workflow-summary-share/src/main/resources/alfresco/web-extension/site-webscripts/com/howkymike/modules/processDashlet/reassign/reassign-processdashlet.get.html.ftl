<#assign el=args.htmlid?html>

<script type="text/javascript">
function howkyReassignTaskSearch() {

    const query = document.getElementById("howkyReassignTaskInput").value;
    if(query.length < 3) {return;}

    const myselect = document.getElementById("${el}howkyReassignTaskDropdownContent");

    const mytestHttp = new XMLHttpRequest();
    const url = howkyInitiatorBaseURL + query + "&size=1000";
    mytestHttp.open("GET", url);
    mytestHttp.send();
    mytestHttp.onreadystatechange = (e) => {
        if (mytestHttp.readyState === 4 && mytestHttp.status === 200) {
            const json = JSON.parse(mytestHttp.responseText);
            const arr = json["data"]["items"];
            const patt = /\([^)]+\)/g;
            while (myselect.children.length > 1) {
		        myselect.removeChild(myselect.lastChild);
		    }
            for (let i = 0; i < arr.length; i++) {
                const option = document.createElement("OPTION");
                option.value = arr[i]["name"];
                option.text = arr[i]["name"];
                option.onclick = function () {
                    const howkyInitiator = document.getElementById("${el}assignee");
                    let res = this.value.match(patt);
                    howkyInitiator.value = res[0].substring(1, res[0].length-1);
                    howkyInitiator.text = this.value;

                    showDropdown(myselect.parentNode);
                };
                myselect.appendChild(option);
            }
        }
    }
}
</script>

<div id="${el}-configDialog" class="reassign-processdashlet">
   <div class="hd">${msg("label.dialogTitle")}</div>

   <div class="bd">
      <form id="${el}-form" action="" method="POST">
		 <div class="yui-gd">
			<div class="yui-u first"><label for="${el}assignee">${msg("label.assignee")}:</label></div>
			<div class="yui-u">
				<input id="${el}assignee" name="assignee" tabindex="0" class="dropbtn myselectclassid" readonly type="text" placeholder="${msg('reassignProcessDashlet.select')}"
					 value="" style="width: 227px" onclick='showDropdown("${el}howkyAssigneeDropdownForm")'/>
				<div id="${el}howkyAssigneeDropdownForm" class="dropdown-frame">
					<input type="text" placeholder="Szukaj.." id="howkyReassignTaskInput" onkeyup="howkyReassignTaskSearch()" onkeydown="return (event.keyCode!=13);">
				    <div id="${el}howkyReassignTaskDropdownContent" class="dropdown-content">
				      	<option value="" onclick="makeDropdownEmpty('${el}assignee')" style="color:grey;">-- ${msg('reassignProcessDashlet.reassignNull')} --</option>
					</div>
				</div>
				
		 </div>
		</div>
         <div class="bdft">
            <input type="submit" id="${el}-ok" value="${msg("button.ok")}" />
            <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" />
         </div>
      </form>
   </div>
<div id="${el}-selectFilterPath"></div>
</div>
