package com.howkymike.processDashlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.workflow.AbstractWorkflowWebscript;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.howkymike.workflow.MyWorkflowPermissionInterceptor;


/**
 * Java backed implementation for REST API to retrieve workflows for the process dashlet.
 * 
 * @author howkymike
 * @since 6.1
 */
public class GetProcessDashlet extends AbstractWorkflowWebscript{
    public static final String PARAM_SITENAME = "siteName";
    public static final String PARAM_DEFINITION_NAME= "definitionName";
    public static final String PARAM_STARTED_BEFORE = "startedBefore";
    public static final String PARAM_STARTED_AFTER = "startedAfter";
    public static final String PARAM_INITIATOR = "initiator";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_ADDITIONAL_FIELD = "additionalField";
    public static final String PARAM_CURR_TASK_AUTHORITY = "currTaskAuthority";
    
    public static final QName QNAME_INITIATOR = QName.createQName(NamespaceService.DEFAULT_URI, "initiator");
    
    public static final String WORKFLOW_DEFINITION_NAME = "definitionName";
    public static final String WORKFLOW_INSTANCE_ID= "instanceID";
    public static final String WORKFLOW_INSTANCE_TITLE = "instanceTitle";
    public static final String WORKFLOW_START_DATE = "startDate";
    public static final String WORKFLOW_END_DATE = "endDate";
    public static final String WORKFLOW_CURRENT_TASK = "currentTask";
    public static final String WORKFLOW_INITIATOR = "initiator";

    public static final String CURRENT_TASK_ID = "taskId";
    public static final String CURRENT_TASK_TITLE = "taskTitle";
    public static final String CURRENT_TASK_START_DATE = "taskStartDate";
    public static final String CURRENT_TASK_AUTHORITY = "taskAuthority";
    
    
    public static final String WORKFLOW_INITIATOR_USERNAME = "userName";
    public static final String WORKFLOW_INITIATOR_FIRSTNAME = "firstName";
    public static final String WORKFLOW_INITIATOR_LASTNAME = "lastName";
    
    static final String GROUP_ALFRESCO_ADMINISTRATORS = "GROUP_ALFRESCO_ADMINISTRATORS";
    
    static final String[] EXCLUDED_WORKFLOWS = {"activiti$activitiAdhoc", "activiti$activitiInvitationModerated", "activiti$activitiInvitationNominated", "activiti$activitiInvitationNominatedAddDirect", "activiti$activitiParallelGroupReview"
        , "activiti$activitiParallelReview", "activiti$activitiReview", "activiti$activitiReviewPooled", "activiti$resetPassword"};
    
    private SiteService siteService;
    

    protected boolean showGroupMembers;
    protected String additionalFieldPropName;
    
    private String requestedCurrTaskAuthrority;
    private String requestedAdditionalFieldValue;
    private QName additionalFieldQName;
    
    
    private static final Log logger = LogFactory.getLog(GetProcessDashlet.class);
    
	@Override
	protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, Status status, Cache cache) {

        final String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        final Set<String> authorities = authorityService.getAuthoritiesForUser(userName);	// firstly, we confirm that user is a manger or admin; secondly, we will check the Site
        final boolean isSiteManager = authorities.contains(MyWorkflowPermissionInterceptor.GROUP_PROCESS_SITE_MANAGER);
        final boolean isAdmin = (authorities.contains(GROUP_ALFRESCO_ADMINISTRATORS) || authorities.contains(MyWorkflowPermissionInterceptor.PROCESS_MANAGER_GROUP_NAME));
        if(!isSiteManager && !isAdmin) {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "You dont have access to execute this request.");
        }
        List<SiteInfo> userSites = siteService.listSites(userName);
        
        // create workflow query
        WorkflowInstanceQuery workflowInstanceQuery = new WorkflowInstanceQuery();
        Map<QName, Object> filters = new HashMap<QName, Object>(9);
        
        if (req.getParameter(PARAM_INITIATOR) != null)
            filters.put(QNAME_INITIATOR, personService.getPerson(req.getParameter(PARAM_INITIATOR)));
        
                
        workflowInstanceQuery.setStartBefore(getDateFromRequest(req, PARAM_STARTED_BEFORE));
        workflowInstanceQuery.setStartAfter(getDateFromRequest(req, PARAM_STARTED_AFTER));
        workflowInstanceQuery.setExcludedDefinitions(Arrays.asList(EXCLUDED_WORKFLOWS));
        
        WorkflowState state = getState(req);
        if (state == null)
            state = WorkflowState.ACTIVE;
        workflowInstanceQuery.setActive(state == WorkflowState.ACTIVE);
        workflowInstanceQuery.setCustomProps(filters);
            	
    	List<WorkflowDefinition> workflowDefinitions = new ArrayList<WorkflowDefinition>();
        if(req.getParameter(PARAM_DEFINITION_NAME) != null) {	// if definition name is provided, fetch all by definition name (there can be many definitions with the same name!!)
        	String definitionName = req.getParameter(PARAM_DEFINITION_NAME);
        	workflowDefinitions = workflowService.getAllDefinitionsByName(definitionName);
        }
        else if(req.getParameter(PARAM_SITENAME) != null) {	// fetch all site workflows, unsafe because workflow's quantity can be very extensive
        	String siteNameParam = req.getParameter(PARAM_SITENAME);
        	workflowDefinitions = getWorkflowSiteDefinitions(siteNameParam);
        }
        else {	// fetch all workflows, unsafe because workflow's quantity can be very extensive
        	if(!isAdmin) {
        		throw new WebScriptException(Status.STATUS_NOT_FOUND, "You dont have access to fetch all workflows.");
        	}
        	workflowDefinitions = workflowService.getAllDefinitions();
        }	
             
        	
        // create workflows list and for each definition check access
        List<WorkflowInstance> workflows = new ArrayList<WorkflowInstance>();
        int total = 0;
        int maxItems = getIntParameter(req, PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS);
        int skipCount = getIntParameter(req, PARAM_SKIP_COUNT, DEFAULT_SKIP_COUNT);
    	int workingSkipCount = skipCount;	// additional logic for huge amount of workflows
    	int itemsToQuery = maxItems;
        for(WorkflowDefinition def : workflowDefinitions) {
        	if(!hasWorkflowAccess(def.getName(), userSites) && !isAdmin)	// check access
        		continue;
        	workflowInstanceQuery.setWorkflowDefinitionId(def.getId());
    		if(maxItems < 0 || itemsToQuery > 0)
    			workflows.addAll(workflowService.getWorkflows(workflowInstanceQuery, itemsToQuery, workingSkipCount));
    		if(maxItems > 0)
    			itemsToQuery = maxItems - workflows.size();
    		total += (int) workflowService.countWorkflows(workflowInstanceQuery);
            if(workingSkipCount > 0)
            {
                workingSkipCount = skipCount - total;
                if(workingSkipCount < 0)
                {
                    workingSkipCount = 0;
                }
            }
        }
      
        this.requestedCurrTaskAuthrority = req.getParameter(PARAM_CURR_TASK_AUTHORITY);
        if(this.requestedCurrTaskAuthrority == null || this.requestedCurrTaskAuthrority.isEmpty())
        	this.requestedCurrTaskAuthrority = null;
        
        this.requestedAdditionalFieldValue = req.getParameter(PARAM_ADDITIONAL_FIELD);
        this.additionalFieldQName = null; // if not null it means the query will be filtered by the additional field value
        if (this.requestedAdditionalFieldValue != null && !this.requestedAdditionalFieldValue.isEmpty()) {
	        try {
	        	this.additionalFieldQName = QName.createQName(this.additionalFieldPropName, this.namespaceService);
	        } catch(NamespaceException e) {
	        	logger.info("additionalFieldPropName does not have a valid qname (" + this.additionalFieldPropName + ")");
	        }
        }
        
        
        List<Map<String, Object>> results = null;
        if(this.additionalFieldQName != null || (this.requestedCurrTaskAuthrority != null)) {
        	results = new ArrayList<Map<String, Object>>();
        } else {
            results = new ArrayList<Map<String, Object>>(total);
            results.addAll(Arrays.asList((Map<String, Object>[]) new Map[total]));
        }
        for (WorkflowInstance workflow : workflows)
        {	
        	Map<String, Object> workflowProcessDashletModel = buildForProcessDashlet(workflow, state == WorkflowState.ACTIVE);
        	if(workflowProcessDashletModel != null) {
        		if((this.requestedCurrTaskAuthrority != null))
        			results.add(buildForProcessDashlet(workflow, state == WorkflowState.ACTIVE));
        		else
        			results.set(skipCount, buildForProcessDashlet(workflow, state == WorkflowState.ACTIVE));
                skipCount++;
        	}
        }	
        
        // create and return results, paginated if necessary
        return createResultModel(req, "workflowInstances", results);
	}
	
	
        
    public static boolean hasWorkflowAccess(String definitionName, List<SiteInfo> userSites) {
    	if(isSiteWorkflow(definitionName)) {
    		String defPrefix = getPrefixFromWorkflowDef(definitionName);
    		for(SiteInfo userSite : userSites) {
    			String sitePrefix = convertSiteNameToWorkflowPrefix(userSite.getShortName());
    			if(sitePrefix.equals(defPrefix))
    				return true;
    		}
    		return false;
    	}
    	return true;
    }
        


	private List<WorkflowDefinition> getWorkflowSiteDefinitions(String siteNameParam) {
		List<WorkflowDefinition> resultDefs = new ArrayList<WorkflowDefinition>();
		List<WorkflowDefinition> defs = workflowService.getAllDefinitions();
		for(WorkflowDefinition def : defs) {
			if(isSiteWorkflow(def.getName())) {
				String defPrefix = getPrefixFromWorkflowDef(def.getName());
				if(defPrefix.equals(convertSiteNameToWorkflowPrefix(siteNameParam))) {
					resultDefs.add(def);
				}
			}
		}
		return resultDefs;
	}
        
	
    /**
     * Converts site name to the workflow prefix.
     */
	private static String convertSiteNameToWorkflowPrefix(String siteName) {
		return siteName.toLowerCase().replaceAll("[^a-z]", "");
	}
	
	
    /**
     * Extracts site prefix from workflow definition name.
     */
	public static String getPrefixFromWorkflowDef(String definitionName) {
		if(definitionName.contains("activiti$")) {
			definitionName = definitionName.substring("activiti$".length());
			if(definitionName.contains("__")) {
				return definitionName.substring(0,definitionName.indexOf("__"));
			}	
		}
		return "";
	}
    
	
    /**
     * Checks if workflow is "assigned" to the specific site.
     */
	private static boolean isSiteWorkflow(String definitionName) {
		return (definitionName.contains("__")) ? true : false;
	}
	
	
    /**
     * Returns a representation of a {@link WorkflowInstance} for the ProcessDashlet.
     * @param workflowInstance The workflow instance to be represented.          
     * @return Map
     */
	private Map<String, Object> buildForProcessDashlet(WorkflowInstance workflowInstance, boolean isActive) {
		Map<String, Object> model = new HashMap<String, Object>();
		
        // get current task params AND apply additionalFiled filter
        if(isActive || this.additionalFieldQName != null) {
            Map<String, Object> currTaskModel = new HashMap<String, Object>();
            WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
            if(isActive)
            	tasksQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            tasksQuery.setProcessId(workflowInstance.getId());
            List<WorkflowTask> tasks = workflowService.queryTasks(tasksQuery, false);
            if(!tasks.isEmpty()) {
                WorkflowTask currTask = tasks.get(0);
                Map<QName, Serializable> taskProps = currTask.getProperties();
                if(this.additionalFieldQName != null) {
                	Serializable taskAdditionalFiledValue = taskProps.get(this.additionalFieldQName);
                	if(!taskAdditionalFiledValue.toString().contains(requestedAdditionalFieldValue))
                		return null;
                }
                if(isActive) {
                	currTaskModel.put(CURRENT_TASK_ID, currTask.getId());
                    currTaskModel.put(CURRENT_TASK_TITLE, currTask.getTitle());
                    if (workflowInstance.getStartDate() == null)
                    	currTaskModel.put(CURRENT_TASK_START_DATE, taskProps.get(WorkflowModel.PROP_START_DATE));
                    else
                    	currTaskModel.put(CURRENT_TASK_START_DATE, ISO8601DateFormat.format((Date) taskProps.get(WorkflowModel.PROP_START_DATE)));
                    String taskAuthority = getTaskAuthority(currTask);
                    if (this.requestedCurrTaskAuthrority != null && !taskAuthority.contains(this.requestedCurrTaskAuthrority))	// currTaskAuthrority filter
                    	return null;	
                    currTaskModel.put(CURRENT_TASK_AUTHORITY, taskAuthority);
                    model.put(WORKFLOW_CURRENT_TASK, currTaskModel);	
                }
            }	
        }
		
		// get basic informations
		model.put(WORKFLOW_INSTANCE_ID, workflowInstance.getId());
		model.put(WORKFLOW_DEFINITION_NAME, workflowInstance.getDefinition().getTitle());
		model.put(WORKFLOW_INSTANCE_TITLE, workflowInstance.getDescription());
        if (workflowInstance.getStartDate() == null)
            model.put(WORKFLOW_START_DATE, workflowInstance.getStartDate());
        else
            model.put(WORKFLOW_START_DATE, ISO8601DateFormat.format(workflowInstance.getStartDate()));
        if (workflowInstance.getEndDate() == null)
            model.put(WORKFLOW_END_DATE, workflowInstance.getEndDate());
        else
            model.put(WORKFLOW_END_DATE, ISO8601DateFormat.format(workflowInstance.getEndDate()));
        
        // get initiator model
        if (workflowInstance.getInitiator() == null || !nodeService.exists(workflowInstance.getInitiator()))
        {
            model.put(WORKFLOW_INITIATOR, null);
        }
        else
        {
            model.put(WORKFLOW_INITIATOR, getPersonModel(nodeService.getProperty(workflowInstance.getInitiator(), ContentModel.PROP_USERNAME)));
        }
		
		return model;
	}
	
	
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
					if(this.showGroupMembers) {
						Set<String> authorities = authorityService.getContainedAuthorities(null, groupName, true);
						for(String author : authorities) {
							AuthorityType authorityType = AuthorityType.getAuthorityType(author);
							if (authorityType.equals(AuthorityType.USER)) {
								actorsStrList.add(getPersonName(author));
							}	// TODO add nested groups
						}	
					} else {
						actorsStrList.add(groupName);
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
	
	
    private Map<String, Object> getPersonModel(Serializable nameSer)
    {
        if (!(nameSer instanceof String))
            return null;

        String name = (String) nameSer;
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(WORKFLOW_INITIATOR_USERNAME, name);
        
        if (personService.personExists(name))
        {
            NodeRef person = personService.getPerson(name);
            Map<QName, Serializable> properties = nodeService.getProperties(person);
            model.put(WORKFLOW_INITIATOR_FIRSTNAME, properties.get(ContentModel.PROP_FIRSTNAME));
            model.put(WORKFLOW_INITIATOR_LASTNAME, properties.get(ContentModel.PROP_LASTNAME));
        }
        
        return model;
    }
	
    /**
     * Gets the specified {@link WorkflowState}, null if not requested.
     * 
     * @param req The WebScript request
     * @return The workflow state or null if not requested
     */
    private WorkflowState getState(WebScriptRequest req)
    {
        String stateName = req.getParameter(PARAM_STATE);
        if (stateName != null)
        {
            try
            {
                return WorkflowState.valueOf(stateName.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                String msg = "Unrecognised State parameter: " + stateName;
                throw new WebScriptException(Status.STATUS_NOT_FOUND, msg);
            }
        }
        
        return null;
    }
    
    // enum to represent workflow states
    private enum WorkflowState
    {
        ACTIVE, COMPLETED;
    }
    
    private Date getDateFromRequest(WebScriptRequest req, String paramName)
    {
        String dateParam = req.getParameter(paramName);
        if (dateParam != null)
        {
            if (!EMPTY.equals(dateParam) && !NULL.equals(dateParam))
            {
                return getDateParameter(req, paramName);
            }
        }

        return null;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * @param showGroupMembers
     *            the showGroupMembers to set
     */
    public void setShowGroupMembers(final boolean showGroupMembers)
    {
        this.showGroupMembers = showGroupMembers;
    }
    
    /**
     * @param additionalFieldPropName
     *            the additionalFieldPropName to set
     */
    public void setAdditionalFieldPropName(final String additionalFieldPropName)
    {
        this.additionalFieldPropName = additionalFieldPropName;
    }
}
