package org.themullers.library.web.forms;

import org.themullers.library.Utils;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the Java object representation of the response to a form submission.
 */
public class FormValidation {

    boolean isSuccess = true;
    List<String> errorMessages = new LinkedList<String>();

    /**
     * Adds an error to the list of messages
     * @param message  the message to add to the list
     */
    public void addError(String message) {
        errorMessages.add(message);
    }

    /**
     * If the given value does not match the regex, add the message to the list.
     * @param value  the value to test
     * @param regex  the regex to match against
     * @param message  the message to add if the value doesn't match the regex
     */
    public void addErrorIfNotMatch(String value, String regex, String message) {
        if (!Utils.isBlank(value) && !value.matches(regex)) {
            errorMessages.add(message);
        }
    }

    /**
     * If the given value is blank, add the message to the list.
     * @param value  the value to test
     * @param message  the message to add to the list
     */
    public void addErrorIfBlank(String value, String message) {
        if (Utils.isBlank(value)) {
            errorMessages.add(message);
        }
    }

    /**
     * If the given condition is true, add the message to the list.
     * @param condition  whether the message should be added
     * @param message  the message to add to the list
     */
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