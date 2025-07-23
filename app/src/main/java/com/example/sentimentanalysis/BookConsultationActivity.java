package com.example.sentimentanalysis;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class BookConsultationActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    DoctorAdapter adapter;
    List<DoctorModel> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_consultation);

        viewPager = findViewById(R.id.viewPager);
        doctorList = new ArrayList<>();

        // Add sample doctors
        doctorList.add(new DoctorModel("Anand", "Helps with anxiety, depression...", "Hindi, English, Tamil", "CBT", "Mon-Fri", R.drawable.doctor1));
        doctorList.add(new DoctorModel("Priya", "Specializes in trauma therapy...", "English, Kannada", "Trauma", "Mon-Wed", R.drawable.doctor3));
        doctorList.add(new DoctorModel("Karan", "Deals with stress, burnout...", "Hindi, Marathi", "Mindfulness", "Tue-Fri", R.drawable.doctor2));
        viewPager.setOffscreenPageLimit(1);

        adapter = new DoctorAdapter(doctorList);
        viewPager.setAdapter(adapter);
    }
}
