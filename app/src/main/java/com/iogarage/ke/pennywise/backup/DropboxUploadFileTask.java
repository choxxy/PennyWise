package com.iogarage.ke.pennywise.backup;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.iogarage.ke.pennywise.PennyApp;
import com.iogarage.ke.pennywise.util.BackUpUtil;
import com.iogarage.ke.pennywise.util.Constants;
import com.iogarage.ke.pennywise.util.Prefs;
import com.iogarage.ke.pennywise.util.TaskDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by choxxy on 04/07/2017.
 */

public class DropboxUploadFileTask extends AsyncTask<Void, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);

        void onError(Exception e);
    }

    public DropboxUploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
            Prefs.putLong(Constants.LAST_BACK_UP_DATE, new Date().getTime());
        }
    }

    @Override
    protected FileMetadata doInBackground(Void... args) {

        File localFile = mContext.getDatabasePath(PennyApp.DB_NAME);

        if (localFile != null) {

            try {
                // Note - this is not ensuring the name is a valid dropbox file name
                //name should be in this pattern (/.*)
                //add slash + extension
                String remoteFileName = BackUpUtil.formatDBFileName(PennyApp.REMOTE_FILE, "pw");

                InputStream inputStream = new FileInputStream(localFile);
                return mDbxClient.files().uploadBuilder(remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (DbxException | IOException e) {
                mException = e;
            }
        }

        return null;
    }


    private boolean fileExists(String dropboxPath) throws DbxException {

        boolean exists = false;

        try {
            mDbxClient.files().getMetadata(dropboxPath);
            exists = true;

        } catch (GetMetadataErrorException e) {

            if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
                System.out.println("File not found.");
                exists = false;
            }
            e.printStackTrace();

        }

        return exists;
    }
}