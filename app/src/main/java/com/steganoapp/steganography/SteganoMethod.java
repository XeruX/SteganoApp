package com.steganoapp.steganography;

import com.steganoapp.steganography.exception.MessageNotFound;

import org.opencv.core.Mat;

public interface SteganoMethod {
    Mat encode(Mat picture, byte[] message);
    byte[] decode(Mat picture) throws MessageNotFound;

    static SteganoMethod getInstance(String methodName) {
        if(methodName.equals("LSB"))
            return new LSB();
        else if(methodName.equals("DFT"))
            return new DFT();
        else
            return new LSB();
    }
}
