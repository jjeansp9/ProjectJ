package kr.jeet.edu.student.server;

import java.util.List;

import kr.jeet.edu.student.model.data.SystemNoticeData;
import kr.jeet.edu.student.model.data.TestReserveData;
import kr.jeet.edu.student.model.request.BriefingReserveRequest;
import kr.jeet.edu.student.model.request.CounselRequest;
import kr.jeet.edu.student.model.request.LevelTestRequest;
import kr.jeet.edu.student.model.request.PushConfirmRequest;
import kr.jeet.edu.student.model.request.SignupRequest;
import kr.jeet.edu.student.model.request.SignupSNSRequest;
import kr.jeet.edu.student.model.request.SmsRequest;
import kr.jeet.edu.student.model.request.UpdateProfileRequest;
import kr.jeet.edu.student.model.request.UpdatePushStatusRequest;
import kr.jeet.edu.student.model.request.UpdatePushTokenRequest;
import kr.jeet.edu.student.model.response.AnnouncementListResponse;
import kr.jeet.edu.student.model.response.BCListResponse;
import kr.jeet.edu.student.model.response.BaseResponse;
import kr.jeet.edu.student.model.response.BoardAttributeResponse;
import kr.jeet.edu.student.model.response.BoardDetailResponse;
import kr.jeet.edu.student.model.response.BriefingDetailResponse;
import kr.jeet.edu.student.model.response.BriefingReserveResponse;
import kr.jeet.edu.student.model.response.BriefingReservedListResponse;
import kr.jeet.edu.student.model.response.BriefingResponse;
import kr.jeet.edu.student.model.response.BusInfoResponse;
import kr.jeet.edu.student.model.response.BusRouteResponse;
import kr.jeet.edu.student.model.response.FindIDResponse;
import kr.jeet.edu.student.model.response.FindPWResponse;
import kr.jeet.edu.student.model.response.GetACAListResponse;
import kr.jeet.edu.student.model.response.GetAttendanceInfoResponse;
import kr.jeet.edu.student.model.response.LTCListResponse;
import kr.jeet.edu.student.model.response.LoginResponse;
import kr.jeet.edu.student.model.response.NoticeListResponse;
import kr.jeet.edu.student.model.response.ScheduleDetailResponse;
import kr.jeet.edu.student.model.response.ScheduleListResponse;
import kr.jeet.edu.student.model.response.SchoolListResponse;
import kr.jeet.edu.student.model.response.SearchChildStudentsResponse;
import kr.jeet.edu.student.model.response.StudentInfoResponse;
import kr.jeet.edu.student.model.response.SystemNoticeResponse;
import kr.jeet.edu.student.model.response.TeacherClsResponse;
import kr.jeet.edu.student.model.response.TestReserveListResponse;
import kr.jeet.edu.student.model.response.TestReserveNoticeResponse;
import kr.jeet.edu.student.model.response.TestTimeResponse;
import kr.jeet.edu.student.model.response.TuitionResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitApi {

    //public final static String SERVER_BASE_URL = "http://192.168.2.55:7777/"; // pjh local
    public final static String SERVER_BASE_URL = "http://192.168.2.77:7777/"; // khj local
    //public final static String SERVER_BASE_URL = "http://211.43.14.242:7777/"; // 이전 cloud local
    //public final static String SERVER_BASE_URL = "http://211.252.86.237:7777/"; // 신규 cloud local

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

    // 캠퍼스 목록 조회(테스트예약)
    @GET("levelTest/campuses")
    Call<LTCListResponse> getLTCList();

    // 학교 목록 조회
    @GET("school")
    Call<SchoolListResponse> getSchoolList();

    // 게시판 속성 조회
    @GET("board/attrb")
    Call<BoardAttributeResponse> getBoardAttribute();

    // 공지사항 목록 조회
    @GET("notices")
    Call<AnnouncementListResponse> getAnnouncementList(@Query("noticeSeq") int noticeSeq, @Query("acaCode") String acaCode);

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

    // 레벨테스트 등록
    @POST("levelTest")
    Call<BaseResponse> requestLevelTest(@Body LevelTestRequest request);

    // 레벨 테스트 이력 조회
    @GET("levelTests")
    Call<TestReserveListResponse> getTestReserveListResponse(@Query("memberSeq") int memberSeq, @Query("ltcCode") int ltcCode, @Query("lastSeq") int lastSeq);

    // 레벨 테스트 테스트시간 조회
    @GET("levelTest/testTimes")
    Call<TestTimeResponse> getTestTime();

    // 레벨테스트 신규등록 공지사항
    @GET("policy/leveltest/notice")
    Call<TestReserveNoticeResponse> getLevelTestNotice();

    // 설명회 목록 조회
    @GET("pts")
    Call<BriefingResponse> getBriefingList(@Query("acaCode") String acaCode, @Query("year") int year, @Query("month") int month);

    // 설명회 글 상세보기
    @GET("pt/{ptSeq}")
    Call<BriefingDetailResponse> getBriefingDetail(@Path("ptSeq") int ptSeq);

    // 설명회 예약 등록
    @POST("pt/reservation")
    Call<BriefingReserveResponse> requestBrfReserve(@Body BriefingReserveRequest request);

    // 설명회 예약자 목록 조회
    @GET("pt/reservation/{ptSeq}/member/{memberSeq}")
    Call<BriefingReservedListResponse> getBrfReservedList(@Path("ptSeq") int ptSeq, @Path("memberSeq") int memberSeq);

    // 알림장 상세 조회
    @GET("systemNotice/{seq}")
    Call<SystemNoticeResponse> getSystemNoticeDetail(@Path("seq") int seq);

    // 캠퍼스일정 목록 조회
    @GET("schedules")
    Call<ScheduleListResponse> getScheduleList(@Query("acaCode") String acaCode, @Query("year") int year, @Query("month") int month);

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
    // 버스 노선 조회
    @GET("bus/route")
    Call<BusRouteResponse> getBusRoute(@Query("busAcaName") String busAcaName, @Query("busCode") int busCode);
}
