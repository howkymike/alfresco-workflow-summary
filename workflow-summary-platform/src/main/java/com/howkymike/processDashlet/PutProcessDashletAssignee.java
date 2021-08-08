package com.howkymike.processDashlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.TaskService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.howkymike.workflow.MyWorkflowPermissionInterceptor;

public class PutProcessDashletAssignee extends AbstractWorkflowWebscript{

	private SiteService siteService;
	private TaskService taskService;
	
	@Override
	protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status,
			Cache cache) {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        
        // getting task id from request parameters
        String taskId = params.get("task_instance_id");
        String actvivityTaskId = (taskId.contains("activiti$") ? taskId.substring(9,  taskId.length()): taskId);
        
        checkAccess(workflowService.getTaskById(taskId).getDefinition().getId());
        
        JSONObject json = null;
        
        try
        {
            WorkflowTask workflowTask = workflowService.getTaskById(taskId);
            // task was not found -> return 404
            if (workflowTask == null)
            {
                throw new WebScriptException(HttpStatus.SC_NOT_FOUND, "Failed to find workflow task with id: " + taskId);
            }
            
            // read request json assignee
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            String assignee = (String) json.get("assignee");
                
            // update task assignee
            taskService.setAssignee(actvivityTaskId, assignee);
          
                
            // build the model for ftl
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("success", true);
               
            return model;
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from request.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request.", je);
        }
        catch (AccessDeniedException ade)
        {
            throw new WebScriptException(HttpStatus.SC_UNAUTHORIZED, "Failed to update workflow task with id: " + taskId, ade);
        }
        catch (WorkflowException we)
        {
            throw new WebScriptException(HttpStatus.SC_UNAUTHORIZED, "Failed to update workflow task with id: " + taskId, we);
        }
        catch (org.activiti.engine.ActivitiObjectNotFoundException ae)
        {
        	throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Failed to set assignee to workflow task with id: " + taskId);
        }
	}

	public void checkAccess(String defId) {
		
        final String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        final Set<String> authorities = authorityService.getAuthoritiesForUser(userName);	// firstly, we confirm that user is a manger or admin; secondly, we will check the Site
        final boolean isSiteManager = authorities.contains(MyWorkflowPermissionInterceptor.GROUP_PROCESS_SITE_MANAGER);
        final boolean isAdmin = (authorities.contains("GROUP_ALFRESCO_ADMINISTRATORS") || authorities.contains(MyWorkflowPermissionInterceptor.PROCESS_MANAGER_GROUP_NAME));
        if(!isSiteManager && !isAdmin) {
        	throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You dont have access to execute this request.");
        }
        List<SiteInfo> userSites = siteService.listSites(userName);
        if(!GetProcessDashlet.hasWorkflowAccess(defId, userSites) && !isAdmin)
        	throw new WebScriptException(Status.STATUS_UNAUTHORIZED, "You dont have access to execute this request.");
	}
	
	
    public TaskService getTaskService()
    {
        return taskService;
    }
    public void setTaskService(TaskService taskService)
    {
        this.taskService = taskService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
}
