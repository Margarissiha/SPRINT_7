package api.utils;

import java.util.Random;

public class RandomUtils {
    private static final Random random = new Random();
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        return sb.toString();
    }

    public static String generateRandomLogin() {
        return "courier_" + generateRandomString(8);
    }

    public static String generateRandomPassword() {
        return "pass_" + generateRandomString(8);
    }

    public static String generateRandomFirstName() {
        return "Name_" + generateRandomString(6);
    }

    public static String generateRandomPhone() {
        StringBuilder sb = new StringBuilder("+7");
        for (int i = 0; i < 10; i++) {
            sb.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        return sb.toString();
    }
}