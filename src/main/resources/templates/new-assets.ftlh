<#import "macros/page-macro.ftl" as c/>

<#assign hdr>

    <#-- links to autocomplete scripts and styles -->
    <script src="https://unpkg.com/@tarekraafat/autocomplete.js@10.2.6/dist/autoComplete.min.js"></script>
    <link rel="stylesheet" href="/styles/autocomplete.css">

    <#-- links to this page's scripts and styles -->
    <script src="/javascript/ajax-form.js"></script>

    <script>
        let books = new Array(<#list books as book>"${book}"<#sep>, </#list>);

        function initializeAutocomplete(elementId, data) {

            let ac = new autoComplete({
                selector: elementId,
                data: {
                    src: data,
                },
                events: {
                    input: {
                        selection: (event) => {
                            let selectedValue = event.detail.selection.value;
                            ac.input.value = selectedValue;
                        }
                    }
                },
            });
        }

        // make all the forms on this page use the ajax
        window.onload = function() {
            document.querySelectorAll("input.book-picker").forEach(input => initializeAutocomplete("#" + input.id, books));
            document.querySelectorAll("form.mobi-attacher").forEach(form => convertFormToAjax(form, csrfHeaderName, csrfToken, [], handleFormSubmitResponse));
        }

        function handleFormSubmitResponse(event) {

            let response = parseResponse(event.target.responseText);
            if (response.success) {
                alert("Book information saved.");

                // remove the item we just "fixed"
                let li = document.getElementById("mobi" + response.mobiIndex);
                li.parentNode.removeChild(li);
            }
            else {
                alert("failure!\n" + response);
            }
        }
    </script>
</#assign>

<@c.page title="New Assets" additionalHeadContent=hdr>
    The following assets have been uploaded, but have not yet been added to the library.

    <div>EPUBs:</div>
    <ul>
        <#list epubs as epub>
            <li>${epub} [<a href="/addBook?epub=${epub?url}">Add to Library</a>]</li>
        </#list>
    </ul>

    <div>MOBIs:</div>
    <ul>
        <#list mobis as mobi>
            <li id="mobi${mobi?index}">
                ${mobi}
                <#-- data-ajax-method is used by convertFormToAjax() to determine what method to use when submitting the form -->
                <form class="mobi-attacher" action="/api/mobi/${mobi?url}" data-ajax-method="put">
                    <input type="hidden" name="mobiIndex" value="${mobi?index?c}" />
                    <input id="picker${mobi?index?c}" class="book-picker" type="text" name="bookInfo" value="" />
                    <input type="submit" value="Update Book" />
                </form>
            </li>
        </#list>
    </ul>

</@c.page>
