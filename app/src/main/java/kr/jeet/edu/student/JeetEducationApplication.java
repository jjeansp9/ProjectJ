package kr.jeet.edu.student;

import android.app.Application;

import kr.jeet.edu.student.db.JeetDatabase;
public class JeetEducationApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JeetDatabase.getInstance(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
