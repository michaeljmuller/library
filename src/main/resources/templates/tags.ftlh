<#import "macros/page-macro.ftl" as c/>

<@c.page title="Tags">
    <table>
        <#list tags as tag,count>
            <tr>
                <td><a href="/tag/${tag?url}">${tag}</a></td>
                <td>${count}</td>
            </tr>
        </#list>
    </table>
</@c.page>