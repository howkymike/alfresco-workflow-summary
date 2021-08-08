/**
 * HOWKY root namespace.
 * 
 * @namespace HOWKY
 */
if (typeof HOWKY == "undefined" || !HOWKY) {
    var HOWKY = {};
    /* global var to Define panel handlers */
    var parent = {};
}

/* check if a list contains an element */
function contains(a, obj) {
    for (var i = 0; i < a.length; i++) {
        if (a[i] === obj) {
            return true;
        }
    }
    return false;
}
/**
 * HOWKY dashlet namespace.
 * 
 * @namespace HOWKY.dashlet
 */
if (typeof HOWKY.dashlet == "undefined" || !HOWKY.dashlet) {
    HOWKY.dashlet = {};
}
/**
 *  dashboard Workflow Reporting component.
 * 
 * @namespace HOWKY.dashlet
 * @class HOWKY.dashlet.Reporting
 * @howkymike
 */
(function () {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom
        , Event = YAHOO.util.Event;

    /**
     * Dashboard Reporting constructor.
     * @param {String} htmlId The HTML id of the parent element
     * @return {HOWKY.dashlet.Reporting} The new component instance
     * @constructor
     */
    HOWKY.dashlet.Reporting = function Reporting_constructor(htmlId) {
        /* Define panel handlers */
        parent = this;
        HOWKY.dashlet.Reporting.superclass.constructor.call(this, "HOWKY.dashlet.Reporting", htmlId);
        return this;
    };
    /**
     * Extends from Alfresco.component.Base
     */
    YAHOO.extend(HOWKY.dashlet.Reporting, Alfresco.component.Base, {
        /**
         * Object container and initialization
         * 
         * @property options
         * @type object
         */
        options: {
			 /**
             *selected workflow definition
             *@type String
             */
            workflow_definition : "",
			isAuthenticated: ""
          }
    });
})();

function processDashlet_onReassignActionClick(e)
{
	// Cancel other onclick events
	e.stopPropagation();

    var actionUrl = Alfresco.constants.URL_SERVICECONTEXT + "modules/howkymike/processdashlet/reassign/" + encodeURIComponent(Date.now());

    if (!this.reassignDialog)
    {
        this.reassignDialog = new Alfresco.module.SimpleDialog(this.id + "-reassignDialog").setOptions(
        {
            width: "30em",
            templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/howkymike/processdashlet/reassign",
            actionUrl: actionUrl,
            onSuccess:
            {
                fn: function processDashlet_onReassignAction_callback(response)
        		{
                    var obj = response.json;

                    // Send PUT changeAssigne request
                    $.ajax({
                        url: Alfresco.constants.PROXY_URI + "howkymike/processdashlet/changeassignee/" + this.dataset.taskId,
                        type: 'PUT',
                        processData: false,
                        "contentType": 'application/json',
                        dataType: "json",
                        data: JSON.stringify({"assignee": obj.assignee}),
                        success: function () {
                        	//console.log("TODO");
                        }
                    });

                    // Update dashlet body with new values
                    $(this).closest("tr")[0].children[6].innerText=obj.assignee;
                },
                scope: this
            },

            doSetupFormsValidation:
            {
                fn: function processDashlet_onReassignAction_doSetupForm_callback(form)
            	{
                      form.addValidation(this.reassignDialog.id + "-assignee", Alfresco.forms.validation.mandatory, null, "keyup");
                },
                scope: this
            }
        });
    }
    
    this.reassignDialog.setOptions(
    {
        actionUrl: actionUrl,
        siteId: "testSiteid",
        containerId: "testcontainerId"
    }).show();
}
