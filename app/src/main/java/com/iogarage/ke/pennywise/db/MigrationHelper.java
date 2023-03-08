package com.iogarage.ke.pennywise.db;


import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.StandardDatabase;
public final class MigrationHelper implements  Migrator{

    public  final boolean DEBUG = false;
    private final String TAG = "MigrationHelper";


    public MigrationHelper() {
    }

    public void migrate(SQLiteDatabase db) {
        Database database = new StandardDatabase(db);

        if (DEBUG) {
            Log.d(TAG, "【Database Version】" + db.getVersion());
        }

        //write your update statements here
        //example  database.execSQL("INSERT INTO table_name(column) values("abc")");
    }


}
