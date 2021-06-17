package com.steganoapp.steganography;

import com.steganoapp.exceptions.MessageNotFound;

import org.opencv.core.Mat;

public interface SteganoMethod {
    Mat encodeT(Mat picture, byte[] message);
    byte[] decodeT(Mat picture) throws MessageNotFound;
    Mat encodeP(Mat picture, Mat pictureToHide);
    Mat decodeP(Mat picture);
}
