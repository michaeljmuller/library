package org.themullers.library.web.forms;

import org.themullers.library.Review;

/**
 * Contains information about a book review.
 */
public class ReviewForm extends Review {
    int bookId;

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
}
