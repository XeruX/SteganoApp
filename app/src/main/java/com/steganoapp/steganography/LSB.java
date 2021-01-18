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

        for(int col = 0; messagePointer < message.length && col < pictureSize; col += 3) {

            if(messagePointer >= message.length) break;
            pixels[col] = (byte) ((pixels[col] & 0xFE) | message[messagePointer]);
            messagePointer++;

            if(messagePointer >= message.length) break;
            pixels[col+1] = (byte) ((pixels[col+1] & 0xFE) | message[messagePointer]);
            messagePointer++;

            if(messagePointer >= message.length) break;
            pixels[col+2] = (byte) ((pixels[col+2] & 0xFE) | message[messagePointer]);
            messagePointer++;
        }
        encodedPicture.put(0, 0, pixels);

        return encodedPicture;
    }

    @Override
    public String decode(Mat picture) {
        // Tablica ze składowymi B, G, R - w takiej kolejności OpenCV wczytuje obraz
        byte[] pixels = new byte[(int) picture.total() * picture.channels()];
        int messageSize = 0;
        int messagePointer = 0;
        int messageBit = 0;
        byte element, mt;
        // Wiadomość w bajtach
        byte[] message;
        // Znak kończący (EM - end of medium)
        byte[] messageTerminator = new byte[] {0, 0, 0, 1, 1, 0, 0, 1,  0, 0, 0, 1, 1, 0, 0, 1,  0, 0, 0, 1, 1, 0, 0, 1,  0, 0, 0, 1, 1, 0, 0, 1};
        //StringBuilder sb = new StringBuilder();

        // Załadowanie składowych pikseli (BGR) z matrycy picture do tablicy pixels
        int pictureSize = picture.get(0, 0, pixels);

        // Pętla sprawdzająca długość wiadomości w bitach na podstawie znaku kończącego
        for(int col = 0, termPointer = 0; termPointer < 32 && col < pictureSize; col++) {
            element = (byte) (pixels[col] & 1);
            mt = (byte) (messageTerminator[termPointer] & 1);
            if(element == mt) {termPointer++;}
            else {termPointer = 0;}

            //if(termPointer >= 32) {messageSize = col;}
            messageSize = col;
        }

        message = new byte[messageSize / 8];
        // Pętla ładująca wiadomość w bajtach do tablicy message
        for(int i = 0; i < (messageSize - 32); i++) {
            messageBit = (byte) (pixels[i] & 1);
            message[messagePointer] = (byte) (message[messagePointer] | messageBit);
            if(i % 8 == 0) messagePointer++;
        }
        String out = Arrays.toString(message);

        return out;
    }
}
