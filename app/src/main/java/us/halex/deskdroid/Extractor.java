package us.halex.deskdroid;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.x.android.XServerNative;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.RawRes;
import androidx.appcompat.app.AlertDialog;

/**
 * Created by HAlexTM on 11/09/2018 12:20
 */
public class Extractor {

    public static boolean extractZip(File source, File destination) {
        try {
            return extractZip(new ZipInputStream(new FileInputStream(source)), destination);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean extractZip(ZipInputStream inputStream, File destination) {
        if (!destination.exists() && !destination.mkdirs()) {
            Log.e("Extractor", "Couldn't create destination folder");
            return false;
        }

        byte[] buffer = new byte[4096];

        try {
            ZipEntry zipEntry = inputStream.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destination, zipEntry.getName());
                System.out.println(">>>>> Writing new file: " + zipEntry.getName());
                //noinspection ResultOfMethodCallIgnored
                newFile.getParentFile().mkdirs();
                if (zipEntry.isDirectory()) {
                    //noinspection ResultOfMethodCallIgnored
                    newFile.mkdir();
                } else {
                    FileOutputStream outputStream = new FileOutputStream(newFile);
                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.close();
                }
                zipEntry = inputStream.getNextEntry();
            }
            inputStream.closeEntry();
            inputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean extractZip(Context context, @RawRes int rawResource, File destination) {
        InputStream inputStream = context.getResources().openRawResource(rawResource);
        return extractZip(new ZipInputStream(inputStream), destination);
    }

    public static boolean extractTar(File from, File to) {
        Log.v("Extractor", "Extracting to " + to.getAbsolutePath());
        try (TarArchiveInputStream is = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(from)))) {
            TarArchiveEntry entry;
            while ((entry = is.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File file = new File(to, entry.getName());
                File parent = file.getParentFile();
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
                if (entry.isSymbolicLink()) {
                    XServerNative.symlink(entry.getLinkName(), file.getAbsolutePath());
                } else {
                    IOUtils.copy(is, new FileOutputStream(file));
                    XServerNative.chmod(file.getAbsolutePath(), entry.getMode());
                }
            }
            Log.v("Extractor", "Done extracted to " + to.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean extractTarFromUrl(Context context, String from, File to, Runnable onSuccess) {
        //https://files.halex.us/deskdroid/lib_with_fluxbox.tar.gz
        // Step 0: open alert
        // Step 1: Download the file in cache folder
        // Step 2: Remember the file name and unTar it with #extractTar(File from, File to);
        // Step 3: Remove downloaded file
        // Step 4: close alert

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Downloading file...")
                .setMessage("Calculating file size...")
                .setCancelable(false)
                .show();

        Handler handler = new Handler(context.getMainLooper());

        new Thread(() -> {
            try {
                URL url = new URL(from);
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());
                if (httpConnection.getResponseCode() != 200) {
                    throw new IOException("Response code is not 200 but " + httpConnection.getResponseCode());
                }

                long total = httpConnection.getContentLength();
                String t = readableFileSize(total);

                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(to));
                BufferedInputStream inputStream = new BufferedInputStream(httpConnection.getInputStream());


                byte[] buffer = new byte[4096];
                long downloaded = 0;
                int len;
                while ((len = inputStream.read(buffer)) >= 0) {
                    downloaded += len;

                    long current = downloaded;
                    handler.post(() -> dialog.setMessage("Progress: " + readableFileSize(current) + " / " + t));

                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                inputStream.close();
                handler.post(onSuccess);
                return;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                handler.post(() -> dialog.setMessage("Couldn't parse download URL, please report:\n\n" + e.getMessage()));
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> dialog.setMessage("IOException:\n\n" + e.getMessage()));
            }
            handler.post(() -> {
                dialog.setTitle("Error");
                dialog.setCancelable(true);
                Button close = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                close.setText(context.getString(R.string.close));
                close.setVisibility(View.VISIBLE);
            });
        }, "Download & Extract file").start();
        return false;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
