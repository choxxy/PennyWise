package com.iogarage.ke.pennywise.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.IOUtils;
import com.iogarage.ke.pennywise.PennyApp;
import com.iogarage.ke.pennywise.R;
import com.iogarage.ke.pennywise.util.BackUpUtil;
import com.iogarage.ke.pennywise.util.TaskDelegate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * Created by choxxy on 01/07/2017.
 */

public class ImportAsyncTask extends AsyncTask<Uri, Void, Boolean> {
    private final Activity mContext;
    private TaskDelegate mDelegate;
    private ProgressDialog mProgressDialog;

    public ImportAsyncTask(Activity context) {
        this.mContext = context;
    }

    public ImportAsyncTask(Activity context, TaskDelegate delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(R.string.title_progress_importing_database);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.show();

        //these methods must be called after progressDialog.show()
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressPercentFormat(null);


    }

    @Override
    protected Boolean doInBackground(Uri... uris) {
        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(uris[0]);
            writeToLocalDb(inputStream);

        } catch (Exception exception) {
            Log.e(ImportAsyncTask.class.getName(), "" + exception.getMessage());
            Crashlytics.log("Could not open: " + uris[0].toString());
            Crashlytics.logException(exception);
            exception.printStackTrace();

            final String err_msg = exception.getLocalizedMessage();
            Crashlytics.log(err_msg);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,
                            mContext.getString(R.string.toast_error_importing_accounts) + "\n" + err_msg,
                            Toast.LENGTH_LONG).show();
                }
            });

            return false;
        }


        return true;
    }


    /**
     * Helper method to write the DriveFile Database to the local SQLite Database file
     *
     * @param inputStream the InputStream of the DriveFile to read data from
     */
    private void writeToLocalDb(InputStream inputStream) {
        String localDb = mContext.getDatabasePath(PennyApp.DB_NAME).getParent();
        BackUpUtil.unzip(inputStream, localDb);
    }


    @Override
    protected void onPostExecute(Boolean importSuccess) {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        } catch (IllegalArgumentException ex) {
            //TODO: This is a hack to catch "View not attached to window" exceptions
            //FIXME by moving the creation and display of the progress dialog to the Fragment
        } finally {
            mProgressDialog = null;
        }

        int message = importSuccess ? R.string.toast_success_importing_accounts : R.string.toast_error_importing_accounts;
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();


        if (mDelegate != null)
            mDelegate.onUploadComplete(null);
    }
}
