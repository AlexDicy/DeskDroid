package us.halex.deskdroid;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import org.x.android.XServerNative;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import us.halex.deskdroid.execute.Executor;

/**
 * Created by HAlexTM on 10/09/2018 12:13
 */
public class DeskDroidApp extends Application {
    private static DeskDroidApp instance;
    private static Map<String, Process> processes = new HashMap<>();

    private static File appFolder;
    private static File cacheFolder;
    private static File home;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        appFolder = getExternalFilesDir(null);
        cacheFolder = getExternalCacheDir();
        home = new File(appFolder, "home");
        if (!home.exists()) {
            if (!home.mkdirs()) {
                Toast.makeText(instance, "Couldn't create home directory", Toast.LENGTH_LONG).show();
            }
        }

        System.loadLibrary("xserver");

        XServerNative.setenv("TMP", cacheFolder.getAbsolutePath());
        XServerNative.setenv("TEMP", cacheFolder.getAbsolutePath());
        XServerNative.setenv("DISPLAY", ":0.0");
        XServerNative.setenv("HOME", home.getAbsolutePath());
        XServerNative.setenv("LANG", getLanguage() + "_" + getCountry());
        XServerNative.setenv("LOCALE", getLanguage() + "-" + getCountry());


        int layoutSize = this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean large = layoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE || layoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;

        DisplayMetrics metrics = new DisplayMetrics();
        // DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        WindowManager manager = ((WindowManager) this.getSystemService(WINDOW_SERVICE));
        if (manager != null) manager.getDefaultDisplay().getRealMetrics(metrics);
        float scale = metrics.scaledDensity;
        if (!large) {
            scale = (float) ((double) scale / 1.5D);
        }

        int width = (int) ((float) metrics.widthPixels / scale);
        int height = (int) ((float) metrics.heightPixels / scale);
        if (width < height) {
            int temp = height;
            //noinspection SuspiciousNameCombination
            height = width;
            width = temp;
        }

        width += width % 2;
        height += height % 2;
        int size = Math.max(width, height);

        // Extract Core, Fluxbox, Java...

        new Executor.Builder()
                .setExecutable("Xvfb")
                .setArguments(new String[]{
                        ":0",
                        "-screen", "0", size + "x" + size + "x16",
                        "-nolock",
                        "-xkbdir", (new File(appFolder, "share/X11/xkb")).getAbsolutePath(),
                        "-xkbbin", (new File(appFolder, "bin")).getAbsolutePath(),
                        "-dpi", "120", "-dumbSched", "+extension", "RANDR", "+render",
                        "-fbdir", this.getCacheDir().getAbsolutePath()})
                .addEnv("XKM_DIR", new File(appFolder, "share/X11/xkb").getAbsolutePath())
                .addEnv("PROTOCOL_TXT", new File(appFolder, "lib/xorg/protocol.txt").getAbsolutePath())
                .create()
                .execute();
    }

    public static boolean isRunning(App app) {
        return processes.containsKey(app.getName());
    }

    public static void openApp(FragmentActivity activity, App app) {
        Process process = processes.get(app.getName());
        if (process != null) {
            Toast.makeText(activity, "Resuming app activity", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Starting the new app", Toast.LENGTH_SHORT).show();
        }
    }

    public static void askPermission(FragmentActivity activity, String permission, String message) {
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(activity)
                    .setTitle("Permission needed")
                    .setMessage(message)
                    .setPositiveButton("Ok", (dialog, which) -> activity.requestPermissions(new String[]{permission}, 1))
                    .create()
                    .show();
        }
    }

    public static void askWritePermission(FragmentActivity activity) {
        String message = "In order to download and start your apps the write permission is needed, if you deny it the app will not work";
        askPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, message);
    }

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    public static DeskDroidApp getInstance() {
        return instance;
    }

    public static File getAppFolder() {
        return appFolder;
    }

    public static File getCacheFolder() {
        return cacheFolder;
    }

    public static File getHomeFolder() {
        return home;
    }
}
