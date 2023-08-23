package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

public class SchoolData {
    @SerializedName("scName")
    public String scName; // 학교 이름
    @SerializedName("scCode")
    public int scCode; // 학교 코드
}
