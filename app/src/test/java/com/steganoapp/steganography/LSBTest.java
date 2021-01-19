package com.steganoapp.steganography;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import static org.junit.Assert.*;

public class LSBTest {
    Mat img = Imgcodecs.imread("C:\\Users\\Patryk\\Desktop\\output.bmp", Imgcodecs.IMREAD_COLOR);
    Mat outputImg;
    byte[] message = new byte[] {0, 1, 1, 1, 0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0}; // "test"
    LSB lsb = new LSB();

    String wzor = "te";
    byte[] out;

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
        System.out.println(Arrays.toString(out));
        //assertEquals(wzor, out);
    }
}