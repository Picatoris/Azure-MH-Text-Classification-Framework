package com.example.sentimentanalysis;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HelplineActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView iconMenu;
    HelplineAdapter adapter;
    ArrayList<HelplineModel> helplineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpline); // Your activity layout

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        iconMenu = findViewById(R.id.iconMenu);

        iconMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        helplineList = new ArrayList<>();

        helplineList.add(new HelplineModel(
                "Tele MANAS",
                "Free 24x7 support and therapy from licensed professionals.",
                "18008914848",
                "24/7"
        ));

        helplineList.add(new HelplineModel(
                "iCall",
                "TISS initiative providing psychosocial support and mental health services.",
                "9152987821",
                "24/7"
        ));

        helplineList.add(new HelplineModel(
                "AASRA",
                "Emotional support for suicidal and depressed individuals.",
                "9820466726",
                "24/7"
        ));

        helplineList.add(new HelplineModel(
                "Jeevan Aastha",
                "Support line operated by Gujarat state government.",
                "18002333330",
                "24/7"
        ));

        adapter = new HelplineAdapter(this, helplineList);
        recyclerView.setAdapter(adapter);
    }
}
