package kr.jeet.edu.student.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.IntroActivity;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.db.PushMessageDao;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import kr.jeet.edu.student.utils.Utils;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG = "firebase";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        LogMgr.e(TAG, "onNewToken() : " + token);
        // 로그인 정보가 있으면 서버에 push token 전달
        int memberSeq = PreferenceUtil.getUserSeq(getApplicationContext());
        if(memberSeq != 0)
        {
            Utils.refreshPushToken(this, memberSeq, token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        LogMgr.d(TAG, "onMessageReceived~");
        super.onMessageReceived(message);
        LogMgr.e(TAG, "Push From : " + message.getFrom());

//        if (message.getData().size() > 0) {
//            String jsonData = new JSONObject(message.getData()).toString();
//            LogMgr.d("FCM", "Received data: " + jsonData);
//        }else{
//            LogMgr.d("FCM", "Received data size 0");
//        }

//        // 알림 메시지가 있는 경우 알림 내용 출력
//        if (message.getNotification() != null) {
//            String notificationBody = message.getNotification().getBody();
//            LogMgr.d("FCM", "Notification body: " + notificationBody);
//        }
        if (message.getNotification() != null) {
            // notify message
            RemoteMessage.Notification notification = message.getNotification();
        }
        if (message.getData().size() > 0) {
            // data message
            Map<String, String> map = message.getData();
            LogMgr.i(TAG, "[FCM] size : " + message.getData().size());
            LogMgr.i(TAG, "[FCM] payload : " + message.getData());
            for (String key : map.keySet()) {
                LogMgr.e(TAG, "key = " + key + " : value = " + map.get(key));
            }
            if(map != null){
                PushMessage pushMsg = PushMessage.buildFromMap(map, getApplicationContext());
                class InsertRunnable implements Runnable {
                    PushMessage[] pushMessages;
                    InsertRunnable(PushMessage... pushMessages) {
                        this.pushMessages = pushMessages;
                    }
                    @Override
                    public void run() {
                        JeetDatabase.getInstance(getApplicationContext()).pushMessageDao().insertAll(this.pushMessages);
                    }
                }
                InsertRunnable insertRunnable = new InsertRunnable(pushMsg);
                Thread t = new Thread(insertRunnable);
                t.start();
                FCMManager fcmManager = new FCMManager(this, pushMsg);
                fcmManager.handlePushMessage();
            }
        }

        // https://moon8089.tistory.com/10
        // https://velog.io/@leeyjwinter/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-Firebase-FCM
    }


//    void subscribeTopic (String topic){
//        FirebaseMessaging.getInstance().subscribeToTopic(topic)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        String msg = "Subscribed";
//                        if (!task.isSuccessful()) {
//                            msg = "Subscribe failed";
//                        }
//                        LogMgr.d(TAG, msg);
//                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    void unSubscribeTopic(String topic){
//        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        String msg = "unSubscribed";
//                        if (!task.isSuccessful()) {
//                            msg = "unSubscribe failed";
//                        }
//                        LogMgr.d(TAG, msg);
//                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

}
