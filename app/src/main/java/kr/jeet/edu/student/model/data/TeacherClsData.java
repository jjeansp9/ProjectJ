package kr.jeet.edu.student.model.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TeacherClsData implements Parcelable {

    public String phoneNumber;    // 강사 휴대폰번호
    public int sfCode;            // jeet 회원 고유값
    public String sfName;         // 강사 이름
    public String clsName;        // 학급명
    public int clsCode;           // 학급 코드
    public String extNumber;      // 강사 내선 번호

    public TeacherClsData() {}

    protected TeacherClsData(Parcel in) {
        phoneNumber = in.readString();
        sfCode = in.readInt();
        sfName = in.readString();
        clsName = in.readString();
        clsCode = in.readInt();
        extNumber = in.readString();
    }

    public static final Creator<TeacherClsData> CREATOR = new Creator<TeacherClsData>() {
        @Override
        public TeacherClsData createFromParcel(Parcel in) {
            return new TeacherClsData(in);
        }

        @Override
        public TeacherClsData[] newArray(int size) {
            return new TeacherClsData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(phoneNumber);
        dest.writeInt(sfCode);
        dest.writeString(sfName);
        dest.writeString(clsName);
        dest.writeInt(clsCode);
        dest.writeString(extNumber);
    }
}
