package com.howkymike.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.howkymike.processDashlet.GetProcessDashlet;

public class MyWorkflowPermissionInterceptor implements MethodInterceptor
{
    private PersonService personService;
    private SiteService siteService;
    private AuthorityService authorityService;
    private WorkflowService workflowService;
    private NodeService nodeService;

    public static final String PROCESS_MANAGER_GROUP_NAME = "GROUP_WORKFLOW_MANAGER";
    public static final String GROUP_PROCESS_SITE_MANAGER = "GROUP_WORKFLOW_SITE_MANAGER";
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        String currentUser = AuthenticationUtil.getRunAsUser();
        
        // See if we can shortcut (for 'System' and 'admin')
        
        if (currentUser != null && (authorityService.isAdminAuthority(currentUser) || AuthenticationUtil.isRunAsUserTheSystemUser()))
        {
            return invocation.proceed();
        }
        
        // MY CUSTOM ADDON
        Set<String> userGroups = authorityService.getAuthoritiesForUser(currentUser);
        boolean isSiteManager = false;
        for (String groupName : userGroups)
        {
            if (groupName.equals(PROCESS_MANAGER_GROUP_NAME))
            {
            	return invocation.proceed();
            }
            if(groupName.equals(GROUP_PROCESS_SITE_MANAGER))
            {
            	isSiteManager = true;
            }
        }
        
        String methodName = invocation.getMethod().getName();

        if (methodName.equals("getTaskById"))
        {
            Object result = invocation.proceed();
            WorkflowTask wt = (WorkflowTask) result;
            
            if (isInitiatorOrAssignee(wt, currentUser) || fromSameParallelReviewWorkflow(wt, currentUser) || 
                        isStartTaskOfProcessInvolvedIn(wt, currentUser) || (isSiteManager && isWorkflowSiteManager(wt, currentUser)))
            {
                return result;
            }
            else
            {
            	
                String taskId = (String) invocation.getArguments()[0];
                throw new AccessDeniedException("Accessing task with id='" + taskId + "' is not allowed for user '" + currentUser + "'");
            }

        }
        
        if(methodName.equals("getStartTask"))
        {
            Object result = invocation.proceed();
            WorkflowTask wt = (WorkflowTask) result;
            
            if (isInitiatorOrAssignee(wt, currentUser) || isUserPartOfProcess(wt, currentUser) || (isSiteManager && isWorkflowSiteManager(wt, currentUser)))
            {
                return result;
            }
            else
            {
                String taskId = (String) invocation.getArguments()[0];
                throw new AccessDeniedException("Accessing task with id='" + taskId + "' is not allowed for user '" + currentUser + "'");
            }
            
        }

        if (methodName.equals("updateTask") || methodName.equals("endTask"))
        {
            String taskId = (String) invocation.getArguments()[0];
            WorkflowTask taskToUpdate = workflowService.getTaskById(taskId);
            if (isInitiatorOrAssignee(taskToUpdate, currentUser) || (isSiteManager && isWorkflowSiteManager(taskToUpdate, currentUser)))
            {
                return invocation.proceed();
            }
            else
            {
                throw new AccessDeniedException("Accessing task with id='" + taskId + "' is not allowed for user '" + currentUser + "'");
            }

        }

        if (methodName.equals("getAssignedTasks") || methodName.equals("getPooledTasks") || methodName.equals("getTasksForWorkflowPath") || methodName.equals("getStartTasks") || methodName.equals("queryTasks"))
        {
            Object result = invocation.proceed();
            List<WorkflowTask> rawList = (List<WorkflowTask>) result;
            List<WorkflowTask> resultList = new ArrayList<WorkflowTask>(rawList.size());

            for (WorkflowTask wt : rawList)
            {
                if (isInitiatorOrAssignee(wt, currentUser) || fromSameParallelReviewWorkflow(wt, currentUser)
                            || isStartTaskOfProcessInvolvedIn(wt, currentUser) || (isSiteManager && isWorkflowSiteManager(wt, currentUser)))
                {
                    resultList.add(wt);
                }
            }

            return resultList;
        }

        return invocation.proceed();
    }

    
    private boolean isWorkflowSiteManager(WorkflowTask wt, String userName) {
    	String workflowDefname = wt.getPath().getInstance().getDefinition().getName();
    	
		List<SiteInfo> userSites = siteService.listSites(userName);
		if(GetProcessDashlet.hasWorkflowAccess(workflowDefname, userSites)) {
			return true;
		}
		return false;
    	
    }
    
    private boolean isInitiatorOrAssignee(WorkflowTask wt, String userName)
    {
        if (wt == null)
        {
            return true;
        }

        NodeRef person = personService.getPerson(userName);
        Map<QName, Serializable> props = wt.getProperties();

        String ownerName = (String) props.get(ContentModel.PROP_OWNER);
		//fix for MNT-14366; if owner value can't be found on workflow properties because initiator nodeRef no longer exists
		//get owner from initiatorhome nodeRef owner property
        if (ownerName == null)
        {
            NodeRef initiatorHomeNodeRef = (NodeRef)props.get( QName.createQName("", WorkflowConstants.PROP_INITIATOR_HOME));
            if (initiatorHomeNodeRef != null )
            {
                ownerName = (String)nodeService.getProperty(initiatorHomeNodeRef, ContentModel.PROP_OWNER);
            }
        }
        if (userName != null && userName.equalsIgnoreCase(ownerName))
        {
            return true;
        }

        List<NodeRef> accessUseres = new ArrayList<NodeRef>();
        accessUseres.add(getUserGroupRef(props.get(WorkflowModel.ASSOC_ASSIGNEE)));
        accessUseres.add(getUserGroupRef(props.get(WorkflowModel.ASSOC_GROUP_ASSIGNEE)));
        accessUseres.addAll(getUserGroupRefs(props.get(WorkflowModel.ASSOC_GROUP_ASSIGNEES)));
        accessUseres.addAll(getUserGroupRefs(props.get(WorkflowModel.ASSOC_ASSIGNEES)));
        accessUseres.addAll(getUserGroupRefs(wt.getProperties().get(WorkflowModel.ASSOC_POOLED_ACTORS)));
        accessUseres.add(wt.getPath().getInstance().getInitiator());

        if (accessUseres.contains(person))
        {
            return true;
        }

        Set<String> userGroups = authorityService.getAuthoritiesForUser(userName);
        for (String groupName : userGroups)
        {
            NodeRef groupRef = authorityService.getAuthorityNodeRef(groupName);
            if (groupRef != null && accessUseres.contains(groupRef))
            {
                return true;
            }
        }

        return false;
    }

    private boolean isStartTaskOfProcessInvolvedIn(WorkflowTask wt, String userName) 
    {
        return wt.getId().contains(ActivitiConstants.START_TASK_PREFIX) && isUserPartOfProcess(wt, userName);
    }
    
    private boolean fromSameParallelReviewWorkflow(WorkflowTask wt, String userName)
    {
        // check whether this is parallel review workflow, "parallel" will match all parallel workflows (any engine)
        if (wt.getPath().getInstance().getDefinition().getName().toLowerCase().contains("parallel"))
        {
            WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
            tasksQuery.setTaskState(null);
            tasksQuery.setActive(null);
            tasksQuery.setProcessId(wt.getPath().getInstance().getId());
            List<WorkflowTask> allWorkflowTasks = workflowService.queryTasks(tasksQuery, true);
            
            for (WorkflowTask task : allWorkflowTasks)
            {
                if (isInitiatorOrAssignee(task, userName))
                {
                    // if at list one match then user has task from the same workflow
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isUserPartOfProcess(WorkflowTask wt, String userName)
    {
        WorkflowTaskQuery tasksQuery = new WorkflowTaskQuery();
        tasksQuery.setTaskState(null);
        tasksQuery.setActive(null);
        tasksQuery.setProcessId(wt.getPath().getInstance().getId());
        List<WorkflowTask> allWorkflowTasks = workflowService.queryTasks(tasksQuery, true);
        
        for (WorkflowTask task : allWorkflowTasks)
        {
            if (isInitiatorOrAssignee(task, userName))
            {
                // if at list one match then user has task from the same workflow
                return true;
            }
        }
        return false;
    }

    private NodeRef getUserGroupRef(Object o)
    {
        NodeRef result = null;
        if (o == null || o instanceof NodeRef)
        {
            result = (NodeRef) o;
        }
        else
        {
            try
            {
                result = personService.getPerson(o.toString());
            }
            catch (Exception e)
            {
                try
                {
                    result = authorityService.getAuthorityNodeRef(o.toString());
                }
                catch (Exception e1)
                {
                    // do nothing
                }
            }

        }

        return result;
    }

    private Collection<NodeRef> getUserGroupRefs(Object o)
    {
        List<NodeRef> result = new ArrayList<NodeRef>();
        if (o != null && o instanceof Collection)
        {
            for (Iterator<?> it = ((Collection<?>) o).iterator(); it.hasNext();)
            {
                result.add(getUserGroupRef(it.next()));

            }
        }

        return result;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
}
