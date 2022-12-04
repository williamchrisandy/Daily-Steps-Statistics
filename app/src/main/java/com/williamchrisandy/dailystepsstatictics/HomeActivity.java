package com.williamchrisandy.dailystepsstatictics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity
{
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void startCountingStep(View view)
    {
        dynamicPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED)
        {
            serviceIntent = new Intent(this, SensorService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        } else
        {
            Toast.makeText(this, "Step counter cannot be started because permission denied.", Toast.LENGTH_LONG).show();
        }
    }

    public void stopCountingStep(View view)
    {
        if (serviceIntent != null)
        {
            stopService(serviceIntent);
        } else
        {
            Toast.makeText(this, "You have not started the service yet.", Toast.LENGTH_LONG).show();
        }
    }

    public void viewStepHistory(View view)
    {
        Intent intent = new Intent(this, ViewActivity.class);
        startActivity(intent);
    }

    private void dynamicPermission()
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
        {
            String[] strings = {Manifest.permission.ACTIVITY_RECOGNITION};
            ActivityCompat.requestPermissions(this, strings, 1);
        }
    }

}