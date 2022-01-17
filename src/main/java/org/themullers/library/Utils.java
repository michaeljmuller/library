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

    public static boolean objectsAreEqual(Object a, Object b) {
        boolean areEqual =  a == null ? b == null : a.equals(b);
        if (!areEqual) {
            System.out.println(String.format("objects differ; %s != %s", a, b));
        }
        return areEqual;
    }

    public static boolean isBlank(String str) {
        return str != null && str.trim().length() > 0;
    }

}
