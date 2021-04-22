package com.aimlab.sleepmonitor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.aimlab.sleepmonitor.R;
import com.aimlab.sleepmonitor.MonitorFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class RecordingService extends Service {
    private SensorRecorder recorder;
    private static final String notification_title = "Sleeping monitor is running";
    //    private static final String notification_message = "Clic";
    private static final String ticker_text = "ticker1";

    public RecordingService() { }

    @Override
    public void onCreate() {
        Log.i("test", "created recording service now");
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
        int frequency = 1;
        if (intent !=null && intent.getExtras()!=null) {
            frequency = intent.getExtras().getInt("frequency");
        }

        Log.i("MONITOR_SERVICE", "String monitor service!");
        System.out.println("TEST PRINT");
        Notification notification = createNotification();
        startForeground(1, notification);

        // Service work goes here
        recorder = new SensorRecorder(this, Sensor.TYPE_ROTATION_VECTOR, frequency);
        recorder.startRecording();
        //in order to stop the service use: stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        recorder.stopRecording();
        System.out.println("destorying");

        // TODO: set file name as current date & time - yyyy-mm-dd hh:mm:ss AM/PM
        String fileName = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", new java.util.Date()).toString();
        System.out.println("File name = " + fileName);
        File recordingDir = new File(getFilesDir(), "recordings");
        File df = new File(recordingDir, fileName);
        if (recorder.saveDataToFile(df)) {
            System.out.println("saved data to file");
            System.out.println("data dir = " + getFilesDir().toString());

            // Print the data file for testing
//            try {
//                Scanner myReader = new Scanner(df);
//                System.out.println("------ printing data file ---------");
//                while (myReader.hasNextLine()) {
//                    String data = myReader.nextLine();
//                    System.out.println(data);
//                }
//                myReader.close();
//            } catch (FileNotFoundException e) {
//                System.out.println("An error occurred.");
//                e.printStackTrace();
//            }
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