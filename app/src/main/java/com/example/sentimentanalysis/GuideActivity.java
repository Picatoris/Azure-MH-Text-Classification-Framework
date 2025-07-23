package com.example.sentimentanalysis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

public class GuideActivity extends AppCompatActivity {

    ImageView iconMenu;
    Button btnTellUsMore, read1, read2, read3, read4, read5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        // Initialize buttons
        btnTellUsMore = findViewById(R.id.btnTellUsMore);
        read1 = findViewById(R.id.btnRead1);
        read2 = findViewById(R.id.btnRead2);
        read3 = findViewById(R.id.btnRead3);
        read4 = findViewById(R.id.btnRead4);
        read5 = findViewById(R.id.btnRead5);
        iconMenu = findViewById(R.id.iconMenu);

        // New Tell Us More dialog with EditText
        btnTellUsMore.setOnClickListener(v -> showRemarksDialog());

        iconMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        read1.setOnClickListener(v -> showDialog("Simple Steps to Nurture Your Mind",
                "1. Practice mindfulness daily\n2. Start a gratitude journal\n3. Exercise regularly\n4. Take breaks from screens\n5. Connect with loved ones.\n\nThese steps help foster mental peace and clarity."));

        read2.setOnClickListener(v -> showDialog("A Gentle Path to Mental Wellness",
                "This guide explores the foundational habits for emotional balance and self-awareness, including deep breathing, healthy routines, and more."));

        read3.setOnClickListener(v -> showDialog("The Inner Space Guide",
                "Discover tools that help you remain grounded and resilient during stressful times. Learn to embrace inner peace through guided meditation and mental exercises."));

        read4.setOnClickListener(v -> showDialog("Your Mental Wellness Toolkit",
                "Equip yourself with actionable strategies to manage anxiety, build focus, and stay emotionally fit in today's fast-paced world."));

        read5.setOnClickListener(v -> showDialog("The Mind Reset",
                "Reset your emotional baseline with simple practices like journaling, nature walks, hydration, and meaningful conversations."));
    }

    // Existing dialog for reading guides
    private void showDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_scrollable_text, null);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogContent = dialogView.findViewById(R.id.dialogContent);

        dialogTitle.setText(title);
        dialogContent.setText(content);

        builder.setView(dialogView)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // New dialog for entering user remarks
    private void showRemarksDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_remarks, null);

        EditText inputRemarks = dialogView.findViewById(R.id.editTextRemarks);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitRemarks);

        AlertDialog dialog = builder.setView(dialogView).create();

        btnSubmit.setOnClickListener(v -> {
            String remarks = inputRemarks.getText().toString().trim();
            if (!remarks.isEmpty()) {
                Toast.makeText(this, "Thank you for your input!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                inputRemarks.setError("Please enter your remarks.");
            }
        });

        dialog.show();
    }
}
