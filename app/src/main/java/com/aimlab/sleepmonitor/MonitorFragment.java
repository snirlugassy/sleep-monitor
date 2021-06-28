package com.aimlab.sleepmonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;

public class MonitorFragment extends Fragment {
    private static final String IS_MONITORING_KEY = "IS_RECORDING_KEY";

    public static final String CHANNEL_ID = "SLEEP_MONITOR_CHANNEL";
    public TextView isRecordingTextView;
    public TextView sensorValuesTextView;
    public Button monitoringButton;

    public SeekBar frequencyBar;
    public TextView frequencyBarLabel;

    private EditText delayInput;
    private int recordingDelay;

    private boolean isMonitoring = false;
    private float samplingFrequency;
    private static final int[] frequencies = {1,2,5,10};
    private float [] sensorValues;
    private int dataLength;

    private Switch therapyModeSwitch;
    private boolean therapyMode;

    private CountDownTimer recordingTimer;
    private Logger logger;

    private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sensorValues = intent.getFloatArrayExtra("sensor-values");
            dataLength = intent.getIntExtra("data-length", -1);
            if (sensorValuesTextView != null) {
                sensorValuesTextView.setText(Integer.toString(dataLength) + ": " + Arrays.toString(sensorValues));
            }
        }
    };

    public MonitorFragment() {
        // Required empty public constructor
    }

    public static MonitorFragment newInstance() {
        return new MonitorFragment();
    }

    private void initMonitoringButton(View view) {
        monitoringButton = (Button) view.findViewById(R.id.monitoring_button);

        if (isMonitoring) {
            monitoringButton.setBackgroundColor(getResources().getColor(R.color.red));
            monitoringButton.setText(R.string.stop_monitoring);
        } else {
            monitoringButton.setBackgroundColor(getResources().getColor(R.color.green));
            monitoringButton.setText(R.string.start_monitoring);
        }

        // Click to start, hold to stop
        monitoringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.monitoring_button:
                        if (!isMonitoring) {
                            startMonitoring(view);
                        }
                }
            }
        });

        monitoringButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (v.getId()) {
                    case R.id.monitoring_button:
                        if (isMonitoring) {
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                            builder.setMessage("Are you sure?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            stopMonitoring();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {}
                                    });

                            AlertDialog confirmDialog = builder.create();
                            confirmDialog.show();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    confirmDialog.cancel();
                                }
                            }, 5000);
                        }
                }
                return true;
            }
        });
    }

    private void initTherapyMode(View view, SharedPreferences preferences, Resources resources) {
        therapyMode =  preferences.getBoolean(resources.getString(R.string.therapy_mode_key), false);
        therapyModeSwitch = (Switch) view.findViewById(R.id.therapyModeSwitch);
        therapyModeSwitch.setChecked(therapyMode);
        therapyModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton view, boolean checked) {
                therapyMode = checked;
                preferences.edit().putBoolean(resources.getString(R.string.therapy_mode_key),checked).apply();
                therapyModeSwitch.setChecked(checked);
            }
        });
    }

    private void initDelayInput(View view, SharedPreferences preferences, Resources resources) {
        delayInput = view.findViewById(R.id.delay_input);

        delayInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int delay = Integer.parseInt(s.toString());
                    preferences.edit().putInt(resources.getString(R.string.recording_delay_key), delay).apply();
                    recordingDelay = delay;
                } catch (NumberFormatException e) {
                    preferences.edit().putInt(resources.getString(R.string.recording_delay_key), 0).apply();
                    recordingDelay = 0;
                }
            }
        });

        recordingDelay = preferences.getInt(resources.getString(R.string.recording_delay_key), 5);
        delayInput.setText(Integer.toString(recordingDelay));
    }

    private void initSamplingFrequency(View view, SharedPreferences preferences, Resources resources) {
        // Frequency
        float freq = preferences.getFloat(resources.getString(R.string.recording_frequency_key), 1);

//        frequencyBar = (SeekBar) view.findViewById(R.id.frequency_bar);
//        frequencyBarLabel = (TextView) view.findViewById(R.id.frequency_bar_label);
//        frequencyBar.setMax(frequencies.length-1);

//        int defaultFreqIndex = Arrays.binarySearch(frequencies, freq);
//        if (defaultFreqIndex > 0) {
//            frequencyBar.setProgress(defaultFreqIndex);
//        } else {
//            // Default frequency was not found - fall back to the first
//            samplingFrequency = frequencies[0];
//            frequencyBar.setProgress(0);
//        }

        setSamplingFrequency(freq);

//        frequencyBar.setProgress(Arrays.binarySearch(frequencies, samplingFrequency));
//        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                System.out.println(frequencies[progress]);
//                setSamplingFrequency(frequencies[progress]);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(msgReceiver, new IntentFilter("sensor-data"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Resources resources = getResources();
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        if (savedInstanceState == null) {
            isMonitoring = false;
        } else {
            isMonitoring = savedInstanceState.getBoolean(IS_MONITORING_KEY);
        }

        isRecordingTextView = (TextView) view.findViewById(R.id.isRecordingTextView);
        sensorValuesTextView = (TextView) view.findViewById(R.id.sensorValuesTextView);


        initMonitoringButton(view);
        initTherapyMode(view, preferences, resources);
        initDelayInput(view, preferences, resources);
        initSamplingFrequency(view, preferences, resources);

        recordingTimer = new CountDownTimer(recordingDelay * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                isRecordingTextView.setText("Recording in " + (1 + (millisUntilFinished / 1000)) + " seconds");
            }

            public void onFinish() {
                Intent recordingServiceIntent = new Intent(getContext(), RecordingService.class);
                recordingServiceIntent.putExtra("frequency", samplingFrequency);
                recordingServiceIntent.putExtra("therapy", therapyMode);
                getContext().startService(recordingServiceIntent);
                isRecordingTextView.setText(R.string.recording_now);
                isMonitoring = true;
            }
        };

        logger = new Logger(getContext());


        return view;
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_MONITORING_KEY, isMonitoring);
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


    public void startMonitoring(View view) {
        if (recordingTimer != null) {
            recordingTimer.cancel();
        }
        isMonitoring = true;
        // frequencyBar.setEnabled(false);
        recordingTimer.start();
        monitoringButton.setBackgroundColor(getResources().getColor(R.color.red));
        monitoringButton.setText(R.string.stop_monitoring);

        // Disable navigation while monitoring
        MainActivity main = (MainActivity) getParentFragment().getActivity();
        main.setNavigationEnabled(false);

    }

    public void stopMonitoring() {
        recordingTimer.cancel();
        getContext().stopService(new Intent(getContext(), RecordingService.class));
        isRecordingTextView.setText(R.string.stopped_recording);
        isMonitoring = false;
        monitoringButton.setBackgroundColor(getResources().getColor(R.color.green));
        monitoringButton.setText(R.string.start_monitoring);
        sensorValuesTextView.setText(null);
        // frequencyBar.setEnabled(true);

        // Enable navigation after monitoring
        MainActivity main = (MainActivity) getParentFragment().getActivity();
        main.setNavigationEnabled(true);

    }
//
//    public void toggleMonitoring(View view) {
//        if (recordingTimer != null) {
//            recordingTimer.cancel();
//        }
////        recordingTimer = new CountDownTimer(recordingDelay * 1000, 1000) {
////
////            public void onTick(long millisUntilFinished) {
////                isRecordingTextView.setText("Recording in " + (1 + (millisUntilFinished / 1000)) + " seconds");
////            }
////
////            public void onFinish() {
////                Intent recordingServiceIntent = new Intent(getContext(), RecordingService.class);
////                recordingServiceIntent.putExtra("frequency", samplingFrequency);
////                recordingServiceIntent.putExtra("therapy", therapyMode);
////                getContext().startService(recordingServiceIntent);
////                isRecordingTextView.setText(R.string.recording_now);
////                isRecording = true;
////            }
////        };
//
//        toggleMonitoringUI();
//        if (isRecording) {
//            // Stop monitoring service
//            getContext().stopService(new Intent(getContext(), RecordingService.class));
//            isRecordingTextView.setText(R.string.stopped_recording);
//            isRecording = false;
////            frequencyBar.setEnabled(true);
//        } else {
//            // Start monitoring service
//            isRecording = true;
////            frequencyBar.setEnabled(false);
//            recordingTimer.start();
//        }
//    }

//    private void toggleMonitoringUI() {
//        if (isRecording) {
//            // Switch from on to off
//            monitoringButton.setBackgroundColor(getResources().getColor(R.color.green));
//            monitoringButton.setText(R.string.start_monitoring);
//            sensorValuesTextView.setText(null);
//        } else {
//            // Switch from off to on
//            monitoringButton.setBackgroundColor(getResources().getColor(R.color.red));
//            monitoringButton.setText(R.string.stop_monitoring);
//        }
//    }

    public void setSamplingFrequency(float freq) {
        samplingFrequency = freq;
        String label = getResources().getString(R.string.frequency_label);
        label = label + " " + Float.toString(freq);
//        frequencyBarLabel.setText(label);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(msgReceiver);
        super.onDestroy();
    }
}