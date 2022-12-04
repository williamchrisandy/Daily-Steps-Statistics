package com.williamchrisandy.dailystepsstatictics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Random;
import java.util.Vector;

public class ViewActivity extends AppCompatActivity
{
//    TextView textViewTest;

    private Vector<StepCount> stepCountVector;
    private DatabaseReference mDatabase;
    private LineChart lineChart;
    int lastMinute;
    boolean isRunning;

    class XAxisValueFormatter extends IndexAxisValueFormatter
    {

        private String[] mValues;

        public XAxisValueFormatter(String[] values)
        {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value)
        {
            int intValue = (int) value;
            return (intValue < 0 || intValue >= mValues.length) ? "" : mValues[intValue];
        }
    }

    //Params
    //Progress
    //Result
    private class RefreshTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids)
        {
            isRunning = true;
            while (isRunning)
            {
                try
                {
                    mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                DataSnapshot dataSnapshot = task.getResult();
                                if(dataSnapshot != null)
                                {
                                    int minute = 0;

                                    stepCountVector.clear();
                                    for (DataSnapshot child : dataSnapshot.getChildren())
                                    {
                                        String deviceId = (String) child.child("deviceId").getValue();
                                        String date = (String) child.child("date").getValue();
                                        int index = searchDeviceIdIndex(deviceId);

                                        if (date != null)
                                        {
                                            minute = (date.charAt(11) - '0') * 600;
                                            minute += (date.charAt(12) - '0') * 60;
                                            minute += (date.charAt(14) - '0') * 10;
                                            minute += date.charAt(15) - '0';
                                        }

                                        if (index == -1) stepCountVector.add(new StepCount(deviceId, minute));
                                        else stepCountVector.get(index).addStep(minute);
//                                      textViewTest.setText(textViewTest.getText().toString() + "\n" + deviceId + " " + date);
                                    }

                                    if (minute > lastMinute) refreshData();
                                    else if (minute < lastMinute) ViewActivity.super.recreate();

                                    lastMinute = minute;
                                }
                            }
                        }
                    });

                    Thread.sleep(60000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids)
        {
            super.onProgressUpdate(voids);
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

//        textViewTest = findViewById(R.id.text_view_test);
        lineChart = findViewById(R.id.line_chart);

        stepCountVector = new Vector<>();

        Legend legend = lineChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        String[] hours = getXString();
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new XAxisValueFormatter(hours));

        lineChart.getDescription().setEnabled(false);
        lineChart.setDragEnabled(true);
        lineChart.setScrollbarFadingEnabled(true);
    }

    @Override
    protected void onResume()
    {
        lastMinute = -1;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        String year = Integer.toString(calendar.get(Calendar.YEAR));
        String month = Integer.toString(calendar.get(Calendar.MONTH) + 1);
        String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));

        String viewDate = year + "-" + (month.length() < 2 ? "0" + month : month) + "-" + (day.length() < 2 ? "0" + day : day);

        mDatabase = FirebaseDatabase.getInstance().getReference(viewDate);
        RefreshTask refreshTask = new RefreshTask();
        refreshTask.execute();
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        isRunning = false;
        super.onPause();
    }

    private String[] getXString()
    {
        int i, j;
        String[] hours = new String[1445];
        String hour, minute;
        for (i = 0; i < 24; i++)
        {
            hour = Integer.toString(i);
            if (hour.length() < 2) hour = "0" + hour;
            for (j = 0; j < 60; j++)
            {
                minute = Integer.toString(j);
                if (minute.length() < 2) minute = "0" + minute;

                hours[i * 60 + j] = hour + ":" + minute;
            }
        }

        return hours;
    }

    private int searchDeviceIdIndex(String deviceId)
    {
        int i = 0, size = stepCountVector.size();
        for (i = 0; i < size; i++)
        {
            if (stepCountVector.get(i).getDeviceId().equals(deviceId)) return i;
        }
        return -1;
    }

    private void refreshData()
    {
        int i, j, size = stepCountVector.size();
        Random random = new Random();
        LineData lineData = new LineData();

        for (i = 0; i < size; i++)
        {
            Vector<Entry> deviceData = new Vector<>();
            StepCount stepCount = stepCountVector.get(i);
            int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

            for (j = 0; j < 1440; j++)
                if (stepCount.isEnabled(j)) deviceData.add(new Entry(j, stepCount.getCount(j)));

            LineDataSet lineDataSet = new LineDataSet(deviceData, stepCount.getDeviceId());
            lineDataSet.setMode(LineDataSet.Mode.LINEAR);
            lineDataSet.setColor(color);
            lineDataSet.setCircleRadius(5f);
            lineDataSet.setCircleColor(color);

            lineData.addDataSet(lineDataSet);

        }

        lineChart.refreshDrawableState();
        lineChart.setData(lineData);
        lineChart.animateXY(100, 500);
    }
}