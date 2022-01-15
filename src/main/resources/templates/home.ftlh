<#import "page-template.ftl" as c/>

<@c.page title="Mike's eBook and Audiobook Library">

    <p>Welcome to my library!  I hope you can find a good read.</p>
    <p> -- Mike</p>

    <div class="section">Recently Added</div>
    <table class="asset-table">
        <#list assets?chunk(3) as row>
            <tr>
                <#list row as book>
                    <td class="cover-image"><img src="/cover?book=${book.ebookS3ObjectKey?url}" /></td>
                    <td class="book-info">
                        <div class="book-title">${book.title}</div>
                        <div>${book.author}</div>
                        <#if book.seriesSequence gt 0>
                            <div>${book.series} #${book.seriesSequence}</div>
                        </#if>
                        <div class="pub-year">${book.publicationYear?c}</div>
                        <div>
                            <#list book.getTags() as tag>${tag}<#sep>, </#list>
                        </div>
                        <div>Added ${book.acquisitionDate}</div>
                        <div class="links">
                            <a href="#"><img src="/images/amazon.png"></a>&nbsp;
                            <a href="#"><img src="/images/download.png"></a>&nbsp;
                            <a href="#"><img src="/images/audiobook.png"></a>
                        </div>
                    </td>
                </#list>
            </tr>
        </#list>
    </table>
    <div class="morebooks"><a href="/recents">More recently added books...</a></div>
</@c.page>