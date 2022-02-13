<#import "macros/page-macro.ftl" as c/>

<#assign hdr>
    <style>
        .book-details {
            display: flex;
            flex-flow: row wrap;
            justify-content: left;
            gap: 10px;
        }

        .book-cover-image, .book-info-text {
            min-width: 500px;
            flex-basis: 0;
            flex: 1 1 0px;
        }

        .book-cover-image img {
            max-height: 100%;
            max-width: 100%;
            border-radius: 20px;
        }

        .book-info-section {
            margin-bottom: 1em;
        }

        img.link-icon {
            width: 3em;
            height: auto;
        }

        .icon-and-link {
            display: flex;
            align-items: center;
        }
    </style>
</#assign>

<#macro link text url iconUrl test newTab=false>
    <#if test>
        <div class="icon-and-link">
            <a href="${url}" <#if newTab>target="_blank"</#if>><img class="link-icon" src="${iconUrl}" /></a>
            &nbsp;
            <a href="${url}" <#if newTab>target="_blank"</#if>>${text}</a>
        </div>
    </#if>
</#macro>

<@c.page title="${book.title}" additionalHeadContent=hdr>
    <div class="book-details">
        <div class="book-info-text">
            <div class="book-info-section">
                <div class="book-info-title">${book.title}</div>
                <#if book.series??>
                    <div class="book-info-series">${book.series} #${book.seriesSequence!}</div>
                </#if>
                <div><a href="/author?name=${book.author?url}">${book.author}</a><#if book.author2??>, ${book.author2}</#if><#if book.author3??>, ${book.author3}</#if></div>
                <div class="book-info-pub-year">${book.publicationYear?c}</div>
            </div>
            <#if book.getTags()?size gt 0>
                <div class="book-info-section book-info-tags">
                    <#list book.getTags() as tag>${tag}<#sep>, </#list>
                </div>
            </#if>
            <div class="book-info-section book-info-acq-date">Added ${book.acquisitionDate}</div>
            <div class="book-info-section">

                <#-- these two assigns are messed up, but if I try to pass !(book.amazonId??) or even !hasAmazonId to the macro, it doesn't parse -->
                <#assign hasAmazonId = book.amazonId?? />
                <#assign missingAmazonId = !(book.amazonId??) />

                <@link "This book on Amazon" "https://www.amazon.com/dp/${book.amazonId!}" "/images/amazon.png" hasAmazonId true />
                <@link "Search Amazon for this book" "https://www.amazon.com/s?k=${book.author?url}%20${book.title?url}" "/images/amazon.png" missingAmazonId true />
                <@link "Download EPUB" "/book/epub/${book.id?c}" "/images/download.png" book.epubObjectKey?? />
                <@link "Download MOBI" "/book/mobi/${book.id?c}" "/images/download.png" book.mobiObjectKey?? />
                <@link "Download M4B Audiobook" "/book/m4b/${book.id?c}" "/images/audiobook.png" book.audiobookObjectKey?? />
                <@link "Edit Book Metadata" "/editBook/${book.id?c}" "/images/edit.png" isAdmin />
            </div>
        </div>
        <div class="book-cover-image">
            <img src="/book/cover/${book.id?c}">
        </div>
    </div>
</@c.page>