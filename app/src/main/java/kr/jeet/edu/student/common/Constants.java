package kr.jeet.edu.student.common;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.server.RetrofitApi;

public class Constants {

    // 사용자 구분 타입
    public final static int USER_TYPE_ADMIN = 0;
    public final static int USER_TYPE_TEACHER = 1;
    public final static int USER_TYPE_STUDENT = 2;
    public final static int USER_TYPE_PARENTS = 3;

    // 로그인 타입
    public final static int LOGIN_TYPE_NORMAL = 0;
    public final static int LOGIN_TYPE_SNS_NAVER = 1;
    public final static int LOGIN_TYPE_SNS_KAKAO = 2;
    public final static int LOGIN_TYPE_SNS_GOOGLE = 3;
    public final static int LOGIN_TYPE_SNS_APPLE = 4;

    public final static String LOGIN_TYPE_SNS_NAVER_STRING = "Naver";
    public final static String LOGIN_TYPE_SNS_KAKAO_STRING = "Kakao";
    public final static String LOGIN_TYPE_SNS_GOOGLE_STRING = "Google";
    public final static String LOGIN_TYPE_SNS_APPLE_STRING = "Apple";

    public final static String LOCALE_LANGUAGE_KR = "ko";

    // 회원가입 or 로그인 400 BAD_REQUEST MSG
    public final static String ALREADY_LOGIN_IN = "ALREADY_LOGIN_IN"; // 이미 로그인된 사용자
    public final static String PASSWORD_MISMATCH = "PASSWORD_MISMATCH"; // 비밀번호가 틀림
    public final static String PARAMETER_BINDING_ERROR = "PARAMETER_BINDING_ERROR"; // 파라미터 바인딩 에러
    public final static String USER_GUBUN_MISMATCH = "USER_GUBUN_MISMATCH"; // JEET 회원이 아님(app 타입과 조회한 회원의 타입이 다를때)
    public final static String USER_NOT_JEET_MEMBER = "USER_NOT_JEET_MEMBER"; // JEET 관리자가 아니면 회원가입/로그인을 할 수 없음
    public final static String DUPLICATE_ID = "DUPLICATE_ID"; // 아이디 중복
    public final static String DUPLICATE_PHONE_NUMBER = "DUPLICATE_PHONE_NUMBER"; // 전화번호 중복
    public final static String DUPLICATE_USER = "DUPLICATE_USER"; // 일반 -> 일반 가입일 때 전화번호와 UserGubun 값이 동일한 경우

    // 아이디/비밀번호 찾기
    public final static int FIND_TYPE_ID = 0;
    public final static int FIND_TYPE_PW = 1;

    // 회원, 비회원
    public final static String MEMBER = "Y";
    public final static String NON_MEMBER = "N";

    public final static int NUMBER_OF_CHILD = 0;

    public final static String CAMPUS_ACA_CODE = "";
    public final static String CAMPUS_ACA_NAME = "";

    public final static int UPDATE_PROFILE_MODE = 0;
    public final static int UPDATE_PHONE_NUM_MODE = 1;

    public final static int PICKER_MIN_YEAR = 1999;
    public final static int PICKER_MAX_YEAR = 2099;

    public final static String POLICY_SERVICE = RetrofitApi.SERVER_BASE_URL+"web/api/policy/service"; // 이용약관 [회원가입, 설정]
    public final static String POLICY_PRIVACY = RetrofitApi.SERVER_BASE_URL+"web/api/policy/privacy"; // 개인정보취급방침 [회원가입, 설정]
    public final static String POLICY_PRIVACY_PT = RetrofitApi.SERVER_BASE_URL+"web/api/policy/privacyPT"; // 개인정보 취급방침 [설명회예약]
    public final static String POLICY_PRIVACY_LEVEL_TEST_PRIVACY = RetrofitApi.SERVER_BASE_URL+"web/api/policy/leveltestPrivacy"; // 약관동의 [테스트예약]
    public final static String POLICY_PRIVACY_LEVEL_TEST_PRIVACY2 = RetrofitApi.SERVER_BASE_URL+"web/api/policy/leveltestPrivacy2"; // 약관동의 [테스트예약]
    public final static String POLICY_PRIVACY_LEVEL_TEST_PRIVACY3 = RetrofitApi.SERVER_BASE_URL+"web/api/policy/leveltestPrivacy3"; // 약관동의 [테스트예약]
    public final static String POLICY_PRIVACY_LEVEL_TEST_PRIVACY4 = RetrofitApi.SERVER_BASE_URL+"web/api/policy/leveltestPrivacy4"; // 약관동의 [테스트예약]

    // -------------------------------------------------


    // handler
    public final static int HANDLER_SNS_LOGIN_COMPLETE = 10010;  // sns login 완료

    //file provider column
    public static final String FILE_ID = "document_id";
    public static final String FILE_MIME_TYPE = "mime_type";
    public static final String FILE_DISPLAY_NAME = "_display_name";
    //    public static final String FILE_LAST_MODIFIED = "last_modified";
//    public static final String FILE_FLAGS = "flags";
    public static final String FILE_SIZE = "_size";

    public enum BoardEditMode{New, Edit};

    public static final int PHONE_NUM_LENGTH_1 = 11; // 휴대폰번호 length
    public static final int PHONE_NUM_LENGTH_2 = 10; // 휴대폰번호2 length
    public static final int RES_REGISTRATION_NUM_LENGTH = 13; // 주민번호 length
    public static final int COMPANY_REGISTRATION_NUM_LENGTH = 10; // 사업자 등록번호 length
    public static final int BIRTH_LENGTH = 8; // 생년월일 length

    //dateFormatter String
    public static final String DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_FORMATTER_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMATTER_YYYY_MM_DD_HH_mm = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMATTER_YYYY_MM = "yyyy-MM";
    public static final String DATE_FORMATTER_YYYYMM = "yyyyMM";
    public static final String DATE_FORMATTER_YYYY_MM_DD_KOR = "yyyy년 M월 d일";
    public static final String DATE_FORMATTER_YYYY_MM_KOR = "yyyy년 M월";
    public static final String DATE_FORMATTER_YYYYMMDD = "yyyyMMdd";
    public static final String DATE_FORMATTER_YYMMDD = "yyMMdd";
    public static final String DATE_FORMATTER_YYYY = "yyyy";
    public static final String DATE_FORMATTER_MM = "MM";
    public static final String TIME_FORMATTER_A_HH_MM = "a hh:mm";
    public static final String TIME_FORMATTER_HH_MM = "HH:mm";
    public static final String TIME_FORMATTER_M_D_E = "M월 d일 E요일";
    public static final String TIME_FORMATTER_YYYY_M_D_E = "yyyy년 M월 d일 E요일";

    public static final String ACTION_JEET_PUSH_MESSAGE_RECEIVED = "action_jeet_push_message_received";

    public static final String FINISH_COMPLETE = "finish_activity";

    public static final String WRITE_ADD = "ADD";
    public static final String WRITE_EDIT = "Edit";

    public static final String elementary = "초등";
    public static final String middleSchool = "중학교";
    public static final String highSchool = "고등";

    public enum AttendanceStatus{
        UNSET(9, "미지정", R.color.gray),
        ATTENDANCE(0, "출석", R.color.color_attendance),
        TARDY(1, "지각", R.color.color_tardy),
        EARLY_LEAVE(2, "조퇴", R.color.color_early_leave),
        ABSENCE(3, "결석", R.color.color_absence),
        MAKEUP_CLASS(4,"보충", R.color.color_makeup_class),
        ONLINE_LECTURE(5, "줌수업", R.color.color_online_lecture);

        private int attendanceCode;
        private String attendanceNameKor;
        private int colorRes;
        private AttendanceStatus(int code, String name, int colorRes) {
            this.attendanceCode = code;
            this.attendanceNameKor = name;
            this.colorRes = colorRes;
        }
        public int getCode() {
            return attendanceCode;
        }
        public String getName() {
            return attendanceNameKor;
        }
        public int getColorRes(){ return colorRes; }
        public static AttendanceStatus getByCode(int code) {
            for (AttendanceStatus status : AttendanceStatus.values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            return null; // 해당하는 코드 값이 없을 경우 null 반환 또는 다른 처리
        }
        public static AttendanceStatus getByName(String name) {
            for (AttendanceStatus status : AttendanceStatus.values()) {
                if (status.getName().equals(name)) {
                    return status;
                }
            }
            return null; // 해당하는 코드 값이 없을 경우 null 반환 또는 다른 처리
        }
        public static List<String> getNameList() {
            List<String> nameList = new ArrayList<>();
            for (AttendanceStatus status : AttendanceStatus.values()) {
                nameList.add(status.getName());
            }
            return nameList;
        }
        public static List<SpannableString> getColoredNameList(Context context) {
            List<SpannableString> coloredNameList = new ArrayList<>();
            for(AttendanceStatus status : AttendanceStatus.values()) {
                SpannableString span = new SpannableString(status.getName());
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, status.colorRes)), 0, span.length(), 0);
                coloredNameList.add(span);
            }
            return coloredNameList;
        }
    }

    // 수강료 타입
    public enum PayType {
        TUITION(0, "수강료"),
        BOOK_PAY(1, "교재비");

        private int code;
        private String nameKor;
        private PayType(int code, String name) {
            this.code = code;
            this.nameKor = name;
        }
        public int getCode() {
            return code;
        }
        public String getNameKor() {
            return nameKor;
        }
        public static PayType getByCode(int code) {
            for (PayType type : PayType.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            return null; // 해당하는 코드 값이 없을 경우 null 반환 또는 다른 처리
        }
        public static PayType getByName(String name) {
            for (PayType status : PayType.values()) {
                if (status.getNameKor().equals(name)) {
                    return status;
                }
            }
            return null; // 해당하는 코드 값이 없을 경우 null 반환 또는 다른 처리
        }
        public static List<String> getNameList() {
            List<String> nameList = new ArrayList<>();
            for (PayType status : PayType.values()) {
                nameList.add(status.getNameKor());
            }
            return nameList;
        }
    }

    public static final int LAYOUT_ANIM_DURATION = 250;

    public static final int MOVE_DEFAULT = -1;
    public static final int MOVE_LEFT = 0;
    public static final int MOVE_RIGHT = 1;
    public static final int MOVE_UP = 2;
    public static final int MOVE_DOWN = 3;
    public static final int MOVE_DETAIL_RIGHT = 4;

    public static final int LEVEL_TEST_TYPE_NEW_CHILD = 0;

    public interface PayListItem extends Comparable<PayListItem> {
        boolean isHeader();
        Constants.PayType getPay();
    }

    // 설치문자 전송을 제외한 나머지 경우 무조건 아래 지정한 값으로 전달
    public static final String SMS_SENDER_CODE = "556"; // 발신자코드(sfCode)
    public static final String SMS_RECEIVER_CODE = "-1"; // 수신자코드(stCode)

    public static final int SHOW_KEBOARD_DELAY = 200;

    public enum ReportCardType{
        E_ELEMENTARY(0, "악어초등", R.color.blue_sub),
        E_MIDDLE(1, "악어중등", R.color.color_makeup_class),
        MIDDLE(2, "중등", R.color.darkgray),
        KJ_E_MATH(3, "KJ악어수학", R.color.red_sub)
        ;

        private int code;
        private String nameKor;
        private int colorRes;
        private ReportCardType(int code, String name, int colorRes) {
            this.code = code;
            this.nameKor = name;
            this.colorRes = colorRes;
        }
        public int getCode() {
            return code;
        }
        public String getNameKor() {
            return nameKor;
        }
        public int getColorRes(){ return colorRes; }
        public static ReportCardType getByCode(int code) {
            for (ReportCardType type : ReportCardType.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            return null; // 해당하는 코드 값이 없을 경우 null 반환 또는 다른 처리
        }
        public static ReportCardType getByName(String name) {
            for (ReportCardType type : ReportCardType.values()) {
                if (type.getNameKor().equals(name)) {
                    return type;
                }
            }
            return null; // 해당하는 코드 값이 없을 경우 null 반환 또는 다른 처리
        }
        public static List<String> getNameList() {
            List<String> nameList = new ArrayList<>();
            for (ReportCardType type : ReportCardType.values()) {
                nameList.add(type.getNameKor());
            }
            return nameList;
        }
        public static List<SpannableString> getColoredNameList(Context context) {
            List<SpannableString> coloredNameList = new ArrayList<>();
            for(ReportCardType type : ReportCardType.values()) {
                SpannableString span = new SpannableString(type.getNameKor());
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, type.colorRes)), 0, span.length(), 0);
                coloredNameList.add(span);
            }
            return coloredNameList;
        }
    }

    public static int REPORT_MATH_SPAN_COUNT = 5; // 악어수학 성적표 span count 설정

    // 태그 : 공지, 비공개, 신청, 접수, 완료, 본인
    public static String QNA_STATE_NOTICE = "Y";       // 공지
    public static String QNA_STATE_OPEN = "Y";         // 비공개
    public static String QNA_STATE_SUBSCRIPTION = "1"; // 신청
    public static String QNA_STATE_RECEPTION = "2";    // 접수
    public static String QNA_STATE_COMPLETE = "3";     // 완료

    public static int IS_READ_DELETE_DAY = 6;

    public static int LOGOUT_TYPE_APP_FINISH = 0; // 앱종료로 로그아웃 api 호출할 때
    public static int LOGOUT_TYPE_TEST_RESERVE = 1; // 테스트예약하여 데이터를 초기화해야할 때
    public static int LOGOUT_TYPE_LOGOUT = 2; // 설정에서 로그아웃 api 호출할 때
}
