package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

public class ACAData {
    @SerializedName("acaName")
    public String acaName; // 캠퍼스 이름
    @SerializedName("acaCode")
    public String acaCode; // 캠퍼스 코드
}
