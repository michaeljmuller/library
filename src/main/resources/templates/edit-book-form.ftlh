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
    <script src="/javascript/edit-book.js"></script>
    <link rel="stylesheet" href="/styles/edit-book.css">

    <script>
        let authors = [ <#list authorList as author>"${author}"<#sep>, </#list>];
        let series = [ <#list seriesList as series>"${series}"<#sep>, </#list>];
        let bookId = ${(book.id!0)?c};

        <#-- TODO: move this to the page macro? -->
        <#if _csrf??>
            let csrfParameterName = "${_csrf.parameterName}";
            let csrfToken = "${_csrf.token}";
            let csrfHeaderName = "${_csrf.headerName}";
        <#else>
            let csrfParameterName = null;
            let csrfToken = null;
            let csrfHeaderName = null;
        </#if>

        Dropzone.autoDiscover = false;

        window.onload = function() {
            initializeAutocomplete("#author", authors);
            initializeAutocomplete("#series", series);
            initializeDropzone(bookId, csrfParameterName, csrfToken);
            convertFormToAjax("bookForm", csrfHeaderName, csrfToken, ["tags"], handleFormSubmitResponse);
        }

        function handleFormSubmitResponse(event) {

            alert(event.target.responseText);

            let response = JSON.parse(event.target.responseText);
            if (response.isSuccess) {
                alert("success!");
                windows.location.href = "/addBooks";
            }
            else {
                alert("failure; see messages at top of page");
                displayErrors(response.messages, "errors");
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

<#macro coverImage index label url>
    <div class="coverImageWrapper">
        <div class="coverImage">
            <label for="image${index}">
                <img src="${url}" />
            </label>
        </div>
        <div class="coverImageLabel">
            <input type="radio" name="coverImage" value="${label}" id="image${index}"><label for="image${index}">${label}</label>
        </div>
    </div>
</#macro>

<@c.page title="Edit Book" additionalHeadContent=hdr>

    <ul id="errors">
        <#if errors?? >
            <#list errors as error>
                <li>${error}</li>
            </#list>
        </#if>
    </ul>

    <form id="bookForm" action="${formAction}" method="post">
        <#if book.id??><#-- consider not even passing the ID; it's in the URL for edits -->
            <input type="hidden" name="bookId" value="${book.id?c}" />
        </#if>
        <#if _csrf??>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        </#if>
        <table id="fieldTable">
            <@textField name="title" label="Title" initialValue="${book.title!}" id="titleField" autofocus=true />
            <@textField name="altTitle1" initialValue="${book.altTitle1!}" />
            <@textField name="altTitle2" initialValue="${book.altTitle2!}" />
            <@textField name="author" label="Author" initialValue="${book.author!}" />
            <@textField name="author2" initialValue="${book.author2!}" />
            <@textField name="author3" initialValue="${book.author3!}" />
            <@textField name="publicationYearString" label="Pub Year" initialValue="${(book.publicationYear?c)!}" />
            <@textField name="series" label="Series" initialValue="${book.series!}" />
            <@textField name="seriesSequence" label="Series #" initialValue="${(book.seriesSequence?c)!}" />
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
            <@optionField name="epubObjectKey" label="EPUB" values=unattachedEpubs selectedValue=book.epubObjectKey! />
            <@optionField name="mobiObjectKey" label="MOBI" values=unattachedMobis selectedValue=book.mobiObjectKey! />
            <@optionField name="audiobookObjectKey" label="Audiobook" values=unattachedAudiobooks selectedValue=book.audiobookObjectKey! />
            <@textField name="amazonId" label="ASIN" initialValue="${book.amazonId!}" />
            <tr>
                <td class="labelCell tall">Cover Image:</td>
                <td>
                    <div id="coverImages">
                        <#if hasCoverImage>
                            <#assign url = "/cover?book=" + book.id?c >
                            <@coverImage 999 "Existing Cover" url />
                        </#if>
                        <#list bookImages as image>
                            <#assign url = "/epubImage?objId=" + book.epubObjectKey?url + "&file=" + image?url >
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