package org.themullers.library.web.forms;

/**
 * This class receives the information submitted by a form prompting the user to associate a MOBI with a book.
 */
public class MobiForm {

    String bookInfo; // the title and author of the book to be updated
    String mobiIndex;  // an identifier indicating which MOBI on the page originated this request

    // ACCESSOR METHODS

    public String getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(String bookInfo) {
        this.bookInfo = bookInfo;
    }

    public String getMobiIndex() {
        return mobiIndex;
    }

    public void setMobiIndex(String mobiIndex) {
        this.mobiIndex = mobiIndex;
    }
}
