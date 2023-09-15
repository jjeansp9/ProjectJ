package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ScheduleData implements Parcelable {
    public int seq;          // 글 seq
    public String title;     // 제목
    public String content;   // 내용
    public String targetCode;    // 대상
    public String acaCode;   // 캠퍼스 코드
    public String acaName;   // 캠퍼스 명
    public int year;         // 년
    public int month;        // 월
    public int day;          // 일
    public int writerSeq;    // 작성자 seq
    public List<ReceiverData> receiverList;
    public boolean campusAll;

    // Parcelable 구현 부분
    protected ScheduleData(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        content = in.readString();
        targetCode = in.readString();
        acaCode = in.readString();
        acaName = in.readString();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        writerSeq = in.readInt();
        receiverList = new ArrayList<>();
        in.readList(receiverList, ReceiverData.class.getClassLoader());
    }

    public static final Creator<ScheduleData> CREATOR = new Creator<ScheduleData>() {
        @Override
        public ScheduleData createFromParcel(Parcel in) {
            return new ScheduleData(in);
        }

        @Override
        public ScheduleData[] newArray(int size) {
            return new ScheduleData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(targetCode);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(writerSeq);
        dest.writeList(receiverList);
    }
}
