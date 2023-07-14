<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="Recent Acquisitions">

    <div class="section"><#if page gt 1>Page ${page}: </#if>${firstBookDate} &mdash; ${lastBookDate}</div>

    <@b.books bookList=books />

    <div class="rightLink">
        <#if hasMore>
            <a href="/recents?page=${nextPage?c}">Page ${nextPage} of Books by Acquisition Date &gt;&gt;</a></div>
        <#else>
            No more books.
        </#if>
</@c.page>