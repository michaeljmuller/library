<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<@c.page title="Mike's Library" tabTitle="Mike's Library">

    <div class="stats">
        <p>Mike's library currently has ${titleCount} books and ${audiobookCount} audiobooks by ${authorCount} different authors.</p>
        <p>Please do not share these books with others, and certainly do not upload them to someplace publicly accessible.</p>
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

    <div class="section">Recommendations</div>

    <#list recs as rec>
        <a href="/book/${rec.book().id?c}" style="display: block; text-decoration: none;">
            <div class="rec-container rec-clearfix">
                <div class="rec-cover-div">
                    <img src="/book/cover/${rec.book().id?c}" />
                </div>
                <div class="rec-text">
                    <div><span class="rec-title">${rec.book().title}</span> by ${rec.book().author}</div>
                    <div>
                        <span class="rec-user">Recommended by ${rec.review().user.firstName}: </span>
                        <span class="rating">
                            <#switch rec.review().rating>
                                <#case 0>Did not finish<#break>
                                <#case 1>Zero Stars<#break>
                                <#case 2>⭐<#break>
                                <#case 3>⭐½<#break>
                                <#case 4>⭐⭐<#break>
                                <#case 5>⭐⭐½<#break>
                                <#case 6>⭐⭐⭐<#break>
                                <#case 7>⭐⭐⭐½<#break>
                                <#case 8>⭐⭐⭐⭐<#break>
                                <#case 9>⭐⭐⭐⭐½<#break>
                                <#case 10>⭐⭐⭐⭐⭐<#break>
                            </#switch>
                        </span>
                    </div>
                    <div>${rec.review().reviewHtml?no_esc ! }</div>
                </div>
            </div>
        </a>
    </#list>

    <div class="rightLink" id="recommendationSection"><a href="/recents">More Recommendations &gt;&gt;</a></div>

</@c.page>