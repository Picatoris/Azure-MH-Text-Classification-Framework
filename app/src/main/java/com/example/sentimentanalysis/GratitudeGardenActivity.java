package com.example.sentimentanalysis;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.atomic.AtomicInteger;

public class GratitudeGardenActivity extends AppCompatActivity {

    private ImageView imgFlower;
    private TextView tvCount;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gratitude_garden);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gratitude Garden");
        }

        imgFlower = findViewById(R.id.imgFlower);
        tvCount = findViewById(R.id.tvCount);
        Button btnAdd = findViewById(R.id.btnAddGratitude);

        prefs = getSharedPreferences("wellness", MODE_PRIVATE);
        AtomicInteger count = new AtomicInteger(prefs.getInt("gratitude_total", 0));  // Normal int
        tvCount.setText(count + " flowers planted");

        btnAdd.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("What are you grateful for?")
                    .setMessage("Your garden grows with every entry")
                    .setPositiveButton("Plant", (d, w) -> {
                        count.getAndIncrement();  // Just use ++
                        prefs.edit().putInt("gratitude_total", count.get()).apply();
                        tvCount.setText(count + " flowers planted");
                        animateFlowerGrowth();
                        Toast.makeText(this, "Beautiful!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Later", null)
                    .show();
        });
    }

    private void animateFlowerGrowth() {
        imgFlower.animate()
                .scaleX(1.5f).scaleY(1.5f).setDuration(400)
                .withEndAction(() -> imgFlower.animate()
                        .scaleX(1f).scaleY(1f).rotationBy(360).setDuration(600).start())
                .start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}