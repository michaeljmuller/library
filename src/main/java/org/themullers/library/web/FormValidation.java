package org.themullers.library.web;

import org.themullers.library.Utils;

import java.util.LinkedList;

public class FormValidation extends LinkedList<String> {
    public void addErrorIfNotMatch(String value, String regex, String message) {
        if (!Utils.isBlank(value) && !value.matches(regex)) {
            add(message);
        }
    }
    public void addErrorIfBlank(String value, String message) {
        if (Utils.isBlank(value)) {
            add(message);
        }
    }
    public void addErrorIf(boolean condition, String message) {
        if (condition) {
            add(message);
        }
    }
    public boolean isOkay() {
        return size() == 0;
    }
}