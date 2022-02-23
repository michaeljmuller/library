<#import "macros/page-macro.ftl" as c/>
<#import "macros/book-grid-macro.ftl" as b/>

<#assign hdr>
    <script>
        function onOrderChange(select) {
            window.location.href = "/audiobooks?order=" + select.value;
        }
    </script>
    <style>
        #orderDropList {
            font-size: 16px;
            float: right;
        }
    </style>
</#assign>

<@c.page title="Audiobooks" additionalHeadContent=hdr>

    <div class="section">
        Audiobooks ${firstBookNum} - ${lastBookNum}
        <select id="orderDropList" onchange="onOrderChange(this)">
            <#list orderOptions?keys?sort as orderOption>
                <option value="${orderOption}" <#if (orderOption == order)>selected</#if>>
                    Sort by ${orderOptions[orderOption]}
                </option>
            </#list>
        </select>
    </div>

    <@b.books bookList=books />

    <#if hasMore>
        <div class="rightLink"><a href="/audiobooks?page=${nextPage?c}">Page ${nextPage} of audiobooks &gt;&gt;</a></div>
    </#if>
</@c.page>