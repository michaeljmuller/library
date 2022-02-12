package org.themullers.library.web.forms;

import org.themullers.library.Utils;

/**
 * The Java object returned in response to an add/edit book request.
 */
public class BookFormValidation extends FormValidation {

    BookForm book;  // this is the form with information about the book that's being added/edited

    public BookFormValidation(BookForm book) {
        this.book = book;
        validate();
    }

    /**
     * Checks form fields for a variety of possible invalid conditions; sets status to indicate failure
     * if any of these conditions are detected.
     */
    protected void validate() {
        addErrorIfBlank(book.getTitle(), "Missing title");
        addErrorIfBlank(book.getAuthor(), "Missing author");
        addErrorIfBlank(book.getPublicationYearString(), "Missing publication year");
        addErrorIfNotMatch(book.getPublicationYearString(), "\\d{4}", "Bad format publication year");
        addErrorIf(Utils.isNotBlank(book.getSeries()) && Utils.isBlank(book.getSeriesSequenceString()), "Missing series # (required when series is provided)");
        addErrorIfNotMatch(book.getSeriesSequenceString(), "\\d+", "Bad format series #");
        addErrorIfBlank(book.getAcquisitionDateString(), "Missing acquisition date");
        addErrorIfNotMatch(book.getAcquisitionDateString(), "\\d{1,2}/\\d{1,2}/\\d{4}", "Acquisition date must be mm/dd/yyyy");
        addErrorIfBlank(book.getEpubObjectKey(), "No EPUB selected");

        setSuccess(getErrorMessages().size() == 0);
    }
}
