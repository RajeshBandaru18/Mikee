package com.prasaddevelops.mikee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.prasaddevelops.mikee.databinding.ActivityResultBinding;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    ActivityResultBinding binding;

    private Bitmap bitmap;
    private TextToSpeech textToSpeech;

    StringBuilder textResult;
    private String uri,from,type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        from = getIntent().getStringExtra("from");
        textToSpeech = new TextToSpeech(this, this);

        if (from.equals("gallery")){
            uri = getIntent().getStringExtra("uri");
            String result = getIntent().getStringExtra("result");
            Uri imageUri = Uri.parse(uri);
            binding.photo.setImageURI(imageUri);
            binding.resultText.setText(result);



        }else {
            bitmap = getIntent().getParcelableExtra("bitmap");


            detectText(bitmap);


            try {
                binding.photo.setImageBitmap(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }


        }



        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(textResult.toString(), TextToSpeech.QUEUE_FLUSH, null, null);

            }
        });


    }


    private void detectText(Bitmap bitmap) {
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

                        textResult = new StringBuilder();

                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                            for (FirebaseVisionText.Line line : block.getLines()) {
                                for (FirebaseVisionText.Element element : line.getElements()) {
                                    textResult.append(element.getText()).append(" ");
                                }
                                textResult.append("\n");
                            }
                            textResult.append("\n");
                        }

                        binding.resultText.setText(textResult.toString());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // Handle the failure
                        binding.resultText.setText(e.getMessage());
                        Toast.makeText(ResultActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
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
//                Log.i("TextToSpeech", "TextToSpeech engine initialized successfully");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (from.equals("gallery")){
                            textToSpeech.speak(binding.resultText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);

                        }else {
                            textToSpeech.speak(textResult.toString(), TextToSpeech.QUEUE_FLUSH, null, null);

                        }
                    }
                },1200);

            }
        } else {
//            Log.e("TextToSpeech", "TextToSpeech initialization failed");
            Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();

        }
    }
}