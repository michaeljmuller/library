package org.themullers.library;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotBlank(String str) {
        return str != null && str.trim().length() > 0;
    }

    public static boolean isValidFormatEmail(String email) {
        // from https://www.baeldung.com/java-email-validation-regex
        var emailRegex = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@"
                + "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
        var pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    public static Map<String, byte[]> unzip(InputStream is) throws IOException {

        var map = new HashMap<String, byte[]>();

        // open the zip
        try (var zis = new ZipInputStream(is)) {
            ZipEntry entry;

            // add each entry to the map
            while ((entry = zis.getNextEntry()) != null) {
                map.put(entry.getName(), zis.readAllBytes());
            }
        }

        return map;
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return "";
        }

        var dotPos = filename.lastIndexOf(".");
        if (dotPos < 0) {
            return "";
        }

        return filename.substring(dotPos + 1, filename.length()).toLowerCase();
    }
}
