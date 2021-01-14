package com.steganoapp.steganography;

import org.opencv.core.Mat;

public class LSB implements SteganoMethod {

    @Override
    public Mat encode(Mat picture, byte[] message) {
        // Składowe B, G, R - w takiej kolejności OpenCV wczytuje obraz
        byte b = 0, g = 0, r = 0;
        byte[] pixels = new byte[(int) picture.total() * picture.channels()];
        int size = picture.get(0, 0, pixels);

        for(int col = 0; col < size; col++) {
            b = pixels[col];
            g = pixels[col+1];
            r = pixels[col+2];


        }



        return new Mat();
    }

    @Override
    public String decode(Mat picture) {

        return "";
    }
}
