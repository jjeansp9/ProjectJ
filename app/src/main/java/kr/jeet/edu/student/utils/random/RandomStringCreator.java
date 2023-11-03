package kr.jeet.edu.student.utils.random;

import android.content.Context;

import java.util.Random;

public class RandomStringCreator {
    private final String characters;
    private static final Random random = new Random();

    public RandomStringCreator(String idPattern) {
        this.characters = idPattern;
    }

    public String generateRandomString(int minLength, int maxLength) {
//        if (minLength < 6 || maxLength > 12 || minLength > maxLength) {
//            throw new IllegalArgumentException("Invalid input parameters");
//        }

        int length = random.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }
}
