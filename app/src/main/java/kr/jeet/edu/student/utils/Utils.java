package kr.jeet.edu.student.utils;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.db.JeetDatabase;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.dialog.DatePickerFragment;
import kr.jeet.edu.student.fcm.FCMManager;
import kr.jeet.edu.student.model.request.UpdatePushTokenRequest;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.server.RetrofitClient;
import kr.jeet.edu.student.view.DrawableAlwaysCrossFadeFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Utils {

    /**
     * 6자리 난수 생성
     */
    public static int generateAuthNum() {
        //return ThreadLocalRandom.current().nextInt(100000, 1000000);
        java.util.Random generator = new java.util.Random();
        generator.setSeed(System.currentTimeMillis());
        return generator.nextInt(1000000) % 1000000;
    }

    /**
     * JEET에서 로그아웃 API 호출하면 자동으로 PUSH Token 도 DB 에서 날리고 Topic도 해제 해주기 때문에
     * Preference 에 저장된 토큰만 삭제하기
    * */
    public static void refreshPushToken(Context context, int memberSeq, String... tokenArg) {

        if(tokenArg == null || tokenArg.length == 0) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if(!task.isSuccessful()) {
                        LogMgr.e("FCM registration token failed : " + task.getException().getMessage());
                        return ;
                    }

                    // todo. request server
                    String newToken = task.getResult();
                    LogMgr.e("new FCM token : " + newToken);
                    if(memberSeq > 0) {
                        requestUpdatePushTokenToServer(context, memberSeq, newToken);
                    }
                }
            });

        } else {
            // todo. request server
            String newToken = tokenArg.length > 0? tokenArg[0] : "";
            if(memberSeq > 0) {
                requestUpdatePushTokenToServer(context, memberSeq, newToken);
            }
        }
    }
    public static void requestUpdatePushTokenToServer(Context context, int memberSeq, String token) {
        if(memberSeq == 0) return;
        UpdatePushTokenRequest request = new UpdatePushTokenRequest(memberSeq, token);
        if(RetrofitClient.getInstance() != null) {
            RetrofitApi retrofitApi = RetrofitClient.getApiInterface();
            retrofitApi.updatePushToken(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if(response.isSuccessful()) {
                        String oldToken = PreferenceUtil.getPrefPushToken(context);
                        LogMgr.e("old FCM token : " + oldToken);
                        if(!token.equalsIgnoreCase(oldToken)) {
                            LogMgr.e("preference put : " + token);
                            PreferenceUtil.setPrefPushToken(context, token);
                        }
                        //requestUpdatePushTopic(context, memberSeq);
                    } else {
                        LogMgr.e("updatePushToken() errBody : " + response.errorBody().toString());
                        Toast.makeText(context, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    LogMgr.e("updatePushToken() onFailure >> " + t.getMessage());
                    Toast.makeText(context, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    // Push Topic update
//    public static void requestUpdatePushTopic(Context context, int memberSeq){
//
//        if (RetrofitClient.getInstance() != null){
//            RetrofitApi retrofitApi = RetrofitClient.getApiInterface();
//            String token = PreferenceUtil.getPrefPushToken(context);
//            retrofitApi.updateSubscribePushTopic(memberSeq, token).enqueue(new Callback<BaseResponse>() {
//                @Override
//                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
//                    try {
//                        if (response.isSuccessful()){
//                            LogMgr.e("requestUpdatePushTopic() success");
//
//                        }else{
//                            Toast.makeText(context, R.string.server_fail, Toast.LENGTH_SHORT).show();
//                        }
//                    }catch (Exception e){
//                        LogMgr.e(e.getMessage());
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<BaseResponse> call, Throwable t) {
//                    try {
//                        LogMgr.e("requestUpdatePushTopic() onFailure >> " + t.getMessage());
//                    }catch (Exception e){
//                    }
//                }
//            });
//        }
//    }
    /**
     * ID찾기에서 문자열 blind 처리 ( 1/2 ceil)
     * @param sourceString 원본 문자열
     * @return blind된 문자열
     */
    public static String makeBlind(String sourceString) {
        int blindCount = 0;
        String convertedString = "";
        if(TextUtils.isEmpty(sourceString)){
            return "";
        }
        blindCount = (sourceString.length() / 2);

        if(blindCount == 0) {
            convertedString = sourceString;
        }else{
            convertedString = sourceString.substring(0, sourceString.length() - blindCount);
            for(int i = 0; i < blindCount; i++) {
                convertedString += "*";
            }
        }
        return convertedString;
    }

    /**
     * 파라미터로 받은 editText의 개수만큼 focus 얻어오고 키보드를 내리는 메소드
     * */
    public static void hideKeyboard(Context mContext, View[] focusList) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) return;
        for (int i = 0; i < focusList.length; i++) {
            View view = focusList[i];
            if (view != null) inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideKeyboard(Context mContext, View focus) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) return;
        inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * EditText List Focus Clear
     * */
    public static void clearFocus(View[] focusList){ for (View v : focusList) v.clearFocus(); }

    public static String getSNSProvider(String snsCode) {
        switch(snsCode) {
            case "N" :
                return Constants.LOGIN_TYPE_SNS_NAVER_STRING;
            case "K":
                return Constants.LOGIN_TYPE_SNS_KAKAO_STRING;
            case "G":
                return Constants.LOGIN_TYPE_SNS_GOOGLE_STRING;
            default:
                return "";
        }
    }

    /**
    * 숫자 천단위마다 콤마(,) 추가하는 메소드
    * */
    public static String decimalFormat(int s){ return new DecimalFormat("###,###").format(s); }

    public static void yearMonthPicker(Context mContext, MonthPickerDialog.OnDateSetListener listener, int year, int month){

        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(mContext, listener, year, month);

        try{
            builder.setYearRange(Constants.PICKER_MIN_YEAR, Constants.PICKER_MAX_YEAR)
                    .setMonthRange(Calendar.JANUARY, Calendar.DECEMBER)
                    .setOnYearChangedListener(v -> {})
                    .setOnMonthChangedListener(v -> {})
                    .setMonthNamesArray(R.array.month_names)
                    .setPositiveText(R.string.ok)
                    .setNegativeText(R.string.cancel)
                    .build().show();
        }catch(Exception e){

        }
    }

    /**
     * number format
    * */
    public static String formatNum(String num){
        String number = "";
        if (num.equals("")){
            number = "010-000-1234";

        }else if (num.length() == Constants.PHONE_NUM_LENGTH_1){ // 휴대폰번호
             number = MessageFormat.format("{0}-{1}-{2}",
                    num.substring(0, 3),
                    num.substring(3, 7),
                    num.substring(7));
        }else if (num.length() == Constants.PHONE_NUM_LENGTH_2){ // 휴대폰번호2
            number = MessageFormat.format("{0}-{1}-{2}",
                    num.substring(0, 3),
                    num.substring(3, 6),
                    num.substring(6));
        }else if (num.length() == Constants.RES_REGISTRATION_NUM_LENGTH){ // 주민번호
            number = MessageFormat.format("{0}-{1}",
                    num.substring(0, 6),
                    num.substring(6));

        }else if (num.length() == Constants.COMPANY_REGISTRATION_NUM_LENGTH){ // 사업자 등록번호
            number = MessageFormat.format("{0}-{1}-{2}",
                    num.substring(0, 3),
                    num.substring(3, 5),
                    num.substring(5));
        }else if(num.length() == Constants.BIRTH_LENGTH){ // 생년월일
            number = MessageFormat.format("{0}-{1}-{2}",
            num.substring(0,4),
            num.substring(4, 6),
            num.substring(6));

        }else{
            number = "010-000-1234";
        }

        return number;
    }

    /**
    * 리스트뷰 구분선에 margin을 주기위한 customDivider
    * */
    public static DividerItemDecoration setDivider(Context mContext) {
        int[] attrs = new int[]{android.R.attr.listDivider};

        TypedArray a = mContext.obtainStyledAttributes(attrs);
        Drawable divider = a.getDrawable(0);
        int inset = mContext.getResources().getDimensionPixelSize(R.dimen.layout_margin);
        InsetDrawable insetDivider = new InsetDrawable(divider, inset, 0, inset, 0);
        a.recycle();

        DividerItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(insetDivider);
        return itemDecoration;
    }

    /**
    * 휴대폰번호의 일정부분 가리기 ( ex: 010-****-**56 )
    * */
    public static String blindPhoneNumber(String number) {
        if (number == null || number.length() != 13) throw new IllegalArgumentException("Invalid phone number format");

        String blind = "-****-**";
        String firstPart = number.substring(0, 3);  // Extracts "010"
        String lastPart = number.substring(number.length() - 2);  // Extracts the last two digits

        return firstPart + blind + lastPart;
    }

    /**
    * 휴대폰번호 유효성검사
    * */
    public static boolean checkPhoneNumber(String str) {
        if(TextUtils.isEmpty(str)) return false;
        return Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", str);
    }

    /**
    * 날짜 포맷 [ yyyy-MM-dd , HH:mm -> M월 d일 (*요일)\n HH시 ss분 ]
    * */
    public static String formatDate(String inputDate, String inputTime, boolean isDetail) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat outputFormat = new SimpleDateFormat("M월 d일 (E요일) ", Locale.KOREA);

        SimpleDateFormat inputTimeFormat = new SimpleDateFormat("HH:mm", Locale.KOREA);
        SimpleDateFormat outputTimeFormat = new SimpleDateFormat("HH시 mm분", Locale.KOREA);

        try {
            Date date = inputDateFormat.parse(inputDate);
            Date time = inputTimeFormat.parse(inputTime);

            String formattedDate = "";
            String formattedTime = "";

            if (time != null) formattedTime = outputTimeFormat.format(time);
            if (date != null) formattedDate = outputFormat.format(date);

            if (isDetail) return formattedDate + "\n" + formattedTime;
            else return formattedDate + inputTime;

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
    * 이메일 형식을 체크해주는 기능
     *
     * @param strEmail - 이메일 형식을 체크하고 싶은 텍스트
    * */
    public static boolean checkEmailForm(String strEmail){

//        final String MATCH = "올바른 이메일 입니다.";
//        final String NOT_MATCH = "올바른 이메일을 입력해주세요.";

        Pattern pattern = Patterns.EMAIL_ADDRESS;
        Matcher matcher = pattern.matcher(strEmail);

        if (matcher.find()) return true;
        else return false;
    }

    public static String getStr(String s){ return TextUtils.isEmpty(s) ? "" : s; }

    /**
    * 현재날짜 가져오기
    * pattern : 날짜 형식 입력 ex) "yyyy.MM.dd HH:ss"
    * */
    public static String currentDate(String pattern){
        Date currentDate = new Date(); // 현재 날짜 가져오기
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault()); // 날짜 형식 지정
        String formattedDate = dateFormat.format(currentDate); // 형식에 맞춰 날짜 문자열로 변환

        return formattedDate;
    }

    public static void changeMessageState2Read(Context getAppContext, String Type) {
        new Thread(() -> {
            try{
                List<PushMessage> pushMessages = JeetDatabase.getInstance(getAppContext).pushMessageDao().getMessageByReadFlagNType(false, Type);
                if(!pushMessages.isEmpty()) {
                    for(PushMessage message : pushMessages) {
                        message.isRead = true;
                        JeetDatabase.getInstance(getAppContext).pushMessageDao().update(message);
                        LogMgr.i("pushMsgDB isRead : ", message.isRead + "");
                    }
                }
            }catch(Exception e){

            }
        }).start();
    }


    public static void createNotification(Context _context, String title, String content){
        PendingIntent pendingIntent;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            pendingIntent = PendingIntent.getActivity(_context, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
//        }else{
//            pendingIntent = PendingIntent.getActivity(_context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//        }
        String channelId = _context.getString(R.string.consult_notification_headup_channel_id);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(_context, channelId)
//                .setTicker(tickerText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);
                //.setContentIntent(pendingIntent);
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

//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMdmsS", Locale.getDefault());
//        String currentTimeStamp = dateFormat.format(new Date());
//
//        int _notifyID = Integer.parseInt(currentTimeStamp);
        long timestamp = System.currentTimeMillis();

        Random random = new Random(timestamp);
        int maxInt = Integer.MAX_VALUE; // 32비트 정수 최대값
        int _notifyID = random.nextInt(maxInt);
        notificationManager.notify(_notifyID, notificationBuilder.build());
    }
}














