<#import "macros/page-macro.ftl" as c/>

<@c.page title="Delete book ${book.title}">

    <form action="/admin/deleteBookForm/${book.id?c}" method="post">

        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

        <#if book.epubObjectKey??>
            <div>This book's EPUB is associated with ${numOtherBooksWithThisEpub} other books.</div>
            <div>
                <input id="deleteEpub" type="checkbox" name="deleteEpub"/>
                <label for="deleteEpub">Delete any associated EPUB asset</label>
            </div>
        <#else>
            <div>There is no EPUB associated with this book.</div>
        </#if>
        <br/>

        <#if book.mobiObjectKey??>
            <div>This book's MOBI is associated with ${numOtherBooksWithThisMobi} other books.</div>
            <div>
                <input id="deleteMobi" type="checkbox" name="deleteMobi" />
                <label for="deleteMobi">Delete any associated MOBI asset</label>
            </div>
        <#else>
            <div>There is no MOBI associated with this book.</div>
        </#if>
        <br/>

        <#if book.audioObjectKey??>
            <div>This book's audiobook is associated with ${numOtherBooksWithThisAudiobook} other books.</div>
            <div>
                <input id="deleteAudiobook" type="checkbox" name="deleteAudiobook" />
                <label for="deleteAudiobook">Delete any associated EPUB asset</label>
            </div>
        <#else>
            <div>There is no audiobook associated with this book.</div>
        </#if>
        <br/>

        <input type="submit" value="Delete Book" /> <a href="${referrer!"/"}">Cancel</a>
    </form>

</@c.page>