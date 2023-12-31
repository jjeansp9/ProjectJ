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
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.activity.IntroActivity;
import kr.jeet.edu.student.activity.MainActivity;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.common.IntentParams;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.request.PushConfirmRequest;
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
    public static final String MSG_TYPE_REPORT = "REPORT";  //성적표
    public static final String MSG_TYPE_TUITION = "TUITION_NO_PAY";  //미납
    public static final String MSG_TYPE_PT = "PT";  //설명회
    public static final String MSG_TYPE_PT_REZ_CNL = "PT_REZ_CNL";  //설명회 예약 취소
    public static final String MSG_TYPE_SYSTEM = "SYSTEM";  //시스템
    public static final String MSG_TYPE_TEST_APPT = "LEVEL_TEST";    //테스트예약
    public static final String MSG_TYPE_TEST_CNL = "LEVEL_CNL";    //테스트예약 취소
    public static final String MSG_TYPE_COUNSEL = "COUNSEL";    //상담요청
    public static final String MSG_TYPE_VIDEO = "VIDEO";    //동영상
    public static final String MSG_TYPE_REPORTCARD = "REPORT";  //성적표
    public static final String MSG_TYPE_ACA_SCHEDULE = "SCHEDULE";    //캠퍼스일정
    public static final String MSG_TYPE_QNA_ING = "QNA_ING";    // qna 접수
    public static final String MSG_TYPE_QNA_COMPLETE = "QNA_COMPLETE";    // qna 접수 완료

    public static final int NOTIFICATION_ID_NONE = 20;

    Context _context;
    PushMessage _pushMessage;
    int _notifyID;
    int requestCode;

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
            }
        } else if (message.pushType.equals(MSG_TYPE_PT) || message.pushType.equals(MSG_TYPE_PT_REZ_CNL)) {
            if (PreferenceUtil.getNotificationSeminar(_context) == false) {
                isReject = true;
            }
        } else if (message.pushType.equals(MSG_TYPE_ATTEND)) {
            if (PreferenceUtil.getNotificationAttendance(_context) == false) {
                isReject = true;
                isRequireConfirmReceived = true;
            }
        } else if (message.pushType.equals(MSG_TYPE_SYSTEM)) {
            if (PreferenceUtil.getNotificationSystem(_context) == false) {
                isReject = true;
                isRequireConfirmReceived = true;
            }
        } else if (message.pushType.equals(MSG_TYPE_ACA_SCHEDULE)) {
            if (PreferenceUtil.getNotificationSchedule(_context) == false) {
                isReject = true;
                isRequireConfirmReceived = true;
            }
        } else if (message.pushType.equals(MSG_TYPE_TEST_APPT)) {
            isReject = false;

        } else if(message.pushType.equals(MSG_TYPE_REPORTCARD)) {
            isReject = false;

        } else if (message.pushType.equals(MSG_TYPE_QNA_ING)) {
            isReject = false;

        } else if (message.pushType.equals(MSG_TYPE_QNA_COMPLETE)) {
            isReject = false;
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
        //sendBroadcast
        Intent sendIntent = new Intent(Constants.ACTION_JEET_PUSH_MESSAGE_RECEIVED);
        sendIntent.putExtra(IntentParams.PARAM_SYSTEM_INFO, message.pushType); // 푸쉬로 알림 noti가 왔을 때 알림 메뉴에 new 표시
        _context.sendBroadcast(sendIntent);
        LogMgr.e(TAG, "sendBroadcast ~ " + message.pushType);
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
    public void requestPushConfirmToServer(PushMessage pushMsg, int stCode){

        PushConfirmRequest request = new PushConfirmRequest();

        List<String> list = new ArrayList<>();
        list.add(pushMsg.pushId);

        request.pushId = list;
        request.pushType = pushMsg.pushType;
        request.seq = pushMsg.connSeq;
        request.stCode = stCode;
        request.userGubun = PreferenceUtil.getUserGubun(_context);

        LogMgr.i(TAG, "pushConfirm" +
                        "\npushId: " + request.pushId.toString() +
                        "\npushType: " + request.pushType +
                        "\nseq: " + request.seq +
                        "\nstCode: " + request.stCode +
                        "\nuserGubun: " + request.userGubun
                );

        if (RetrofitClient.getInstance() != null) {
            RetrofitApi mRetrofitApi = RetrofitClient.getApiInterface();
            mRetrofitApi.pushConfirmed(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try {
                        if (response.isSuccessful()) {

                        } else {
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
                }
            });
        }
    }
    private void createNotification(PushMessage msg) {

        String tickerText = "";
        if (msg != null && msg.pushType != null) {
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
                case MSG_TYPE_PT_REZ_CNL:
                    tickerText = _context.getString(R.string.push_noti_received_pt);
                    break;
                case MSG_TYPE_COUNSEL:
                    tickerText = _context.getString(R.string.push_noti_received_counsel);
                    break;
                case MSG_TYPE_SYSTEM:
                    tickerText = _context.getString(R.string.push_noti_received_system);
                    break;
                case MSG_TYPE_ACA_SCHEDULE:
                    tickerText = _context.getString(R.string.push_noti_received_schedule);
                    break;
                case MSG_TYPE_REPORT:
                    tickerText = _context.getString(R.string.push_noti_received_report_card);
                    break;
                case MSG_TYPE_TUITION:
                    tickerText = _context.getString(R.string.push_noti_received_tuition);
                    break;
                case MSG_TYPE_QNA_ING:
                    tickerText = _context.getString(R.string.push_noti_received_qna);
                    break;
                case MSG_TYPE_QNA_COMPLETE:
                    tickerText = _context.getString(R.string.push_noti_received_qna);
                    break;
                default :
                    tickerText = "JEET알림";
                    break;
            }
        }

        Intent intent = new Intent(_context, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (msg != null) {
            if (msg.pushType != null) {
                LogMgr.e(TAG, "push CreateNoti pushType: " + msg.pushType + ", push ConnSeq: " + msg.connSeq);
            } else {
                LogMgr.e(TAG, "push CreateNoti pushType is null");
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(IntentParams.PARAM_PUSH_MESSAGE, msg);
            intent.putExtras(bundle);
        }

        PendingIntent pendingIntent;
        requestCode = NotificationID.getID(); // notification 생성될 때 마다 다른 값을 추가해줘야함. [ 각각의 notify가 가지고있는 데이터를 구분하기 위함 ]

        pendingIntent = PendingIntent.getActivity(_context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

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
        _notifyID = NotificationID.getID(); // notification 생성될 때 마다 다른 값을 추가해줘야함. [ 기존에 생성된 notify가 덮어쓰기 되지않게 하기 위함 ]
        notificationManager.notify(_notifyID, notificationBuilder.build());
    }
}
