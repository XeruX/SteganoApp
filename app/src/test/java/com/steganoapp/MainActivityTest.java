package com.example.steganoapp;

import org.junit.Test;

import static com.example.steganoapp.MainActivity.stringToBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MainActivityTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void shouldReturnByteArray() {
        assertArrayEquals(stringToBytes("aaa"), new String("aaa").getBytes());
    }
}