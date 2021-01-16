package com.steganoapp.steganography;

import org.opencv.core.Mat;

public class DFT implements SteganoMethod {
    @Override
    public Mat encode(Mat picture, int[] message) {
        return new Mat();
    }

    @Override
    public String decode(Mat picture) {
        return "Nie zaimplementowano";
    }
}
