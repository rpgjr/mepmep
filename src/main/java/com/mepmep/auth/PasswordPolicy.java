package com.mepmep.auth;

import java.util.ArrayList;
import java.util.List;

/** Basic password rules: 8+ characters with a lowercase letter, a number and a special character. */
public final class PasswordPolicy {
    private PasswordPolicy() {}

    /** Returns what is missing, or an empty list when the password is fine. */
    public static List<String> problems(String password) {
        List<String> out = new ArrayList<>();
        if (password == null || password.length() < 8) out.add("at least 8 characters");
        if (password == null || password.chars().noneMatch(Character::isLowerCase)) out.add("a lowercase letter");
        if (password == null || password.chars().noneMatch(Character::isDigit)) out.add("a number");
        if (password == null || password.chars().noneMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)))
            out.add("a special character");
        return out;
    }

    public static String message(List<String> problems) {
        return "Password needs " + String.join(", ", problems);
    }
}
