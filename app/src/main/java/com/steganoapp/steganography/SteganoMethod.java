package com.steganoapp.steganography;

import org.opencv.core.Mat;

public interface SteganoMethod {
    Mat encode(Mat picture, int[] message);
    byte[] decode(Mat picture);

    static SteganoMethod getInstance(String methodName) {
        if(methodName.equals("LSB"))
            return new LSB();
        else if(methodName.equals("DFT"))
            return new DFT();
        else
            return new LSB();
    }
}
