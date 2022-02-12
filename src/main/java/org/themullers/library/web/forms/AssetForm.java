package org.themullers.library.web.forms;

/**
 * This class receives the information submitted by a form prompting the user to associate an asset with a book.
 */
public class AssetForm {

    String bookInfo; // the title and author of the book to be updated
    String assetIndex;  // an identifier indicating which MOBI on the page originated this request

    // ACCESSOR METHODS

    public String getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(String bookInfo) {
        this.bookInfo = bookInfo;
    }

    public String getAssetIndex() {
        return assetIndex;
    }

    public void setAssetIndex(String assetIndex) {
        this.assetIndex = assetIndex;
    }
}
