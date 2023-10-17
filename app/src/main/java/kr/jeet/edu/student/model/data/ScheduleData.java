package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ScheduleData implements Parcelable {
    public int seq;          // 글 seq
    public String title;     // 제목
    public String content;   // 내용
//    public String target;    // 부서
//    public String targetCode;    // 부서 코드
    public String acaCode;   // 캠퍼스 코드
    public String acaName;   // 캠퍼스 명
    public String acaGubunCode;
    public String acaGubunName;
    public int year;         // 년
    public int month;        // 월
    public int day;          // 일
    public int writerSeq;    // 작성자 seq
    public ArrayList<ReceiverData> receiverList;
    public boolean campusAll;

    protected ScheduleData(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        content = in.readString();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        writerSeq = in.readInt();
        receiverList = in.createTypedArrayList(ReceiverData.CREATOR);
        campusAll = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(writerSeq);
        dest.writeTypedList(receiverList);
        dest.writeByte((byte) (campusAll ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
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
}
