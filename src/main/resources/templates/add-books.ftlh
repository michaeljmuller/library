<#import "macros/page-macro.ftl" as c/>

<@c.page title="Add Books">
    The following EBPUBs have been uploaded, but have not yet been added to the library.

    <ul>
        <#list epubs as epub>
            <li><a href="/addBook?epub=${epub?url}">${epub}</a></li>
        </#list>
    </ul>

</@c.page>
