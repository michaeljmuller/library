package org.themullers.library.web;

import org.themullers.library.Utils;

import java.util.LinkedList;
import java.util.List;

public class FormValidation {

    boolean isSuccess = true;
    List<String> errorMessages = new LinkedList<String>();

    public void addError(String message) {
        errorMessages.add(message);
    }

    public void addErrorIfNotMatch(String value, String regex, String message) {
        if (!Utils.isBlank(value) && !value.matches(regex)) {
            errorMessages.add(message);
        }
    }
    public void addErrorIfBlank(String value, String message) {
        if (Utils.isBlank(value)) {
            errorMessages.add(message);
        }
    }
    public void addErrorIf(boolean condition, String message) {
        if (condition) {
            errorMessages.add(message);
        }
    }

    // ACCESSOR METHODS

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
}