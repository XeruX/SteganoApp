package com.steganoapp.steganography;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import static org.junit.Assert.*;

public class LSBTest {
    Mat img = Imgcodecs.imread("C:\\Users\\Patryk\\Desktop\\output.png", Imgcodecs.IMREAD_COLOR);
    Mat outputImg;
    int[] message = new int[] {0,1,0,1,0,1,0,0, 0,1,1,0,0,1,0,1, 0,1,1,1,0,0,1,1, 0,1,1,1,0,1,0,0}; // Test
    LSB lsb = new LSB();

    String wzor = "test";
    String out;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.load("D:\\Download\\opencv\\build\\java\\x64\\opencv_java440.dll");
    }

    @Before
    public void setUp() throws Exception {
    }

//    @Test
//    public void twoMatricesMustNotEquals() {
//        outputImg = lsb.encode(img, message);
//        assertNotEquals(img, outputImg);
//    }
    @Test
    public void decode() {
        out = lsb.decode(img);
        assertEquals(wzor, out);
    }
}