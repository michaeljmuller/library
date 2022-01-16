<#macro books bookList>
    <div class="book-grid-container">
        <#list bookList as book>
            <div class="book-grid-item">
                <div class="cover-image-container">
                    <img src="/cover?book=${book.ebookS3ObjectKey?url}" />
                </div>
                <div class="book-info">
                    <div class="book-info-text">
                        <div class="book-info-section">
                            <div class="book-info-title">${book.title}</div>
                            <div class="book-info-author">${book.author}</div>
                            <#if book.seriesSequence gt 0>
                                <div class="book-info-series">${book.series} #${book.seriesSequence}</div>
                            </#if>
                            <div class="book-info-pub-year">${book.publicationYear?c}</div>
                        </div>
                        <div class="book-info-tags">
                            <#list book.getTags() as tag>${tag}<#sep>, </#list>
                        </div>
                        <div class="book-info-acq-date">Added ${book.acquisitionDate}</div>
                    </div>
                    <div class="book-info-links">
                        <a href="https://www.amazon.com/s?k=${book.author?url}%20${book.title?url}"><img class="book-action-icon" src="/images/amazon.png"></a>&nbsp;
                        <a href="#"><img class="book-action-icon" src="/images/download.png"></a>&nbsp;
                        <a href="#"><img class="book-action-icon" src="/images/audiobook.png"></a>
                    </div>
                </div>
            </div>
        </#list>
    </div>
</#macro>
