package ua.parus.pmo.parus8claims.rest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.ProgressWindow;
import ua.parus.pmo.parus8claims.utils.Constants;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */
public class DownloadUpdate extends AsyncTask<Void, Void, File> {
    private String downloadUrl;
    private final Activity activity;
    private ProgressWindow pw;

    public DownloadUpdate(Activity activity, String downloadUrl) {
        this.downloadUrl = downloadUrl;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        pw = new ProgressWindow(activity, R.string.dowloading_file);
        super.onPreExecute();
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, Constants.PARUS8CLAIMS_APK);
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            path.mkdirs();
            file.createNewFile();
            InputStream inputStream = connection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[Constants.MEGABYTE];
            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            fileOutputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        pw.dismiss();
        if (file != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
        super.onPostExecute(file);
    }
}
