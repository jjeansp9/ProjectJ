package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ChildStudentInfo implements Parcelable {
    @SerializedName("seq")
    public int seq; // 원생 seq
    @SerializedName("gender")
    public String gender; // 성별
    @SerializedName("acaCode")
    public String acaCode; // 캠퍼스 코드
    @SerializedName("deptCode")
    public int deptCode; // 부서 코드
    @SerializedName("clstCode")
    public int clstCode; // 학년 코드
    @SerializedName("stCode")
    public int stCode; // 학생 코드
    @SerializedName("stName")
    public String stName; // 학생 이름
    @SerializedName("acaName")
    public String acaName; // 캠퍼스 이름
    @SerializedName("deptName")
    public String deptName; // 부서 이름
    @SerializedName("clstName")
    public String clstName; // 학년 이름
    @SerializedName("birth")
    public String birth; // 생일
    @SerializedName("stGrade")
    public String stGrade; // 학년 이름

    // Parcelable 구현 부분
    protected ChildStudentInfo(Parcel in) {
        seq = in.readInt();
        gender = in.readString();
        acaCode = in.readString();
        deptCode = in.readInt();
        clstCode = in.readInt();
        stCode = in.readInt();
        stName = in.readString();
        acaName = in.readString();
        deptName = in.readString();
        clstName = in.readString();
        birth = in.readString();
        stGrade = in.readString();
    }

    public static final Creator<ChildStudentInfo> CREATOR = new Creator<ChildStudentInfo>() {
        @Override
        public ChildStudentInfo createFromParcel(Parcel in) {
            return new ChildStudentInfo(in);
        }

        @Override
        public ChildStudentInfo[] newArray(int size) {
            return new ChildStudentInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(seq);
        dest.writeString(gender);
        dest.writeString(acaCode);
        dest.writeInt(deptCode);
        dest.writeInt(clstCode);
        dest.writeInt(stCode);
        dest.writeString(stName);
        dest.writeString(acaName);
        dest.writeString(deptName);
        dest.writeString(clstName);
        dest.writeString(birth);
        dest.writeString(stGrade);
    }
}
