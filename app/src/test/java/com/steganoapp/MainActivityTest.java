package com.steganoapp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainActivityTest {
    MainActivity mainActivity;

    @Before
    public void setUp() {
        mainActivity = new MainActivity();
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void shouldConvertFromByteArrayToBitArray() {
        String message = "ab";
        int[] expected = {0b01100001, 0b01100010};
        int[] result = mainActivity.byteToBits(message.getBytes());
        assertEquals(expected, result);
    }
}