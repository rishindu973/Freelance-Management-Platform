package com.freelance.freelancepm.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordGenerator {
    private static final String UPPER="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER="abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBER="0123456789";
    private static final String SPECIAL="!@#$%^&*";
    private static final String ALL_CHARS= UPPER + LOWER + NUMBER + SPECIAL;

    private static SecureRandom random = new SecureRandom();

    public static String generatePassword(int length) {
        if (length <4) {
            throw new IllegalArgumentException("length must be at least 6 characters");
        }
        List<Character> passwordChars = new ArrayList<Character>();
        passwordChars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        passwordChars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        passwordChars.add(NUMBER.charAt(random.nextInt(NUMBER.length())));
        passwordChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        for(int i=4; i<length; i++) {
            passwordChars.add(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        Collections.shuffle(passwordChars);
        return passwordChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
    public static void main(String[] args) {
        System.out.println("Generated: "+generatePassword(12));
    }
}
