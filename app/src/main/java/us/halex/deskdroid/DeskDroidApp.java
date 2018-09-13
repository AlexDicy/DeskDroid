package us.halex.deskdroid;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import org.x.android.XServerNative;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
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

    private static int width;
    private static int height;
    private static float scale;
    private static Process fluxbox;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Must use app dir, external folder is not executable, learnt the hard way
        appFolder = getFilesDir();
        if (true) {
            //deleteFiles(appFolder);
            System.out.println(">>> --- >>> Files: " + Arrays.toString(appFolder.list()));
            //return;
        }
        cacheFolder = getCacheDir();

        home = new File(appFolder, "home");
        if (!home.exists()) {
            if (!home.mkdirs()) {
                Toast.makeText(instance, "Couldn't create home directory", Toast.LENGTH_LONG).show();
            }
        }

        System.loadLibrary("xserver");
    }

    public static void loadSystem(Context context, Runnable onSuccess) {
        XServerNative.setenv("TMP", cacheFolder.getAbsolutePath());
        XServerNative.setenv("TEMP", cacheFolder.getAbsolutePath());
        XServerNative.setenv("DISPLAY", ":0.0");
        XServerNative.setenv("HOME", home.getAbsolutePath());
        XServerNative.setenv("LANG", getLanguage() + "_" + getCountry());
        XServerNative.setenv("LOCALE", getLanguage() + "-" + getCountry());


        if (!new File(appFolder, "bin").exists()) {
            // Get size in MB
            //long size = 100000;//getResources().openRawResourceFd(R.raw.lib_with_fluxbox).getLength();
            //String mb = new DecimalFormat("0.00").format(size / (1024 * 1024));
            Toast.makeText(context, "Extracting core files...", Toast.LENGTH_LONG).show();
            // Extract zip file and start activity
            File temp = new File(appFolder, "temp");
            Runnable executeLater = () -> {
                Arrays.stream(temp.listFiles()).sorted().forEach((file) -> {
                    if (file.getName().endsWith(".tar.gz")) {
                        if (!Extractor.extractTar(file, appFolder)) {
                            Toast.makeText(instance, "Error extracting file " + file.getName(), Toast.LENGTH_LONG).show();
                        }
                    }
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                });
                //noinspection ResultOfMethodCallIgnored
                temp.delete();
                new Executor.Builder()
                        .setExecutable("fc-list")
                        .addEnv("FONTCONFIG_PATH", new File(appFolder, "etc/fonts").getAbsolutePath())
                        .addEnv("FONTCONFIG_FILE", new File(appFolder, "etc/fonts/fonts.conf").getAbsolutePath())
                        .addEnv("XDG_DATA_HOME", new File(appFolder, "share").getAbsolutePath())
                        .waitFor().create().execute();

                Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                onSuccess.run();
            };

            Extractor.extractZipFromUrl(context, context.getString(R.string.download_lib_with_fluxbox), temp, executeLater);
        } else {
            setupScreen(context);
            onSuccess.run();
        }
    }

    private static void setupScreen(Context context) {
        int layoutSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean large = layoutSize == Configuration.SCREENLAYOUT_SIZE_LARGE || layoutSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;

        DisplayMetrics metrics = new DisplayMetrics();
        // DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        WindowManager manager = ((WindowManager) context.getSystemService(WINDOW_SERVICE));
        if (manager != null) manager.getDefaultDisplay().getRealMetrics(metrics);
        scale = metrics.scaledDensity;
        if (!large) {
            scale = (float) ((double) scale / 1.5D);
        }

        width = (int) ((float) metrics.widthPixels / scale);
        height = (int) ((float) metrics.heightPixels / scale);
        if (width < height) {
            int temp = height;
            //noinspection SuspiciousNameCombination
            height = width;
            width = temp;
        }

        width += width % 2;
        height += height % 2;
        int size = Math.max(width, height);

        new Executor.Builder()
                .setExecutable("Xvfb")
                .setArguments(new String[]{
                        ":0",
                        "-screen", "0", size + "x" + size + "x16",
                        "-nolock",
                        "-xkbdir", (new File(appFolder, "share/X11/xkb")).getAbsolutePath(),
                        "-xkbbin", (new File(appFolder, "bin")).getAbsolutePath(),
                        "-dpi", "120", "-dumbSched", "+extension", "RANDR", "+render",
                        "-fbdir", getCacheFolder().getAbsolutePath()})
                .addEnv("XKM_DIR", new File(appFolder, "share/X11/xkb").getAbsolutePath())
                .addEnv("PROTOCOL_TXT", new File(appFolder, "lib/xorg/protocol.txt").getAbsolutePath())
                .create()
                .execute();
    }

    public static void deleteFiles(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteFiles(child);

        if (!fileOrDirectory.delete()) {
            System.out.println("File not deleted: " + fileOrDirectory.getAbsolutePath());
        }
        //instance.deleteFile(fileOrDirectory.getAbsolutePath());
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

            loadSystem(activity, () -> {
                startFluxbox();

                new Executor.Builder()
                        .setExecutable(app.getExecutable())
                        .setArguments(app.getArguments())
                        .create().execute();

                activity.startActivity(new Intent(activity, DesktopActivity.class));
            });
        }
    }


    public static void startFluxbox() {
        if (fluxbox == null || !fluxbox.isAlive()) {
            File destination = new File(home, ".fluxbox");
            if (!destination.exists()) {
                if (!destination.mkdirs()) throw new Error("Couldn't create Fluxbox directory");
            }
            File source = new File(appFolder, "share/fluxbox");

            copy(new File(source, "apps"), new File(destination, "apps"));
            copy(new File(source, "init"), new File(destination, "init"));
            copy(new File(source, "keys"), new File(destination, "keys"));
            copy(new File(source, "menu"), new File(destination, "menu"));
            copy(new File(source, "overlay"), new File(destination, "overlay"));
            copy(new File(source, "windowmenu"), new File(destination, "windowmenu"));

            Executor executor = new Executor.Builder()
                    .setExecutable("fluxbox")
                    .addEnv("FLUXBOX_CATDIR", new File(appFolder, "share/fluxbox/nls").getAbsolutePath())
                    .create();
            executor.execute();
            fluxbox = executor.getProcess();
        }
    }


    public static void copy(File from, File to) {
        try (FileInputStream inStream = new FileInputStream(from)) {
            try (FileOutputStream outStream = new FileOutputStream(to)) {
                FileChannel inChannel = inStream.getChannel();
                FileChannel outChannel = outStream.getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setFolderExecutable(File folder) {
        for (File file : folder.listFiles()) {
            //noinspection ResultOfMethodCallIgnored
            file.setExecutable(true, false);
            //XServerNative.chmod(file.getAbsolutePath(), 777);
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

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    public static float getScale() {
        return scale;
    }
}
