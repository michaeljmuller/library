package org.themullers.library;

import org.springframework.security.core.context.SecurityContextHolder;
import org.themullers.library.auth.LibraryUserDetails;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class contains generic utility methods that are not specific to the library application.
 */
public class Utils {

    /**
     * Writes an exception's call stack to a string.
     * @param t  the throwable to inspect
     * @return  the call stack
     * @throws IOException  thrown if an unexpected error occurs writing to the string
     */
    public static String toString(Throwable t) throws IOException {
        try (var sw = new StringWriter(); var pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            return sw.toString();
        }
    }

    /**
     * If the first arg is null, returns the second arg; returns the first org otherwise.
     * @param maybeNull  check this value for null
     * @param notNull  the value to return if the first arg is null
     * @param <T>  the type of the object we're checking for null
     * @return  the second arg if the first arg is null; otherwise the first arg
     */
    public static <T> T ifNull(T maybeNull, T notNull) {
        return maybeNull == null ?  notNull : maybeNull;
    }

    /**
     * Checks whether two objects are equal without possibly throwing
     * a null pointer exception.
     * @param a  a reference to an object
     * @param b  a reference to another object
     * @return  true if the objects are equal, false otherwise
     */
    public static boolean objectsAreEqual(Object a, Object b) {
        boolean areEqual =  a == null ? b == null : a.equals(b);
        if (!areEqual) {
            System.out.println(String.format("objects differ; %s != %s", a, b));
        }
        return areEqual;
    }

    /**
     * Check whether a string is null, an empty string, or all white space.
     * @param str  the string to test
     * @return  whether the string is blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * Check whether a string is not null and contains some non-whitespace character.
     * @param str  the string to test
     * @return  false if the string is blank, true otherwise
     */
    public static boolean isNotBlank(String str) {
        return str != null && str.trim().length() > 0;
    }

    /**
     * Check whether an email address is a valid format.
     * @param email  the email address to check
     * @return  whether the email address is a valid format
     */
    public static boolean isValidFormatEmail(String email) {
        // from https://www.baeldung.com/java-email-validation-regex
        var emailRegex = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@"
                + "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
        var pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    /**
     * Un-zips a zip file into a map of byte arrays; the keys of the  map
     * are the filenames and the values are the binary content of each file.
     * @param is  a stream from which the zip file can be read
     * @return  a map of filenames and binary content
     * @throws IOException  thrown if an unexpected error occurs while unzipping the file
     */
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

    /**
     * Get the extension from a filename.
     * @param filename  the filename to search
     * @return  the file's extension (without the dot), or an empty string if the extension could not be found
     */
    public static String getExtension(String filename) {

        // if the filename is null, return an empty string
        if (filename == null) {
            return "";
        }

        // if there is no dot in the filename, return an empty string
        var dotPos = filename.lastIndexOf(".");
        if (dotPos < 0) {
            return "";
        }

        // return everything after the last dot
        return filename.substring(dotPos + 1, filename.length()).toLowerCase();
    }

    public static Integer getCurrentUserId() {
        try {
            var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ((LibraryUserDetails) principal).getId();
        }
        catch (Exception x) {
            return null;
        }
    }
}
