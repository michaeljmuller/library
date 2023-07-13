<#import "macros/page-macro.ftl" as c/>

<#assign hdr>

    <#-- links to page-specific scripts and styles -->
    <script src="/javascript/ajax-form.js"></script>

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

        details {
            margin-top: 5px;
        }

        summary {
            cursor: pointer;
        }

        textarea {
            width: 100%;
            max-width: 100%;
            min-width: 100%;
            height: 10em;
            font-family: 'Open Sans', sans-serif;
            font-size: 16px;
        }

        label {
            font-weight: bold;
        }

    </style>
    <script>
        window.onload = function() {
            convertFormToAjax(document.getElementById("reviewForm"), csrfHeaderName, csrfToken, [], handleFormSubmitResponse);
        }

        function handleFormSubmitResponse(event) {
            let response = parseResponse(event.target.responseText);
            if (response.success) {
                alert("Review saved.");
                window.location.href = "${referrer!"/"}";
            }
            else {
                alert("failure; see messages at top of page");
                displayErrors(response.errorMessages, "errors");
            }
        }

        function displayErrors(errors, errorListElementId) {
            let errorListElement = document.getElementById(errorListElementId);
            removeAllChildren(errorListElement);

            for (const error of errors) {
                let listItemElement = document.createElement("li");
                listItemElement.appendChild(document.createTextNode(error));
                errorListElement.appendChild(listItemElement);
            }

            // scroll to the top of the page -- https://stackoverflow.com/a/4210821
            document.body.scrollTop = document.documentElement.scrollTop = 0;
        }


        function removeAllChildren(parent) {
            var child = parent.lastElementChild;
            while (child) {
                parent.removeChild(child);
                child = parent.lastElementChild;
            }
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

<@c.page title="Review: ${book.title}" additionalHeadContent=hdr>
    <div class="book-details">
        <div class="book-info-text">
            <div class="book-info-section">
                <div class="book-info-title">${book.title}</div>
                <#if book.series??>
                    <div class="book-info-series">${book.series} #${book.seriesSequence!}</div>
                </#if>
                <div>${book.author}<#if book.author2??>, ${book.author2}</#if><#if book.author3??>, ${book.author3}</#if></div>
                <div class="book-info-pub-year">${book.publicationYear?c}</div>
            </div>
            <div class="book-info-section">
                <div class="review">

                    <ul id="errors">
                        <#if errors?? >
                            <#list errors as error>
                                <li>${error}</li>
                            </#list>
                        </#if>
                    </ul>

                    <#--
                    HTML forms can't submit with PUT, but we're using AJAX to submit this form,
                    the javascrpt that submits the form will inspect the data-ajax-method to
                    determine whether to GET/POST/PUT the form.  See: convertFormToAjax()
                    -->
                    <form id="reviewForm" action="/api/review" data-ajax-method="post">
                        <label for="rating">Rating: </label>
                        <select id="rating" name="rating">
                            <option value="-1"></option>
                            <option value="0" <#if ((review.rating)?? && (review.rating == 0)) >selected</#if>>Did Not Finish</option>
                            <option value="1" <#if ((review.rating)?? && (review.rating == 1)) >selected</#if>>Zero Stars</option>
                            <option value="2" <#if ((review.rating)?? && (review.rating == 2)) >selected</#if>>⭐</option>
                            <option value="3" <#if ((review.rating)?? && (review.rating == 3)) >selected</#if>>⭐½</option>
                            <option value="4" <#if ((review.rating)?? && (review.rating == 4)) >selected</#if>>⭐⭐</option>
                            <option value="5" <#if ((review.rating)?? && (review.rating == 5)) >selected</#if>>⭐⭐½</option>
                            <option value="6" <#if ((review.rating)?? && (review.rating == 6)) >selected</#if>>⭐⭐⭐</option>
                            <option value="7" <#if ((review.rating)?? && (review.rating == 7)) >selected</#if>>⭐⭐⭐½</option>
                            <option value="8" <#if ((review.rating)?? && (review.rating == 8)) >selected</#if>>⭐⭐⭐⭐</option>
                            <option value="9" <#if ((review.rating)?? && (review.rating == 9)) >selected</#if>>⭐⭐⭐⭐½</option>
                            <option value="10" <#if ((review.rating)?? && (review.rating == 10)) >selected</#if>>⭐⭐⭐⭐⭐</option>
                        </select>
                        <div>
                            <label for="review">Review: </label>
                            <br />
                            <textarea id="review" name="review" wrap="soft" placeholder="Reviews, spoilers, and private notes are formatted with Markdown.">${(review.review)!}</textarea>
                        </div>
                        <div>
                            <label for="spoilers">Spoilers: </label>
                            <br />
                            <textarea id="spoilers" name="spoilers" wrap="soft" placeholder="If you've got comments that might spoil the experience for first-time readers of this book, place them here; these comments will be hidden by default.">${(review.spoilers)!}</textarea>
                        </div>
                        <div>
                            <label for="privateNotes">Private Notes: </label>
                            <br />
                            <textarea id="privateNotes" name="privateNotes" wrap="soft" placeholder="These notes won't be shared with other users.  I intend to use this to remind myself what's going on in long-running series.">${(review.privateNotes)!}</textarea>
                        </div>
                        <div>
                            <label for="isRecommended">Add this book to my list of recommendations:</label> <input type="checkbox" id="isRecommended" name="recommended" value="true" <#if ((review.recommended)?? && (review.recommended))>checked</#if>>
                        </div>
                        <div>
                            <input class="submitButton" type="submit" value="Submit" />
                        </div>
                        <input type="hidden" name="bookId" value="${book.id?c}" />
                    </form>
                </div>
            </div>
        </div>
        <div class="book-cover-image">
            <img src="/book/cover/${book.id?c}">
        </div>
    </div>
</@c.page>