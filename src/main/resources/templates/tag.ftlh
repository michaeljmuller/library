<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="Tag: ${tag}">

    <#if (page gt 1) || hasMore >
        <div class="section">Books ${firstBookNum} - ${lastBookNum} tagged ${tag}</div>
    </#if>

    <@b.books bookList=books />

    <#if hasMore>
        <div class="rightLink"><a href="/tag/${tag?url}?page=${nextPage?c}">Page ${nextPage} of ${tag} &gt;&gt;</a></div>
    </#if>
</@c.page>