package com.example.voskapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "VoskApplication";
    private TextView textView;
    private Model model;

    private volatile boolean isModelReady = false;
    public boolean isModelReady() {
        return isModelReady;
    }

    // ✅ For integration test wait
    public CountDownLatch modelReadyLatch = new CountDownLatch(1);

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permission granted. Starting transcription...", Toast.LENGTH_SHORT).show();
                    startTranscription();
                } else {
                    Toast.makeText(this, "Permission denied. Cannot access audio file.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.resultText);
        textView.setText("Loading model...");
        loadVoskModel();
    }

    private void loadVoskModel() {
        new Thread(() -> {
            try {
                String modelPath = copyModelFromAssets();
                model = new Model(modelPath);
                isModelReady = true;
                modelReadyLatch.countDown();  // ✅ Unblock test

                runOnUiThread(() -> {
                    textView.setText("Model loaded. Checking permissions...");

                    // ✅ Avoid IllegalStateException by delaying launcher usage
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        checkAndRequestPermissions();
                    }, 100);
                });

            } catch (IOException e) {
                Log.e(TAG, "Error loading model", e);
                isModelReady = false;
                modelReadyLatch.countDown();  // ✅ Ensure test unblocks even on error

                runOnUiThread(() -> {
                    textView.setText("Model load error: " + e.getMessage());
                    Toast.makeText(this, "Model load failed", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }


    private void checkAndRequestPermissions() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startTranscription();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void startTranscription() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            File audioFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "output.wav");
            if (audioFile.exists()) {
                convertSpeechToText(Uri.fromFile(audioFile));
            } else {
                textView.setText("output.wav not found in Downloads folder.");
            }
        }, 1000);
    }

    private void convertSpeechToText(Uri uri) {
        textView.setText("Transcribing...");
        new Thread(() -> {
            Recognizer recognizer = null;
            InputStream inputStream = null;
            try {
                recognizer = new Recognizer(model, 16000.0f);
                inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) throw new IOException("Failed to open input stream");

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    recognizer.acceptWaveForm(buffer, bytesRead);
                }
                String result = recognizer.getFinalResult();
                runOnUiThread(() -> textView.setText("Result: " + result));
            } catch (IOException e) {
                Log.e(TAG, "Error transcribing audio", e);
                runOnUiThread(() -> textView.setText("Error: " + e.getMessage()));
            } finally {
                if (recognizer != null) recognizer.close();
                if (inputStream != null) try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }).start();
    }

    private String copyModelFromAssets() throws IOException {
        AssetManager assetManager = getAssets();
        File modelDir = new File(getFilesDir(), "vosk_model");
        if (!modelDir.exists()) modelDir.mkdirs();

        copyAssetFolder(assetManager, "model", modelDir.getAbsolutePath());

        return modelDir.getAbsolutePath();
    }

    private boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) throws IOException {
        String[] files = assetManager.list(fromAssetPath);
        boolean res = true;

        if (files == null || files.length == 0) {
            // Copy file
            res &= copyAsset(assetManager, fromAssetPath, toPath);
        } else {
            File dir = new File(toPath);
            if (!dir.exists()) dir.mkdirs();
            for (String file : files)
                res &= copyAssetFolder(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
        }
        return res;
    }

    private boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) throws IOException {
        InputStream in = assetManager.open(fromAssetPath);
        OutputStream out = new FileOutputStream(toPath);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (model != null) model.close();
    }
}
