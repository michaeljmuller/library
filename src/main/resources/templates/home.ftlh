<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="Mike's Library" tabTitle="Mike's Library">

    <div class="stats">Mike's library currently has ${titleCount} books and ${audiobookCount} audiobooks by ${authorCount} different authors.</div>

    <form action="/search" method="get">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input class="search" type="text" name="for" value="${searchText!}" autofocus />
        <input class="submitButton" type="submit" value="Search" />
    </form>

    <div class="section">Recently Added</div>
    <@b.books bookList=books />
    <div class="rightLink"><a href="/recents">More New Acquisitions &gt;&gt;</a></div>
</@c.page>