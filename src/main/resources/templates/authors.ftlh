<#import "macros/page-macro.ftl" as c/>
<@c.page title="Authors">

    <table class="author-table">
        <#list authors?chunk(3) as row>
            <tr>
                <#list row as author>
                    <td><a href="/author?name=${author?url}">${author}</a></td>
                </#list>
            </tr>
        </#list>
    </table>

</@c.page>