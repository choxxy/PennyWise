package com.iogarage.ke.pennywise;

import com.iogarage.ke.pennywise.entities.DaoSession;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Joshua on 2/9/2015.
 */
@Module
public class AppModule {

    PennyApp pennyApp;

    public AppModule(PennyApp app) {
        this.pennyApp = app;
    }

    @Provides
    @Singleton
    public DaoSession providesDaoSession() {
        return pennyApp.getDaoSession();
    }

 }
