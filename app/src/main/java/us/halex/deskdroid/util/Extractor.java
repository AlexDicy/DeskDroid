package us.halex.deskdroid.util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.x.android.XServerNative;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.appcompat.app.AlertDialog;
import us.halex.deskdroid.DeskDroidApp;
import us.halex.deskdroid.R;

/**
 * Created by HAlexTM on 11/09/2018 12:20
 */
public class Extractor {

    private static boolean extractZip(File source, File destination) throws IOException {
        if (!destination.exists() && !destination.mkdirs()) {
            Log.e("Extractor", "Couldn't create destination folder");
            return false;
        }

        byte[] buffer = new byte[4096];

        try (ZipInputStream inputStream = new ZipInputStream(new FileInputStream(source))) {
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
        }
    }

    public static boolean extractTar(File from, File to) {
        Log.v("Extractor", "Extracting to " + to.getAbsolutePath());
        try (TarArchiveInputStream is = new TarArchiveInputStream(new FileInputStream(from))) {
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

    /**
     * Downloads a zip file from the specified url, extracts it and deletes the zip file
     * Will open an {@link AlertDialog} informing the user about the status of the operation
     * User canNOT close the dialog unless there was an error or the process has ended.
     * <p>
     * This method is Async but will still block user actions
     *
     * @param context    Activity context used to open the dialog
     * @param from       url used to download the file
     * @param to         the folder where to extract the zip archive
     * @param identifier download identifier, used to recover interrupted downloads
     * @param onSuccess  {@link Runnable} containing actions to run if the extraction was successful
     *                   and the user clicked on "OK" to close the dialog. Sync to the App main thread
     */
    public static void extractZipFromUrl(Context context, String from, File to, String identifier, Runnable onSuccess) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Downloading file...")
                .setMessage("Calculating file size...")
                .setCancelable(false)
                .setNegativeButton("error", null)
                .setPositiveButton("error", null)
                .show();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);

        Handler handler = new Handler(context.getMainLooper());

        new Thread(() -> {
            try {
                URL url = new URL(from);
                HttpURLConnection httpConnection = (HttpURLConnection) (url.openConnection());

                long downloaded = 0; // Used to recover file not downloaded successfully and keep track of the download progress
                File tempFile = new File(DeskDroidApp.getCacheFolder(), "TEMP." + identifier + ".delete");
                if (tempFile.exists()) {
                    downloaded = tempFile.length();
                    httpConnection.setRequestProperty("Range", "bytes=" + downloaded + "-");
                }

                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw new IOException("Response code is not 200 but " + httpConnection.getResponseCode());
                }

                long total = httpConnection.getContentLength();
                String t = readableFileSize(total);

                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile, downloaded > 0));
                BufferedInputStream inputStream = new BufferedInputStream(httpConnection.getInputStream());


                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) >= 0) {
                    downloaded += len;

                    long current = downloaded;
                    handler.post(() -> dialog.setMessage("Progress: " + readableFileSize(current) + " / " + t));

                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                inputStream.close();

                handler.post(() -> {
                    dialog.setTitle("Extracting file...");
                    dialog.setMessage("Please wait\n\nExtraction speed depends on your phone hardware");
                });
                if (extractZip(tempFile, to)) {
                    boolean deleted = tempFile.delete();
                    handler.post(() -> {
                        dialog.setTitle("File extracted");
                        dialog.setMessage("The file was successfully extracted, downloaded file " +
                                (deleted ? "deleted to save space" : "was not deleted, an error occurred but you can still use the app"));
                        dialog.setCancelable(true);
                        dialog.setCanceledOnTouchOutside(true);
                        Button ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        ok.setText(context.getString(R.string.ok));
                        ok.setVisibility(View.VISIBLE);
                        ok.setOnClickListener(v -> dialog.dismiss());
                        dialog.setOnDismissListener(d -> handler.post(onSuccess));
                    });
                    return;
                }
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
    }

    private static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
