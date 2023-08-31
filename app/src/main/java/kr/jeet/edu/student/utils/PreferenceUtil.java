package kr.jeet.edu.student.utils;

import android.content.Context;
import android.content.SharedPreferences;

import kr.jeet.edu.student.common.Constants;

public class PreferenceUtil {

    private static final String PREFERENCE_NAME = "kr.jeet.edu.student.preferences";

    public static final String PREF_AUTO_LOGIN = "auto_login"; // 자동로그인
    public static final String PREF_SNS_USERID = "sns_user_id"; // SNS USER ID
    public static final String PREF_LOGIN_TYPE = "login_type"; // LOGIN TYPE
    public static final String PREF_USER_TYPE = "user_type"; // USER TYPE

    public static final String PREF_USER_IS_ORIGINAL = "user_is_original"; // 회원, 비회원 구분

    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_PW = "user_pw";
    public static final String PREF_USER_PHONE_NUM_STU = "user_phone_num_stu";
    public static final String PREF_USER_PHONE_NUM_PARENT = "user_phone_num_parent";
    public static final String PREF_USER_SEQ = "user_seq"; // 사용자 seq
    public static final String PREF_USER_GUBUN = "user_gubun"; // 사용자 구분
    public static final String PREF_USER_STCODE = "user_stcode"; // 사용자 stcode
    public static final String PREF_USER_GENDER = "user_gender"; // 자녀원생 성별
    public static final String PREF_STU_SEQ = "stu_seq"; // 원생 seq
    public static final String PREF_ST_NAME = "user_stname"; // 자녀원생 이름
    public static final String PREF_STU_GENDER = "user_stu_gender"; // 자녀원생 성별
    public static final String PREF_NUMBER_OF_CHILD = "number_of_child"; // 자녀 인원 수(1명인 경우 HeaderView에 자녀선택화면 아이콘 숨기기)

    public static final String PREF_ACA_CODE = "aca_code"; // 캠퍼스 코드
    public static final String PREF_ACA_NAME = "aca_name"; // 캠퍼스 이름
    public static final String PREF_ACA_TEL = "aca_tel"; // 캠퍼스 대표번호

    public static final String PREF_PUSH_TOKEN = "push_token";    //PUSH TOKEN

    public static final String PREF_NOTIFICATION_ANNOUNCEMENT = "notification_announcement";    //공지사항 push 여부
    public static final String PREF_NOTIFICATION_SEMINAR = "notification_seminar";    //설명회 push 여부
    public static final String PREF_NOTIFICATION_ATTENDANCE = "notification_attendance";    //출석 push 여부
    public static final String PREF_NOTIFICATION_SYSTEM = "notification_system";    //시스템알림 push 여부
    //Auto Login
    public static void setAutoLogin(Context context, boolean set) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_AUTO_LOGIN, set).apply();
    }

    public static boolean getAutoLogin(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_AUTO_LOGIN, false);
    }

    //Login Type
    public static void setLoginType(Context context, int loginType) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_LOGIN_TYPE, loginType).apply();
    }

    public static int getLoginType(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_LOGIN_TYPE, Constants.LOGIN_TYPE_NORMAL);
    }

    // user is original
    public static void setUserIsOriginal(Context context, String loginType) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_USER_IS_ORIGINAL, loginType).apply();
    }

    public static String getUserIsOriginal(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_IS_ORIGINAL, "");
    }

    //User Type
    public static void setUserType(Context context, String userType) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_USER_TYPE, userType).apply();
    }

    public static String getUserType(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_TYPE, Constants.MEMBER);
    }

    //SNS User ID
    public static void setSNSUserId(Context context, String id) {
        if (id != null) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_SNS_USERID, id).apply();
        }
    }

    public static String getSNSUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_SNS_USERID, "");
    }

    // PREF_MEMBER_SEQ
    public static void setUserSeq(Context context, int seq) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_USER_SEQ, seq).apply();
    }

    public static int getUserSeq(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_USER_SEQ, 0);
    }

    // member id
    public static void setUserId(Context context, String id) {
        if (id != null) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_USER_ID, id).apply();
        }
    }

    public static String getUserId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_ID, "");
    }

    // member pw
    public static void setUserPw(Context context, String id) {
        if (id != null) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_USER_PW, id).apply();
        }
    }

    public static String getUserPw(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_PW, "");
    }

    // stu phoneNum
    public static void setStuPhoneNum(Context context, String phoneNum) {
        if (phoneNum != null) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_USER_PHONE_NUM_STU, phoneNum).apply();
        }
    }

    public static String getStuPhoneNum(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_PHONE_NUM_STU, "");
    }

    // parent phoneNum
    public static void setParentPhoneNum(Context context, String phoneNum) {
        if (phoneNum != null) {
            SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            pref.edit().putString(PREF_USER_PHONE_NUM_PARENT, phoneNum).apply();
        }
    }

    public static String getParentPhoneNum(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_PHONE_NUM_PARENT, "");
    }

    // PREF_USER_GUBUN
    public static void setUserGubun(Context context, int seq) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_USER_GUBUN, seq).apply();
    }

    public static int getUserGubun(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_USER_GUBUN, Constants.USER_TYPE_ADMIN);
    }

    // PREF_STU_SEQ
    public static void setStuSeq(Context context, int stuSeq) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_STU_SEQ, stuSeq).apply();
    }

    public static int getStuSeq(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_STU_SEQ, 0);
    }

    // PREF_USER_GENDER
    public static void setUserGender(Context context, String gender) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_USER_GENDER, gender).apply();
    }

    public static String getUserGender(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_USER_GENDER, "");
    }

    // PREF_STU_GENDER
    public static void setStuGender(Context context, String gender) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_STU_GENDER, gender).apply();
    }

    public static String getStuGender(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_STU_GENDER, "");
    }

    // PREF_ST_NAME
    public static void setStName(Context context, String stName) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_ST_NAME, stName).apply();
    }

    public static String getStName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_ST_NAME, "");
    }

    // PREF_USER_STCODE
    public static void setUserSTCode(Context context, int stCode) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_USER_STCODE, stCode).apply();
    }

    public static int getUserSTCode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_USER_STCODE, Constants.USER_TYPE_ADMIN);
    }

    // PREF_NUMBER_OF_CHILD
    public static void setNumberOfChild(Context context, int num) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_NUMBER_OF_CHILD, num).apply();
    }

    public static int getNumberOfChild(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_NUMBER_OF_CHILD, Constants.NUMBER_OF_CHILD);
    }

    //PUSH TOKEN
    public static void setPrefPushToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_PUSH_TOKEN, token).apply();
    }

    public static String getPrefPushToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_PUSH_TOKEN, "");
    }

    // aca code
    public static void setAcaCode(Context context, String acaCode) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_ACA_CODE, acaCode).apply();
    }

    public static String getAcaCode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_ACA_CODE, Constants.CAMPUS_ACA_CODE);
    }

    // aca name
    public static void setAcaName(Context context, String acaName) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_ACA_NAME, acaName).apply();
    }

    public static String getAcaName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_ACA_NAME, Constants.CAMPUS_ACA_NAME);
    }

    // aca name
    public static void setAcaTel(Context context, String acaTel) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_ACA_TEL, acaTel).apply();
    }

    public static String getAcaTel(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_ACA_TEL, Constants.CAMPUS_ACA_NAME);
    }

    // notification announcement
    public static void setNotificationAnnouncement(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_NOTIFICATION_ANNOUNCEMENT, flag).apply();
    }
    public static boolean getNotificationAnnouncement(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_NOTIFICATION_ANNOUNCEMENT, true);
    }

    // notification seminar
    public static void setNotificationSeminar(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_NOTIFICATION_SEMINAR, flag).apply();
    }
    public static boolean getNotificationSeminar(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_NOTIFICATION_SEMINAR, true);
    }

    // notification attendance
    public static void setNotificationAttendance(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_NOTIFICATION_ATTENDANCE, flag).apply();
    }
    public static boolean getNotificationAttendance(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_NOTIFICATION_ATTENDANCE, true);
    }

    // notification system
    public static void setNotificationSystem(Context context, boolean flag) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_NOTIFICATION_SYSTEM, flag).apply();
    }
    public static boolean getNotificationSystem(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_NOTIFICATION_SYSTEM, true);
    }

}
