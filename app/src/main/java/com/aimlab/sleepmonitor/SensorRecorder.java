package com.aimlab.sleepmonitor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.Context.SENSOR_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class SensorRecorder extends Activity implements SensorEventListener {
    private final Sensor sensor;
    private final SensorManager sensorManager;
    private ArrayList<float[]> data;

    protected Date begin_time, end_time;
    protected Context context;

    public int sensorType;
    public int samplingDelay;

    public SensorRecorder(Context context, int sensorType, int frequency) {
        data = new ArrayList<float[]>();
        Log.i("test", "building recorder");
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);
        this.context = context;
        this.sensorType = sensorType;
        this.samplingDelay = (int) ((float) 1 / frequency * Math.pow(10,6));
        System.out.println("DELAY:");
        System.out.println(samplingDelay);

    }

    protected void onResume() {
        Log.i("test", "recorder resumed");
        super.onResume();
        sensorManager.registerListener(this, sensor, samplingDelay);

    }

    protected void onPause() {
        Log.i("test", "recorder paused");
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensor.getType()) {
            data.add(event.values);
            // Print the sensor data while recording:
            Log.i("SENSOR", Arrays.toString(event.values));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    public void startRecording() {
        sensorManager.registerListener(this, sensor, samplingDelay);
        Log.i("min sensor delay", Integer.toString(sensor.getMinDelay()));
    }

    public void stopRecording() {
        sensorManager.unregisterListener(this);
    }

    public boolean saveDataToFile(File dataFile) {
        // the return value indicates the success of writing the data to file
        try {
            FileWriter fileWriter = new FileWriter(dataFile, false);
            for (float [] row: data) {
                for (int i=0; i<row.length-1; i++) {
                    fileWriter.write(Float.toString(row[i]) + ",");
                }
                fileWriter.write(Float.toString(row[row.length-1]) + "\n");
            }
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList getData() { return this.data; }
    public Date getBeginTime() { return this.begin_time; }
    public Date getEndTime() { return this.end_time; }
}
