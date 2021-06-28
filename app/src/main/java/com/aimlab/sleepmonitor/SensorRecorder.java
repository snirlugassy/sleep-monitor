package com.aimlab.sleepmonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SensorRecorder extends Activity implements SensorEventListener {
    private final Sensor sensor;
    private final SensorManager sensorManager;
    private Intent dataChangedIntent;
    private int dataBroadcastInterval;
    private float[] rotationMatrix;
    private float[] adjustedRotationMatrix;
    private float[] orientation;

    protected Date begin_time, end_time;
    protected Context context;

    public ArrayList<float[]> data;
    public ArrayList<Long> data_timestamps;
    public int sensorType;
    public int samplingDelay;

    public SensorRecorder(Context context, int sensorType, float frequency) {
        data = new ArrayList<float[]>();
        data_timestamps = new ArrayList<Long>();

        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);
        this.context = context;
        this.sensorType = sensorType;
        this.samplingDelay = (int) ((1 / frequency) * Math.pow(10,6));
        this.dataChangedIntent = new Intent("sensor-data");
        this.dataBroadcastInterval = 4;
        this.rotationMatrix = new float[9];
        this.adjustedRotationMatrix = new float[9];
        this.orientation = new float[3];

        // TEST
        Log.i("FREQ", Float.toString(frequency));
        Log.i("DELAY", Integer.toString(samplingDelay));
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, samplingDelay);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensor.getType()) {
            long current_timestamp = System.currentTimeMillis();
            int ts_count = this.data_timestamps.size();
            if (ts_count == 0 || (current_timestamp - this.data_timestamps.get(ts_count-1)) * 1000 > this.samplingDelay) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);
                SensorManager.getOrientation(adjustedRotationMatrix, orientation);

                data_timestamps.add(current_timestamp);
                data.add(orientation.clone());

                System.out.println("Added new data point");
                System.out.println(data.size());

//                System.out.println(Arrays.toString(orientation));

                if (data.size() % dataBroadcastInterval == 0) {
//                    System.out.println("Updating UI vector");
                    dataChangedIntent.putExtra("sensor-values", orientation);
                    dataChangedIntent.putExtra("data-length", data.size());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(dataChangedIntent);
                }
            }
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
        System.out.println("Writing data to file, number of data points:");
        System.out.println(data.size());

        try {
            FileWriter fileWriter = new FileWriter(dataFile, false);
            for (int i = 0; i < data.size(); i++) {
                float [] row = data.get(i);
                fileWriter.write(Long.toString(data_timestamps.get(i)) + ",");
                for (int j=0; j < row.length-1; j++) {
                    fileWriter.write(Float.toString(row[j]) + ",");
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
