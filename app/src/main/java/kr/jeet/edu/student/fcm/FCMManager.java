package kr.jeet.edu.student.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.IntroActivity;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.PreferenceUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FCMManager {
    private static final String TAG = "FCMManager";

    public static final String MSG_TYPE_NOTICE = "NOTICE";  //공지사항
    public static final String MSG_TYPE_ATTEND = "ATTEND";  //출결
    public static final String MSG_TYPE_PT = "PT";  //설명회
    public static final String MSG_TYPE_SYSTEM = "SYSTEM";  //시스템
    public static final String MSG_TYPE_TEST_APPT = "LEVEL_TEST";    //테스트예약
    public static final String MSG_TYPE_COUNSEL = "COUNSEL";    //상담요청
    public static final String MSG_TYPE_VIDEO = "VIDEO";    //동영상
    public static final String MSG_TYPE_ACA_SCHEDULE = "SCHEDULE";    //캠퍼스일정
    //public static final String MSG_TYPE_TEST_APPT = "TEST_APPT";    //테스트예약

    public static final int NOTIFICATION_ID_NOTICE = 1000001;
    public static final int NOTIFICATION_ID_VIDEO = 1000002;
    public static final int NOTIFICATION_ID_ATTEND = 1000003;
    public static final int NOTIFICATION_ID_ACA_SCHEDULE = 1000004;
    public static final int NOTIFICATION_ID_PT = 1000005;
    public static final int NOTIFICATION_ID_SYSTEM = 1000006;
    public static final int NOTIFICATION_ID_TEST_APPT = 1000007;
    public static final int NOTIFICATION_ID_COUNSEL = 1000008;

    Context _context;
    PushMessage _pushMessage;
    int _notifyID;

    public FCMManager(Context context) {
        this._context = context;
    }

    public FCMManager(Context context, PushMessage message) {
        this(context);
        this._pushMessage = message;
    }

    public void setMessage(PushMessage message) {
        this._pushMessage = message;
    }

    public void notifyReceivedMessage(PushMessage message) {

    }
    public void handlePushMessage() {
        handlePushMessage(_pushMessage);
    }
    public void handlePushMessage(PushMessage message) {
        boolean isReject = false;
        boolean isRequireConfirmReceived = false;
        if (message.pushType.equals(MSG_TYPE_NOTICE)) {
            if (PreferenceUtil.getNotificationAnnouncement(_context) == false) {
                isReject = true;
                _notifyID = NOTIFICATION_ID_NOTICE;
            }
        } else if (message.pushType.equals(MSG_TYPE_PT)) {
            if (PreferenceUtil.getNotificationSeminar(_context) == false) {
                isReject = true;
                _notifyID = NOTIFICATION_ID_PT;
            }
        } else if (message.pushType.equals(MSG_TYPE_ATTEND)) {
            if (PreferenceUtil.getNotificationAttendance(_context) == false) {
                isReject = true;
                isRequireConfirmReceived = true;
                _notifyID = NOTIFICATION_ID_ATTEND;
            }
        } else if (message.pushType.equals(MSG_TYPE_SYSTEM)) {
            if (PreferenceUtil.getNotificationSystem(_context) == false) {
                isReject = true;
                isRequireConfirmReceived = true;
                _notifyID = NOTIFICATION_ID_SYSTEM;
            }
        }
        LogMgr.d(TAG, "isReject = " + isReject);
        if(!isReject) {
            createNotification(message);
            if(isRequireConfirmReceived && !TextUtils.isEmpty(message.pushId)) {
                List<String> list = new ArrayList<String>();
                list.add(message.pushId);
                requestPushReceivedToServer(list);
            }
//            Intent intent = new Intent(_context, PushPopupActivity.class);
//            intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, message);
//            _context.startActivity(intent);

        }
    }
    public void requestPushReceivedToServer(List<String> pushId){
        if (RetrofitClient.getInstance() != null) {
            RetrofitApi mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.pushReceived(pushId).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                        } else {
                            Toast.makeText(_context, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestPushReceivedToServer() Exception: ", e.getMessage());
                    }

                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestPushReceivedToServer() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(_context, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    public void requestPushConfirmToServer(List<String> pushId){
        if (RetrofitClient.getInstance() != null) {
            RetrofitApi mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.pushConfirmed(pushId).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                        } else {
                            Toast.makeText(_context, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestPushReceivedToServer() Exception: ", e.getMessage());
                    }

                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestPushReceivedToServer() onFailure >> " + t.getMessage());
                    } catch (Exception e) {
                    }
                    Toast.makeText(_context, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    private void createNotification(PushMessage msg) {
        Intent intent = new Intent(_context, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(IntentParams.PARAM_PUSH_MESSAGE, msg);
        String tickerText = "";
        switch(msg.pushType) {
            case MSG_TYPE_NOTICE:
                tickerText = _context.getString(R.string.push_noti_received_announcement);
                break;
            case MSG_TYPE_ATTEND:
                tickerText = _context.getString(R.string.push_noti_received_attendance);
                break;
            case MSG_TYPE_TEST_APPT:
                tickerText = _context.getString(R.string.push_noti_received_leveltest);
                break;
            case MSG_TYPE_PT:
                tickerText = _context.getString(R.string.push_noti_received_pt);
                break;
            case MSG_TYPE_COUNSEL:
                tickerText = _context.getString(R.string.push_noti_received_counsel);
                break;
            case MSG_TYPE_SYSTEM:
                tickerText = _context.getString(R.string.push_noti_received_system);
            default :
                tickerText = "JEET알림";
                break;
        }
        PendingIntent pendingIntent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(_context, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        }else{
            pendingIntent = PendingIntent.getActivity(_context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }
        String channelId = _context.getString(R.string.default_notification_headup_channel_id);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(_context, channelId)
//                .setTicker(tickerText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(msg.title)
                .setContentText(msg.body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent);
//                .setFullScreenIntent(pendingIntent, true);
        AudioManager audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }else {
            notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);

        }
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager notificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "jeet_notification", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("jeet");
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(_notifyID, notificationBuilder.build());
    }
}
