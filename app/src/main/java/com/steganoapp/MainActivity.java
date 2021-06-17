package com.steganoapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.app.ActivityCompat;

import com.steganoapp.steganography.LSB;
import com.steganoapp.steganography.SteganoMethod;
import com.steganoapp.exceptions.MessageNotFound;
import com.steganoapp.utility.SteganoUtility;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static final int PERMISSION_EXTERNAL_STORAGE = 0;
    public static final int ACTIVITY_GET_PICTURE_CARRIER = 1;
    public static final int ACTIVITY_GET_PICTURE_MESSAGE = 2;
    public final int METHOD_LSB_TEXT = 0;
    public final int METHOD_LSB_PICTURE = 1;

    private TextView imagePathTextView;
    private Switch switchButton;
    private Button encodeButton;
    private Button decodeButton;
    private TextView messageText;
    private TextView imageMessageTextView;
    private EditText messageEditText;
    private Mat imageCarrier = new Mat();
    private Mat imageMessage = new Mat();
    private int availableCharacters = 0;
    private byte[] message;
    private int selectedMethod;
    private String extension;
    private ImageView imageView;

    static {
        if (OpenCVLoader.initDebug()){
            Log.d("Check","OpenCV skonfigurowano pomyślnie");
        } else {
            Log.d("Check","OpenCV nie zostało pomyślnie skonfigurowane!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pobranie uprawnień odczytu / zapisu plików
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Brak uprawnień odczytu / zapisu - komunikat o udzielenie uprawnień
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    PERMISSION_EXTERNAL_STORAGE);
        }

        // Inicjalizacja pól widoku
        imageView = (ImageView) findViewById(R.id.imageView);
        imagePathTextView = (TextView) findViewById(R.id.imagePathTextView);
        imagePathTextView.setText(R.string.fileMissing);
        messageText = (TextView) findViewById(R.id.messageText);
        messageText.setText(R.string.maxMessageSize);
        imageMessageTextView = (TextView) findViewById(R.id.imageMessageTextView);
        imageMessageTextView.setText(R.string.imageMessageTextView);
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
            intent.setType("image/*");
            startActivityForResult(intent, ACTIVITY_GET_PICTURE_CARRIER);
        });
        Button loadImageButtonMessage = (Button) findViewById(R.id.loadImageButtonMessage);
        loadImageButtonMessage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, ACTIVITY_GET_PICTURE_MESSAGE);
        });
        //########## Przycisk zmiany trybu
        switchButton = (Switch) findViewById(R.id.switchButton);
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                switchButton.setText(R.string.switchButtonOn);
                messageEditText.clearFocus();
                if(selectedMethod == METHOD_LSB_TEXT) {
                    messageEditText.setEnabled(false);
                    messageEditText.setVisibility(View.INVISIBLE);
                    messageText.setText(R.string.messageContent);
                    messageText.setMovementMethod(new ScrollingMovementMethod());
                    imageMessageTextView.setEnabled(false);
                }
                else if(selectedMethod == METHOD_LSB_PICTURE) {
                    imageMessageTextView.setEnabled(true);
                }
                encodeButton.setEnabled(false);
                encodeButton.setVisibility(View.INVISIBLE);
                decodeButton.setEnabled(true);
                decodeButton.setVisibility(View.VISIBLE);
            }
            else {
                switchButton.setText(R.string.switchButtonOff);
                messageEditText.setText("");
                messageEditText.clearFocus();
                messageEditText.setEnabled(true);
                messageEditText.setVisibility(View.VISIBLE);

                if(imageCarrier.empty()) {
                    availableCharacters = 0;
                }
                else {
                    availableCharacters = SteganoUtility.calculateAvailableCharacters(imageCarrier);
                }

                messageText.setText(R.string.maxMessageSize);
                messageText.append(" " + availableCharacters);
                encodeButton.setEnabled(true);
                encodeButton.setVisibility(View.VISIBLE);
                decodeButton.setEnabled(false);
                decodeButton.setVisibility(View.INVISIBLE);
            }
        });
        //########## Przycisk kodowania
        encodeButton = (Button) findViewById(R.id.encodeButton);
        encodeButton.setOnClickListener(v -> {
            if(selectedMethod == METHOD_LSB_TEXT) {
                if(imageCarrier.empty())
                    Toast.makeText(getApplicationContext(), "Najpierw załaduj obraz!", Toast.LENGTH_SHORT).show();
                else if(messageEditText.length() <= 0)
                    Toast.makeText(getApplicationContext(), "Wiadomość nie może być pusta!", Toast.LENGTH_SHORT).show();
                else {
                    message = messageEditText.getText().toString().getBytes();
                    encode(new LSB(), imageCarrier, message);
                }
            }
            else if(selectedMethod == METHOD_LSB_PICTURE) {
                Toast.makeText(getApplicationContext(), "LSB PICTURE", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(getApplicationContext(), "Błąd - nie ma takiej metody!", Toast.LENGTH_SHORT).show();
        });
        //########## Przycisk dekodowania
        decodeButton = (Button) findViewById(R.id.decodeButton);
        decodeButton.setOnClickListener(v -> {
            if(imageCarrier.empty())
                Toast.makeText(getApplicationContext(), "Najpierw załaduj obraz!", Toast.LENGTH_SHORT).show();
            else {
                //decode(SteganoMethod.getInstance(methodName), image);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_GET_PICTURE_CARRIER) {
            if(resultCode == RESULT_CANCELED)
                System.err.println("Wybór nośnika anulowano! - brak danych!");
            else {
                String PARTIAL_PATH = "/document/primary:";
                String partial = data.getData().getPath().replace(PARTIAL_PATH, "/");
                String externalStorage = Environment.getExternalStorageDirectory().getPath();
                File imagePath = new File(externalStorage + partial);
                // Wczytanie obrazu do miniaturki
                imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath.getAbsolutePath()));
                imageCarrier = loadImage(imagePath);
            }
        }
        if(requestCode == ACTIVITY_GET_PICTURE_MESSAGE) {
            if(resultCode == RESULT_CANCELED)
                System.err.println("Wybór obrazu-wiadomości anulowano! - brak danych!");
            else {
                String PARTIAL_PATH = "/document/primary:";
                String partial = data.getData().getPath().replace(PARTIAL_PATH, "/");
                String externalStorage = Environment.getExternalStorageDirectory().getPath();
                File imagePath = new File(externalStorage + partial);
                imageMessage = loadImage(imagePath);
            }
        }
    }
    // Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        selectedMethod = parent.getSelectedItemPosition();
        switchButton.refreshDrawableState();
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
        // Wczytanie obrazu do matrycy OpenCV
        Mat img = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);

        // Pobiera rozszerzenie pliku
        if (imageFile.getPath().contains(".bmp"))
            extension = ".bmp";
        else if (imageFile.getPath().contains(".jpg")
                || imageFile.getPath().contains(".JPG")
                || imageFile.getPath().contains(".JPEG")
                || imageFile.getPath().contains(".jpeg")) {
            img.release();
        }
        else extension = ".png";

        if (img.empty()) imagePathTextView.setText(R.string.fileNotLoaded);
        else {
            imagePathTextView.setText(R.string.pathEmpty);
            imagePathTextView.append(imageFile.getPath());
            availableCharacters = SteganoUtility.calculateAvailableCharacters(img);
            messageText.setText(R.string.maxMessageSize);
            messageText.append(" " + availableCharacters);
        }
        return img;
    }

    // Zapisuje obraz pod ścieżką /Pictures/output + rozszerzenie pliku, który był załadowany
    private void saveImage(Mat picture) {
        String path = Environment.getStorageDirectory() + "/self/primary/Pictures/output" + extension;
        if (Imgcodecs.haveImageWriter(path)) {
            Imgcodecs.imwrite(path, picture);
            Toast.makeText(getApplicationContext(), "Zapisano w /Pictures/output" + extension, Toast.LENGTH_LONG).show();
        }
        else System.err.println("Nie można zapisać pliku!");
    }

    // Kodowanie obrazu przy użyciu wybranej metody
    private void encode(SteganoMethod method, Mat picture, byte[] message) {
        if (selectedMethod == METHOD_LSB_TEXT) {
            if (message.length <= SteganoUtility.calculateAvailableCharacters(picture)) {
                byte[] msg = SteganoUtility.messageToBits(message);
                Mat mat = method.encodeT(picture, msg);
                saveImage(mat);
            } else messageText.setText(R.string.messageSizeTooLarge);
        }
        else if (selectedMethod == METHOD_LSB_PICTURE) {

        }
    }

    // Dekodowanie obrazu przy użyciu wybranej metody
    private void decode(SteganoMethod method, Mat picture) {
        String outputMessage = "";
        try {
            outputMessage = SteganoUtility.bitsToMessage(method.decodeT(picture));
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
}