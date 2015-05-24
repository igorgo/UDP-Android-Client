package ua.parus.pmo.parus8claims.rest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.MaterialDialogBuilder;
import ua.parus.pmo.parus8claims.utils.Constants;

/**
 * It's a part of project parus8claims
 * Created by igor-go (igor-go@parus.com.ua)
 * Copyright (C) 2015 Parus-Ukraine Corporation (www.parus.ua)
 */
public class CheckUpdates extends AsyncTask<Void, Void, Boolean> {
    private static final String URL_LATEST = "https://api.github.com/repos/igorgo/UDP-Android-Client/releases/latest";
    private final WeakReference<Activity> activityReference;
    private final Activity activity;
    private SharedPreferences prefs;
    private String downloadUrl;

    public CheckUpdates(Activity activity) {
        this.activityReference = new WeakReference<Activity>(activity);
        this.activity = activity;
        this.prefs = activity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String releaseData = getLatestRelease();
        boolean needPromptUpdate = false;
        long nextCheck = prefs.getLong(Constants.PREF_LAST_UPDATE_CHECK, 0) + Constants.UPDATE_MIN_FREQUENCY;

        needPromptUpdate = System.currentTimeMillis() > nextCheck;

        if (!needPromptUpdate) return false;
        try {
            JSONObject json = new JSONObject(releaseData);
            String serverVersion = json.getString("name");
            JSONArray assets = json.getJSONArray("assets");
            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                if (asset.getString("name").equals(Constants.PARUS8CLAIMS_APK)) {
                    downloadUrl = asset.getString("browser_download_url");
                    break;
                }
            }
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(), 0);
            String installedVersion = pInfo.versionName;

            needPromptUpdate = !installedVersion.equals(serverVersion);
        } catch (JSONException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return needPromptUpdate;
    }


    @Override
    protected void onPostExecute(Boolean needPromptUpdate) {
        boolean isAlive = !(activityReference.get() == null || activityReference.get().isFinishing());
        if (isAlive) {
            if (needPromptUpdate) {
                promptUpdate();
            } else {
                showChangeLog();
            }
        }

    }

    private void promptUpdate() {
        prefs.edit().putLong(Constants.PREF_LAST_UPDATE_CHECK, System.currentTimeMillis())
                .commit();
        new MaterialDialogBuilder(activityReference.get())
                .cancelable(false)
                .content(R.string.new_update_available)
                .positiveText(R.string.update)
                .negativeText(R.string.not_now)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        new DownloadUpdate(activityReference.get(),downloadUrl).execute();
                        dialog.dismiss();
                        super.onPositive(dialog);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                        super.onNegative(dialog);
                    }
                })
                .show();
    }

    private void showChangeLog() {
        try {
            String packageVersion = activityReference.get().getPackageManager().getPackageInfo(
                    activityReference.get().getPackageName(), 0).versionName;
            String prefVersion = prefs.getString(Constants.PREF_CURRENT_APP_VERSION, "");
            if (!packageVersion.equals(prefVersion)) {
                new MaterialDialogBuilder(activityReference.get())
                        .customView(R.layout.activity_changelog, false)
                        .positiveText(android.R.string.ok)
                        .build().show();
                prefs.edit().putString(Constants.PREF_CURRENT_APP_VERSION,
                        packageVersion).commit();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getLatestRelease() {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(URL_LATEST);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                content.append(inputLine);
            }
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

}
