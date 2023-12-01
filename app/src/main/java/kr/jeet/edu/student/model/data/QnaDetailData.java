package kr.jeet.edu.student.model.data;

public class QnaDetailData {
    public int seq;                // 글 seq
    public int writerSeq;          // 작성자 seq
    public String writerNm;        // 작성자명
    public int userGubun;          // 작성자 유저구분
    public int stCode;             // (부모앱) 원생 고유번호
    public String acaCode;         // 캠퍼스코드
    public String acaName;         // 캠퍼스이름
    public String acaGubunCode;    // 캠퍼스 구분코드
    public String acaGubunName;    // 캠퍼스 구분이름
    public String title;           // 제목
    public String content;         // 내용
    public String reply;           // 답변 내용
    public int replyMberSeq;       // 답변자 seq
    public String replyMberNm;     // 답변자명
    public int sfCode;             // 답변자 고유번호
    public String isOpen;          // 공개여부(Y/N)
    public String isMain;          // 공지여부(Y/N)
    public String state;           // 상태(신청1/접수2/완료3)
    public int rdcnt;              // 조회수
    public String insertDate;      // 작성일
    public String isSubAdmin;      // 중간관리자여부(Y/N)
    public String isReplyAdmin;    // 답변관리자여부(Y/N)
}
