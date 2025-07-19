package com.datapeice.event;

import java.util.Random;

public class RandomNumber {
    private static final Random RANDOM = new Random();


    public static int[] getRandomPos() {
        int x = getRandomNumber();
        int y = RANDOM.nextInt(320);
        int z = getRandomNumber();
        return new int[]{x, y, z};
    }

    private static int getRandomNumber() {
        return RANDOM.nextInt(2001) - 1000; //-1000 ~ 1000
    }
}
