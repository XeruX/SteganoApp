package com.steganoapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.steganoapp.steganography.SteganoMethod;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.osgi.OpenCVInterface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class MainActivity extends Activity {

    public static final int PERMISSION_EXTERNAL_STORAGE = 0;
    public static final int ACTIVITY_GET_CONTENT = 1;

    private Context context;
    private TextView imagePathTextView;
    private TextView imageStatusTextView;
    private ImageView imageView;
    private Switch switchButton;
    private Button encodeButton;
    private Button decodeButton;
    private TextView messageText;
    private EditText messageEditText;
    private Mat image;
    private byte[] message;
    private int availableCharacters;

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
        imagePathTextView = (TextView) findViewById(R.id.imagePathTextView);
        imagePathTextView.setText(R.string.pathEmpty);
        imageStatusTextView = (TextView) findViewById(R.id.imageStatusTextView);
        imageStatusTextView.setText(R.string.fileMissing);
        messageText = (TextView) findViewById(R.id.messageText);
        messageText.setText(R.string.maxMessageSize);
        messageEditText = (EditText) findViewById(R.id.messageEditText);

        //#################### PRZYCISKI ####################
        Button loadImageButton = (Button) findViewById(R.id.loadImageButton);
        loadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, ACTIVITY_GET_CONTENT);
        });
        //########## Przycisk zmiany trybu
        switchButton = (Switch) findViewById(R.id.switchButton);
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                switchButton.setText(R.string.switchButtonOn);
                messageEditText.setText("");
                messageEditText.clearFocus();
                messageEditText.setActivated(false);
                messageEditText.setVisibility(View.INVISIBLE);
                messageText.setVisibility(View.INVISIBLE);
                encodeButton.setActivated(false);
                encodeButton.setVisibility(View.INVISIBLE);
                decodeButton.setActivated(true);
                decodeButton.setVisibility(View.VISIBLE);
            }
            else {
                switchButton.setText(R.string.switchButtonOff);
                messageEditText.setText("");
                messageEditText.clearFocus();
                messageEditText.setActivated(true);
                messageEditText.setVisibility(View.VISIBLE);
                messageText.setVisibility(View.VISIBLE);
                encodeButton.setActivated(true);
                encodeButton.setVisibility(View.VISIBLE);
                decodeButton.setActivated(false);
                decodeButton.setVisibility(View.INVISIBLE);
            }
        });
        //########## Przycisk kodowania
        encodeButton = (Button) findViewById(R.id.encodeButton);
        encodeButton.setOnClickListener(v -> {
            if(image == null)
                Toast.makeText(getApplicationContext(), "Najpierw załaduj obraz!", Toast.LENGTH_SHORT).show();
            else {
                message = messageEditText.getText().toString().getBytes();
                Toast.makeText(getApplicationContext(), messageEditText.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        //########## Przycisk dekodowania
        decodeButton = (Button) findViewById(R.id.decodeButton);
        decodeButton.setOnClickListener(v -> {
            if(image == null)
                Toast.makeText(getApplicationContext(), "Najpierw załaduj obraz!", Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(getApplicationContext(), "Dekoduj", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_GET_CONTENT) {
            if(resultCode == RESULT_CANCELED)
                System.err.println("getData(): Błąd! - brak danych!");
            else {
                String PARTIAL_PATH = "/document/primary:";
                String partial = data.getData().getPath().replace(PARTIAL_PATH, "/");
                String externalStorage = Environment.getExternalStorageDirectory().getPath();
                File imagePath = new File(externalStorage + partial);
                imagePathTextView.setText(imagePath.getAbsolutePath());
                image = loadImage(imagePath);
            }
        }
    }

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
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));

        // Wczytanie obrazu do matrycy OpenCV
        Mat img = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);

        if(img.empty())
            imageStatusTextView.setText(R.string.fileNotLoaded);
        else {
            imageStatusTextView.setText(R.string.fileLoaded);
            imageStatusTextView.append("\nRozmiar: " + imageFile.length() + " bajtów");
        }
        return img;
    }

    // Kodowanie obrazu przy użyciu wybranej metody
    private void encode(SteganoMethod method, Mat picture, byte[] message) {
        if(checkIfMessageFitInPicture(message, picture)) {
            method.encode(picture, message);
        }
        else messageText.setText(R.string.messageSizeTooLarge);
    }

    // Dekodowanie obrazu przy użyciu wybranej metody
    private String decode(SteganoMethod method, Mat picture) {

        return "";
    }

    private boolean checkIfMessageFitInPicture(byte[] message, Mat picture) {
        int availableSpace = (int) picture.total() * picture.channels();
        availableCharacters = availableSpace / 8;
        return message.length <= availableCharacters;
    }
}