package com.example.numberguessingapp;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private DrawView drawView;
    private Button btnClear, btnPredict;
    private TextView tvPrediction, tvConfidence, tvLogout;

    private Interpreter tfliteInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI elements ko XML se connect karna
        drawView = findViewById(R.id.drawView);
        btnClear = findViewById(R.id.btnClear);
        btnPredict = findViewById(R.id.btnPredict);
        tvPrediction = findViewById(R.id.tvPrediction);
        tvConfidence = findViewById(R.id.tvConfidence);
        tvLogout = findViewById(R.id.tvLogout);

        // TFLite Model ko load karna
        try {
            tfliteInterpreter = new Interpreter(loadModelFile());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Model loaded!", Toast.LENGTH_SHORT).show();
        }

        // Logout Click Listener
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Clear Button Click Listener
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clearCanvas();
                tvPrediction.setText("Prediction: -");
                tvConfidence.setText("Confidence: 0%");
            }
        });

        // Predict Button Click Listener (Asal AI Model Prediction)
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tfliteInterpreter != null) {
                    try {
                        // 1. DrawView se bitmap lena aur 28x28 par resize karna
                        Bitmap bitmap = drawView. getBitmap();                                                // Ensure DrawView mein getBitmap() method mojood ho
                        if (bitmap != null) {
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true);


                            float[][] result = new float[1][10];
                            float[][][][] input = preprocessImage(scaledBitmap);


                            tfliteInterpreter.run(input, result);


                            int digit = 0;
                            float maxProbability = result[0][0];
                            for (int i = 1; i < 10; i++) {
                                if (result[0][i] > maxProbability) {
                                    maxProbability = result[0][i];
                                    digit = i;
                                }
                            }

                            int confidence = (int) (maxProbability * 100);

                            // 5. UI par result show karna
                            tvPrediction.setText("Prediction: " + digit);
                            tvConfidence.setText("Confidence: " + confidence + "%");
                        } else {
                            Toast.makeText(MainActivity.this, "Firstly draw something!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "There is an error in predection!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "model is not initializing!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Assets folder se .tflite file read karne ka method
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("digit_model.tflite"); // Agar aap ki file ka naam kuch aur hai jaise digit_model.tflite toh yahan change kar lein
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Bitmap ko model ke input format (28x28 grayscale normalized) mein convert karna
    private float[][][][] preprocessImage(Bitmap bitmap) {
        float[][][][] inputArray = new float[1][28][28][1];
        int[] pixels = new int[28 * 28];
        bitmap.getPixels(pixels, 0, 28, 0, 0, 28, 28);

        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                int pixel = pixels[i * 28 + j];
                // Background black (0) aur drawing white (255) hone ki normalization
                float red = (pixel >> 16) & 0xFF;
                float green = (pixel >> 8) & 0xFF;
                float blue = pixel & 0xFF;
                float gray = (red + green + blue) / 3.0f / 255.0f;
                inputArray[0][i][j][0] = gray;
            }
        }
        return inputArray;
    }
}