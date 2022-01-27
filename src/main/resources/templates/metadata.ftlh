<#import "macros/page-macro.ftl" as c/>
<@c.page title="Metadata">

    <div class="message"><span class="status-${status!}">${msg!}</span></div>

    <div class="section">Download Metadata</div>

    <form method="GET" action="/ss">
        <input class="submitButton" type="submit" value="Download Metadata Spreadsheet" />
    </form>

    <div class="section">Upload Metadata</div>

    <form method="POST" enctype="multipart/form-data" action="/ss<#if _csrf??>?${_csrf.parameterName}=${_csrf.token}</#if>">
        <#if _csrf??>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        </#if>
        <div><input type="file" name="file" /></div>
        <div><input class="submitButton" type="submit" value="Upload Metadata" /></div>
    </form>

    <#if stackDump?? && stackDump?has_content>
        <pre class="stackDump">${stackDump}</pre>
    </#if>
</@c.page>