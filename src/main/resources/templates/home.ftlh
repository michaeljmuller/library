<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="Mike's eBook and Audiobook Library">

    <p>Welcome to my library!  I hope you can find a good read.</p>
    <p> -- Mike</p>

    <div class="section">Recently Added</div>
    <@b.books bookList=books></@b.books>
    <div class="morebooks"><a href="/recents">More &gt;&gt;</a></div>
</@c.page>