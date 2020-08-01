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