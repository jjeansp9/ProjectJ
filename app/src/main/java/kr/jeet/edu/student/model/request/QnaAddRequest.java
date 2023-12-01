package kr.jeet.edu.student.model.request;

public class QnaAddRequest {
    public int writerSeq;        // 작성자 seq
    public String writerNm;      // 원생명
    public int userGubun;        // 유저 구분
    public String acaCode;       // 캠퍼스 코드
    public String acaName;       // 캠퍼스 이름
    public String acaGubunCode;  // 캠퍼스 구분코드
    public String acaGubunName;  // 캠퍼스 구분이름
    public String title;         // 제목
    public String content;       // 내용
    public String isOpen;        // 공개여부(Y/N)
    public int stCode;           // 원생 고유번호
}
