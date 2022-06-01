package com.rallibau.shared.domain;

public final class IntegerMother {
    public static Integer random() {
        return MotherCreator.random().number().randomDigit();
    }

    public static String randomString() {
        return String.valueOf(MotherCreator.random().number().randomDigit());
    }
}
