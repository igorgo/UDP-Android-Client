package ua.parus.pmo.parus8claims;
// todo: выход

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;
import java.util.Locale;

import ua.parus.pmo.parus8claims.objects.filter.Filter;
import ua.parus.pmo.parus8claims.objects.filter.FilterListAdapter;
import ua.parus.pmo.parus8claims.utils.Constants;

public class ClaimApplication extends Application {
    private String sessionId;
    private boolean isPmoUser;
    private boolean cacheRefreshed;
    static SharedPreferences prefs;
    private FilterListAdapter filters;
    private Filter lastDefaultFilter;

    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_MULTI_PROCESS);
        updateLanguage(this, null);
        // Грязный хак для показа трех точек в меню
        this.getOverflowMenu();
        this.filters = null;
        super.onCreate();
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isPmoUser() {
        return this.isPmoUser;
    }

    public void setPmoUser(boolean isPmoUser) {
        this.isPmoUser = isPmoUser;
    }

    public boolean isNotCacheRefreshed() {
        return !this.cacheRefreshed;
    }

    public void setCacheRefreshed() {
        this.cacheRefreshed = true;
    }

    public FilterListAdapter getFilters() {
        return filters;
    }

    public void setFilters(FilterListAdapter filters) {
        this.filters = filters;
    }

    public Filter getLastDefaultFilter() {
        return lastDefaultFilter;
    }

    public void setLastDefaultFilter(Filter lastDefaultFilter) {
        this.lastDefaultFilter = lastDefaultFilter;
    }

    /**
     * Updates default language with forced one
     */
    public static void updateLanguage(Context ctx, String lang) {
        Configuration cfg = new Configuration();
        String language = prefs.getString(Constants.PREF_LANG, "");
        if (TextUtils.isEmpty(language) && lang == null) {
            cfg.locale = Locale.getDefault();

            String tmp = "";
            tmp = Locale.getDefault().toString().substring(0, 2);

            prefs.edit().putString(Constants.PREF_LANG, tmp).commit();
        } else if (lang != null) {
            // Checks country
            if (lang.contains("_")) {
                cfg.locale = new Locale(lang.split("_")[0], lang.split("_")[1]);
            } else {
                cfg.locale = new Locale(lang);
            }
            prefs.edit().putString(Constants.PREF_LANG, lang).commit();

        } else if (!TextUtils.isEmpty(language)) {
            // Checks country
            if (language.contains("_")) {
                cfg.locale = new Locale(language.split("_")[0], language.split("_")[1]);
            } else {
                cfg.locale = new Locale(language);
            }
        }
        ctx.getResources().updateConfiguration(cfg, null);
    }

    @Override
    // Used to restore user selected locale when configuration changes
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String language = prefs.getString(Constants.PREF_LANG, "");
        super.onConfigurationChanged(newConfig);
        updateLanguage(this, language);
    }

    public static void restartApp(final Context context) {
        if (MainActivity.getInstance() != null) {
            MainActivity.getInstance().finish();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);
        }
    }
}
