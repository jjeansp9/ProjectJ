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

    @SerializedName("acaName")
    public String acaName;

    @SerializedName("acaGubunCode")
    public String acaGubunCode;

    @SerializedName("acaGubunName")
    public String acaGubunName;

    @SerializedName("insertDate")
    public String insertDate;

    @SerializedName("memberResponseVO")
    public MemberResponseVO memberResponseVO;

    public boolean isRead = false;

    @SerializedName("fileList")
    public List<FileData> fileList;
    public AnnouncementData(Parcel in) {
        readFromParcel(in);
    }
    public AnnouncementData() {
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
        parcel.writeString(acaName);
        parcel.writeString(acaGubunCode);
        parcel.writeString(acaGubunName);
        parcel.writeString(insertDate);
        parcel.writeParcelable(memberResponseVO, i);
        parcel.writeByte((byte) (isRead ? 1 : 0));
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
        acaName = in.readString();
        acaGubunCode = in.readString();
        acaGubunName = in.readString();
        insertDate = in.readString();
        memberResponseVO = in.readParcelable(MemberResponseVO.class.getClassLoader());
        isRead = in.readByte() != 0;
        int size = in.readInt();
        fileList = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            fileList.add(in.readParcelable(FileData.class.getClassLoader()));
        }
    }
}
