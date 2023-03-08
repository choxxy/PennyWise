
/*
 * Copyright (c) 2016 Ngewi Fet <ngewif@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iogarage.ke.pennywise.backup;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.iogarage.ke.pennywise.PennyApp;
import com.iogarage.ke.pennywise.util.BackUpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Helper class for commonly used DropBox methods
 */
public class DropboxDownloadFileTask extends AsyncTask<Void, Void, File> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final CallBack mCallback;
    private Exception mException;

    public interface CallBack {

        void onError(Exception e);

        void onDownloadComplete(File result);
    }

    public DropboxDownloadFileTask(Context context, DbxClientV2 dbxClient, CallBack callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    @Override
    protected File doInBackground(Void... params) {

        try {
            File parent = mContext.getDatabasePath(PennyApp.DB_NAME).getParentFile();
            String fileName = PennyApp.DB_NAME;
            File file = new File(parent, fileName);

            // Make sure the Downloads directory exists.
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + parent);
                }
            } else if (!parent.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + parent);
                return null;
            }

            if (file.exists())
                file.delete();

            String remoteFileName = BackUpUtil.formatDBFileName(PennyApp.REMOTE_FILE, "pw");

            // Download the file.
            try (OutputStream outputStream = new FileOutputStream(file)) {
                mDbxClient.files().download(remoteFileName)
                        .download(outputStream);
            }

            return file;

        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}