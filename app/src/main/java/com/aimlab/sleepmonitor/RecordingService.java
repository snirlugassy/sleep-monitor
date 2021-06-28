package com.aimlab.sleepmonitor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.aimlab.sleepmonitor.R;
import com.aimlab.sleepmonitor.MonitorFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class RecordingService extends Service {
    private SensorRecorder recorder;
    private static final String notification_title = "Sleeping monitor is running";
    //    private static final String notification_message = "Clic";
    private static final String ticker_text = "ticker1";
    private static final int DATA_CHANGED_MESSAGE = 1;
    private Handler msgHandler;
    private Logger logger;

    public boolean saveFileOnDestruction;
    public float frequency;

    public RecordingService() { }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, MonitorFragment.CHANNEL_ID)
                .setContentTitle(notification_title)
//                .setContentText(notification_message)
                .setSmallIcon(R.drawable.ic_baseline_bedtime_24)
                .setContentIntent(pendingIntent)
                .setTicker(ticker_text)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
//                .addAction(android.R.drawable.ic_media_pause, "Pause")
                .build();

        return notification;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger = new Logger(this);
        logger.write("RECORDING STARTED", true);
        if (intent !=null && intent.getExtras()!=null) {
            frequency = intent.getFloatExtra("frequency", 1);
            saveFileOnDestruction = !intent.getBooleanExtra("therapy", true);
            if (saveFileOnDestruction) {
                System.out.println("I will save a file");
            } else {
                System.out.println("I will not save a file");
            }
        }

        Notification notification = createNotification();
        startForeground(1, notification);

        // Service work goes here
        recorder = new SensorRecorder(this, Sensor.TYPE_ROTATION_VECTOR, frequency);
        recorder.startRecording();
        //in order to stop the service use: stopSelf();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        logger.write("RECORDING STOPPED", true);

        recorder.stopRecording();

        if (saveFileOnDestruction) {
            String fileName = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", new java.util.Date()).toString();
            System.out.println("File name = " + fileName);
            File recordingDir = new File(getExternalFilesDir(null), "recordings");
            File df = new File(recordingDir, fileName + getResources().getString(R.string.file_suffix));
            if (recorder.saveDataToFile(df)) {
                System.out.println("data was saved to file successfully");
            }

        }

        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}