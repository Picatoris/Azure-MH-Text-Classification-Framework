package com.example.sentimentanalysis;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChartPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_chart);

        LineChart lineChart = findViewById(R.id.lineChart);
        TextView chartHeading = findViewById(R.id.chartHeading);

        // Retrieve dates and results from intent
        ArrayList<String> dates = getIntent().getStringArrayListExtra("dates");
        ArrayList<String> results = getIntent().getStringArrayListExtra("results");

        if (dates == null || results == null) {
            Log.e("ChartPopupActivity", "Dates or results data is missing");
            return;
        }

        Log.d("ChartPopupActivity", "Dates: " + dates);
        Log.d("ChartPopupActivity", "Results: " + results);

        // Map to store sentiment values
        Map<String, Integer> sentimentMap = new HashMap<>();
        sentimentMap.put("positive", 2);
        sentimentMap.put("neutral", 1);
        sentimentMap.put("negative", 0);

        // List to store entries for the line chart
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            String result = results.get(i);
            Integer sentimentValue = sentimentMap.get(result);

            if (sentimentValue != null) {
                entries.add(new Entry(i, sentimentValue));
            } else {
                Log.e("ChartPopupActivity", "Invalid sentiment value: " + result);
            }
        }

        // List to hold data sets for the chart
        List<ILineDataSet> dataSets = new ArrayList<>();

        // Create LineDataSet for each sentiment transition
        List<Entry> segmentEntries = new ArrayList<>();
        int currentColor = Color.TRANSPARENT;
        LineDataSet currentDataSet = null;

        for (int i = 0; i < entries.size() - 1; i++) {
            Entry startEntry = entries.get(i);
            Entry endEntry = entries.get(i + 1);

            int startSentiment = (int) startEntry.getY();
            int endSentiment = (int) endEntry.getY();

            int segmentColor;
            if (startSentiment == 0 && endSentiment == 1) {
                segmentColor = Color.YELLOW; // Negative to Neutral
            } else if (startSentiment == 0 && endSentiment == 2) {
                segmentColor = Color.GREEN;  // Negative to Positive
            } else if (startSentiment == 1 && endSentiment == 2) {
                segmentColor = Color.GREEN;  // Neutral to Positive
            } else if (startSentiment == 1 && endSentiment == 0) {
                segmentColor = Color.RED;    // Neutral to Negative
            } else if (startSentiment == 2 && endSentiment == 1) {
                segmentColor = Color.YELLOW; // Positive to Neutral
            } else if (startSentiment == 2 && endSentiment == 0) {
                segmentColor = Color.RED;    // Positive to Negative
            } else {
                segmentColor = currentColor; // No change in sentiment
            }

            if (segmentColor != currentColor) {
                if (currentDataSet != null) {
                    currentDataSet.setValues(segmentEntries);
                    dataSets.add(currentDataSet);
                }
                segmentEntries = new ArrayList<>();
                currentDataSet = new LineDataSet(segmentEntries, "");
                currentDataSet.setColor(segmentColor);
                currentDataSet.setLineWidth(2.5f);
                currentColor = segmentColor;
            }

            if (currentDataSet != null) {
                segmentEntries.add(startEntry);
            }
        }

        if (currentDataSet != null && !segmentEntries.isEmpty()) {
            currentDataSet.setValues(segmentEntries);
            dataSets.add(currentDataSet);
        }

        // Create LineData with the list of data sets
        LineData lineData = new LineData(dataSets);

        // Set data to the chart
        lineChart.setData(lineData);

        // Customizing the XAxis to show dates
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new XAxisValueFormatter(dates));
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(dates.size());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);

        // Customizing the YAxis to show sentiment labels
        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setValueFormatter(new YAxisValueFormatter());
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setGranularityEnabled(true);
        yAxisLeft.setLabelCount(3, true); // Set the number of labels to 3 for "Positive", "Neutral", "Negative"

        // Disable right YAxis
        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        // Customizing Legend
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // Custom Legend Entries
        LegendEntry[] legendEntries = new LegendEntry[]{
                new LegendEntry("Negative", Legend.LegendForm.SQUARE, 8f, 8f, null, Color.RED),
                new LegendEntry("Neutral", Legend.LegendForm.SQUARE, 8f, 8f, null, Color.YELLOW),
                new LegendEntry("Positive", Legend.LegendForm.SQUARE, 8f, 8f, null, Color.GREEN)
        };
        legend.setCustom(legendEntries);

        // Refresh the chart
        lineChart.invalidate();
    }

    // Custom XAxis Value Formatter
    public static class XAxisValueFormatter extends ValueFormatter {

        private final ArrayList<String> dates;
        private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.ENGLISH);

        public XAxisValueFormatter(ArrayList<String> dates) {
            this.dates = dates;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = (int) value;
            if (index >= 0 && index < dates.size()) {
                String dateTimeString = dates.get(index);
                try {
                    Date date = inputFormat.parse(dateTimeString);
                    assert date != null;
                    return outputFormat.format(date);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    return dateTimeString; // Handle error case gracefully
                }
            } else {
                return "";
            }
        }
    }

    // Custom YAxis Value Formatter
    private static class YAxisValueFormatter extends ValueFormatter {

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            switch ((int) value) {
                case 2:
                    return "Positive";
                case 1:
                    return "Neutral";
                case 0:
                    return "Negative";
                default:
                    return "";
            }
        }
    }
}