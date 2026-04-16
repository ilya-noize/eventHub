package com.event.common.tool;

import lombok.experimental.UtilityClass;

import java.util.Random;
import java.util.UUID;

@UtilityClass
public class RandomResources {
    public static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    public static int getRandomInteger() {
        return Math.abs(new Random(2).nextInt());
    }
}
