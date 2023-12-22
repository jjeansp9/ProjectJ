package kr.jeet.edu.student.model.data;

import android.os.Parcelable;

import java.util.List;

import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.utils.Utils;

public class SystemNoticeData implements ReadData {
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
    public boolean isRead = true;

    @Override
    public String getDate() {
        return Utils.formatDate(insertDate, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
    }

    @Override
    public String getTime() {
        return null;
    }

    @Override
    public int getSeq() {
        return seq;
    }

    @Override
    public boolean getIsRead() {
        return isRead;
    }

    @Override
    public void setDate(String date) {
        this.insertDate = Utils.formatDate(date, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
    }

    @Override
    public void setTime(String time) {
        // 시스템알림엔 time 파라미터가 없음. date 파라미터 참고
    }

    @Override
    public void setSeq(int seq) {
        this.seq = seq;
    }

    @Override
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
