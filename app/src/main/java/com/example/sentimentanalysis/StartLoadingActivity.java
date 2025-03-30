package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sentimentanalysis.StartActivity;

public class StartLoadingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_loading);

        progressBar = findViewById(R.id.progressBar);

        // Simulate loading progress
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;

                    // Update the progress bar and display the current value
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });

                    try {
                        // Sleep for 50 milliseconds to simulate loading
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // After loading is complete, you can start a new activity or perform other actions
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StartLoadingActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(StartLoadingActivity.this, StartActivity.class));
                        finish();
                    }
                });
            }
        }).start();
    }
}