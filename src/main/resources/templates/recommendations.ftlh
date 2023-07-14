<#import "macros/page-macro.ftl" as c/>
<#import "macros/recommendations-macro.ftl" as r/>

<#assign hdr>
    <script>
        function onUserChange(select) {
            window.location.href = "/recommendations/" + select.value;
        }
    </script>
    <style>
        #userFilterDropList {
            font-size: 16px;
            float: right;
        }
    </style>
</#assign>

<@c.page title="Recommendations" additionalHeadContent=hdr>

    <div class="section">
        Recommendations from ${userName} <#if page gt 1>Page ${page}</#if>

        <select id="userFilterDropList" onchange="onUserChange(this)">
            <option value="">Recommendations from everyone</option>
            <#list users as user>
                <option value="${user.id}" <#if (user.id == userId)>selected</#if>>
                    Recommendations from ${user.firstName} ${user.lastName}
                </option>
            </#list>
        </select>

    </div>

    <@r.recommendations recoList=recommendations />

    <div class="rightLink">
        <#if hasMore>
            <a href="/recommendations?page=${nextPage?c}">Page ${nextPage} of Recommendations &gt;&gt;</a>
        <#else>
            No more recommendations.
        </#if>
    </div>
</@c.page>