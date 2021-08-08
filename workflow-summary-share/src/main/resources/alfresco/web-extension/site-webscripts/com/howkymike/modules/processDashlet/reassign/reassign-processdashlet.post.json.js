/*
*       Get Latest Document configuration component POST method
*/

function main()
{

	var c = sitedata.getComponent(url.templateArgs.componentId);

	var saveValue = function(name, value)
	{
        model[name] = value;
	}

	saveValue("assignee", String(json.get("assignee")));

}

main();
