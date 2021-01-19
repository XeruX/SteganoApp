package com.steganoapp.steganography;

import org.opencv.core.Mat;

import java.util.Arrays;

public class LSB implements SteganoMethod {

    @Override
    public Mat encode(Mat picture, byte[] message) {
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
        int messageSize = 0;
        byte element = 0;
        // Wiadomość w bajtach
        byte[] message;
        byte[] messageTerminator = {0,0,1,0,1,1,1,1, 0,0,1,1,0,0,0,0, 0,0,1,0,1,1,1,1, 0,0,1,1,0,0,0,0};

        // Załadowanie składowych pikseli (BGR) z matrycy picture do tablicy pixels
        int pictureSize = picture.get(0, 0, pixels);

        // Pętla sprawdzająca długość wiadomości w bitach na podstawie znaku kończącego
        for(int col = 0, termPointer = 0; col < pictureSize; col++) {
            if((pixels[col] & 1) == (messageTerminator[termPointer] & 1)) { termPointer++; }
            else { termPointer = 0; }

            if(termPointer >= 31) {
                messageSize = col;
                break;
            }
        }

        message = new byte[messageSize];
        byte character = 0;
        // Pętla ładująca wiadomość w bajtach do tablicy message
        for (int i = 0, j = 7, pointer = 0; i < message.length; i++, j--) {
            character |= (pixels[i] >>> j & 1);

            if(j == 0) {
                j = 7;
                message[pointer] = character;
                pointer++;
                character = 0;
            }
        }


        return message;
    }
}
