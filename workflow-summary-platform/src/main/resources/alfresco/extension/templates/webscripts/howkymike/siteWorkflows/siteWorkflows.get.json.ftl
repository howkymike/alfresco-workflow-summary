{"userWorkflows" : [
    <#list userWorkflows as child>
        {
            "name" : "${child}"
        }
        <#if !(child == userWorkflows?last)>,</#if>
    </#list>
    ]
}