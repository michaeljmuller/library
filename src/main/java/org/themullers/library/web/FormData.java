package org.themullers.library.web;

import org.themullers.library.Utils;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface FormData {
    HttpServletRequest request();
    SimpleDateFormat dateFormat();

    default String getString(String parameter) {
        var value = request().getParameter(parameter);
        return Utils.isBlank(value) ? null : value.trim();
    }

    default Integer getInteger(String parameter) {
        var value = request().getParameter(parameter);
        return Utils.isBlank(value) ? null: Integer.valueOf(value);
    }

    default Date getDate(String parameter) {
        try {
            var value = request().getParameter(parameter);
            return Utils.isBlank(value) ? null : dateFormat().parse(value);
        }
        catch (ParseException x) {
            throw new RuntimeException("Date does not match format " + dateFormat(), x);
        }
    }
}