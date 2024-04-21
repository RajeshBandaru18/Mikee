package com.prasaddevelops.mikee;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Toast;

import com.prasaddevelops.mikee.databinding.ActivityObjectResultBinding;
import com.prasaddevelops.mikee.ml.MobilenetV110224Quant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class ObjectResultActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    ActivityObjectResultBinding binding;

    private String uri,from,type;
    private Bitmap bitmap;
    private TextToSpeech textToSpeech;
    StringBuilder textResult;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityObjectResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        textResult = new StringBuilder();
        from = getIntent().getStringExtra("from");
        textToSpeech = new TextToSpeech(this, this);

        if (from.equals("gallery")){
            uri = getIntent().getStringExtra("uri");
            String result = getIntent().getStringExtra("result");
            Uri imageUri = Uri.parse(uri);
            binding.photo.setImageURI(imageUri);
            binding.resultText.setText(result);

            String[] lables = new String[1001];
            int cont = 0;

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("lables.txt")));
                String line = bufferedReader.readLine();
                while (line !=null){
                    lables[cont] = line;
                    cont++;

                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                Bitmap imageMap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                performObjectDetection(imageMap,lables);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        else {
            String[] lables = new String[1001];
            int cont = 0;

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("lables.txt")));
                String line = bufferedReader.readLine();
                while (line !=null){
                    lables[cont] = line;
                    cont++;

                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            bitmap = getIntent().getParcelableExtra("bitmap");
            performObjectDetection(bitmap,lables);

            try {
                binding.photo.setImageBitmap(bitmap);

            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                textToSpeech.speak(binding.resultText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);

            }
        });

    }

//    private void performObjectDetection(Bitmap image){
//
//        FirebaseVisionObjectDetectorOptions options =
//                new FirebaseVisionObjectDetectorOptions.Builder()
//                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
//                        .enableClassification()  // Optional
//                        .build();
//
////        FirebaseVisionObjectDetectorOptions options =
////                new FirebaseVisionObjectDetectorOptions.Builder()
////                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
////                        .enableMultipleObjects()
////                        .enableClassification()  // Optional
////                        .build();
//
//        FirebaseVisionObjectDetector objectDetector =
//                FirebaseVision.getInstance().getOnDeviceObjectDetector();
//
//        FirebaseVisionImage photo = FirebaseVisionImage.fromBitmap(image);
//        objectDetector.processImage(photo)
//                .addOnSuccessListener(
//                        new OnSuccessListener<List<FirebaseVisionObject>>() {
//                            @Override
//                            public void onSuccess(List<FirebaseVisionObject> detectedObjects) {
//                                // Task completed successfully
//                                // ...
//                                for (FirebaseVisionObject obj : detectedObjects) {
//                                    Integer id = obj.getTrackingId();
//                                    Rect bounds = obj.getBoundingBox();
//
//                                    // If classification was enabled:
//                                    int category = obj.getClassificationCategory();
//                                    Float confidence = obj.getClassificationConfidence();
//
//                                    textResult.append("ID: ").append(id).append("\n");
//                                    textResult.append("Category: ").append(category).append("\n");
//                                    textResult.append("Bounding Box: ").append(bounds).append("\n");
//                                    Toast.makeText(ObjectResultActivity.this, "Hello"+confidence, Toast.LENGTH_SHORT).show();
//
//                                }
//
//                                binding.resultText.setText(textResult.toString());
//
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Task failed with an exception
//                                // ...
//                                Toast.makeText(ObjectResultActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
//
//
//    }


    private void performObjectDetection(Bitmap bitmap, String[] lables){
        try {
            MobilenetV110224Quant quant = MobilenetV110224Quant.newInstance(ObjectResultActivity.this);

            TensorBuffer inputFeatures = TensorBuffer.createFixedSize(new int[]{1,224,224,3}, DataType.UINT8);

            bitmap = Bitmap.createScaledBitmap(bitmap,224,224,true);
            inputFeatures.loadBuffer(TensorImage.fromBitmap(bitmap).getBuffer());

            MobilenetV110224Quant.Outputs outputs = quant.process(inputFeatures);
            TensorBuffer outputFeature = outputs.getOutputFeature0AsTensorBuffer();

            binding.resultText.setText(String.valueOf(lables[getMax(outputFeature.getFloatArray())]));

            quant.close();

        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private int getMax(float[] floatArray) {
        int max = 0;
        for (int i=0; i<floatArray.length; i++){
            if (floatArray[i] > floatArray[max]) max = i;
        }

        return max;
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
                            textToSpeech.speak(binding.resultText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);

                        }
                    }
                },1200);

            }
        } else {
//            Log.e("TextToSpeech", "TextToSpeech initialization failed");
            Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


}