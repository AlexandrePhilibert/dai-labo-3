package ch.heigvd.utils;

import java.util.Random;

public class RandomGenerator {
    public static String randomValue(Long max) {
        return String.valueOf((long) (Math.random() * max));
    }
}
