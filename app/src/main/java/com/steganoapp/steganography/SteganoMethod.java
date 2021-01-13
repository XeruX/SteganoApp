package com.steganoapp.steganography;

public interface SteganoMethod {
    byte[] encode(byte[] picture, byte[] message);
    byte[] decode(byte[] picture);
}
