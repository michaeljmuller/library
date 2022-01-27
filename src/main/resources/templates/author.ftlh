<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="${authorName}">

    <table class="book-table">

        <#list groupedBooks as series, books>
            <div class="section">${series}</div>
            <@b.books bookList=books></@b.books>
        </#list>

        <#if standalone??>
            <div class="section">Standalone Books</div>
            <@b.books bookList=standalone></@b.books>
        </#if>
    </table>

</@c.page>