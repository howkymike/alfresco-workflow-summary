var connector = remote.connect("alfresco"); 
var jsonA = connector.get("/howkymike/processdashlet/getDefinitions");
//model.getDefinitionsJSON = jsonA.toString();
jsonA = jsonUtils.toObject(jsonA);
var workflow_definition_list_name = new Array();
var workflow_definition_list_title = new Array();
var workflow_definition_list_sitePrefix = new Array();
for (var i = 0; i< jsonA.data.length; i++) {
	workflow_definition_list_name.push(jsonA.data[i].name);
	workflow_definition_list_title.push(jsonA.data[i].title);
	workflow_definition_list_sitePrefix.push(jsonA.data[i].sitePrefix);
}
model.workflow_definition_list_name = workflow_definition_list_name.toString();
model.workflow_definition_list_title = workflow_definition_list_title.toString();
model.workflow_definition_list_sitePrefix = workflow_definition_list_sitePrefix.toString();

//test
//model.groups = people.getContainerGroups(person);
//model.isAdmin = people.isAdmin(person);	// error: "people" is not defined
//model.username = user.name.toString();
var userSites = new Array();
var userSitesJSON = connector.get("/api/people/" + user.name.toString() + "/sites");
userSitesJSON = jsonUtils.toObject(userSitesJSON);
for (var i = 0; i < userSitesJSON.length; i++) {
	userSites.push(userSitesJSON[i].shortName);
}
model.userSites = userSites.toString();

var jsonB = connector.get("/api/people/" + user.name.toString() + "?groups=true");
jsonB = jsonUtils.toObject(jsonB);
var groupList = new Array();
var isAuthenticated = false;

for (var i = 0; i < jsonB.groups.length; i++) {
	groupList.push(jsonB.groups[i].displayName);
	if(jsonB.groups[i].itemName === "GROUP_WORKFLOW_MANAGER" || jsonB.groups[i].itemName === "GROUP_WORKFLOW_SITE_MANAGER" || jsonB.groups[i].itemName === "GROUP_ALFRESCO_ADMINISTRATORS") {
		isAuthenticated = true;
		break;
	}
}
model.isAuthenticated = isAuthenticated;