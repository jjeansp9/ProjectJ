package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BriefingData implements Parcelable {
    public int seq; // 설명회 글 seq
    public String title; // 제목
    public String content; // 내용
    public String acaCode; // 캠퍼스 코드
    public String acaName; // 캠퍼스 이름
    public String acaGubunCode; // 캠퍼스 구분코드
    public String acaGubunName; // 캠퍼스 구분이름
    public int writerSeq; // 작성자 seq
    public String date; // 날짜(yyyy-MM-dd)
    public String ptTime; // 시간
    public String place; // 장소
    public int participantsCnt; // 참가인원
    public int reservationCnt; // 현재예약수
    public int rdcnt; // 조회수
    public String fileId; // 파일 ID
    public List<FileData> fileList; // 파일목록

    public BriefingData() {}

    protected BriefingData(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        content = in.readString();
        acaCode = in.readString();
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        writerSeq = in.readInt();
        date = in.readString();
        ptTime = in.readString();
        place = in.readString();
        participantsCnt = in.readInt();
        reservationCnt = in.readInt();
        rdcnt = in.readInt();
        fileId = in.readString();
        fileList = in.createTypedArrayList(FileData.CREATOR);
    }

    public static final Creator<BriefingData> CREATOR = new Creator<BriefingData>() {
        @Override
        public BriefingData createFromParcel(Parcel in) {
            return new BriefingData(in);
        }

        @Override
        public BriefingData[] newArray(int size) {
            return new BriefingData[size];
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
        dest.writeString(acaCode);
        dest.writeString(acaName);
        dest.writeString(acaGubunCode);
        dest.writeString(acaGubunName);
        dest.writeInt(writerSeq);
        dest.writeString(date);
        dest.writeString(ptTime);
        dest.writeString(place);
        dest.writeInt(participantsCnt);
        dest.writeInt(reservationCnt);
        dest.writeInt(rdcnt);
        dest.writeString(fileId);
        dest.writeTypedList(fileList);
    }
}
