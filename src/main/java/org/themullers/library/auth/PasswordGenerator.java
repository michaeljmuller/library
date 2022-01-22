package org.themullers.library.auth;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Generates a random password.
 * I've prevented selection of some characters that are often confused, like 0/O/o and 1/l.
 * Adapted from https://stackoverflow.com/a/41891760
 */
@Component
public class PasswordGenerator {

    protected static final String LOWER = "abcdefghjkmnpqrstxyz";
    protected static final String UPPER = "ABCDEFGHIJKLMNPQRSTXYZ";
    protected static final String DIGITS = "23456789";
    protected static final String PUNCTUATION = "@#$%&*+=[]?><";

    protected Random random = new Random(System.nanoTime());

    public String generate(int length, boolean useLower, boolean useUpper, boolean useDigits, boolean usePunctuation) {

        // Collect the categories to use.
        var chars = "";
        if (useLower) {
            chars += LOWER;
        }
        if (useUpper) {
            chars += UPPER;
        }
        if (useDigits) {
            chars += DIGITS;
        }
        if (usePunctuation) {
            chars += PUNCTUATION;
        }

        // Argument Validation.
        if (length <= 0 || chars.length() <= 0) {
            throw new IllegalArgumentException("bad arguments passed to PasswordGenerator.generate()");
        }

        // Build the password.
        var password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int position = random.nextInt(chars.length());
            password.append(chars.charAt(position));
        }
        return new String(password);
    }

    public static void main(String args[]) {
        var g = new PasswordGenerator();
        var pw = g.generate(12, true, false, true, false);
        System.out.println(pw);
    }
}
