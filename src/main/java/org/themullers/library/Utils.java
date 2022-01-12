package org.themullers.library;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {

    public static String toString(Throwable t) throws IOException {
        try (var sw = new StringWriter(); var pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            return sw.toString();
        }
    }

    public static <T> T ifNull(T maybeNull, T notNull) {
        return maybeNull == null ?  notNull : maybeNull;
    }
}
