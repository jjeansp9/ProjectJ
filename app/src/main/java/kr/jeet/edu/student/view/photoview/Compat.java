package kr.jeet.edu.student.view.photoview;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

public class Compat {

    public static void postOnAnimation(View view, Runnable runnable) {
        view.postOnAnimation(runnable);
    }



}
