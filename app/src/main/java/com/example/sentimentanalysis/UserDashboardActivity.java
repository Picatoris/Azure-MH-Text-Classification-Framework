package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class UserDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView moodResultTextView; // Not final anymore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize views
        TextView greetingText = findViewById(R.id.greetingText);
        ImageView sideNavButton = findViewById(R.id.sideNavButton);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Retrieve intent extras
        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String regNo = getIntent().getStringExtra("regNo");
        String phone = getIntent().getStringExtra("phone");

        // Set greeting
        if (username != null && !username.isEmpty()) {
            greetingText.setText("Hello, " + username);
        } else {
            greetingText.setText("Hello, User");
        }

        // Update the navigation header with the username
        TextView navHeaderUsername = navigationView.getHeaderView(0).findViewById(R.id.nav_header_username);
        if (username != null && !username.isEmpty()) {
            navHeaderUsername.setText(username);
        }

        // Toggle drawer open/close
        sideNavButton.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.openDrawer(navigationView);
            } else {
                drawerLayout.closeDrawer(navigationView);
            }
        });

        // Handle navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Handle Home
            } else if (id == R.id.nav_guide) {
                Intent intent = new Intent(UserDashboardActivity.this, GuideActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_chat) {
                Intent intent  = new Intent(UserDashboardActivity.this, ChatActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_helpline) {
                Intent intent = new Intent(UserDashboardActivity.this, HelplineActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_book){
                Intent intent = new Intent(UserDashboardActivity.this, BookConsultationActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_mood_check) {
                Intent intent = new Intent(UserDashboardActivity.this, SentimentAnalysisActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("regNo", regNo);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                getSharedPreferences("loginPrefs", MODE_PRIVATE).edit().clear().apply();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(this, UserLoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }

            drawerLayout.closeDrawers(); // Close after selection
            return true;
        });
    }

    public void updateClassificationResult(String result) {
        if (moodResultTextView != null) {
            moodResultTextView.setText("Mood: " + result);
        }
    }
}