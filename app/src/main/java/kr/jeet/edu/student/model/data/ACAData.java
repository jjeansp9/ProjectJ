package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

public class ACAData {
    @SerializedName("acaName")
    public String acaName; // 캠퍼스 이름
    @SerializedName("acaCode")
    public String acaCode; // 캠퍼스 코드
    @SerializedName("acaTel")
    public String acaTel; // 캠퍼스 대표 번호
    public ACAData(String acaCode, String acaName, String acaTel) {
        this.acaCode = acaCode;
        this.acaName = acaName;
        this.acaTel = acaTel;
    }
}
