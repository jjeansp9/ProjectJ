package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class FileData implements Parcelable {
    @SerializedName("seq")
    public int seq;

    @SerializedName("fileId")
    public String fileId;

    @SerializedName("orgName")
    public String orgName;

    @SerializedName("saveName")
    public String saveName;

    @SerializedName("path")
    public String path;

    @SerializedName("extension")
    public String extension;

    public String tempFileName;

    @SerializedName("contentType")
    public String contentType;

    protected FileData(Parcel in) {
        seq = in.readInt();
        fileId = in.readString();
        orgName = in.readString();
        saveName = in.readString();
        path = in.readString();
        extension = in.readString();
        tempFileName = in.readString();
        contentType = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(fileId);
        dest.writeString(orgName);
        dest.writeString(saveName);
        dest.writeString(path);
        dest.writeString(extension);
        dest.writeString(tempFileName);
        dest.writeString(contentType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FileData> CREATOR = new Creator<FileData>() {
        @Override
        public FileData createFromParcel(Parcel in) {
            return new FileData(in);
        }

        @Override
        public FileData[] newArray(int size) {
            return new FileData[size];
        }
    };

    public void initTempFileName() {
        this.tempFileName = String.format("%d_%s", seq, orgName);
    }
}
