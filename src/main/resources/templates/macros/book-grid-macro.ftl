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
                                <div class="book-info-title">${book.title}</div>
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
                            <div class="book-info-acq-date">Added ${book.acquisitionDate}</div>
                            <#if book.audiobookObjectKey??>
                                <img class="audiobook-indicator" src="/images/audiobook.png" title="Audiobook is available for this title">
                            </#if>
                        </div>
                    </div>
                </div>
            </a>
        </#list>
    </div>
</#macro>
