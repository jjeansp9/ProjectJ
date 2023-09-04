package kr.jeet.edu.student.model.data;

import java.util.List;

public class SystemNoticeData {
    public int seq;                    // 알림장 seq
    public int writerSeq;              // 작성자 seq
    public String writerName;          // 작성자 이름
    public String title;               // 제목
    public String content;             // 내용
    public String isSendSms;           // SMS 발신 여부
    public int receiverCnt;            // 수신인 수
    public String acaCode;             // 캠퍼스 코드
    public String acaName;             // 캠퍼스 이름
    public String fileId;              // 첨부 파일 id
    public String insertDate;          // 등록일
    public List<ReceiverData> receiverList;
    public List<FileData> fileList;
}
