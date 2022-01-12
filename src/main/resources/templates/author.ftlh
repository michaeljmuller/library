<#import "page-template.ftl" as c/>

<#macro seriesMacro seriesTitle assetsList>
    <tr class="series"><td colspan="6"><div>${seriesTitle}</div></td></tr>
    <#list assetsList?chunk(3) as row>
        <tr>
            <#list row as book>
                <td class="cover-image"><img src="/cover?book=${book.ebookS3ObjectKey?url}" /></td>
                <td class="book-info">
                    <div class="book-title">${book.title}</div>
                    <#if book.seriesSequence gt 0>
                        <div>${seriesTitle} #${book.seriesSequence}</div>
                    </#if>
                    <div>${book.publicationYear?c}</div>
                    <div>
                        <#list book.getTags() as tag>${tag}<#sep>, </#list>
                    </div>
                </td>
            </#list>
        </tr>
    </#list>
</#macro>

<@c.page title="${authorName}">

    <table class="asset-table">

        <#list groupedAssets as series, assets>
            <@seriesMacro seriesTitle="${series}" assetsList=assets></@seriesMacro>
        </#list>

        <#if standalone??>
            <@seriesMacro seriesTitle="Standalone Books" assetsList=standalone></@seriesMacro>
        </#if>
    </table>

</@c.page>