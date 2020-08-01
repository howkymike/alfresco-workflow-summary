var connector = remote.connect("alfresco"); 
var jsonA = connector.get("/api/workflow-definitions");
jsonA = jsonUtils.toObject(jsonA);
var workflow_definition_list_name = new Array();
var workflow_definition_list_title = new Array();
for (var i = 0; i< jsonA.data.length; i++) {
	workflow_definition_list_name.push(jsonA.data[i].name);
	workflow_definition_list_title.push(jsonA.data[i].title);
}
model.workflow_definition_list_name = workflow_definition_list_name.toString();
model.workflow_definition_list_title = workflow_definition_list_title.toString();


var jsonB = connector.get("/api/people/" + user.name.toString() + "?groups=true");
jsonB = jsonUtils.toObject(jsonB);
var groupList = new Array();
var isAuthenticated = false;

for (var i = 0; i < jsonB.groups.length; i++) {
	groupList.push(jsonB.groups[i].displayName);
	if(jsonB.groups[i].itemName === "GROUP_MANAGER" || jsonB.groups[i].itemName === "GROUP_ALFRESCO_ADMINISTRATORS") {
		isAuthenticated = true;
		break;
	}
}
model.isAuthenticated = isAuthenticated;