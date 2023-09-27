package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ReceiverData implements Parcelable {
    public int seq;                    // 수신인 테이블 seq
    public int stCode;                 // JEET 회원 고유값
    public String stName;              // 원생/학부모 이름
    public int userGubun;              // JEET 회원 구분값
    public String acaCode;             // 캠퍼스 코드
    public String acaName;             // 캠퍼스 이름
    public String isRead;              // 확인여부

    public ReceiverData() {
        // 기본 생성자
    }

    // Parcelable 인터페이스를 구현하는 코드
    protected ReceiverData(Parcel in) {
        seq = in.readInt();
        stCode = in.readInt();
        stName = in.readString();
        userGubun = in.readInt();
        acaCode = in.readString();
        acaName = in.readString();
        isRead = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeInt(stCode);
        dest.writeString(stName);
        dest.writeInt(userGubun);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(isRead);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReceiverData> CREATOR = new Creator<ReceiverData>() {
        @Override
        public ReceiverData createFromParcel(Parcel in) {
            return new ReceiverData(in);
        }

        @Override
        public ReceiverData[] newArray(int size) {
            return new ReceiverData[size];
        }
    };
}
