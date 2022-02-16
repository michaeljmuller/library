<#import "macros/page-macro.ftl" as c/>
<@c.page title="Authors">

    <#assign reverse = (sortBy=="last")?then("first","last") />

    <table>
        <#list authorInfoMap as letter, authorInfoList >

            <tr>
                <td class="section" colspan="3">
                    <div>
                        ${letter}
                        <#if letter=="A">
                            <a class="sortLink" href="/authors?sortBy=${reverse}">Change: sort by ${reverse}</a>
                        </#if>
                    </div>
                </td>
            </tr>
            <#list authorInfoList as authorInfo>
                <#assign name = (sortBy=="last")?then(authorInfo.byLast,authorInfo.name) />
                <#if name?upper_case?starts_with(letter)>
                    <tr>
                        <td nowrap><a href="/author?name=${authorInfo.name?url}">${name}</a><!--</td>
                        <td nowrap> -->, ${authorInfo.numTitles} book<#if authorInfo.numTitles gt 1>s</#if></td>
                        <td><#list authorInfo.tags as tag>${tag}<#sep>, </#list></td>
                    </tr>
                </#if>
            </#list>
        </#list>
    </table>

</@c.page>