package com.williamchrisandy.dailystepsstatictics;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;

import java.time.Year;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SensorService extends Service implements SensorEventListener
{
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private DatabaseReference mDatabase;
    private String deviceId;
    private final String CHANNEL_INFO = "info";
    private final int NOTIF_ID = 1;

    public SensorService()
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(new OnCompleteListener<String>()
                {
                    @Override
                    public void onComplete(@NonNull Task<String> task)
                    {
                        if (task.isSuccessful())
                        {
                            deviceId = task.getResult();
//                            Toast.makeText(SensorService.this, "Device ID:" + deviceId, Toast.LENGTH_SHORT).show();
                        } else
                        {
                            Toast.makeText(SensorService.this, "Unable to get Installation ID", Toast.LENGTH_SHORT).show();
                            stopSelf();
                        }
                    }
                });
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepCounterSensor == null)
        {
            Toast.makeText(this, "The Censor Do Not Exists.", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(this, "Step detector started.", Toast.LENGTH_LONG).show();
        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notifBuilder =
                new NotificationCompat.Builder(this, CHANNEL_INFO)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("Service running")
                        .setContentText("Your step count is being counted.")
                        .setContentIntent(pendingIntent);

        Notification notification = notifBuilder.build();

        NotificationManager notifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(CHANNEL_INFO, "Counter Notification", NotificationManager.IMPORTANCE_LOW);
            notifManager.createNotificationChannel(channel);
        }

        startForeground(NOTIF_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        sensorManager.unregisterListener(this);
        Toast.makeText(this, "Step detector stopped.", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        float step = sensorEvent.values[0];
//        Toast.makeText(this, (int) step + " step detected.", Toast.LENGTH_SHORT).show();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        String year = Integer.toString(calendar.get(Calendar.YEAR));
        String month = Integer.toString(calendar.get(Calendar.MONTH) + 1);
        String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
        String minute = Integer.toString(calendar.get(Calendar.MINUTE));
        String second = Integer.toString(calendar.get(Calendar.SECOND));
        String miLlisecond = Integer.toString(calendar.get(Calendar.MILLISECOND));

        String createdDate = year + "-" + (month.length() < 2 ? "0" + month : month) + "-" + (day.length() < 2 ? "0" + day : day);
        String createdDateDetail = (hour.length() < 2 ? "0" + hour : hour) + ":" + (minute.length() < 2 ? "0" + minute : minute) + ":" + (second.length() < 2 ? "0" + second : second) + ":" + miLlisecond;

        String key = mDatabase.child(createdDate).push().getKey();
        Data data = new Data(deviceId, createdDate + " " + createdDateDetail);
        Map<String, Object> dataValues = data.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + createdDate + "/" + key, dataValues);
        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {

    }

}