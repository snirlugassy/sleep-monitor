package com.aimlab.sleepmonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Arrays;

public class MonitorFragment extends Fragment implements View.OnClickListener {
    public static final String CHANNEL_ID = "SLEEP_MONITOR_CHANNEL";
    public TextView isRecordingTextView;
    public Button monitoringButton;

    public SeekBar frequencyBar;
    public TextView frequencyBarLabel;

    private boolean isRecording = false;
    private int samplingFrequency;
    private static final int[] frequencies = {1,2,5,10};

    public MonitorFragment() {
        // Required empty public constructor
    }

    public static MonitorFragment newInstance() {
        return new MonitorFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);
        Resources resources = getResources();
        isRecordingTextView = (TextView) view.findViewById(R.id.isRecordingTextView);
        monitoringButton = (Button) view.findViewById(R.id.monitoring_button);
        monitoringButton.setOnClickListener(this);

        // Frequency
        int freq = resources.getInteger(R.integer.default_sampling_freq);

        frequencyBar = (SeekBar) view.findViewById(R.id.frequency_bar);
        frequencyBarLabel = (TextView) view.findViewById(R.id.frequency_bar_label);
        frequencyBar.setMax(frequencies.length-1);

        int defaultFreqIndex = Arrays.binarySearch(frequencies, freq);
        if (defaultFreqIndex > 0) {
            frequencyBar.setProgress(defaultFreqIndex);
        } else {
            // Default frequency was not found - fall back to the first
            samplingFrequency = frequencies[0];
            frequencyBar.setProgress(0);
        }

        setSamplingFrequency(freq);

        frequencyBar.setProgress(Arrays.binarySearch(frequencies, samplingFrequency));
        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println(frequencies[progress]);
                setSamplingFrequency(frequencies[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "name";
            String description = "desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void toggleMonitoring(View view) {
        toggleMonitoringButton();
        if (isRecording) {
            // Stop monitoring service
            getContext().stopService(new Intent(getContext(), RecordingService.class));
            isRecordingTextView.setText(R.string.stopped_recording);
            // isRecordingTextView.setTextColor(Color.RED);
            isRecording = false;
            frequencyBar.setEnabled(true);
        } else {
            // Start monitoring service
            Intent recordingServiceIntent = new Intent(getContext(), RecordingService.class);
            recordingServiceIntent.putExtra("frequency", samplingFrequency);
            getContext().startService(recordingServiceIntent);

            isRecordingTextView.setText(R.string.recording_now);
            // isRecordingTextView.setTextColor(Color.GREEN);
            isRecording = true;
            frequencyBar.setEnabled(false);
        }
    }

    private void toggleMonitoringButton() {
        if (isRecording) {
            // Switch from on to off
            monitoringButton.setBackgroundColor(getResources().getColor(R.color.green));
            monitoringButton.setText(R.string.start_monitoring);
        } else {
            // Switch from off to on
            monitoringButton.setBackgroundColor(getResources().getColor(R.color.red));
            monitoringButton.setText(R.string.stop_monitoring);
        }
    }

    public void setSamplingFrequency(int freq) {
        samplingFrequency = freq;
        String label = getResources().getString(R.string.frequency_label);
        label = label + " " + Integer.toString(freq);
        frequencyBarLabel.setText(label);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.monitoring_button:
                toggleMonitoring(v);
        }
    }
}