package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

public class TeacherClsData {
    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("sfCode")
    public int sfCode;

    @SerializedName("sfName")
    public String sfName;

    @SerializedName("clsName")
    public String clsName;
}
