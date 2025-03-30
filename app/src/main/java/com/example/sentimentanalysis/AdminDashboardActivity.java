package com.example.sentimentanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class AdminDashboardActivity extends AppCompatActivity {

    private ArrayList<String> regNoList;
    private ArrayAdapter<String> regNoAdapter;
    private String selectedRegNo = "All Batches";

    private ArrayList<String> usernamesList;
    private RecyclerViewAdapter recyclerViewAdapter;

    private int negativeCount = 0;
    private int positiveCount = 0;
    private int neutralCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        PieChart pieChart = findViewById(R.id.pieChart);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabViewChart = findViewById(R.id.fabViewChart);
        Spinner regNoSpinner = findViewById(R.id.regNoSpinner);

        usernamesList = new ArrayList<>();
        recyclerViewAdapter = new RecyclerViewAdapter(usernamesList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(recyclerViewAdapter);

        regNoList = new ArrayList<>();
        regNoList.add("Select Here");
        regNoList.add("All Batches");
        regNoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, regNoList);
        regNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regNoSpinner.setAdapter(regNoAdapter);

        // Set 'All Batches' as the default selection in the spinner
        regNoSpinner.setSelection(0);
        filterRecyclerView();
        DatabaseReference sentimentsReference = FirebaseDatabase.getInstance().getReference("sentiments");

        sentimentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usernamesList.clear();
                regNoList.clear();
                regNoList.add("Select Here");
                regNoList.add("All Batches");

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    usernamesList.add(username);

                    for (DataSnapshot uniqueKeySnapshot : userSnapshot.getChildren()) {
                        String regNo = uniqueKeySnapshot.child("regNo").getValue(String.class);

                        if (regNo != null && regNo.length() >= 2) {
                            // Extract the first two characters of the registration number
                            String regNoPrefix = regNo.substring(0, 2);

                            // Ensure that only digits are added
                            if (regNoPrefix.matches("\\d{2}") && !regNoList.contains(regNoPrefix)) {
                                regNoList.add(regNoPrefix);
                            }
                        } else {
                            Log.w("AdminDashboardActivity", regNo == null ? "regNo is null for user " + username : "regNo is too short: " + regNo);
                        }
                    }
                }

                regNoAdapter.notifyDataSetChanged();
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorDialog("Database Error", error.getMessage());
            }
        });

        recyclerViewAdapter.setOnItemClickListener(position -> {
            String selectedUsername = usernamesList.get(position);
            displayUserData(selectedUsername);
        });

        fabViewChart.setOnClickListener(v -> showPieChart());

        regNoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedRegNo = regNoList.get(position);
                filterRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    private void filterRecyclerView() {
        recyclerViewAdapter.filterByRegNo(selectedRegNo);
    }

    private void displayUserData(String username) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("sentiments").child(username);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<UserDataEntry> userDataEntries = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot uniqueKeySnapshot : snapshot.getChildren()) {
                        String results = uniqueKeySnapshot.child("result").getValue(String.class);
                        String sentence = uniqueKeySnapshot.child("sentence").getValue(String.class);
                        String dateTime = uniqueKeySnapshot.child("dateTime").getValue(String.class);
                        String regNo = uniqueKeySnapshot.child("regNo").getValue(String.class);
                        String sadClassification = uniqueKeySnapshot.child("SadClassification").getValue(String.class);
                        String suggestion = uniqueKeySnapshot.child("suggestion").getValue(String.class);

                        // Ensure SadClassification is not "Null"
                        if ("Null".equals(sadClassification)) {
                            sadClassification = null;
                        }

                        UserDataEntry entry = new UserDataEntry(results, sentence, dateTime, regNo, sadClassification, suggestion);
                        userDataEntries.add(entry);
                    }
                    showDataDialog(username, userDataEntries);
                } else {
                    showErrorDialog("User Data Error", "No data found for the selected user.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorDialog("Database Error", error.getMessage());
            }
        });
    }

    private void showDataDialog(String username, ArrayList<UserDataEntry> userDataEntries) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Data - " + username);

        // Inflate custom layout for displaying multiple entries
        View dialogView = getLayoutInflater().inflate(R.layout.user_data_dialog, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.userDataRecyclerView);
//        FloatingActionButton fab = dialogView.findViewById(R.id.fab);

        // Set layout manager and adapter for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        UserDataAdapter adapter = new UserDataAdapter(userDataEntries);
        recyclerView.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();

        // Show the FAB
//        fab.setVisibility(View.VISIBLE);

        // Handle FAB click event
//        fab.setOnClickListener(v -> {
//            // Prepare data for the chart
//            ArrayList<String> dates = new ArrayList<>();
//            ArrayList<String> results = new ArrayList<>();
//            for (UserDataEntry e : userDataEntries) {
//                dates.add(e.getDateTime());
//                results.add(e.getResults());
//            }
//
//            // Start ChartPopupActivity with the prepared data
//            Intent intent = new Intent(this, ChartPopupActivity.class);
//            intent.putStringArrayListExtra("dates", dates);
//            intent.putStringArrayListExtra("results", results);
//
//            startActivity(intent);
//        });
    }

    private void showPieChart() {
        DatabaseReference sentimentsReference = FirebaseDatabase.getInstance().getReference("sentiments");

        sentimentsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                negativeCount = 0;
                positiveCount = 0;
                neutralCount = 0;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot uniqueKeySnapshot : userSnapshot.getChildren()) {
                        String result = uniqueKeySnapshot.child("result").getValue(String.class);

                        switch (Objects.requireNonNull(result)) {
                            case "negative":
                                negativeCount++;
                                break;
                            case "positive":
                                positiveCount++;
                                break;
                            case "neutral":
                                neutralCount++;
                                break;
                        }
                    }
                }

                // Update pie chart here
                updatePieChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorDialog("Database Error", error.getMessage());
            }
        });
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(negativeCount, "Negative"));
        entries.add(new PieEntry(positiveCount, "Positive"));
        entries.add(new PieEntry(neutralCount, "Neutral"));

        PieDataSet dataSet = new PieDataSet(entries, "Sentiment Analysis Results");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);

        PieChart pieChart = findViewById(R.id.pieChart);
        pieChart.setData(data);
        pieChart.invalidate();

        String toastMessage = "Negative: " + negativeCount + ", Positive: " + positiveCount + ", Neutral: " + neutralCount;
        Toast.makeText(AdminDashboardActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private final ArrayList<String> usernamesList;
        private final ArrayList<String> filteredList;
        private OnItemClickListener onItemClickListener;

        public RecyclerViewAdapter(ArrayList<String> usernamesList) {
            this.usernamesList = usernamesList;
            this.filteredList = new ArrayList<>(usernamesList);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String username = filteredList.get(position);
            holder.bindData(username);
            holder.itemView.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(usernamesList.indexOf(filteredList.get(adapterPosition)));
                }
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            onItemClickListener = listener;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView regNoTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                regNoTextView = itemView.findViewById(R.id.regNoTextView);
            }

            public void bindData(String username) {
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("sentiments").child(username);

                userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String regNo = null;

                            for (DataSnapshot uniqueKeySnapshot : snapshot.getChildren()) {
                                regNo = uniqueKeySnapshot.child("regNo").getValue(String.class);
                            }

                            // Bind data to views
                            nameTextView.setText(username);
                            if (regNo != null) {
                                regNoTextView.setText(regNo);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
            }
        }

        public void filterByRegNo(String regNo) {
            filteredList.clear();
            if (regNo.equals("All Batches")) {
                filteredList.addAll(usernamesList);
            } else {
                for (String username : usernamesList) {
                    // Check if username's regNo starts with the selected regNo
                    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("sentiments").child(username);

                    userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String regNo = null;

                                for (DataSnapshot uniqueKeySnapshot : snapshot.getChildren()) {
                                    regNo = uniqueKeySnapshot.child("regNo").getValue(String.class);
                                }

                                // Filter usernames that start with the selected regNo
                                if (regNo != null && regNo.startsWith(selectedRegNo)) {
                                    filteredList.add(username);
                                }
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error
                        }
                    });
                }
            }
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class UserDataEntry {
        private final String results;
        private final String sentence;
        private final String dateTime;
        private final String regNo;
        private final String sadClassification;
        private final String suggestion;
        private final boolean expanded; // Track expanded state

        public UserDataEntry(String results, String sentence, String dateTime, String regNo, String sadClassification, String suggestion) {
            this.results = results;
            this.sentence = sentence;
            this.dateTime = dateTime;
            this.regNo = regNo;
            this.sadClassification = sadClassification;
            this.suggestion = suggestion;
            this.expanded = false; // Initially not expanded
        }

        // Getters and setters
        public boolean isExpanded() {
            return expanded;
        }

        public String getResults() {
            return results;
        }

        public String getSentence() {
            return sentence;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getRegNo() {
            return regNo;
        }

        public String getSadClassification() {
            return sadClassification;
        }

        public String getSuggestion() {
            return suggestion;
        }
    }


    public static class UserDataAdapter extends RecyclerView.Adapter<UserDataAdapter.ViewHolder> {
        private final ArrayList<UserDataEntry> userDataEntries;

        public UserDataAdapter(ArrayList<UserDataEntry> userDataEntries) {
            this.userDataEntries = userDataEntries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_data_entry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserDataEntry entry = userDataEntries.get(position);
            holder.bindData(entry);

            holder.fab.setOnClickListener(v -> {
                // Prepare data for the chart
                ArrayList<String> dates = new ArrayList<>();
                ArrayList<String> results = new ArrayList<>();
                for (UserDataEntry e : userDataEntries) {
                    dates.add(e.getDateTime());
                    results.add(e.getResults());
                }

                Intent intent = new Intent(v.getContext(), ChartPopupActivity.class);
                intent.putStringArrayListExtra("dates", dates);
                intent.putStringArrayListExtra("results", results);

                // Log to verify intent creation
                Log.d("FABClick", "Starting ChartPopupActivity with Intent");
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return userDataEntries.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView resultsTextView;
            TextView sentenceTextView;
            TextView dateTimeTextView;
            TextView regNoTextView;
            TextView sadClassificationTextView;
            TextView suggestionTextView;
            FloatingActionButton fab;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                resultsTextView = itemView.findViewById(R.id.resultsTextView);
                sentenceTextView = itemView.findViewById(R.id.sentenceTextView);
                dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
                regNoTextView = itemView.findViewById(R.id.regNoTextView);
                sadClassificationTextView = itemView.findViewById(R.id.sadClassificationTextView);
                suggestionTextView = itemView.findViewById(R.id.suggestionTextView);
                fab = itemView.findViewById(R.id.fab);
            }

            public void bindData(UserDataEntry entry) {
                resultsTextView.setText("Results: " + entry.getResults());
                sentenceTextView.setText("Sentence: " + entry.getSentence());
                dateTimeTextView.setText("DateTime: " + entry.getDateTime());
                regNoTextView.setText("Reg No: " + entry.getRegNo());

                if (entry.getSadClassification() != null && !entry.getSadClassification().isEmpty()) {
                    sadClassificationTextView.setVisibility(View.VISIBLE);
                    sadClassificationTextView.setText("Sad Classification: " + entry.getSadClassification());
                } else {
                    sadClassificationTextView.setVisibility(View.GONE);
                }

                suggestionTextView.setText(entry.getSuggestion());

                // Show the FAB if the entry is expanded
                fab.setVisibility(entry.isExpanded() ? View.VISIBLE : View.GONE);
            }
        }
    }
}