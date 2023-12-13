package kr.jeet.edu.student.model.request;

public class CounselRequest {
    public int memberSeq;  // 멤버 seq
    public String memberName;  // 멤버 이름
    public int userGubun;  // 유저구분
    public String writerName;  // 작성자 이름
    public String counselDate;  // 상담 날짜
    public String acaCode;  // 캠퍼스 코드
    public String acaName;  // 캠퍼스 이름
    public int stCode;  // 원생 고유 값
    public int sfCode;  // 강사 고유 값
    public String sfName;  // 강사 이름
    public String memo;  // 메모
    public String clsName;  // 반 이름
    public String phoneNumber;  // 상담요청자 폰번호
    public String managerPhoneNumber;  // 강사 폰번호
    public String smsSender;  // 캠퍼스 대표번호
    public String isSendNoti;  // (사용안함)알림전송여부(Y/N)-API문서작성시필요
    public String callWishDate; // 전화 요청일시
    public int bagCode; // 항목 코드
    public String bagName; // 항목 이름
}
