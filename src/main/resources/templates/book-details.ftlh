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

        .book-info-acq-date {
            margin-top: 10px;
        }

        img.link-icon {
            width: 3em;
            height: auto;
        }

        .icon-and-link {
            display: flex;
            align-items: center;
        }

        .review {
            margin-bottom: 20px;
        }

        .reviewUser, .rating {
            font-weight: bold;
        }

        .reviewText {
            padding-left: 10px;
        }

        details {
            margin-top: 5px;
        }

        summary {
            cursor: pointer;
        }

        .spoilers {
            background-color: #eeeeee;
            padding: 5px;
        }

    </style>
    <script>
        function rateBook(ratingElement) {
            let rating = ratingElement.value;
            alert(rating);
            let formData = new FormData();
            formData.set("rating", rating);
            formData.set(csrfParameterName, csrfToken);
            let xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() {
                if (xhr.readyState == 4 && xhr.status != 200) {
                    alert("error submitting rating!");
                }
            };
            xhr.open("POST", "book/rating");
            xhr.send(formData);
        }
    </script>
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
            <div class="book-info-section">
                <#list reviews as review>
                    <div class="review">
                        <span class="reviewUser">${review.user.firstName}: </span>
                        <span class="rating">
                            <#switch review.rating>
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
                        <#if review.user.id == thisUsersId>
                            <a href="/review/${book.id?c}">edit review</a>
                        </#if>
                        <div class="reviewText">
                            ${review.reviewHtml?no_esc}
                            <#if review.hasSpoilers() >
                                <details>
                                    <summary>Spoilers (click to show/hide)</summary>
                                    <div class="spoilers">${review.spoilersHtml?no_esc}</div>
                                </details>
                            </#if>
                            <#if review.user.id == thisUsersId && review.hasPrivateNotes() >
                                <div>
                                    <p>Your notes:</p>
                                    ${review.privateNotesHtml?no_esc}
                                </div>
                            </#if>
                        </div>
                    </div>
                </#list>
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
                    <@link "Edit Book Metadata" "/admin/editBook/${book.id?c}" "/images/edit.png" isAdmin />
                    <@link "Delete this Book" "/admin/deleteBookForm/${book.id?c}" "/images/delete.png" isAdmin />
                    <@link "Review this Book" "/review/${book.id?c}" "/images/star.png" true />
                </div>
            </div>
        </div>
        <div class="book-cover-image">
            <img src="/book/cover/${book.id?c}">
        </div>
    </div>
</@c.page>