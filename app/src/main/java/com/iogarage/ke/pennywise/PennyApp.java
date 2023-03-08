package com.iogarage.ke.pennywise;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import androidx.multidex.MultiDex;
import android.util.Log;

import com.amitshekhar.DebugDB;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.iogarage.ke.pennywise.db.MySQLiteOpenHelper;
import com.iogarage.ke.pennywise.entities.DaoMaster;
import com.iogarage.ke.pennywise.entities.DaoSession;
import com.iogarage.ke.pennywise.security.LoginFragment;
import com.iogarage.ke.pennywise.security.RegistrationFragment;
import com.iogarage.ke.pennywise.util.Prefs;
import com.jakewharton.threetenabp.AndroidThreeTen;

import javax.inject.Singleton;

import autodagger.AutoComponent;
import io.fabric.sdk.android.Fabric;
import io.multimoon.colorful.ColorfulKt;
import io.multimoon.colorful.Defaults;
import io.multimoon.colorful.ThemeColor;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Joshua on 2/8/2015.
 */
@AutoComponent(
        modules = {AppModule.class},
        subcomponents = {PennyMain.class
                , PaymentView.class
                , TransactionView.class
                , LoginFragment.class
                , RegistrationFragment.class
                , ViewActivity.class
        }
)
@Singleton
public class PennyApp extends Application {

    private final String TAG = PennyApp.class.getSimpleName();
    private final String DB_DEBUG = "debug";

    private DaoSession daoSession;
    private PennyAppComponent mComponent;
    public static String DB_NAME = "pennywise-db";
    public static String REMOTE_FILE = "backup";

    private static Context context;

    private final long DAY = 10_800_000L; //24 hours


    @Override
    public void onCreate() {
        MultiDex.install(this);
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        PennyApp.context = getApplicationContext();

        mComponent = DaggerPennyAppComponent.builder()
                .appModule(new AppModule(this))
                .build();


        Answers.getInstance().logCustom(new CustomEvent("Application Started"));

        initDaoSession();

        AndroidThreeTen.init(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        Log.i(DB_DEBUG, DebugDB.getAddressLog());

        Defaults defaults = new Defaults(ThemeColor.GREEN,
                ThemeColor.AMBER,
                false,
                false,
                0);


        ColorfulKt.initColorful(this, defaults);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(null)
                .setUseDefaultSharedPreference(true)
                .build();

    }

    /**
     * Returns the application context
     */
    public static Context getAppContext() {
        return PennyApp.context;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        Answers.getInstance().logCustom(new CustomEvent("Application Terminated"));
    }

    public PennyAppComponent getComponent() {
        return mComponent;
    }

    private void initDaoSession() {
        //with migration
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(PennyApp.this, PennyApp.DB_NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();

    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

}
