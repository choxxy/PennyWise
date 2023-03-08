package com.iogarage.ke.pennywise.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.iogarage.ke.pennywise.entities.DaoMaster;

/**
 * Created by Growth on 2016/3/3.
 */
public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Migrator migrator = new  MigrationHelper();
        migrator.migrate(db);
    }
}
