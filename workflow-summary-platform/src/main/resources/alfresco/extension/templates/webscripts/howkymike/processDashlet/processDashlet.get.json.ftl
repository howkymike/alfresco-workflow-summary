<#-- Workflow Instances collection for ProcessDashlet-->

{
   "data": 
   [
      <#list workflowInstances as workflowInstance>
      <#escape x as jsonUtils.encodeJSONString(x)>
      {
        "instanceID": "${workflowInstance.instanceID!""}",
      	"definitionName": "${workflowInstance.definitionName!""}",
      	"instanceTitle": "${workflowInstance.instanceTitle!""}",
      	"startDate": "${workflowInstance.startDate!""}",
      	"endDate": "${workflowInstance.endDate!""}"<#if workflowInstance.currentTask??>,
      	"currentTask": 
      	{
        	"taskTitle": "${workflowInstance.currentTask.taskTitle!""}",
        	"taskAuthority": "${workflowInstance.currentTask.taskAuthority!""}",
        	"taskStartDate": "${workflowInstance.currentTask.taskStartDate!""}"
      	}</#if>,
      	"initiator": 
      	<#if workflowInstance.initiator??>
		{
        	"userName": "${workflowInstance.initiator.userName}"<#if workflowInstance.initiator.firstName??>,
        	"firstName": "${workflowInstance.initiator.firstName}"</#if><#if workflowInstance.initiator.lastName??>,
        	"lastName": "${workflowInstance.initiator.lastName}"</#if>
		}
		<#else>
		null
		</#if>
      }
      </#escape>
      <#if workflowInstance_has_next>,</#if>
      </#list>
   ]
}
