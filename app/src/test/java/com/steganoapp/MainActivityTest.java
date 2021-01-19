package com.steganoapp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainActivityTest {
    byte[] message = new byte[] {0, 1, 1, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0}; // "test"

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

}