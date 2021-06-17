package com.steganoapp.utility;

import org.opencv.core.Mat;

public class SteganoUtility {

    public static String bitsToMessage(byte[] message) {
        byte[] msg = new byte[message.length / 8];
        int pointer = 0;
        // Pętla konwertująca wiadomość do postaci bajtowej
        for (int i = 0; i < msg.length; i++) {
            for (int j = 7; j >= 0; j--, pointer++) {
                msg[i] |= message[pointer] << j;
            }
        }
        // Pętla konwertująca wartości bajtowe na tekst
        StringBuilder output = new StringBuilder();
        for (byte b : msg) {
            output.append((char) b);
        }
        return output.toString();
    }

    public static byte[] messageToBits(byte[] message) {
        int pointer = 0;
        int messageLength = message.length * 8 + 32;
        byte[] messageBits = new byte[messageLength];
        byte[] length = new byte[4];

        length[0] = (byte) ( messageLength >> 24 );
        length[1] = (byte) ( (messageLength << 8) >> 24 );
        length[2] = (byte) ( (messageLength << 16) >> 24 );
        length[3] = (byte) messageLength;

        // Kodowanie rozmiaru wiadomości
        for (byte value : length) {
            for (int j = 7; j >= 0; j--) {
                messageBits[pointer] = (byte) (value >>> j & 1);
                pointer++;
            }
        }
        // Kodowanie wiadomości
        for (byte m : message) {
            for (int j = 7; j >= 0; j--) {
                messageBits[pointer] = (byte) (m >>> j & 1);
                pointer++;
            }
        }
        return messageBits;
    }

    public static int calculateAvailableCharacters(Mat picture) {
        int availableSpace = (int) picture.total() * picture.channels();
        return availableSpace / 8 - 4;
    }
}
