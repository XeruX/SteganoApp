package com.steganoapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.RenderScript;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.steganoapp.steganography.SteganoMethod;
import com.steganoapp.steganography.exception.MessageNotFound;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.osgi.OpenCVInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static final int PERMISSION_EXTERNAL_STORAGE = 0;
    public static final int ACTIVITY_GET_CONTENT = 1;

    private TextView imagePathTextView;
    private TextView imageStatusTextView;
    private Switch switchButton;
    private Button encodeButton;
    private Button decodeButton;
    private TextView messageText;
    private EditText messageEditText;
    private Mat image;
    private int availableCharacters = 0;
    private byte[] message;
    private String methodName;
    private String extension;
    private ImageView imageView;

    static {
        if(OpenCVLoader.initDebug()){
            Log.d("Check","OpenCV skonfigurowano pomyślnie");
        } else{

            Log.d("Check","OpenCV nie zostało pomyślnie skonfigurowane!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pobranie uprawnień zapisu/odczytu plików
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Uprawnienia przyznane
        }
        else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Uprawnienia przyznane
        }
        else {
            // Brak uprawnień odczytu/zapisu - komunikat o udzielenie uprawnień
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_EXTERNAL_STORAGE);
        }

        // Inicjalizacja pól widoku
        imageView = (ImageView) findViewById(R.id.imageView);
        imagePathTextView = (TextView) findViewById(R.id.imagePathTextView);
        imagePathTextView.setText(R.string.pathEmpty);
        imageStatusTextView = (TextView) findViewById(R.id.imageStatusTextView);
        imageStatusTextView.setText(R.string.fileMissing);
        messageText = (TextView) findViewById(R.id.messageText);
        messageText.setText(R.string.maxMessageSize);
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > before) {
                    availableCharacters--;
                    messageText.setText(R.string.maxMessageSize);
                    messageText.append(" " + availableCharacters);
                }
                else if(s.length() < before) {
                    availableCharacters++;
                    messageText.setText(R.string.maxMessageSize);
                    messageText.append(" " + availableCharacters);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // Spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.methods_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //#################### PRZYCISKI ####################
        Button loadImageButton = (Button) findViewById(R.id.loadImageButton);
        loadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, ACTIVITY_GET_CONTENT);
        });
        //########## Przycisk zmiany trybu
        switchButton = (Switch) findViewById(R.id.switchButton);
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                switchButton.setText(R.string.switchButtonOn);
                messageEditText.clearFocus();
                messageEditText.setActivated(false);
                messageEditText.setVisibility(View.INVISIBLE);
                messageText.setText(R.string.messageContent);
                encodeButton.setActivated(false);
                encodeButton.setVisibility(View.INVISIBLE);
                decodeButton.setActivated(true);
                decodeButton.setVisibility(View.VISIBLE);
                messageText.setMovementMethod(new ScrollingMovementMethod());
            }
            else {
                switchButton.setText(R.string.switchButtonOff);
                messageEditText.setText("");
                messageEditText.clearFocus();
                messageEditText.setActivated(true);
                messageEditText.setVisibility(View.VISIBLE);

                if(image.empty()) {
                    availableCharacters = 0;
                }
                else {
                    availableCharacters = calculateAvailableCharacters(image);
                }

                messageText.setText(R.string.maxMessageSize);
                messageText.append(" " + availableCharacters);
                encodeButton.setActivated(true);
                encodeButton.setVisibility(View.VISIBLE);
                decodeButton.setActivated(false);
                decodeButton.setVisibility(View.INVISIBLE);
            }
        });
        //########## Przycisk kodowania
        encodeButton = (Button) findViewById(R.id.encodeButton);
        encodeButton.setOnClickListener(v -> {
            if(image.empty())
                Toast.makeText(getApplicationContext(), "Najpierw załaduj obraz!", Toast.LENGTH_SHORT).show();
            else if(messageEditText.length() <= 0)
                Toast.makeText(getApplicationContext(), "Wiadomość nie może być pusta!", Toast.LENGTH_SHORT).show();
            else {
                message = messageEditText.getText().toString().getBytes();
                encode(SteganoMethod.getInstance(methodName), image, message);
            }
        });
        //########## Przycisk dekodowania
        decodeButton = (Button) findViewById(R.id.decodeButton);
        decodeButton.setOnClickListener(v -> {
            if(image.empty())
                Toast.makeText(getApplicationContext(), "Najpierw załaduj obraz!", Toast.LENGTH_SHORT).show();
            else {
                decode(SteganoMethod.getInstance(methodName), image);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_GET_CONTENT) {
            if(resultCode == RESULT_CANCELED)
                System.err.println("getData(): Anulowano! - brak danych!");
            else {
                String PARTIAL_PATH = "/document/primary:";
                String partial = data.getData().getPath().replace(PARTIAL_PATH, "/");
                String externalStorage = Environment.getExternalStorageDirectory().getPath();
                File imagePath = new File(externalStorage + partial);
                image = loadImage(imagePath);
            }
        }
    }
    // Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Object method = parent.getItemAtPosition(pos);
        methodName = Objects.toString(method);
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }//-----Spinner

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Uprawnienia przyznane
            } else {
                // Zakończyć aplikację
            }
        }
    }

    // Wczytuje obraz z pamięci telefonu i zwraca w postaci matrycy OpenCV
    private Mat loadImage(File imageFile) {
        // Wczytanie obrazu do miniaturki
        imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
        // Wczytanie obrazu do matrycy OpenCV
        Mat img = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);

        // Pobiera rozszerzenie pliku
        if(imageFile.getPath().contains(".bmp"))
            extension = ".bmp";
        else if(imageFile.getPath().contains(".jpg")
                || imageFile.getPath().contains(".JPG")
                || imageFile.getPath().contains(".JPEG")
                || imageFile.getPath().contains(".jpeg")) {
            img.release();
        }
        else extension = ".png";

        if(img.empty()) imageStatusTextView.setText(R.string.fileNotLoaded);
        else {
            imagePathTextView.setText(R.string.pathEmpty);
            imagePathTextView.append(imageFile.getPath());
            imageStatusTextView.setText(R.string.fileLoaded);
            imageStatusTextView.append("\nRozmiar: " + imageFile.length() + " bajtów");
            availableCharacters = calculateAvailableCharacters(img);
            messageText.setText(R.string.maxMessageSize);
            messageText.append(" " + availableCharacters);
        }
        return img;
    }

    // Zapisuje obraz pod ścieżką /Pictures/output + rozszerzenie pliku, który był załadowany
    private void saveImage(Mat picture) {
        String path = Environment.getStorageDirectory() + "/self/primary/Pictures/output" + extension;
        if(Imgcodecs.haveImageWriter(path)) {
            Imgcodecs.imwrite(path, picture);
            Toast.makeText(getApplicationContext(), "Zapisano w /Pictures/output" + extension, Toast.LENGTH_LONG).show();
        }
        else System.err.println("Nie można zapisać pliku!");
    }

    // Kodowanie obrazu przy użyciu wybranej metody
    private void encode(SteganoMethod method, Mat picture, byte[] message) {
        if(message.length <= calculateAvailableCharacters(picture)) {
            byte[] msg = messageToBits(message);
            Mat mat = method.encode(picture, msg);
            saveImage(mat);
        }
        else messageText.setText(R.string.messageSizeTooLarge);
    }

    // Dekodowanie obrazu przy użyciu wybranej metody
    private void decode(SteganoMethod method, Mat picture) {
        String outputMessage = "";
        try {
            outputMessage = bitsToMessage(method.decode(picture));
            messageText.setText(R.string.messageContent);
            messageText.append(" " + outputMessage);
        }
        catch (MessageNotFound ex) {
            messageText.setText(ex.getMessage());
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.toString());
            System.out.println(ex.getMessage());
        }
    }

    private int calculateAvailableCharacters(Mat picture) {
        int availableSpace = (int) picture.total() * picture.channels();
        return availableSpace / 8 - 4;
    }

    public byte[] messageToBits(byte[] message) {
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

    public String bitsToMessage(byte[] message) {
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
}