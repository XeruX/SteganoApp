package com.steganoapp.steganography;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DFT implements SteganoMethod {
    @Override
    public Mat encode(Mat picture, byte[] message) {
        //byte[] pixels = new byte[(int) picture.total() * picture.channels()];
        //int messagePointer = 0;
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
    public byte[] decode(Mat picture) {
        return new byte[] {0,1,0,0,0,1,0,0, 0,1,0,0,0,1,1,0, 0,1,0,1,0,1,0,0};
    }

    private Mat encodeColorPicture(Mat colorPicture, byte[] message) {
        // Lista do przechowywania matryc (płaszczyzn) z liczbami rzeczywistymi i urojonymi (liczby zespolone)
        // Lista do przechowywania matryc ze składowymi B, G, R
        List<Mat> complexPlanes = new ArrayList<>();
        List<Mat> planesBGR = new ArrayList<>();
        Mat pictureTmp = new Mat(colorPicture.size(), colorPicture.type());
        // Liczba pikseli o jaką trzeba rozszerzyć obraz, aby funkcja DFT była efektywna
        int pixelRowsGrowth = Core.getOptimalDFTSize(colorPicture.rows());
        int pixelColsGrowth = Core.getOptimalDFTSize(colorPicture.cols());

        // Rozszerzenie obrazu o wymaganą liczbę (pustych) pikseli
        Core.copyMakeBorder(colorPicture, pictureTmp,
                0, pixelRowsGrowth - colorPicture.rows(),
                0, pixelColsGrowth - colorPicture.cols(),
                Core.BORDER_CONSTANT, Scalar.all(0));
        // Konwersja obrazu do liczb rzeczywistych
        pictureTmp.convertTo(pictureTmp, CvType.CV_32F);

        // Rozdzielenie kanałów B, G, R i podział na pojedyncze płaszczyzny
        Core.split(pictureTmp, planesBGR);
        Mat blue = planesBGR.get(0);
        Mat green = planesBGR.get(1);
        Mat red = planesBGR.get(2);

        complexPlanes.add(blue);
        complexPlanes.add(Mat.zeros(pictureTmp.size(), CvType.CV_32F));
        // Scalenie płaszczyzn do jednej matrycy
        Core.merge(complexPlanes, pictureTmp);

        complexPlanes.clear();
        // Wynikiem działania funkcji jest matryca liczb zespolonych,
        Core.dft(pictureTmp, pictureTmp);
        // dlatego trzeba rozbić tę matrycę na dwie części (część rzeczywistą i urojoną),
        Core.split(pictureTmp, complexPlanes);
        // by możliwe było obliczenie modułu liczb zespolonych (odległości od początku układu współrzędnych)
        Core.magnitude(complexPlanes.get(0), complexPlanes.get(1), pictureTmp);
        float[] data = new float[(int) pictureTmp.total()];
        pictureTmp.get(0, 0, data);
        for (int i = 0; i < 5; i++) {
            System.out.println(data[i]);
        }
        Core.add(Mat.ones(pictureTmp.size(), CvType.CV_32F), pictureTmp, pictureTmp);
        // Zmiana skali z liniowej na logarytmiczną, ponieważ otrzymane wyniki są o zbyt dużych wartościach
        Core.log(pictureTmp, pictureTmp);

        pictureTmp.get(0, 0, data);
        for (int i = 0; i < 5; i++) {
            System.out.println(data[i]);
        }
        // Konwersja do formatu umożliwiającego wyświetlenie obrazu
        pictureTmp.convertTo(pictureTmp, CvType.CV_8UC1);
        byte[] byteData = new byte[(int) pictureTmp.total()];
        pictureTmp.get(0, 0, byteData);
        for (int i = 0; i < 5; i++) {
            System.out.println(byteData[i]);
        }
        // Znormalizowanie wartości pikseli do przedziału (0-255)
        Core.normalize(pictureTmp, pictureTmp, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);
        pictureTmp.get(0, 0, byteData);
        for (int i = 0; i < 5; i++) {
            System.out.println(byteData[i]);
        }

        return colorPicture;
    }
}
