package kr.jeet.edu.student.common;

public class IntentParams {

    public final static String PARAM_LOGIN_TYPE = "loginType";  // 로그인 타입 [ 일반, sns ]
    public final static String PARAM_LOGIN_USER_NAME = "loginUserName"; //사용자 이름
    public final static String PARAM_LOGIN_USER_GENDER = "loginUserGender"; //사용자 성별
    public final static String PARAM_LOGIN_USER_SNSID = "loginUserSnsId"; //sns id
    public final static String PARAM_LOGIN_USER_GUBUN = "loginUserGubun"; // 로그인 유저 타입
    public final static String PARAM_LOGIN_USER_TYPE = "loginUserType"; // [ 회원, 비회원 구분 ]

    public final static String PARAM_STU_SEQ = "stuSeq";  // 원생 seq
    public final static String PARAM_STU_ACACODE = "acaCode";  // 캠퍼스 코드
    public final static String PARAM_STU_DEPTCODE = "deptCode";  // 부서 코드
    public final static String PARAM_STU_CLSTCODE = "clstCode";  // 학년 코드
    public final static String PARAM_STU_STCODE = "stCode";  // 원생 코드
    public final static String PARAM_STU_STNAME = "stName";  // 원생 이름
    public final static String PARAM_STU_ACANAME = "acaName";  // 캠퍼스 이름
    public final static String PARAM_STU_DEPTNAME = "deptName";  // 부서 이름
    public final static String PARAM_STU_CLSTNAME = "clstName";  // 학년 이름
    public final static String PARAM_BCNAME = "bcName";  // 캠퍼스 이름
    public final static String PARAM_BOARD_SEQ = "boardSeq";  // 게시글 seq
    public final static String PARAM_REPORT_SEQ = "reportSeq";  // 성적표 seq
    public final static String PARAM_WRITER_SEQ = "writerSeq";  // 작성자 seq
    public final static String PARAM_STU_GRADECODE = "gradeCode";  // 구분 코드

    public final static String PARAM_CHILD_STUDENT_INFO = "childStudentInfo";  // 자녀원생 정보
    public final static String PARAM_ANNOUNCEMENT_INFO = "announcementInfo";  // 선택한 공지사항 정보
    public final static String PARAM_BOARD_ITEM = "board_item";  // 글 상세 아이템

    public final static String PARAM_WRITE_MODE = "writeMode";  // 작성모드

    //board resultLauncher
    public final static String PARAM_BOARD_ADDED = "board_added";
    public final static String PARAM_BOARD_EDITED = "board_edited";
    public final static String PARAM_BOARD_DELETED = "board_deleted";

    public final static String PARAM_ANNOUNCEMENT_DETAIL_IMG = "announcementDetailImg";  // 공지사항 상세화면 이미지 item
    public final static String PARAM_BOARD_POSITION = "board_position";  // 선택한 아이템 position
    public final static String PARAM_WEB_DETAIL_IMG = "webDetailImg";  // web 이미지 item (차량)

    public final static String PARAM_WEB_VIEW_URL = "webViewUrl";  // 웹뷰에서 로드할 url
    public final static String PARAM_PUSH_MESSAGE = "pushMessage";  // pushMessage
    public final static String PARAM_APPBAR_TITLE = "appbarTitle";  // appbarTitle
    public final static String PARAM_DATA_TYPE = "dataType";  // 데이터 타입
    public final static String PARAM_TEST_RESERVE_WRITE = "testReserveWrite";  // 테스트예약 등록 항목

    public final static String PARAM_LIST_ITEM = "listItem"; // 리스트 내의 Model Info Key
    public final static String PARAM_SUCCESS_DATA = "successData"; // 글을 수정했을 때 데이터 전달하기 위한 key

    public final static String PARAM_TEST_RESERVE_ADDED = "testReserveAdded"; // 테스트 예약 등록 resultLauncher
    public final static String PARAM_TEST_RESERVE_EDITED = "testReserveEdited"; // 테스트 예약 수정 resultLauncher
    public final static String PARAM_TEST_RESERVE_SAVED = "testReserveSaved"; // 테스트 예약 입력한 데이터 저장
    public final static String PARAM_TEST_NEW_CHILD = "testReserveNewChild"; // 테스트 예약 신규원생 등록
    public final static String PARAM_TEST_NEW_CHILD_FROM_MAIN = "testReserveNewChildFromMain"; // 테스트 예약 신규원생 등록
    public final static String PARAM_TEST_TYPE = "testReserveType"; // 테스트 예약 타입

    public final static String PARAM_BRIEFING_INFO = "briefingInfo";  // 선택한 설명회 정보
    public final static String PARAM_BRIEFING_PARTICIPANTS_CNT = "briefingParticipantsCnt";  // 참가인원
    public final static String PARAM_BRIEFING_RESERVATION_CNT = "briefingReservationCnt";  // 현재예약수
    public final static String PARAM_BRIEFING_RESERVE_ADDED = "briefingPtSeq";  // 설명회 예약 등록

    public final static String PARAM_NOTICE_INFO = "noticeinfo"; // 알림장 목록에서 클릭한 data
    public final static String PARAM_TYPE_NOTICE = "typeNotice"; // 알림으로 진입할 때 처음으로 보여지게 할 타입

    public final static String PARAM_SCHEDULE_INFO = "scheduleInfo"; // 선택한 캠퍼스일정 정보

    public final static String PARAM_ATTENDANCE_INFO = "attendanceInfo"; // Main에서 출석현황 클릭 시 전달
    public final static String PARAM_SYSTEM_INFO = "systemInfo"; // Main에서 알림 클릭 시 전달
    public final static String PARAM_TEACHER_INFO = "teacherInfo";  // Main에서 지도강사 클릭 시 전달

    public final static String PARAM_RD_CNT_ADD = "rdCntAdd";  // 게시글 조회수
    public final static String PARAM_ACCOUNT_NO = "accountNo"; // 수강료 계좌
    public final static String PARAM_REPORT_LIST = "reportList"; // 성적표 목록

}
