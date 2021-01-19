package com.steganoapp.steganography;

import org.opencv.core.Mat;

public class DFT implements SteganoMethod {
    @Override
    public Mat encode(Mat picture, int[] message) {
        return new Mat();
    }

    @Override
    public byte[] decode(Mat picture) {
        return new byte[0];
    }
}
