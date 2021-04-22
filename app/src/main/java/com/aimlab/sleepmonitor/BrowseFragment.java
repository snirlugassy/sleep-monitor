package com.aimlab.sleepmonitor;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;


public class BrowseFragment extends Fragment {
    public String[] mobileArray = {"File 1", "File 2", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3", "File 3"};
    public String[] recordingFileNames;
    public File[] recordingFiles;
    public ListView recordingListView;
    private String[] contextMenuActions = {"Open", "Delete"};


    public BrowseFragment() {
        // Required empty public constructor
    }


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

        File dir = new File(getContext().getFilesDir(), "recordings");
        recordingFiles = dir.listFiles();
        recordingFileNames = new String[recordingFiles.length];
        for (int i = 0; i < recordingFiles.length; i++) {
            recordingFileNames[i] = recordingFiles[i].getName();
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, recordingFileNames);
        recordingListView = (ListView) view.findViewById(R.id.recordings_list_view);
        recordingListView.setAdapter(adapter);
        registerForContextMenu(recordingListView);
        recordingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(recordingFileNames[position]);
//                PopupMenu popup = new PopupMenu(getContext(), view);
//                MenuInflater inflater = popup.getMenuInflater();
//                inflater.inflate(R.menu.file_actions_menu, popup.getMenu());
//                popup.show();
                getActivity().openContextMenu(view);
            }
        });
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

            case "Open":
                openRecordingFile(recordingFiles[info.position]);
                break;

            case "Delete":
                deleteRecordingFile(recordingFiles[info.position]);
                break;

        }

        return true;
    }

    private void openRecordingFile(File file) {

        File recordingsDir = new File(getContext().getFilesDir(), "recordings");
        Uri recordingUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, file);

        Intent myIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        myIntent.setDataAndType(recordingUri, "text/plain");
        myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(myIntent);

//
//        System.out.println("Opening file " + file.getPath());
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setDataAndType(contentUri, "text/plain");
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        startActivityForResult(intent, 1);
    }
    private void deleteRecordingFile(File file) {
        System.out.println("Deleting file " + file.getName());
    }
}