package us.halex.deskdroid;

import android.content.Context;
import android.util.Log;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.x.android.XServerNative;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.RawRes;

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

    public static boolean extractTar(Context context, @RawRes int rawResource, File destination) {
        InputStream inputStream = context.getResources().openRawResource(rawResource);
        return extractTar(inputStream, destination);
    }

    public static boolean extractTar(File from, File to) {
        try {
            return extractTar(new FileInputStream(from), to);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean extractTar(InputStream inputStream, File to) {
        /*try {
            System.out.println("______________DETECTED______________");
            System.out.println(ArchiveStreamFactory.detect(inputStream));
            return false;
        } catch (ArchiveException e) {
            e.printStackTrace();
        }*/
        Log.v("Extractor", "Extracting to " + to.getAbsolutePath());
        try (TarArchiveInputStream is = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {
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
}
