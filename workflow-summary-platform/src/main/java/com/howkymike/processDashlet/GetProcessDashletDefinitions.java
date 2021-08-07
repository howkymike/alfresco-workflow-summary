package com.howkymike.processDashlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.howkymike.workflow.MyWorkflowPermissionInterceptor;


/**
 * Java backed implementation for REST API to retrieve workflow definitions for the process dashlet.
 * 
 * @author howkymike
 * @since 6.1
 */
public class GetProcessDashletDefinitions extends AbstractWorkflowWebscript{
    
    public static final String SITE_PREFIX = "sitePrefix";
    private static final String WORKFLOW_DEFINITIONS = "workflowDefinitions";
        
    private static final String EXCLUDE_FILTER = "activiti$activitiAdhoc,activiti$activitiInvitationModerated,activiti$activitiInvitationNominated,activiti$activitiInvitationNominatedAddDirect,activiti$activitiParallelGroupReview,activiti$activitiParallelReview,activiti$activitiReview,activiti$activitiReviewPooled,activiti$resetPassword";
    
    private SiteService siteService;
    
	@Override
	protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache) {

        final String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        final Set<String> authorities = authorityService.getAuthoritiesForUser(userName);	// firstly, we confirm that user is a manger or admin; secondly, we will check the Site
        final boolean isSiteManager = authorities.contains(MyWorkflowPermissionInterceptor.GROUP_PROCESS_SITE_MANAGER);
        final boolean isAdmin = (authorities.contains(GetProcessDashlet.GROUP_ALFRESCO_ADMINISTRATORS) || authorities.contains(MyWorkflowPermissionInterceptor.PROCESS_MANAGER_GROUP_NAME));
        if(!isSiteManager && !isAdmin) {
        	//throw new WebScriptException(Status.STATUS_NOT_FOUND, "You dont have access to execute this request.");
        	return new HashMap<String, Object>();	// avoid early errors
        }
        List<SiteInfo> userSites = siteService.listSites(userName);
        ExcludeFilter excludeFilter = new ExcludeFilter(EXCLUDE_FILTER);
        
        List<WorkflowDefinition> workflowDefinitions = workflowService.getDefinitions();
        ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        for (WorkflowDefinition workflowDefinition : workflowDefinitions)            
        {
        	if (excludeFilter == null || !excludeFilter.isMatch(workflowDefinition.getName())) {
            	if(!GetProcessDashlet.hasWorkflowAccess(workflowDefinition.getName(), userSites) && !isAdmin)	// check access
            		continue;
            	Map<String, Object> defModel = modelBuilder.buildSimple(workflowDefinition);
            	defModel.put(SITE_PREFIX, GetProcessDashlet.getPrefixFromWorkflowDef(workflowDefinition.getName()));
            	results.add(defModel);	
        	}
        }
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(WORKFLOW_DEFINITIONS, results);
        return model;
	}
	

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
}
