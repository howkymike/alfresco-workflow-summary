<#-- Workflow definition collection for ProcessDashlet-->

{
   "data": 
   [
   <#if workflowDefinitions??>
      <#list workflowDefinitions as workflowDefinition>
		<#escape x as jsonUtils.encodeJSONString(x)>
		      {
		         "id" : "${workflowDefinition.id}",
		         "url": "${workflowDefinition.url}",
		         "name": "${workflowDefinition.name}",
		         "title": "${workflowDefinition.title!""}",
		         "description": "${workflowDefinition.description!""}",
		         "sitePrefix": "${workflowDefinition.sitePrefix!""}",
		         "version": "${workflowDefinition.version}"
		      }
		</#escape>
         <#if workflowDefinition_has_next>,</#if>
      </#list>
      </#if>
   ]
}