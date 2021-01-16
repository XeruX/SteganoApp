package com.steganoapp.steganography;

import org.opencv.core.Mat;

public class LSB implements SteganoMethod {

    @Override
    public Mat encode(Mat picture, int[] message) {
        // Składowe B, G, R - w takiej kolejności OpenCV wczytuje obraz
        byte blue = 0, green = 0, red = 0;
        byte[] pixels = new byte[(int) picture.total() * picture.channels()];
        int size = picture.get(0, 0, pixels);

//        for(int col = 0; col < 5; col++) {
//            blue = pixels[col];
//            green = pixels[col+1];
//            red = pixels[col+2];
//
//        }
        return picture;
    }

    @Override
    public String decode(Mat picture) {

        return "";
    }
}
