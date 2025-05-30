package com.vietflight.inventory;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class VietFlightApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo Firebase cho toàn bộ app
        FirebaseApp.initializeApp(this);
    }
}