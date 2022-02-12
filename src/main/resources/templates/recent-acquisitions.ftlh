<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="Recent Acquisitions">
<#--
    <div class="section">Recently Added</div>
    -->
    <@b.books bookList=books />
    <div class="morebooks"><a href="/recents">More &gt;&gt;</a></div>
</@c.page>