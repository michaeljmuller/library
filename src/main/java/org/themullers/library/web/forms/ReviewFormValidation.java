package org.themullers.library.web.forms;

public class ReviewFormValidation extends FormValidation {

    ReviewForm reviewForm;

    public ReviewFormValidation(ReviewForm reviewForm) {
        this.reviewForm = reviewForm;
        validate();
    }

    protected void validate() {
        addErrorIf(reviewForm.getRating() < 0, "You must provide a rating.");

        setSuccess(getErrorMessages().size() == 0);
    }

}
