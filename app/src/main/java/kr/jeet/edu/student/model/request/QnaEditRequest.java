package kr.jeet.edu.student.model.request;

public class QnaEditRequest {
    public int seq;          // 글 seq
    public int userGubun;    // 유저 구분
    public String title;     // 제목
    public String content;   // 내용
    public String isOpen;    // 공개여부(Y/N)
//    private String reply;     // (매니저)답변
//    private String isMain;    // (매니저)공지여부(Y/N)
}
