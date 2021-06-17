package com.steganoapp.steganography;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DFT implements SteganoMethod {
    @Override
    public Mat encodeT(Mat picture, byte[] message) {
        Mat finalPicture = new Mat(picture.size(), picture.type());

        if(picture.type() == CvType.CV_8UC3) {
            System.out.println("===========================Typ: CV_8UC3");
            finalPicture = encodeColorPicture(picture, message);
        }
        else if(picture.type() == CvType.CV_8UC1) {
            System.out.println("===========================Typ: CV_8UC1");
        }
        else System.out.println("============Typ: Inny, akceptowane są tylko obrazy 3-kolorowe lub w skali szarości!!");

        return finalPicture;
    }

    @Override
    public byte[] decodeT(Mat picture) {
        List<Mat> complexB = new ArrayList<>();
        List<Mat> complexG = new ArrayList<>();
        List<Mat> complexR = new ArrayList<>();
        List<Mat> planesBGR = new ArrayList<>();
        Mat pictureTmp = new Mat(picture.size(), CvType.CV_32F);
        picture.convertTo(pictureTmp, CvType.CV_32F);

        // Rozdzielenie składowych obrazu (B,G,R)
        Core.split(pictureTmp, planesBGR);
        Mat blue = planesBGR.get(0);
        Mat green = planesBGR.get(1);
        Mat red = planesBGR.get(2);
        planesBGR.clear();

        float[] data0 = new float[(int) blue.total()];
        blue.get(0, 0, data0);
        for (int i = 0; i < 20; i++) {
            System.out.println(data0[i]);
        }

//        complexB.add(blue);
//        complexB.add(Mat.zeros(blue.size(), CvType.CV_32F));
//        complexG.add(green);
//        complexG.add(Mat.zeros(green.size(), CvType.CV_32F));
//        complexR.add(red);
//        complexR.add(Mat.zeros(red.size(), CvType.CV_32F));
//        // Scalenie matrycy liczb rzeczywistych i urojonych do jednej
//        Core.merge(complexB, blue);
//        Core.merge(complexG, green);
//        Core.merge(complexR, red);
        // Wynikiem działania funkcji jest matryca liczb zespolonych,
        Core.dft(blue, blue);
        Core.dft(green, green);
        Core.dft(red, red);
        // dlatego trzeba rozbić każdą matrycę na część rzeczywistą i urojoną`
        Core.split(blue, complexB);
        Core.split(green, complexG);
        Core.split(red, complexR);

        //Mat realBlue = complexB.get(0);

        System.out.println("out(1)======================================");
        float[] data = new float[(int) complexB.get(0).total()];
        int size1 = complexB.get(0).get(0, 0, data);
        for (int i = 0; i < 12; i++) {
            System.out.println(data[i]);
        }
        System.out.println("size1: "+size1);

        System.out.println("out(2)======================================");
        float[] data2 = new float[(int) blue.total()];
        blue.get(0, 0, data2);
        for (int i = 0; i < 12; i++) {
            System.out.println(data2[i]);
        }

        return new byte[] {0,1,0,0,0,1,0,0, 0,1,0,0,0,1,1,0, 0,1,0,1,0,1,0,0};
    }

    @Override
    public Mat encodeP(Mat picture, Mat pictureToHide) {
        return null;
    }

    @Override
    public Mat decodeP(Mat picture) {
        return null;
    }

    private Mat encodeColorPicture(Mat colorPicture, byte[] message) {
        // Lista do przechowywania matryc (płaszczyzn) z liczbami rzeczywistymi i urojonymi (liczby zespolone)
        List<Mat> complexB = new ArrayList<>();
        List<Mat> complexG = new ArrayList<>();
        List<Mat> complexR = new ArrayList<>();
        // Lista do przechowywania matryc ze składowymi B, G, R
        List<Mat> planesBGR = new ArrayList<>();
        Mat pictureTmp = new Mat(colorPicture.size(), CvType.CV_32F);
        // Liczba pikseli o jaką trzeba rozszerzyć obraz, aby funkcja DFT była efektywna
//        int pixelRowsGrowth = Core.getOptimalDFTSize(colorPicture.rows());
//        int pixelColsGrowth = Core.getOptimalDFTSize(colorPicture.cols());

        // Rozszerzenie obrazu o wymaganą liczbę (pustych) pikseli
//        Core.copyMakeBorder(colorPicture, pictureTmp,
//                0, pixelRowsGrowth - colorPicture.rows(),
//                0, pixelColsGrowth - colorPicture.cols(),
//                Core.BORDER_CONSTANT, Scalar.all(0));
        // Konwersja obrazu do liczb rzeczywistych
        colorPicture.convertTo(pictureTmp, CvType.CV_32F);

        // Rozdzielenie składowych obrazu (B,G,R)
        Core.split(pictureTmp, planesBGR);
        Mat blue = planesBGR.get(0);
        Mat green = planesBGR.get(1);
        Mat red = planesBGR.get(2);
        planesBGR.clear();

        float[] data0 = new float[(int) blue.total()];
        blue.get(0, 0, data0);
        for (int i = 0; i < 12; i++) {
            System.out.println(data0[i]);
        }

//        complexB.add(blue);
//        complexB.add(Mat.zeros(blue.size(), CvType.CV_32F));
//        complexG.add(green);
//        complexG.add(Mat.zeros(green.size(), CvType.CV_32F));
//        complexR.add(red);
//        complexR.add(Mat.zeros(red.size(), CvType.CV_32F));
//        // Scalenie matrycy liczb rzeczywistych i urojonych do jednej
//        Core.merge(complexB, blue);
//        Core.merge(complexG, green);
//        Core.merge(complexR, red);
        //complexPlanes.clear();
        // Wynikiem działania funkcji jest matryca liczb zespolonych,
        Core.dft(blue, blue);
        Core.dft(green, green);
        Core.dft(red, red);
        // dlatego trzeba rozbić każdą matrycę na część rzeczywistą i urojoną
        Core.split(blue, complexB);
        Core.split(green, complexG);
        Core.split(red, complexR);
        //Core.add(Mat.ones(blue.size(), CvType.CV_32F), complexPicture, complexPicture);
//        // Zmiana skali z liniowej na logarytmiczną, ponieważ otrzymane wyniki są o zbyt dużych wartościach
//        Core.log(blue, blue);
//        Core.log(green, green);
//        Core.log(red, red);
//        // Konwersja do formatu umożliwiającego wyświetlenie obrazu
//        blue.convertTo(blue, CvType.CV_8U);
//        green.convertTo(green, CvType.CV_8U);
//        red.convertTo(red, CvType.CV_8U);

        float[] insert = new float[] {255.0F, 255.0F, 250.0F, 255.0F, 255.0F};
        int pointer = 0;
        // Część rzeczywista
        Mat realBlue = complexB.get(0);
//        for(int rowStart = 0, rowEnd = 1; rowEnd < realBlue.rows() && pointer < insert.length; rowStart += 2, rowEnd += 2) {
//            for(int colStart = 0, colEnd = 1; colEnd < realBlue.cols() && pointer < insert.length; colStart += 2, colEnd += 2, pointer++) {
//                maskBlue = complexB.get(0).submat(rowStart, rowEnd, colStart, colEnd);
//            }
//        }

        for(int i = 1; pointer < insert.length && i < realBlue.cols(); i += 2, pointer++) {
            realBlue.put(0, i, insert[pointer]);
        }

        System.out.println("dft(1)======================================");
        float[] data = new float[(int) realBlue.total()];
        int size1 = realBlue.get(0, 0, data);
        for (int i = 0; i < 12; i++) {
            System.out.println(data[i]);
        }
        System.out.println("size1: "+size1);

        Core.merge(complexB, blue);
        //Core.merge(complexG, green);
        //Core.merge(complexR, red);

//        Core.split(blue, complexB);
//        float[] insertB = new float[100000];
//        int sizeB = complexB.get(0).put(500, 500, insertB);
//        Core.merge(complexB, blue);
//        System.out.println("sizeB: "+sizeB);
//
//        Core.split(green, complexG);
//        float[] insertG = new float[100000];
//        int sizeG = complexG.get(0).put(500, 500, insertG);
//        Core.merge(complexG, green);
//        System.out.println("sizeG: "+sizeG);
//
//        Core.split(red, complexR);
//        float[] insertR = new float[100000];
//        int sizeR = complexR.get(0).put(500, 500, insertR);
//        Core.merge(complexR, red);
//        System.out.println(complexR.get(0).cols()+ ", row: "+ complexR.get(0).rows());
//        System.out.println("sizeR: "+sizeR);

        System.out.println("dft(2)======================================");
        float[] data1 = new float[(int) blue.total()];
        int size2 = blue.get(0, 0, data1);
        for (int i = 0; i < 12; i++) {
            System.out.println(data1[i]);
        }
        System.out.println("size2: "+size2);


        Core.idft(blue, blue);
        Core.idft(green, green);
        Core.idft(red, red);


        Core.split(blue, complexB);
        Core.split(green, complexG);
        Core.split(red, complexR);

        System.out.println("idft(1)======================================");
        float[] data2 = new float[(int) complexB.get(0).total()];
        complexB.get(0).get(0, 0, data2);
        for (int i = 0; i < 12; i++) {
            System.out.println(data2[i]);
        }

        Core.normalize(complexB.get(0), blue, 0, 255, Core.NORM_MINMAX);
        Core.normalize(complexG.get(0), green, 0, 255, Core.NORM_MINMAX);
        Core.normalize(complexR.get(0), red, 0, 255, Core.NORM_MINMAX);

        System.out.println("idft(2)======================================");
        float[] data3 = new float[(int) blue.total()];
        blue.get(0, 0, data3);
        for (int i = 0; i < 12; i++) {
            System.out.println(data3[i]);
        }


        // Scalenie składowych B,G,R w jeden obraz (kolorowy)
        Mat finalImage = new Mat(colorPicture.size(), CvType.CV_32F);
        planesBGR = Arrays.asList(blue, green, red);
        Core.merge(planesBGR, finalImage);
        finalImage.convertTo(finalImage, CvType.CV_8U);

        return finalImage;
    }

    private Mat encodeGrayPicture(Mat grayPicture, byte[] message) {

        return grayPicture;
    }
}
