package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class SystemNoticeListData implements Parcelable {
    public int seq;             // 게시물 SEQ
    public String title;        // 알림장 타이틀
    public int receiverCnt;     // 수신자 수
    public String acaCode;      // 캠퍼스 코드
    public String acaName;      // 캠퍼스 이름
    public String acaGubunCode; // 캠퍼스 구분 코드
    public String acaGubunName; // 캠퍼스 구분 이름
    public String insertDate;   // 게시물 등록일
    public String searchType;   // 게시물 유형
    public String pushId;       // (부모앱) PushId

    // Parcelable 생성자
    protected SystemNoticeListData(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        receiverCnt = in.readInt();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        insertDate = in.readString();
        searchType = in.readString();
        pushId = in.readString();
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
        dest.writeString(title);
        dest.writeInt(receiverCnt);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeString(insertDate);
        dest.writeString(searchType);
        dest.writeString(pushId);
    }
}
