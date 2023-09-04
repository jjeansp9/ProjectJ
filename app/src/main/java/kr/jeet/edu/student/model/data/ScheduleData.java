package kr.jeet.edu.student.model.data;

import java.util.List;

public class ScheduleData {
    public int seq;          // 글 seq
    public String title;     // 제목
    public String content;   // 내용
    public String target;    // 대상
    public String acaCode;   // 캠퍼스 코드
    public String acaName;   // 캠퍼스 명
    public int year;         // 년
    public int month;        // 월
    public int day;          // 일
    public int writerSeq;    // 작성자 seq
    public List<ReceiverData> receiverList;
}
