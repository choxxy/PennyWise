package com.iogarage.ke.pennywise.util;


import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.iogarage.ke.pennywise.BuildConfig;
import com.iogarage.ke.pennywise.PennyApp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by choxxy on 03/06/2017.
 */

public class BackUpUtil {

    private static final String EXPORT_FILENAME_EXTENSION = ".zip";
    /**
     * Tag for logging
     */
    protected static String LOG_TAG = "BackUpUtil";

    /**
     * Application folder on external storage
     *
     * @deprecated Use {@link #BASE_FOLDER_PATH} instead
     */
    @Deprecated
    public static final String LEGACY_BASE_FOLDER_PATH = Environment.getExternalStorageDirectory() + "/" + BuildConfig.APPLICATION_ID;

    /**
     * Application folder on external storage
     */
    public static final String BASE_FOLDER_PATH = PennyApp.getAppContext().getExternalFilesDir(null).getAbsolutePath();

    private static final SimpleDateFormat EXPORT_FILENAME_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private final Context mContext;


    public BackUpUtil(String dbName) {
        mContext = PennyApp.getAppContext();

    }

    /**
     * Strings a string of any characters not allowed in a file name.
     * All unallowed characters are replaced with an underscore
     *
     * @param inputName Raw file name input
     * @return Sanitized file name
     */
    public static String sanitizeFilename(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    /**
     * Builds a file name based on the current time stamp for the exported file
     *
     * @return String containing the file name
     */
    public static String buildExportFilename() {
        return EXPORT_FILENAME_DATE_FORMAT.format(new Date(System.currentTimeMillis()))
                + "_pennywise_backup" + EXPORT_FILENAME_EXTENSION;
    }

    /**
     * Parses the name of an export file and returns the date of export
     *
     * @param filename Export file name generated by {@link #buildExportFilename()}
     * @return Date in milliseconds
     */
    public static long getExportTime(String filename) {
        String[] tokens = filename.split("_");
        long timeMillis = 0;
        if (tokens.length < 2) {
            return timeMillis;
        }
        try {
            Date date = EXPORT_FILENAME_DATE_FORMAT.parse(tokens[0] + "_" + tokens[1]);
            timeMillis = date.getTime();
        } catch (ParseException e) {
            Log.e("Exporter", "Error parsing time from file name: " + e.getMessage());
            Crashlytics.logException(e);
        }
        return timeMillis;
    }


    /**
     * Recursively delete all files in a directory
     *
     * @param directory File descriptor for directory
     */
    private void purgeDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory())
                purgeDirectory(file);
            else
                file.delete();
        }
    }

    /**
     * Returns the path to the file where the exporter should save the export during generation
     * <p>This path is a temporary cache file whose file extension matches the export format.<br>
     * This file is deleted every time a new export is started</p>
     *
     * @return Absolute path to file
     */
    protected String getExportCacheFilePath() {
        // The file name contains a timestamp, so ensure it doesn't change with multiple calls to
        // avoid issues like #448
        /*if (mExportCacheFilePath == null) {
            String cachePath = mCacheDir.getAbsolutePath();
            if (!cachePath.endsWith("/"))
                cachePath += "/";
            String bookName = BooksDbAdapter.getInstance().getAttribute(mBookUID, DatabaseSchema.BookEntry.COLUMN_DISPLAY_NAME);
            mExportCacheFilePath = cachePath + buildExportFilename(mExportParams.getExportFormat(), bookName);
        }*/

        return "";//mExportCacheFilePath;
    }

    /**
     * Returns that path to the export folder for the book with GUID {@code bookUID}.
     * This is the folder where exports like QIF and OFX will be saved for access by external programs
     *
     * @param bookUID GUID of the book being exported. Each book has its own export path
     * @return Absolute path to export folder for active book
     */
    public static String getExportFolderPath(String bookUID) {
        String path = BASE_FOLDER_PATH + "/" + bookUID + "/exports/";
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return path;
    }

    /**
     * Returns the path to the backups folder for the book with GUID {@code bookUID}
     * Each book has its own backup path
     *
     * @return Absolute path to backup folder for the book
     */
    public static String getBackupFolderPath(String bookUID) {
        String path = BASE_FOLDER_PATH + "/" + bookUID + "/backups/";
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();
        return path;
    }


    /**
     * Returns the MIME type for this exporter.
     *
     * @return MIME type as string
     */
    public String getExportMimeType() {
        return "text/plain";
    }


    /**
     * Moves a file from <code>src</code> to <code>dst</code>
     *
     * @param src Absolute path to the source file
     * @param dst Absolute path to the destination file
     * @throws IOException if the file could not be moved.
     */
    public void moveFile(String src, String dst) throws IOException {
        File srcFile = new File(src);
        File dstFile = new File(dst);
        FileChannel inChannel = new FileInputStream(srcFile).getChannel();
        FileChannel outChannel = new FileOutputStream(dstFile).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            outChannel.close();
        }
        srcFile.delete();
    }

    /**
     * Move file from a location on disk to an outputstream.
     * The outputstream could be for a URI in the Storage Access Framework
     *
     * @param src          Input file (usually newly exported file)
     * @param outputStream Output stream to write to
     * @throws IOException if error occurred while moving the file
     */
    public void moveFile(@NonNull String src, @NonNull OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        try (FileInputStream inputStream = new FileInputStream(src)) {
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } finally {
            outputStream.flush();
            outputStream.close();
        }
        Log.i(LOG_TAG, "Deleting temp export file: " + src);
        new File(src).delete();
    }

    public static void zip(OutputStream dest, String[] _files) {

        int BUFFER = 1024;

        try {
            BufferedInputStream origin = null;
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzip(InputStream src, String _targetLocation) {

        //create target location folder if not exist
        dirChecker(_targetLocation);

        try {
            ZipInputStream zin = new ZipInputStream(src);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {

                //create dir if required while unzipping
                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else {

                    File out = new File(_targetLocation + "/" + ze.getName());

                    if (out.exists()) {
                        out.delete();
                    }

                    FileOutputStream fout = new FileOutputStream(_targetLocation + "/" + ze.getName());

                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    private static void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    //name should be in this pattern (/.*)
    //add slash + extension
    public static String formatDBFileName(String filename, String extension) {
        String path = "/";
        if (filename.startsWith("/"))
            path = "";

        path += filename + "." + extension;
        return path;
    }
}