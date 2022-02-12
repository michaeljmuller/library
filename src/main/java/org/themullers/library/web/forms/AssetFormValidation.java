package org.themullers.library.web.forms;

/**
 * The Java object returned in response to a request to update an asset associated with a book.
 */
public class AssetFormValidation extends FormValidation {

    String assetIndex;  // this identifies which asset on the page was submitted

    public AssetFormValidation(String assetIndex) {
        this.assetIndex = assetIndex;
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

    public String getAssetIndex() {
        return assetIndex;
    }

    public void setAssetIndex(String assetIndex) {
        this.assetIndex = assetIndex;
    }
}