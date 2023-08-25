package kr.jeet.edu.student.model.data;

import java.util.List;

public class SystemNoticeData {
    public int seq;                    // 알림장 seq
    public int writerSeq;              // 작성자 seq
    public String title;               // 제목
    public String content;             // 내용
    public String isSendSms;           // SMS 발신 여부
    public int receiverCnt;            // 수신인 수
    public String fileId;              // 첨부 파일 id
    public String insertDate;          // 등록일
    public String updateDate;          // 수정일
    public List<Receiver> systemNoticeReceiverVOList;
    public List<FileData> fileVOList;

    public static class Receiver {
        public int seq;                    // 수신인 테이블 seq
        public int systemNoticeSeq;        // 알림장 seq
        public int stCode;                 // JEET 회원 고유값
        public String stName;              // 원생/학부모 이름
        public int userGubun;              // JEET 회원 구분값
        public String insertDate;          // 등록일
    }
}
