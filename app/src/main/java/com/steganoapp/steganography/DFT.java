package com.steganoapp.steganography;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

public class DFT implements SteganoMethod {
    @Override
    public Mat encode(Mat picture, int[] message) {
        //byte[] pixels = new byte[(int) picture.total() * picture.channels()];
        //int messagePointer = 0;
        Mat pictureTmp = new Mat(picture.size(), picture.type());
        List<Mat> planes = new ArrayList<>();
        // Liczba pikseli o jaką trzeba rozszerzyć obraz, aby funkcja DFT była efektywna
        int pixelRowsGrowth = Core.getOptimalDFTSize(picture.rows());
        int pixelColsGrowth = Core.getOptimalDFTSize(picture.cols());

        System.out.println("pixelRowsGrowth: "+pixelRowsGrowth);
        System.out.println("pixelColsGrowth: "+pixelColsGrowth);

        // Rozszerzenie obrazu o wymaganą liczbę (pustych) pikseli
        Core.copyMakeBorder(picture, pictureTmp, 0, pixelRowsGrowth - picture.rows(), 0, pixelColsGrowth - picture.cols(), Core.BORDER_CONSTANT, Scalar.all(0));
        // Konwersja obrazu do liczb rzeczywistych
        pictureTmp.convertTo(pictureTmp, CvType.CV_32F);

        // Dodaję znormalizowaną płaszczyznę
        planes.add(pictureTmp);
        planes.add(Mat.zeros(pictureTmp.size(), CvType.CV_32F));

        Core.merge(planes, pictureTmp);

        Core.dft(pictureTmp, pictureTmp);

        return pictureTmp;
    }

    @Override
    public byte[] decode(Mat picture) {
        return new byte[] {0,1,0,0,0,1,0,0, 0,1,0,0,0,1,1,0, 0,1,0,1,0,1,0,0};
    }
}
