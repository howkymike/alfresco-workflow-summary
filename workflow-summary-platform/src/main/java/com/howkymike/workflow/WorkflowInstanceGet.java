package com.howkymike.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * MODIFICATION: added task's assignees
 * @author unknown
 * @since 3.4
 */
public class WorkflowInstanceGet extends AbstractWorkflowWebscript
{
    public static final String PARAM_INCLUDE_TASKS = "includeTasks";
    
    public static final String TASK_ASSIGNEES_FULLNAMES = "assigneesFullNames"; // added

    @Override
    protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();

        // getting workflow instance id from request parameters
        String workflowInstanceId = params.get("workflow_instance_id");

        boolean includeTasks = getIncludeTasks(req);

        WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowInstanceId);

        // task was not found -> return 404
        if (workflowInstance == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to find workflow instance with id: " + workflowInstanceId);
        }

        Map<String, Object> model = new HashMap<String, Object>();
        // build the model for ftl
        model.put("workflowInstance", buildDetailedWithTaskAssigneees(workflowInstance, includeTasks, modelBuilder));

        return model;
    }

    
    /**
     * Copy of WorkflowModelBuilder.buildDetailed with task assignees
     */
    private Map<String, Object> buildDetailedWithTaskAssigneees(WorkflowInstance workflowInstance, boolean includeTasks, WorkflowModelBuilder modelBuilder)
    {
        Map<String, Object> model = modelBuilder.buildSimple(workflowInstance);

        Serializable startTaskId = null;
        WorkflowTask startTask = workflowService.getStartTask(workflowInstance.getId());
        if (startTask != null)
        {
            startTaskId = startTask.getId();
        }
        
        if (workflowService.hasWorkflowImage(workflowInstance.getId()))
        {
            model.put(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_DIAGRAM_URL, "api/workflow-instances/" + workflowInstance.getId() + "/diagram");
        }
        
        model.put(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_START_TASK_INSTANCE_ID, startTaskId);
        model.put(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_DEFINITION, modelBuilder.buildDetailed(workflowInstance.getDefinition()));

        if (includeTasks)
        {
            // get all tasks for workflow
            WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
            tasksQuery.setTaskState(null);
            tasksQuery.setActive(null);
            tasksQuery.setProcessId(workflowInstance.getId());
            List<WorkflowTask> tasks = workflowService.queryTasks(tasksQuery);

            ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>(tasks.size());

            for (WorkflowTask task : tasks)
            {
            	Map<String, Object> taskModel = modelBuilder.buildSimple(task, null);
            	taskModel.put(TASK_ASSIGNEES_FULLNAMES, getTaskAuthority(task));	// ADDED!
                results.add(taskModel);
            }

            model.put(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_TASKS, results);
        }

        return model;
    }
    
    
    
    private boolean getIncludeTasks(WebScriptRequest req)
    {
        String includeTasks = req.getParameter(PARAM_INCLUDE_TASKS);
        if (includeTasks != null)
        {
            try
            {
                return Boolean.valueOf(includeTasks);
            }
            catch (Exception e)
            {
                // do nothing, false will be returned
            }
        }

        // Defaults to false.
        return false;
    }

    
    /**
     * Copy of GetProcessDashlet.getTaskAuthority
     */
	private String getTaskAuthority(WorkflowTask task) {
		String owner = (String) task.getProperties().get(ContentModel.PROP_OWNER);
		if (owner != null) {
			return getPersonName(owner);
		}
		
		Collection<?> actors = (Collection<?>) task.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS);
		List<String> actorsStrList = new ArrayList<String>(actors.size());	// can be more elements than actors.size
		for(Object actorObj : actors) {
			if(actorObj instanceof NodeRef) {
				NodeRef actorRef = (NodeRef) actorObj;
				Map<QName, Serializable> properties = nodeService.getProperties(actorRef);
				if(properties.containsKey(ContentModel.PROP_AUTHORITY_NAME)) {	// group, iterate over its members
					String groupName = (String) properties.get(ContentModel.PROP_AUTHORITY_NAME);
					Set<String> authorities = authorityService.getContainedAuthorities(null, groupName, true);
					for(String author : authorities) {
						AuthorityType authorityType = AuthorityType.getAuthorityType(author);
						if (authorityType.equals(AuthorityType.USER)) {
							actorsStrList.add(getPersonName(author));
						}	// TODO add nested groups
					}
				}
				else if(properties.containsKey(ContentModel.PROP_OWNER)) {	// person
					String propOwner = (String) properties.get(ContentModel.PROP_OWNER);
					actorsStrList.add(getPersonName(propOwner));
				}
			}
		}
		String actorsStr = String.join(", ", actorsStrList);
		return actorsStr;
	}
	
	
	/**
	 * Returns person's fullName or username
	 */
	private String getPersonName(String username) {
		if (personService.personExists(username)) {
			NodeRef personRef = personService.getPerson(username);
			Map<QName, Serializable> properties = nodeService.getProperties(personRef);
			String firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
			String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
			if(firstName != null && lastName != null)
				return firstName + " " + lastName;	
		}
		return username;
	}
}
