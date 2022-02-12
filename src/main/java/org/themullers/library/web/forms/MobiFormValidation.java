package org.themullers.library.web.forms;

/**
 * The Java object returned in response to a request to update the MOBI associated with a book.
 */
public class MobiFormValidation extends FormValidation {

    String mobiIndex;  // this identifies which mobi on the page was submitted

    public MobiFormValidation(String mobiIndex) {
        this.mobiIndex = mobiIndex;
    }

    /**
     * set the success indicator to false and add an error message
     * @param message  an error message
     */
    public void fail(String message) {
        addError(message);
        setSuccess(false);
    }

    // ACCESSOR METHODS

    public String getMobiIndex() {
        return mobiIndex;
    }

    public void setMobiIndex(String mobiIndex) {
        this.mobiIndex = mobiIndex;
    }
}