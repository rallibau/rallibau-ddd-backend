package com.rallibau.shared.domain;

public final class ShortMother {
    public static Short random() {
        return Integer.valueOf(MotherCreator.random().number().randomDigit()).shortValue();
    }
}
