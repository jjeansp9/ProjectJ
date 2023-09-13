package kr.jeet.edu.student.common;

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

    public final static String LOGIN_TYPE_SNS_NAVER_STRING = "Naver";
    public final static String LOGIN_TYPE_SNS_KAKAO_STRING = "Kakao";
    public final static String LOGIN_TYPE_SNS_GOOGLE_STRING = "Google";

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
    public static final String DATE_FORMATTER_YYYY_MM_DD = "yyyy-MM-dd";
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

    public static final String ACTION_JEET_PUSH_MESSAGE_RECEIVED = "action_jeet_push_message_received";

    public static final String FINISH_COMPLETE = "finish_activity";

    public static final String WRITE_ADD = "ADD";
    public static final String WRITE_EDIT = "Edit";
}
