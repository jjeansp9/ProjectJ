package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.utils.Utils;

public class SystemNoticeListData implements Parcelable, ReadData {
    public int seq;             // Push SEQ (for paging)
    public int connSeq;         // 게시물 SEQ (for viewing)
    public String title;        // 알림장 타이틀
    public String content;      // 알림장 내용 [미납에서 사용]
    public int receiverCnt;     // 수신자 수
    public int stCode;          // stCode
    public String acaCode;      // 캠퍼스 코드
    public String acaName;      // 캠퍼스 이름
    public String acaGubunCode; // 캠퍼스 구분 코드
    public String acaGubunName; // 캠퍼스 구분 이름
    public String insertDate;   // 게시물 등록일
    public String searchType;   // 게시물 유형
    public String writerName;   // 상담 - 작성자(학생)명
    public String pushId;       // (부모앱) PushId
    public boolean isRead = true;

    // Parcelable 생성자
    protected SystemNoticeListData(Parcel in) {
        seq = in.readInt();
        connSeq = in.readInt();
        title = in.readString();
        content = in.readString();
        receiverCnt = in.readInt();
        stCode = in.readInt();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        insertDate = in.readString();
        searchType = in.readString();
        writerName = in.readString();
        pushId = in.readString();
        isRead = in.readByte() != 0;
    }

    public SystemNoticeListData() {
    }

    // Parcelable.Creator 구현
    public static final Creator<SystemNoticeListData> CREATOR = new Parcelable.Creator<SystemNoticeListData>() {
        @Override
        public SystemNoticeListData createFromParcel(Parcel in) {
            return new SystemNoticeListData(in);
        }

        @Override
        public SystemNoticeListData[] newArray(int size) {
            return new SystemNoticeListData[size];
        }
    };

    // Parcelable 인터페이스 메서드 구현
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeInt(connSeq);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeInt(receiverCnt);
        dest.writeInt(stCode);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeString(insertDate);
        dest.writeString(searchType);
        dest.writeString(writerName);
        dest.writeString(pushId);
        dest.writeByte((byte) (isRead ? 1 : 0));
    }

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
        return connSeq;
    }

    @Override
    public boolean getIsRead() {
        return isRead;
    }

    @Override
    public void setDate(String date) {
        this.insertDate = Utils.formatDate(date, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm); // yyyy-MM-dd HH:ss
    }

    @Override
    public void setTime(String time) {
        // 시스템알림엔 time 파라미터가 없음. date 파라미터 참고
    }

    @Override
    public void setSeq(int seq) {
        this.connSeq = seq;
    }

    @Override
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
