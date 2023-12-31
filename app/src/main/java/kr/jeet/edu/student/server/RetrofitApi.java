package kr.jeet.edu.student.server;

import java.util.List;

import kr.jeet.edu.student.model.request.BriefingReserveRequest;
import kr.jeet.edu.student.model.request.CounselRequest;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.model.request.PushConfirmRequest;
import kr.jeet.edu.student.model.request.QnaAddRequest;
import kr.jeet.edu.student.model.request.QnaEditRequest;
import kr.jeet.edu.student.model.request.SignupRequest;
import kr.jeet.edu.student.model.request.SignupSNSRequest;
import kr.jeet.edu.student.model.request.SmsRequest;
import kr.jeet.edu.student.model.request.UpdateProfileRequest;
import kr.jeet.edu.student.model.request.UpdatePushStatusRequest;
import kr.jeet.edu.student.model.request.UpdatePushTokenRequest;
import kr.jeet.edu.student.model.response.AnnouncementListResponse;
import kr.jeet.edu.student.model.response.BCListResponse;
import kr.jeet.edu.student.model.response.BagResponse;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.BoardAttributeResponse;
import kr.jeet.edu.student.model.response.BoardDetailResponse;
import kr.jeet.edu.student.model.response.BoardNewResponse;
import kr.jeet.edu.student.model.response.BriefingDetailResponse;
import kr.jeet.edu.student.model.response.BriefingReserveResponse;
import kr.jeet.edu.student.model.response.BriefingReservedListResponse;
import kr.jeet.edu.student.model.response.BriefingResponse;
import kr.jeet.edu.student.model.response.BusDriveHistoryResponse;
import kr.jeet.edu.student.model.response.BusInfoResponse;
import kr.jeet.edu.student.model.response.BusRouteResponse;
import kr.jeet.edu.student.model.response.FindIDResponse;
import kr.jeet.edu.student.model.response.FindPWResponse;
import kr.jeet.edu.student.model.response.GetACAListResponse;
import kr.jeet.edu.student.model.response.GetAttendanceInfoResponse;
import kr.jeet.edu.student.model.response.LTCListResponse;
import kr.jeet.edu.student.model.response.LevelTestSubjectResponse;
import kr.jeet.edu.student.model.response.LoginResponse;
import kr.jeet.edu.student.model.response.QnaDetailResponse;
import kr.jeet.edu.student.model.response.QnaListResponse;
import kr.jeet.edu.student.model.response.ReportCardShowResponse;
import kr.jeet.edu.student.model.response.ReportCardSummaryResponse;
import kr.jeet.edu.student.model.response.ScheduleDetailResponse;
import kr.jeet.edu.student.model.response.ScheduleListResponse;
import kr.jeet.edu.student.model.response.SchoolListResponse;
import kr.jeet.edu.student.model.response.SearchChildStudentsResponse;
import kr.jeet.edu.student.model.response.StudentGradeListResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.model.response.SystemNoticeListResponse;
import kr.jeet.edu.student.model.response.SystemNoticeResponse;
import kr.jeet.edu.student.model.response.SystemReportListResponse;
import kr.jeet.edu.student.model.response.TeacherClsResponse;
import kr.jeet.edu.student.model.response.TestInflowResponse;
import kr.jeet.edu.student.model.response.TestReserveListResponse;
import kr.jeet.edu.student.model.response.TestReserveNoticeResponse;
import kr.jeet.edu.student.model.response.TestTimeResponse;
import kr.jeet.edu.student.model.response.TuitionResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitApi {

    //public final static String SERVER_BASE_URL = "http://192.168.2.51:7777/";   //kyt local
    //public final static String SERVER_BASE_URL = "http://192.168.2.55:7777/"; // pjh local
    //public final static String SERVER_BASE_URL = "http://192.168.2.77:7777/"; // khj local
    public final static String SERVER_BASE_URL = "http://192.168.2.83:7777/"; // jwj local
    //public final static String SERVER_BASE_URL = "http://211.252.86.237:7777/"; // 테스트 cloud local
    //public final static String SERVER_BASE_URL = "http://211.252.86.6/"; // 운영 cloud
    //public final static String SERVER_BASE_URL = "https://www.jeetapp.kr/"; // domain

    public final static String PREFIX = "mobile/api/";
    public final static String FILE_SUFFIX_URL = SERVER_BASE_URL + "attachFile/";

    public final static int RESPONSE_CODE_SUCCESS = 200;
    public final static int RESPONSE_CODE_SUCCESS2 = 201;
    public final static int RESPONSE_CODE_BINDING_ERROR= 400;
    public final static int RESPONSE_CODE_NOT_FOUND= 404;
    public final static int RESPONSE_CODE_DUPLICATE_ERROR= 409;
    public final static int RESPONSE_CODE_INERNAL_SERVER_ERROR= 500;

    // 회원가입
    @POST("member/signUp")
    Call<BaseResponse> signUp(@Body SignupRequest signUpRequest);

    // 로그인(일반)
    @POST("member/signIn")
    Call<LoginResponse> signIn(@Query("id") String id, @Query("pw") String pw);

    // 로그아웃
    @POST("member/logout")
    Call<BaseResponse> logout(@Query("memberSeq") int memberSeq);
    // 회원탈퇴
    @POST("member/withdrawal")
    Call<BaseResponse> leave(@Query("memberSeq") int memberSeq);
    // SMS 전송
    @POST("sms")
    Call<BaseResponse> sendSms(@Body SmsRequest request);

    // 회원가입 SNS
    @POST("member/sns/signUp")
    Call<BaseResponse> signUpFromSNS(@Body SignupSNSRequest signUpRequest);

    //  로그인(SNS)
    @POST("member/sns/signIn")
    Call<LoginResponse> signInSNS(@Query("snsId") String id);

    //  아이디찾기
    @GET("member/id")
    Call<FindIDResponse> findID(@Query("phoneNumber") String phoneNumber, @Query("appType") String appType);

    //  PW 찾기
    @GET("member/pw")
    Call<FindPWResponse> findPW(@Query("phoneNumber") String phoneNumber, @Query("memberId") String memberId);

    //UPDATE PUSH TOKEN
    @POST("push/token")
    Call<BaseResponse> updatePushToken(@Body UpdatePushTokenRequest request);

    //SUBSCRIBE / Unscribe PUSH TOPIC
//    @POST("push/topic")
//    Call<BaseResponse> updateSubscribePushTopic(@Query("memberSeq") int memberSeq, @Query("token") String token);

    //푸쉬 수신처리
    @POST("push/receive")
    Call<BaseResponse> pushReceived(@Query("pushId") List<String> pushId);

    //푸쉬 확인처리
    @POST("push/confirm")
    Call<BaseResponse> pushConfirmed(@Body PushConfirmRequest request);
    // 학부모의 자녀 원생 정보 조회
    @GET("member/{parentMemberSeq}/students")
    Call<SearchChildStudentsResponse> searchChildStudents(@Path("parentMemberSeq") int parentMemberSeq);

    // 원생 정보 조회
    @GET("member/{memberSeq}/st/{stCode}")
    Call<StudentInfoResponse> studentInfo(@Path("memberSeq") int memberSeq, @Path("stCode") int stCode);

    // 캠퍼스 목록 조회
    @GET("aca")
    Call<GetACAListResponse> getACAList();

    // 지역기준 캠퍼스 조회
    @GET("appAca")
    Call<GetACAListResponse> getLocalACAList();
    // 캠퍼스 목록 조회(테스트예약)
    @GET("levelTest/campuses")
    Call<LTCListResponse> getLTCList();
    // 학생등급 조회 (지역기준 캠퍼스)
    @GET("appAcaGubun/{appAcaCode}")
    Call<StudentGradeListResponse> getStudentGradeList(@Path("appAcaCode") String appAcaCode);
    // 학교 목록 조회
    @GET("school")
    Call<SchoolListResponse> getSchoolList();

    // 게시판 속성 조회
    @GET("board/attrb")
    Call<BoardAttributeResponse> getBoardAttribute();

    // 공지사항 목록 조회
    @GET("notices")
    Call<AnnouncementListResponse> getAnnouncementList(@Query("noticeSeq") int noticeSeq, @Query("acaCode") String acaCode, @Query("acaGubunCode") String acaGubunCode);

    // 공지사항 글 상세보기
    @GET("notice/{noticeSeq}")
    Call<BoardDetailResponse> getBoardDetail(@Path("noticeSeq") int noticeSeq);

    // 회원 정보 수정(이름, 성별)
    @PATCH("member")
    Call<BaseResponse> updateProfile(@Body UpdateProfileRequest updateProfileRequest);

    // 회원 비밀번호 수정
    @PATCH("member/pw")
    Call<BaseResponse> updatePassword(@Query("memberSeq") int memberSeq, @Query("pw") String pw);

    // 회원 알림 수신 여부 수정
    @PATCH("member/pushStatus")
    Call<BaseResponse> updatePushStatus(@Body UpdatePushStatusRequest updatePushStatus);

    // 회원 휴대폰번호 수정
    @PATCH("member/pn")
    Call<BaseResponse> updatePhoneNum(@Query("memberSeq") int memberSeq, @Query("phoneNumber") String phoneNumber);

    // 수강료 조회
    @GET("tuition/{tuitionDate}/st/{stCode}")
    Call<TuitionResponse> getTuitionList(@Path("tuitionDate") String tuitionDate, @Path("stCode") int stCode);

    // Get-Student-Attendance (출결 조회 - 월별)
    @GET("attendance/{attendanceDate}/cls/{clsCode}/st/{stCode}")
    Call<GetAttendanceInfoResponse> getMonthlyAttendanceInfo(@Path("attendanceDate") String attendanceDate, @Path("clsCode") int clsCode, @Path("stCode") int stCode);

    // 원생 학급 정보 조회
    @GET("member/st/{stCode}/clsDate/{clsDate}/class")
    Call<TeacherClsResponse> requestTeacherCls(@Path("stCode") int stCode, @Path("clsDate") String clsDate);

    // 상담요청
    @POST("counsel")
    Call<BaseResponse> requestCounsel(@Body CounselRequest counselRequest);

    // 상담 분류항목 조회
    @GET("counsel/bag")
    Call<BagResponse> getBag();

    // 레벨테스트 등록
    @POST("levelTest")
    Call<BaseResponse> requestLevelTest(@Body LevelTestRequest request);

    // 레벨 테스트 이력 조회
    @GET("levelTests")
    Call<TestReserveListResponse> getTestReserveListResponse(@Query("memberSeq") int memberSeq, @Query("ltcCode") int ltcCode, @Query("lastSeq") int lastSeq);

    // 레벨테스트 유입경로 조회
    @GET("levelTest/inflow")
    Call<TestInflowResponse> requestInflow();

    // 레벨 테스트 테스트시간 조회
    @GET("levelTest/testTimes")
    Call<TestTimeResponse> getTestTime();

    // 레벨 테스트 테스트과목 조회
    @GET("levelTest/subject")
    Call<LevelTestSubjectResponse> getLTCSubject();

    // 레벨테스트 신규등록 공지사항
    @GET("policy/leveltest/notice")
    Call<TestReserveNoticeResponse> getLevelTestNotice();

    // 설명회 목록 조회
    @GET("pts")
    Call<BriefingResponse> getBriefingList(@Query("acaCode") String acaCode, @Query("acaGubunCode") String acaGubunCode, @Query("year") int year, @Query("month") int month);

    // 설명회 글 상세보기
    @GET("pt/{ptSeq}")
    Call<BriefingDetailResponse> getBriefingDetail(@Path("ptSeq") int ptSeq);

    // 설명회 예약 등록
    @POST("pt/reservation")
    Call<BriefingReserveResponse> requestBrfReserve(@Body BriefingReserveRequest request);

    // 설명회 예약 취소
    @DELETE("pt/reservation")
    Call<BaseResponse> requestBrfCancel(@Query("reservationSeq") int reservationSeq, @Query("memberSeq") int memberSeq, @Query("userGubun") int userGubun);

    // 설명회 예약자 목록 조회
    @GET("pt/reservation/{ptSeq}/member/{memberSeq}")
    Call<BriefingReservedListResponse> getBrfReservedList(@Path("ptSeq") int ptSeq, @Path("memberSeq") int memberSeq);

    // 알림장 목록 조회
    @GET("systemNotices")
    Call<SystemNoticeListResponse> getSystemNoticeList(
            @Query("searchType") String searchType,
            @Query("searchDate") String searchDate,
            @Query("sfCode") int sfCode,
            @Query("stCode") int stCode,
            @Query("memberSeq") int memberSeq,
            @Query("userGubun") int userGubun,
            @Query("seq") int seq,
            @Query("acaCode") String acaCode,
            @Query("acaGubunCode") String acaGubunCode
    );

    // 알림장 상세 조회
    @GET("systemNotice/{seq}")
    Call<SystemNoticeResponse> getSystemNoticeDetail(@Path("seq") int seq);

    // 캠퍼스일정 목록 조회
    @GET("schedules")
    Call<ScheduleListResponse> getScheduleList(@Query("acaCode") String acaCode, @Query("acaGubunCode") String acaGubunCode, @Query("year") int year, @Query("month") int month);
    // 캠퍼스일정 상세 조회
    @GET("schedule/{scheduleSeq}")
    Call<ScheduleDetailResponse> getScheduleDetail(@Path("scheduleSeq") int scheduleSeq);

    //버스 캠퍼스 조회
    @GET("bus/campuses")
    Call<BCListResponse> getBusCampusList();
    // 버스 정보 조회
    @GET("bus")
    Call<BusInfoResponse> getBusInfo(@Query("phoneNumber") String phoneNumber);
    // 버스 목록 조회
    @GET("buses")
    Call<BusInfoResponse> getBusesInfo(@Query("busAcaName") String busAcaName);
    // 버스 운행 이력 조회
    @GET("bus/drive")
    Call<BusDriveHistoryResponse> getBusDriveHistory(@Query("busAcaName") String busAcaName, @Query("busCode") int busCode);
    // 버스 노선 조회
    @GET("bus/route")
    Call<BusRouteResponse> getBusRoute(@Query("busAcaName") String busAcaName, @Query("busCode") int busCode);
    // 성적표 목록 조회
    @GET("reportCards")
    Call<SystemReportListResponse> getReportList(
            @Query("reportSeq") int reportSeq,
            @Query("memberSeq") int memberSeq,
            @Query("userGubun") int userGubun,
            @Query("stCode") int stCode,
            @Query("searchKeyword") String searchKeyword
            //@Query("acaCode") String acaCode
    );
    //성적표 상세조회
    @GET("reportCard/{reportSeq}")
    Call<ReportCardSummaryResponse> getReportCardDetailList(@Path("reportSeq") int reportSeq);
    //성적표별 데이터 상세조회
    @GET("reportCard/{reportSeq}/list/{reportListSeq}")
    Call<ReportCardShowResponse> getReportCardShowList(@Path("reportSeq") int reportSeq, @Path("reportListSeq") int reportListSeq);
    //QnA 목록 조회
    @GET("qnas")
    Call<QnaListResponse> getQnaList(
            @Query("qnaSeq") int qnaSeq,
            @Query("memberSeq") int memberSeq,
            @Query("userGubun") int userGubun,
            @Query("acaCode") String acaCode,
            @Query("acaGubunCode") String acaGubunCode,
            @Query("isOriginalMember") String isOriginalMember
    );
    //QnA 상세 조회
    @GET("qna/detail")
    Call<QnaDetailResponse> getQnaDetail(
            @Query("qnaSeq") int qnaSeq,
            @Query("userGubun") int userGubun
    );
    // QnA 글 등록
    @POST("qna")
    Call<BaseResponse> insertQna(@Body QnaAddRequest request);
    // QnA 글 수정
    @PATCH("qna")
    Call<BaseResponse> updateQna(@Body QnaEditRequest request);
    // QnA 글 삭제
    @DELETE("qna/{qnaSeq}")
    Call<BaseResponse> deleteQna(@Path("qnaSeq") int qnaSeq);
    // 공지사항 일주일 이내 글 SEQ - 공지사항 메뉴아이콘에 New 표시하기위한 api
    @GET("notices/new")
    Call<BoardNewResponse> getNoticeNewList();
    // 설명회예약 일주일 이내 글 SEQ - 설명회에약 메뉴아이콘에 New 표시하기위한 api
    @GET("pts/new")
    Call<BoardNewResponse> getPtNewList();
}
