package kr.jeet.edu.student.utils.random;

import java.util.Random;

public class RandomNumStrCreator {
    private static final String NUMBERS = "0123456789";
    private static final Random random = new Random();

    public String generateRandomNumberString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(NUMBERS.length());
            char randomChar = NUMBERS.charAt(index);
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }
}
