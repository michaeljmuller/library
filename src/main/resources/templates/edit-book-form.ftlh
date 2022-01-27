<#import "macros/page-macro.ftl" as c/>

<#-- https://tarekraafat.github.io/autoComplete.js/#/ -->

<#assign hdr>
    <script src="https://unpkg.com/@tarekraafat/autocomplete.js@10.2.6/dist/autoComplete.min.js"></script>
    <#--
    <link rel="stylesheet" href="https://unpkg.com/@tarekraafat/autocomplete.js@10.2.6/dist/css/autoComplete.css">
    -->
    <link rel="stylesheet" href="/styles/autocomplete.css">
    <script>
        let authors = [ <#list authorList as author>"${author}"<#sep>, </#list>];
        let series = [ <#list seriesList as series>"${series}"<#sep>, </#list>];
        window.onload = function() {
            const authorAutoComplete = new autoComplete({
                selector: "#author",
                data: {
                    src: authors,
                },
                events: {
                    input: {
                        selection: (event) => {
                            authorAutoComplete.input.value = event.detail.selection.value;
                        }
                    }
                },
            });
            const seriesAutoComplete = new autoComplete({
                selector: "#series",
                data: {
                    src: series,
                },
                events: {
                    input: {
                        selection: (event) => {
                            seriesAutoComplete.input.value = event.detail.selection.value;
                        }
                    }
                },
            });
        }
    </script>
    <style>

        select {
            font-size: 16px;
        }

        input[type=text] {
            font-size: 16px;
            padding: 2px;
            width: 600px;
            border-radius: 4px;
        }

        #tagsLabel {
            vertical-align: top;
            padding-top: 5px;
        }

        td.labelCell {
            font-weight: bold;
            text-align: right;
            padding-right: 0.5em;
        }

        label.tagName {
            padding-right: 0.5em;
        }
    </style>

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

<#macro checkbox name value isChecked id=name>
    <div>
        <input type="checkbox" id="${id}" value="${value}" <#if isChecked>checked</#if> />
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

<@c.page title="Edit Book" additionalHeadContent=hdr>
    <form action="/metadata/book/${book.id}" method="post">
        <table id="fieldTable">
            <@textField name="title" label="Title" initialValue="${book.title!}" id="titleField" autofocus=true />
            <@textField name="altTitle1" initialValue="${book.altTitle1!}" />
            <@textField name="altTitle2" initialValue="${book.altTitle2!}" />
            <@textField name="author" label="Author" initialValue="${book.author!}" />
            <@textField name="author2" initialValue="${book.author2!}" />
            <@textField name="author3" initialValue="${book.author3!}" />
            <@textField name="publicationYear" label="Publication Year" initialValue="${book.author3!}" />
            <@textField name="series" label="Series" initialValue="${book.series!}" />
            <@textField name="seriesSequence" label="Series #" initialValue="${book.seriesSequence!}" />
            <@textField name="acquisitionDate" label="Acquisition Date" initialValue="${book.acquisitionDate?string['MM/dd/yyyy']}" />

            <tr>
                <td id="tagsLabel" class="labelCell">Tags:</td>
                <td>
                    <table id="tagTable">
                        <#list tagList?chunk(3) as row>
                            <tr>
                                <#list row as tag>
                                    <td><@checkbox name="tag-row${row?index}-col${tag?index}" value="${tag}" isChecked=book.getTags()?seq_contains(tag) /></td>
                                </#list>
                            </tr>
                        </#list>
                    </table>
                </td>
            </tr>
            <@textField name="newTags" label="New Tags" />
            <@optionField name="ebookS3ObjectKey" label="EPUB Object Key" values=unattachedEpubs selectedValue=book.ebookS3ObjectKey! />
            <@optionField name="audiobookS3ObjectKey" label="Audiobook Object Key" values=unattachedAudiobooks selectedValue=book.audiobookS3ObjectKey! />
            <@textField name="asin" label="Amazon ID" initialValue="${book.amazonId!}" />
            <tr>
                <td></td>
                <td>
                    <input class="submitButton" type="submit" value="Submit" />
                </td>
            </tr>
            <input type="hidden" name="bookId" value="${book.id}" />
            <#if _csrf??>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </#if>
        </table>
    </form>
</@c.page>