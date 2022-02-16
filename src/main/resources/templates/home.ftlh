<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="Mike's Library" tabTitle="Mike's Library">

    <div class="stats">
        <p>Mike's library currently has ${titleCount} books and ${audiobookCount} audiobooks by ${authorCount} different authors.</p>
        <p>The source code for this site is on <a href="https://github.com/michaeljmuller/library" target="_blank">github</a>.</p>
    </div>

    <form action="/search" method="get">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input class="search" type="text" name="for" value="${searchText!}" autofocus />
        <input class="submitButton" type="submit" value="Search" />
    </form>

    <div class="section">Recently Added</div>
    <@b.books bookList=books />
    <div class="rightLink"><a href="/recents">More New Acquisitions &gt;&gt;</a></div>
</@c.page>