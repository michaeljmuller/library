<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="Mike's eBook and Audiobook Library">

    <p>Welcome to my library!  I hope you can find a good read.</p>
    <p> -- Mike</p>

    <form action="/search" method="get">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input class="search" type="text" name="for" value="${searchText!}" autofocus />
        <input class="submitButton" type="submit" value="Search" />
    </form>

    <div class="section">Recently Added</div>
    <@b.books bookList=books></@b.books>
    <div class="morebooks"><a href="/recents">More New Acquisitions &gt;&gt;</a></div>
</@c.page>