package ua.parus.pmo.parus8claims;

import android.app.Application;
import android.os.StrictMode;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

/**
 * Created by igor-go on 09.04.2015.
 * ${PACKAGE_NAME}
 */
public class ClaimApplication extends Application {
    private String sessionId;
    private boolean isPmoUser;
    private boolean cacheRefreched;

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
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Грязный хак для показа трех точек в меню
        this.getOverflowMenu();
        //Releases.RefreshCache(getBaseContext());
        //Builds.RefreshCache(getBaseContext());
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isPmoUser() {
        return this.isPmoUser;
    }

    public void setPmoUser(boolean isPmoUser) {
        this.isPmoUser = isPmoUser;
    }

    public boolean isCacheRefreched() {
        return this.cacheRefreched;
    }

    public void setCacheRefreched() {
        this.cacheRefreched = true;
    }

}
