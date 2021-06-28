package com.aimlab.sleepmonitor;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private File log;
    private PrintWriter out;

    public Logger(Context context) {
        // CREATE LOG FILE
        System.out.println(context.getExternalFilesDir(null).getPath());
        log = new File(context.getExternalFilesDir(null), context.getResources().getString(R.string.log_file));
        if (!log.exists())  {
            try  {
                log.createNewFile();
            } catch (IOException e)  {
                e.printStackTrace();
            }
        }

        // CREATE PrintWriter
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(this.log, true)), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String output) {
        out.println(output);
    }

    public void write(String output, boolean date) {
        if (date) {
            String datetime = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss a", new java.util.Date()).toString();
            out.println(datetime + ": " + output);
        } else {
            out.println(output);
        }
    }

    public PrintWriter getPrintWriter() {
        return out;
    }

    public File getLogFile() {
        return log;
    }
}
