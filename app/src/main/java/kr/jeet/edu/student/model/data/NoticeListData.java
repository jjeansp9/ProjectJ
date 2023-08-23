package kr.jeet.edu.student.model.data;

public class NoticeListData {
    public String noticeType;
    public String noticeAttendanceState;
    public String noticeDate;
    public String noticeReceiver;
    public int noticeSenderAndReceiver;

    public NoticeListData(String noticeType, String noticeAttendanceState, String noticeDate, String noticeReceiver, int noticeSenderAndReceiver) {
        this.noticeType = noticeType;
        this.noticeAttendanceState = noticeAttendanceState;
        this.noticeDate = noticeDate;
        this.noticeReceiver = noticeReceiver;
        this.noticeSenderAndReceiver = noticeSenderAndReceiver;
    }
}
