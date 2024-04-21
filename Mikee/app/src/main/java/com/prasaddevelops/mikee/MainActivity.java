package com.prasaddevelops.mikee;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.prasaddevelops.mikee.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    ActivityMainBinding binding;

    private SpeechRecognizer speechRecognizer;

    private static final int REQUEST_IMAGE_CAPTURE = 111;
    private static final int REQUEST_OBJECT_CAPTURE = 1111;


    Bitmap bitmap;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        checkPermissions();

        binding.btnPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechRecognizer.setRecognitionListener(new MyRecognitionListener());
                startListening();
            }
        });

        textToSpeech = new TextToSpeech(this, this);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            // Set the language to the default locale
            int langResult = textToSpeech.setLanguage(Locale.getDefault());

            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("TextToSpeech", "Language is not supported or missing data");
                Toast.makeText(this, "Language is not supported or missing data", Toast.LENGTH_SHORT).show();
            } else {
                Log.i("TextToSpeech", "TextToSpeech engine initialized successfully");

            }
        } else {
//            Log.e("TextToSpeech", "TextToSpeech initialization failed");
            Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();

        }
    }

    public class MyRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {


        }

        @Override
        public void onError(int i) {

        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String command = matches.get(0);
                processCommand(command);
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }

        private void processCommand(String command) {
            // Implement logic to handle different commands
            if (command.contains("take picture")) {
                // Open the camera or initiate the process to take a picture
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else if (command.contains("instructions")) {
                // Greet the user or perform some other action
                textToSpeech.speak(binding.txtSpeak.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);

            }else if (command.contains("select image")){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 123);

            }
        }
    }

    private void selectImage(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_OBJECT_CAPTURE);
        }
    }


    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA},1000);
        }else {
            speechRecognizer.setRecognitionListener(new MyRecognitionListener());
            startListening();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with speech recognition
                Log.d("PERMISSIONS","granted");
                binding.cardPermissions.setVisibility(View.GONE);
                speechRecognizer.setRecognitionListener(new MyRecognitionListener());

            } else {
                // Permission denied, handle accordingly
                binding.cardPermissions.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "Please access permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            if (resultCode == RESULT_OK && data !=null){
                bitmap = (Bitmap) data.getExtras().get("data");
//                BitmapParcelable bitmapParcelable = new BitmapParcelable(bitmap);

                Intent intent = new Intent(MainActivity.this,ResultActivity.class);
                intent.putExtra("bitmap",bitmap);
                intent.putExtra("from","camera");
                intent.putExtra("type","text_detection");

                startActivity(intent);
                //process here
//                detectText(bitmap);
            }
        } else if (requestCode == REQUEST_OBJECT_CAPTURE) {
            if(resultCode == RESULT_OK && data !=null){
                bitmap = (Bitmap) data.getExtras().get("data");
                Intent intent = new Intent(MainActivity.this,ObjectResultActivity.class);
                intent.putExtra("bitmap",bitmap);
                intent.putExtra("from","camera");
                intent.putExtra("type","object_detection");

                startActivity(intent);
            }

        } else if (requestCode == 123){
            if (resultCode == RESULT_OK && data !=null){
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    detectText(bitmap,uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        speechRecognizer.startListening(intent);
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        cursor.close();
        return filePath;
    }

    private void detectText(Bitmap bitmap, Uri uri) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("en", "hi", "te", "ta"))
                .build();
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        Task<FirebaseVisionText> result = detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        // Task completed successfully

                        StringBuilder textResult = new StringBuilder();

                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                for (FirebaseVisionText.Element element : line.getElements()) {
                                    textResult.append(element.getText()).append(" ");
                                }
                                textResult.append("\n");
                            }
                            textResult.append("\n");
                        }

                        Intent intent = new Intent(MainActivity.this,ResultActivity.class);
                        intent.putExtra("uri",uri.toString());
                        intent.putExtra("result",textResult.toString());
                        intent.putExtra("from","gallery");
                        startActivity(intent);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // Handle the failure
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}