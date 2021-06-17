package com.steganoapp.steganography;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.Arrays;

public class LSBTest {
    Mat img = Imgcodecs.imread("C:\\Users\\Patryk\\Desktop\\output.bmp", Imgcodecs.IMREAD_COLOR);
    Mat outputImg;
    byte[] message = new byte[] {0, 1, 1, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0}; // "test"
    LSB LSB = new LSB();

    String wzor = "te";
    byte[] out;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.load("D:\\Download\\opencv\\build\\java\\x64\\opencv_java440.dll");
    }

    @Test
    public void decode() {
        out = LSB.decodeT(img);
        System.out.println(Arrays.toString(out));
        //assertEquals(wzor, out);
    }
}