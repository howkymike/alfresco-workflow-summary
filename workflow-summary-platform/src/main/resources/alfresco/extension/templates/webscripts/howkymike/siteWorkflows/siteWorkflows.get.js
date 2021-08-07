function contains(a, obj) {
    var i = a.length;
    while (i--) {
       if (a[i] === obj) {
           return true;
       }
    }
    return false;
}

function getUserWorkflowDefinitions() {
var userSites = siteService.listUserSites(person.properties.userName);
var dataListContainer = "dataLists";
var workflowIds = [];

for (var i = 0; i < userSites.length; i++) {
	var site = userSites[i];
	var siteNode = site.getNode();
	var dataLists = siteNode.childByNamePath(dataListContainer);
	if (dataLists) {													// Wyszukaj folderu Obiegi											
		var dataListChildren = dataLists.getChildren();
		for (var j = 0; j < dataListChildren.length; j++) {
			var dataListChild = dataListChildren[j];
			if(dataListChild.properties["cm:title"] == "Obiegi") {		// Znajdz wszystkie workflowId												
				var workflowNodeIds = dataListChild.getChildren();
				for (var k = 0; k < workflowNodeIds.length; k++) {
					var workflowIdNode = workflowNodeIds[k];
					var workflowId = workflowIdNode.properties["epmswdm:workflowId"];
					if(!contains(workflowIds, workflowId)) {
						workflowIds.push(workflowId);
					}
				}
			}
		}
	}
}

model.userWorkflows = workflowIds;
}


getUserWorkflowDefinitions(args.userId);	
