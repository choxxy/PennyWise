package com.iogarage.ke.pennywise.db;

import android.database.sqlite.SQLiteDatabase;


/**
 * Created by choxxy on 13/06/2017.
 */

public interface Migrator {

    void migrate(SQLiteDatabase db);
}
