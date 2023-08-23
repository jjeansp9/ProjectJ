package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementData implements Parcelable {
    @SerializedName("seq")
    public int seq;

    @SerializedName("title")
    public String title;

    @SerializedName("content")
    public String content;

    @SerializedName("rdcnt")
    public int rdcnt;

    @SerializedName("fileId")
    public String fileId;

    @SerializedName("acaCode")
    public String acaCode;

    @SerializedName("insertDate")
    public String insertDate;

    @SerializedName("memberResponseVO")
    public MemberResponseVO memberResponseVO;

    @SerializedName("fileList")
    public List<FileData> fileList;

    public AnnouncementData(){}
    public AnnouncementData(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<AnnouncementData> CREATOR = new Creator<AnnouncementData>() {
        @Override
        public AnnouncementData createFromParcel(Parcel in) {
            return new AnnouncementData(in);
        }

        @Override
        public AnnouncementData[] newArray(int size) {
            return new AnnouncementData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(seq);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeInt(rdcnt);
        parcel.writeString(fileId);
        parcel.writeString(acaCode);
        parcel.writeString(insertDate);
        parcel.writeParcelable(memberResponseVO, i);
        parcel.writeInt(fileList.size());
        for(FileData data : fileList) {
            parcel.writeParcelable(data, i);
        }
    }

    public void readFromParcel(Parcel in) {
        seq = in.readInt();
        title = in.readString();
        content = in.readString();
        rdcnt = in.readInt();
        fileId = in.readString();
        acaCode = in.readString();
        insertDate = in.readString();
        memberResponseVO = in.readParcelable(MemberResponseVO.class.getClassLoader());
        int size = in.readInt();
        fileList = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            fileList.add(in.readParcelable(FileData.class.getClassLoader()));
        }
    }
}
