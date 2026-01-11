package com.find.doc;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class DoctorOffLine extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
