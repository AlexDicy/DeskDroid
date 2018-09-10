package us.halex.deskdroid;

import android.app.Application;

/**
 * Created by HAlexTM on 10/09/2018 12:13
 */
public class DeskDroidApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        System.loadLibrary("xserver");
    }
}
