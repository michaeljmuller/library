<#macro books bookList>
    <div class="book-grid-container">
        <#list bookList as book>
            <a href="/book/${book.id?c}" style="display: block; text-decoration: none;">
                <div class="book-grid-item">
                    <div class="cover-image-container">
                        <img src="/book/cover/${book.id?c}" />
                    </div>
                    <div class="book-info">
                        <div class="book-info-text">
                            <div class="book-info-section">
                                <div class="book-info-title">
                                    ${book.title}
                                    <#if book.audiobookObjectKey??> 🔊</#if>
                                </div>
                                <div class="book-info-author">${book.author}</div>
                                <#if book.seriesSequence?default(0) gt 0>
                                    <div class="book-info-series">${book.series} #${book.seriesSequence}</div>
                                </#if>
                                <div class="book-info-pub-year">${book.publicationYear?c}</div>
                            </div>
                            <#if book.getTags()?size gt 0>
                                <div class="book-info-tags">
                                    <#list book.getTags() as tag>${tag}<#sep>, </#list>
                                </div>
                            </#if>
                            <#if book.avgRating??>
                                <div class="book-info-avg-rating">
                                    <#switch book.avgRating>
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

                                </div>
                            </#if>
                            <div class="book-info-acq-date">Added ${book.acquisitionDate}</div>
                        </div>
                    </div>
                </div>
            </a>
        </#list>
    </div>
</#macro>
