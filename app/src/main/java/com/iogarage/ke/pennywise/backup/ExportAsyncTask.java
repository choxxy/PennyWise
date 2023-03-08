package com.iogarage.ke.pennywise.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.iogarage.ke.pennywise.PennyApp;
import com.iogarage.ke.pennywise.R;
import com.iogarage.ke.pennywise.util.BackUpUtil;
import com.iogarage.ke.pennywise.util.Constants;
import com.iogarage.ke.pennywise.util.Prefs;
import com.iogarage.ke.pennywise.util.TaskDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by choxxy on 04/06/2017.
 */

public class ExportAsyncTask extends AsyncTask<Uri, Void, Boolean> {


    private final String DROPBOX = "dropbox";
    private final String URI = "uri";

    /**
     * App context
     */
    private final Context mContext;

    private ProgressDialog mProgressDialog;
    private TaskDelegate mDelegate;

    /**
     * Log tag
     */
    public static final String TAG = "ExportAsyncTask";

    private String mDBFilename;


    public ExportAsyncTask(Context context, TaskDelegate delegate) {
        this.mContext = context;
        this.mDBFilename = PennyApp.DB_NAME;
        this.mDelegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mContext instanceof Activity) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle(R.string.backup_in_progress);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setProgressNumberFormat(null);
            mProgressDialog.setProgressPercentFormat(null);

            mProgressDialog.show();
        }
    }

    /**
     * Generates the appropriate exported transactions file for the given parameters
     *
     * @param params Export parameters
     * @return <code>true</code> if export was successful, <code>false</code> otherwise
     */
    @Override
    protected Boolean doInBackground(Uri... params) {
        try {

            Uri exportUri = params[0];

            if (exportUri == null) {
                Log.w(TAG, "No URI found for export destination");
                return false;
            }

            if (!TextUtils.isEmpty(mDBFilename)) {
                try {

                    OutputStream outputStream = mContext.getContentResolver().openOutputStream(exportUri);
                    String filepath = mContext.getDatabasePath(mDBFilename).toString();
                    BackUpUtil.zip(outputStream, new String[]{filepath});

                } catch (IOException ex) {
                    Log.e(TAG, "Error when zipping backup file for export");
                    ex.printStackTrace();
                    Crashlytics.logException(ex);
                }
            }

        } catch (final Exception e) {
            Log.e(TAG, "Error exporting: " + e.getMessage());
            Crashlytics.logException(e);
            e.printStackTrace();
            if (mContext instanceof Activity) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,
                                mContext.getString(R.string.toast_export_error)
                                        + "\n" + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return false;
        }


        return true;
    }

    /**
     * Transmits the exported transactions to the designated location, either SD card or third-party application
     * Finishes the activity if the export was starting  in the context of an activity
     *
     * @param exportSuccessful Result of background export execution
     */
    @Override
    protected void onPostExecute(Boolean exportSuccessful) {
        if (exportSuccessful) {
            mDelegate.onUploadComplete(null);
            Prefs.putLong(Constants.LAST_BACK_UP_DATE, new Date().getTime());
        } else {
            mDelegate.onError(null);
        }

        dismissProgressDialog();
    }

    private void dismissProgressDialog() {
        if (mContext instanceof Activity) {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }

}
