package com.steganoapp.steganography;

import org.opencv.core.Mat;

import java.util.Arrays;

public class LSB implements SteganoMethod {

    @Override
    public Mat encode(Mat picture, int[] message) {
        // Tablica ze składowymi B, G, R - w takiej kolejności OpenCV wczytuje obraz
        byte[] pixels = new byte[(int) picture.total() * picture.channels()];
        int messagePointer = 0;
        Mat encodedPicture = new Mat(picture.size(), picture.type());

        // Załadowanie składowych pikseli (BGR) z matrycy picture do tablicy pixels
        int pictureSize = picture.get(0, 0, pixels);

        for(int col = 0; messagePointer < message.length && col < pictureSize; col++) {
            pixels[col] = (byte) ((pixels[col] & 0xFE) | message[messagePointer]);
            messagePointer++;
        }
        int tmp = encodedPicture.put(0, 0, pixels);

        System.out.println("("+tmp+")"+"Zakodowano LSB: " + Arrays.toString(message));

        return encodedPicture;
    }

    @Override
    public byte[] decode(Mat picture) {
        // Tablica ze składowymi B, G, R - w takiej kolejności OpenCV wczytuje obraz
        byte[] pixels = new byte[(int) picture.total() * picture.channels()];
        int[] length = new int[32];
        int messageLength = 0;
        int pointer = 0;
        // Tablica na wiadomość w postaci bitowej
        byte[] message;

        // Załadowanie składowych pikseli (BGR) z matrycy picture do tablicy pixels
        int pictureSize = picture.get(0, 0, pixels);

        // Pętla sprawdzająca długość wiadomości w bitach
        for (int col = 0; pointer < 32; col++, pointer++) {
            length[pointer] = (pixels[col] & 1);
        }
        // Konwersja rozmiaru wiadomości z postaci bitowej do liczby całkowitej
        for (int i = 0, j = 31; i < length.length; i++, j--) {
            messageLength |= length[i] << j;
        }

        System.out.println("Rozmiar wiadomości: "+messageLength);

        // Ustawienie dokładnego rozmiaru wiadomości w bitach
        message = new byte[messageLength - 32];
        pointer = 0;

        // Pętla pobierająca wiadomość do tablicy message
        for (int col = 32; pointer < 32; col++, pointer++) {
            message[pointer] = (byte) (pixels[col] & 1);
        }
        System.out.println("Wiadomość: "+Arrays.toString(message));
        return message;
    }
}
