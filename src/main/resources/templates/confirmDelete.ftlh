<#import "macros/page-macro.ftl" as c/>

<@c.page title="Book ${bookId?c} deleted">

    <div>Deleted book with id ${bookId?c} from the database.</div>

    <#list deletedAssets as asset>
        <div>Deleted asset from the object store: ${asset}</div>
    </#list>

</@c.page>