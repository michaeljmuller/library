<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="${authorName}">

    <table class="asset-table">

        <#list groupedAssets as series, assets>
            <div class="section">${series}</div>
            <@b.books bookList=assets></@b.books>
        </#list>

        <#if standalone??>
            <div class="section">Standalone Books</div>
            <@b.books bookList=standalone></@b.books>
        </#if>
    </table>

</@c.page>