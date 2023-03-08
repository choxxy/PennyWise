package com.iogarage.ke.pennywise.util;

import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;

/**
 * Interface for delegates which can be used to execute functions when an AsyncTask is complete
 *
 * @author Ngewi Fet <ngewif@gmail.com>
 */
public interface TaskDelegate {

    void onUploadComplete(FileMetadata result);

    void onError(Exception e);

    void onDownloadComplete(File result);
}