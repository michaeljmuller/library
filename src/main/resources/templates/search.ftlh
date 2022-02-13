<#import "macros/page-macro.ftl" as c/>

<#assign hdr>
    <style>
        div.category {
            padding-top: 1em;
            font-weight: bold;
        }
    </style>
</#assign>

<#assign pageTitle>Search<#if searchText??> for "${searchText}"</#if></#assign>

<@c.page title="${pageTitle}" additionalHeadContent=hdr>

    <#if searchText?? && (authors!?size = 0) && (titles!?size == 0) >
        <div class="message">No matches found for *${searchText}* in the library's titles or authors.</div>
    </#if>

    <form action="/search" method="get">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        <input class="search" type="text" name="for" value="${searchText!}" autofocus />
        <input class="submitButton" type="submit" value="Search" />
    </form>

    <#if titles!?size gt 0 >
        <div class="category-wrapper">
            <div class="category">TITLES:</div>
            <div class="results">
                <#list titles as book>
                    <div><a href="/book/${book.id?c}">${book.title}</a> (${book.author})</div>
                </#list>
            </div>
        </div>
    </#if>

    <#if seriesMap!?size gt 0 >
        <div class="category-wrapper">
            <div class="category">SERIES:</div>
            <div class="results">
                <#list seriesMap as series, authors>
                    <div>
                        ${series}
                        <#list authors as author>
                            <a href="/author?name=${author?url}">${author}</a><#sep>,
                        </#list>
                    </div>
                </#list>
            </div>
        </div>
    </#if>

    <#if authors!?size gt 0 >
        <div class="category-wrapper">
            <div class="category">AUTHORS:</div>
            <div>
                <#list authors as author>
                    <div><a href="/author?name=${author?url}">${author}</a></div>
                </#list>
            </div>
        </div>
    </#if>

</@c.page>