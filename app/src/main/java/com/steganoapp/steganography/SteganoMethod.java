package com.steganoapp.steganography;

import org.opencv.core.Mat;

public interface SteganoMethod {
    Mat encode(Mat picture, byte[] message);
    String decode(Mat picture);
}
