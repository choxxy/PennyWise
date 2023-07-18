package com.iogarage.ke.pennywise.service.gdrive

import android.content.Context
import android.os.Environment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import com.iogarage.ke.pennywise.domain.TransactionRepository
import com.iogarage.ke.pennywise.domain.entity.TransactionWithPayments
import com.iogarage.ke.pennywise.service.Resource
import com.iogarage.ke.pennywise.service.Session
import com.iogarage.ke.pennywise.util.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DriveManager @Inject constructor(
    @ApplicationContext val context: Context,
    private val session: Session,
    private val repository: TransactionRepository,
    private val preference: AppPreferences
) {

    companion object {
        const val IMAGE_CONTENT = "image/*"
        const val JSON_CONTENT = "application/json"
        const val TRANSACTIONS = "transactions"
        const val MEDIAS = "medias"
        const val FOLDER = "application/vnd.google-apps.folder"
    }

    //region Google Drive Util functions

    private fun getDriveService(): Drive? {
        GoogleSignIn.getLastSignedInAccount(context)?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount.account!!
            return Drive.Builder(
                NetHttpTransport(), GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName(session.appName)
                .build()
        }
        Timber.e(Throwable("Returning null to caller"))
        return null
    }

    private fun createFolder(): String? {
        val googleDrive = getDriveService()
        var folderId: String? = null
        if (googleDrive != null) {
            try {
                val folderData = com.google.api.services.drive.model.File()
                folderData.name = session.appName
                folderData.mimeType = FOLDER
                val folder = googleDrive.files().create(folderData).execute()
                Timber.d("Folder Id: $folder")
                folderId = folder.id
                preference.putString(AppPreferences.GOOGLE_DRIVE_FOLDER_ID, folder.id)

            } catch (ex: Exception) {
                Timber.e(ex)
                ex.printStackTrace()
            }
        }
        return folderId

    }

    private fun searchFile(
        fileName: String? = null,
        mimeType: String? = null,
        folderId: String? = null
    ): List<com.google.api.services.drive.model.File>? {
        Timber.d("Searching.. $fileName")

        var fileList: List<com.google.api.services.drive.model.File>? = null
        try {
            val drive = getDriveService()
            if (drive != null) {
                var pageToken: String? = null
                val request = drive.files().list().apply {
                    fields = "nextPageToken, files(id, name)"
                    pageToken = pageToken
                }
                var query: String? = null
                fileName?.let {
                    query = "name=\"$fileName\""
                }

                mimeType?.let {
                    query = if (query == null) {
                        "mimeType='$it'"
                    } else {
                        "$query and mimeType = '$it'"
                    }

                }
                folderId?.let {
                    request.spaces = it
                }
                request.q = query

                val result = request.execute()
                fileList = result.files
            }

        } catch (ex: Exception) {
            Timber.e(ex)
            ex.printStackTrace()
        }
        Timber.d("File found: ${fileList?.size}")
        return fileList
    }

    private fun uploadFileToGDrive(
        file: File,
        type: String,
        folderId: String? = null
    ): com.google.api.services.drive.model.File? {
        getDriveService()?.let { googleDriveService ->
            try {
                val gfile = com.google.api.services.drive.model.File()
                gfile.name = file.name
                folderId?.let {
                    gfile.parents = listOf(folderId)
                }
                val fileContent = FileContent(type, file)
                val result = googleDriveService.Files().create(gfile, fileContent).execute()

                Timber.d("File has been updated loaded successfully: ${result.id}, ${result.name}, ${result.webContentLink}, ${result.webViewLink}")
                return result
            } catch (e: Exception) {
                Timber.e(e)
                e.printStackTrace()
            }

        } ?: Timber.e("Signin error - not logged in")
        return null
    }


    // To perform delete operation
    private fun delete(fileId: String) {
        try {
            val drive = getDriveService()
            drive?.files()?.delete(fileId)?.execute()
            Timber.d("Deleted files")
        } catch (ex: Exception) {
            Timber.e("Exception during delete: $ex")
        }

    }

    /**
     * spaces — A comma-separated list of spaces to query within the corpus. Supported values are drive, appDataFolder and photos. Here we use drive.
    fields — The information required about each file like id, name, etc.
    q — A query for filtering the file results. See the “Search for Files” guide for supported syntax.

    @link https://medium.com/android-dev-hacks/integrating-google-drive-api-in-android-applications-18024f42391c]*/
    private suspend fun accessDriveFiles() {
        withContext(Dispatchers.IO) {
            Timber.d("accessDriveFiles")
            getDriveService()?.let { googleDriveService ->
                var pageToken: String? = null
                do {
                    val result = googleDriveService.files().list().apply {
                        fields = "nextPageToken, files(id, name)"
                        pageToken = this.pageToken
                        spaces = spaces
                    }.execute()
                    Timber.d("Result : ${result.files.size}")
                    for (file in result.files) {
                        Timber.d("name=${file.name} id=${file.id}")
                    }
                } while (pageToken != null)
            }
        }
    }

//endregion

//region Perform Backup
    /***
     * To perform backup operations
     */
    suspend fun doSync() = channelFlow {
        send(Resource.loading("Connecting with Google Drive.."))
        val isDataSync = preference.getString(AppPreferences.LAST_SYNC_TIME)

        // Data was not synced before and some data is existed before the sync
        if (isDataSync == null && repository.getAllDebtWithPayments().isNotEmpty()) {
            Timber.d("Local data exist, so performing merge operation")
            // Have local data, need to merge the items during import
            doDownloadDBAndMerge().collect {
                send(it)
            }
            doUploadBackUp().collect {
                send(it)
            }

        } else {
            doDownLoadAndFreshSync().collect {
                send(it)
            }
        }

    }

    private fun doDownLoadAndFreshSync() = channelFlow {
        val isDataSync = preference.getString(AppPreferences.LAST_SYNC_TIME)
        if (isDataSync == null) {
            Timber.d("Data was NOT synced before. Perform sync first")
            doDownloadAndSync().collect {
                Timber.d("Received callback from performRestore: ${it.data}")
                send(it)
            }

            doUploadBackUp().collect {
                Timber.d("Received callback from performBackup: ${it.data}")
                send(it)
            }
        } else {
            doUploadBackUp().collect {
                send(it)
            }
        }
    }

    private suspend fun doUploadBackUp() = channelFlow {
        val folderId = getFolderId()
        withContext(Dispatchers.IO) {
            send(Resource.loading("Uploading Database backup"))
            doUpdateDatabase(folderId = folderId)
            send(Resource.loading("Uploading media backup"))
            doUpdateMedia(folderId = folderId)
            send(Resource.success("Completed"))
        }
    }

    private fun getFolderId(): String? {
        var folderId = preference.getString(AppPreferences.GOOGLE_DRIVE_FOLDER_ID)
        if (folderId == null) {
            folderId = searchFile(session.appName, mimeType = FOLDER)?.firstOrNull()?.id
            if (folderId == null) {
                folderId = createFolder()
            }
        }
        return folderId
    }


    private fun jsonFileName(): String {
        return session.appName + ".json"
    }

    private suspend fun doUpdateDatabase(folderId: String?) {
        withContext(Dispatchers.IO) {
            var prevJsonFile = preference.getString(AppPreferences.JSON_FILE_ID)
            if (prevJsonFile == null) {
                val files = searchFile(
                    jsonFileName(),
                    mimeType = JSON_CONTENT
                )

                // Remove all files ends with json
                files?.forEach {
                    Timber.d("Trying to delete ${it.name} & ${it.id}")
                    delete(it.id)
                }

            } else {
                Timber.d("Trying to delete OLD Json $prevJsonFile")
                delete(prevJsonFile)
            }

            val lstTransactions = repository.getAllDebtWithPayments()

            val jsonObject = JSONObject()
            val gson = Gson()
            jsonObject.put(TRANSACTIONS, gson.toJson(lstTransactions))
            val json = jsonObject.toString()
            val jsonPath = context.getExternalFilesDir(null)
            try {
                jsonPath?.let {
                    val jsonFile = File(it.path + "/" + jsonFileName())
                    val fileOutputStream = FileOutputStream(jsonFile)
                    fileOutputStream.write(json.toByteArray())

                    val fileInfo = uploadFileToGDrive(
                        file = jsonFile,
                        type = JSON_CONTENT,
                        folderId = folderId
                    )
                    Timber.d("Json file name: ${fileInfo?.id}")
                    fileInfo?.let { googleFile ->
                        preference.putString(AppPreferences.JSON_FILE_ID, googleFile.id)
                    }
                }

            } catch (ex: Exception) {
                Timber.e(ex)
            }

        }


    }

    private suspend fun doUpdateMedia(folderId: String?) {
        val pictureDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (pictureDir?.isDirectory == true) {
            val files = pictureDir.listFiles()

            files?.let {
                for (file in files) {
                    if (searchFile(file.name)?.isEmpty() == true) {
                        Timber.d("File name: ${file.name}")
                        uploadFileToGDrive(file, IMAGE_CONTENT, folderId)
                    } else {
                        Timber.d("File already EXIST")
                    }
                }
            }
            accessDriveFiles()
        }
    }

//endregion

    //region Restore Backup
    private fun downloadFileFromGDrive(id: String): ByteArrayOutputStream? {
        val drive = getDriveService()
        if (drive != null) {
            try {
                val outputStream = ByteArrayOutputStream()
                drive.files().get(id).executeMediaAndDownloadTo(outputStream)
                return outputStream
            } catch (ex: Exception) {
                Timber.e("Exception during download $ex")
            }
        }
        return null
    }

    private suspend fun doDownloadAndSync() = channelFlow {
        withContext(Dispatchers.IO) {
            send(Resource.loading("Restoring database..."))
            doDownloadDBAndSync()
            // send(Resource.loading("Downloading media files..."))
            // downloadMedia()
        }
    }

    private fun doDownloadDB(): Tables {

        var tables = Tables()
        val prevJsonFile = preference.getString(AppPreferences.JSON_FILE_ID)

        val databaseJson = searchFile(
            fileName = jsonFileName(),
            mimeType = JSON_CONTENT
        )?.firstOrNull()
        if (prevJsonFile == null) {
            Timber.d("Sync is not performed yet in this device")
            if (databaseJson != null) {
                Timber.d("Found database file in Google drive, just import as it is: ${databaseJson.name}")
                val byteStream = downloadFileFromGDrive(databaseJson.id)
                byteStream?.toByteArray()?.let { byteArray ->
                    val jsonString = String(byteArray)
                    Timber.d("Json: $jsonString")
                    val jsonObject = JSONObject(jsonString)
                    val gson = Gson()

                    if (jsonObject.has(TRANSACTIONS)) {
                        val events = gson.fromJson(
                            jsonObject.get(TRANSACTIONS).toString(),
                            Array<TransactionWithPayments>::class.java
                        )
                        tables = tables.copy(lstOfTransactions = events.toList())
                    }
                }
            }
        }
        return tables
    }

    private suspend fun doDownloadDBAndMerge() = channelFlow {
        withContext(Dispatchers.IO) {
            send(Resource.loading("Reading old database"))
            val data = doDownloadDB()
            send(Resource.loading("Updating transactions table"))
            data.lstOfTransactions?.let {
                it.forEach { transaction ->
                    transaction.transaction.transactionId = 0L
                    repository.insertTransactionWithPayments(transactions = transaction)
                }
            }
            // send(Resource.loading("Downloading media files..."))
            // downloadMedia()
        }
    }

    private suspend fun doDownloadDBAndSync() {
        withContext(Dispatchers.IO) {
            val download = doDownloadDB()
            download.lstOfTransactions?.let { it ->
                it.forEach { transaction ->
                    repository.insertTransactionWithPayments(transaction)
                }
            }
        }

    }

    private suspend fun downloadMedia() {
        withContext(Dispatchers.IO) {
            Timber.d("accessDriveFiles")
            getDriveService()?.let { googleDriveService ->
                var pageToken: String? = null
                do {
                    val result = googleDriveService.files().list().apply {
                        fields = "nextPageToken, files(id, name)"
                        pageToken = this.pageToken
                        spaces = spaces
                    }.execute()
                    for (file in result.files) {
                        Timber.d("name=${file.name} id=${file.id}, file extention : ${file.fileExtension}")
                        val outputStream = downloadFileFromGDrive(file.id)
                        outputStream?.let { bos ->
                            val folderName =
                                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            val localFile = File(folderName, file.name)
                            val fileOutputStream = FileOutputStream(localFile)
                            try {
                                bos.writeTo(fileOutputStream)
                            } catch (ex: Exception) {
                                Timber.e(ex)
                                fileOutputStream.close()
                            }
                            Timber.d("Download completed")
                        }

                    }
                } while (pageToken != null)
            }
        }
    }
}