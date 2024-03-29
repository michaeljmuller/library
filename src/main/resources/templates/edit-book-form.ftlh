<#import "macros/page-macro.ftl" as c/>

<#-- https://tarekraafat.github.io/autoComplete.js/#/ -->
<#assign hdr>
    <#-- links to autocomplete scripts and styles -->
    <script src="https://unpkg.com/@tarekraafat/autocomplete.js@10.2.6/dist/autoComplete.min.js"></script>
    <link rel="stylesheet" href="/styles/autocomplete.css">

    <#-- links to dropzone scripts and styles -->
    <link rel="stylesheet" href="https://unpkg.com/dropzone@5/dist/min/dropzone.min.css" type="text/css" />
    <script src="https://unpkg.com/dropzone@5/dist/min/dropzone.min.js"></script>

    <#-- links to page-specific scripts and styles -->
    <script src="/javascript/ajax-form.js"></script>
    <script src="/javascript/edit-book.js"></script>
    <link rel="stylesheet" href="/styles/edit-book.css">

    <script>
        let authors = [ <#list authorList as author>"${author}"<#sep>, </#list>];
        let series = [ <#list seriesList as series>"${series}"<#sep>, </#list>];
        let bookId = ${(book.id!0)?c};

        Dropzone.autoDiscover = false;

        window.onload = function() {
            initializeAutocomplete("#author", authors);
            initializeAutocomplete("#series", series);
            initializeDropzone(bookId, csrfParameterName, csrfToken);
            convertFormToAjax(document.getElementById("bookForm"), csrfHeaderName, csrfToken, ["tags"], handleFormSubmitResponse);
        }

        function handleFormSubmitResponse(event) {

            let response = parseResponse(event.target.responseText);
            if (response.success) {
                alert("Book information saved.");
                window.location.href = "${referrer!"/"}";
            }
            else {
                alert("failure; see messages at top of page");
                displayErrors(response.errorMessages, "errors");
            }
        }
    </script>
</#assign>

<#macro textField name label="" initialValue="" id=name autofocus=false>
    <tr>
        <td class="labelCell">
            <label for="${id}">
                <#if label != "">${label}: </#if>
            </label>
        </td>
        <td>
            <input id="${id}" type="text" name="${name}" value="${initialValue}" <#if autofocus>autofocus</#if> />
        </td>
    </tr>
</#macro>

<#macro checkbox name id value isChecked>
    <div>
        <input type="checkbox" id="${id}" name="${name}" value="${value}" <#if isChecked>checked</#if> />
        <label class="tagName" for="${id}">${value}</label>
    </div>
</#macro>

<#macro optionField name values label="" selectedValue="" id=name>
    <tr>
        <td class="labelCell">
            <label for="${id}">
                <#if label != "">${label}: </#if>
            </label>
        </td>
        <td>
            <select id="${id}" name="${name}">
                <option value="" <#if selectedValue == "">selected</#if>>(none)</option>
                <#list values as value>
                    <option value="${value}" <#if value == selectedValue>selected</#if>>${value}</option>
                </#list>
            </select>
        </td>
    </tr>
</#macro>

<#macro coverImage index label url checked=false>
    <div class="coverImageWrapper">
        <div class="coverImage">
            <label for="image${index}">
                <img src="${url}" />
            </label>
        </div>
        <div class="coverImageLabel">
            <input type="radio" name="coverImage" value="${label}" id="image${index}" <#if checked>checked</#if>><label for="image${index}">${label}</label>
        </div>
    </div>
</#macro>

<@c.page title=(operation == "add")?then("Add Book", "Edit Book") additionalHeadContent=hdr>

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
    <form id="bookForm" action="${formAction}" data-ajax-method="<#if operation == "add">post<#else>put</#if>">
        <#if book.id??><#-- consider not even passing the ID; it's in the URL for edits -->
            <input type="hidden" name="bookId" value="${book.id?c}" />
        </#if>
        <table id="fieldTable">
            <@optionField name="epubObjectKey" label="EPUB" values=unattachedEpubs selectedValue=book.epubObjectKey! />
            <@optionField name="mobiObjectKey" label="MOBI" values=unattachedMobis selectedValue=book.mobiObjectKey! />
            <@optionField name="audiobookObjectKey" label="Audiobook" values=unattachedAudiobooks selectedValue=book.audiobookObjectKey! />
            <@textField name="title" label="Title" initialValue="${book.title!}" id="titleField" autofocus=true />
            <@textField name="altTitle1" initialValue="${book.altTitle1!}" />
            <@textField name="altTitle2" initialValue="${book.altTitle2!}" />
            <@textField name="author" label="Author" initialValue="${book.author!}" />
            <@textField name="author2" initialValue="${book.author2!}" />
            <@textField name="author3" initialValue="${book.author3!}" />
            <@textField name="publicationYearString" label="Pub Year" initialValue="${(book.publicationYear?c)!}" />
            <@textField name="series" label="Series" initialValue="${book.series!}" />
            <@textField name="seriesSequenceString" label="Series #" initialValue="${(book.seriesSequence?c)!}" />
            <@textField name="acquisitionDateString" label="Acq Date" initialValue="${(book.acquisitionDate?string['MM/dd/yyyy'])!}" />
            <tr>
                <td class="labelCell tall">Tags:</td>
                <td>
                    <table id="tagTable">
                        <#list tagList?chunk(3) as row>
                            <tr>
                                <#list row as tag>
                                    <td><@checkbox name="tags" id="tag-row${row?index}-col${tag?index}" value="${tag}" isChecked=book.getTags()?seq_contains(tag) /></td>
                                </#list>
                            </tr>
                        </#list>
                    </table>
                </td>
            </tr>
            <@textField name="newTags" label="New Tags" />
            <@textField name="amazonId" label="ASIN" initialValue="${book.amazonId!}" />
            <tr>
                <td class="labelCell tall">Cover Image:</td>
                <td>
                    <div id="coverImages">
                        <#if hasCoverImage>
                            <#assign url = "/book/cover/" + book.id?c >
                            <@coverImage 999 "Existing Cover" url true/>
                        </#if>
                        <#list bookImages as image>
                            <#assign url = "/epubImage?epubObjKey=" + book.epubObjectKey?url + "&file=" + image?url >
                            <@coverImage image?index image url />
                        </#list>
                        <div id="dz-wrapper">
                            <div id="dz" class="dropzone"></div>
                        </div>
                    </div>
                </td>
            </tr>
            <tr>
                <td></td>
                <td>
                    <input class="submitButton" type="submit" value="Submit" />
                </td>
            </tr>
        </table>
    </form>

</@c.page>