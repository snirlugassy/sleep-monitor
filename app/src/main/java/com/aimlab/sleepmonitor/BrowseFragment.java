package com.aimlab.sleepmonitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.Collections;


public class BrowseFragment extends Fragment {
    public String[] recordingFileNames;
    public File[] recordingFiles;
    public ListView recordingListView;
    public TextView noRecordingsTextView;

    public Button deleteAllRecordingsButton;
    public Button openRecordingsFolderButton;
    public Button shareLogFileButton;

    private String[] contextMenuActions = {"Send", "Open", "Delete"};


    public BrowseFragment() {}


    public static BrowseFragment newInstance(String param1, String param2) {
        BrowseFragment fragment = new BrowseFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        noRecordingsTextView = (TextView) view.findViewById(R.id.noRecordingsTextView);
        recordingListView = (ListView) view.findViewById(R.id.recordings_list_view);

        deleteAllRecordingsButton = (Button) view.findViewById(R.id.deleteAllRecordingsButton);
        deleteAllRecordingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllRecordings();
            }
        });

        openRecordingsFolderButton = (Button) view.findViewById(R.id.openRecordingsFolderButton);
        openRecordingsFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                openRecordingsFolder();
            }
        });

        shareLogFileButton = (Button) view.findViewById(R.id.shareLogFileButton);
        shareLogFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File logFile = new File(getContext().getExternalFilesDir(null), getResources().getString(R.string.log_file));
                sendFile(logFile);
            }
        });

        listRecordingFiles(view);

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.recordings_list_view) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo info =
                    (AdapterView.AdapterContextMenuInfo) menuInfo;
            String recordingFileName = ((TextView) info.targetView).getText().toString();
            menu.setHeaderTitle(recordingFileName);
            for (int i=0;i<contextMenuActions.length;i++){
                menu.add(Menu.NONE, i, i, contextMenuActions[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemIndex = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String menuItemName = contextMenuActions[menuItemIndex];

        switch (menuItemName) {
            case "Send":
                sendFile(recordingFiles[info.position]);
                break;

            case "Open":
                openFile(recordingFiles[info.position]);
                break;

            case "Delete":
                deleteRecordingFile(recordingFiles[info.position]);
                break;

        }

        return true;
    }

    private void listRecordingFiles(View view) {
        File dir = new File(getContext().getExternalFilesDir(null), "recordings");
        recordingFiles = dir.listFiles();

        if (recordingFiles != null && recordingFiles.length > 0) {
            recordingListView.setVisibility(View.VISIBLE);
            noRecordingsTextView.setVisibility(View.INVISIBLE);
            noRecordingsTextView.setText(null);

            recordingFileNames = new String[recordingFiles.length];
            for (int i = 0; i < recordingFiles.length; i++) {
                recordingFileNames[i] = recordingFiles[i].getName();
            }

            ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, recordingFileNames);

            recordingListView.setAdapter(adapter);
            registerForContextMenu(recordingListView);
            recordingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println(recordingFileNames[position]);
                    getActivity().openContextMenu(view);
                }
            });
        } else {
            System.out.println("NO RECORDINGS");
            recordingListView.setVisibility(View.GONE);
            noRecordingsTextView.setVisibility(View.VISIBLE);
            noRecordingsTextView.setText(R.string.no_recordings_text);
        }

    }

    private void sendFile(File file) {
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/*");
        sendIntent.setData(uri);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(sendIntent, "Share sleep recordings"));
    }


    private void openFile(File file) {
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setType("text/*");
        openIntent.setData(uri);
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(openIntent);
    }


    private void deleteRecordingFile(File file) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setMessage("Are you sure?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(file.delete()) {
                            System.out.println(file.getName() + " deleted!");
                            listRecordingFiles(getView());
                        } else {
                            System.out.println("error deleting " + file.getName());
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        builder.show();
    }

    private void deleteAllRecordings() {
        if (recordingFiles != null && recordingFiles.length > 0) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
            builder.setMessage("Are you sure?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            for (File file: recordingFiles) {
                                if(file.delete()) {
                                    System.out.println(file.getName() + " deleted!");
                                } else {
                                    System.out.println("error deleting " + file.getName());
                                }
                            }
                            listRecordingFiles(getView());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });

            builder.show();
        }
    }

    public void openRecordingsFolder() {
        File dir = new File(getContext().getExternalFilesDir(null), "recordings");
//        Uri recordingsFolderUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", dir);
        Intent openFolderIntent = new Intent(Intent.ACTION_VIEW);
        openFolderIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        openFolderIntent.setType("*/*");
        openFolderIntent.setData(Uri.parse("content://" + dir.getAbsolutePath()));
        startActivity(openFolderIntent);
    }

}