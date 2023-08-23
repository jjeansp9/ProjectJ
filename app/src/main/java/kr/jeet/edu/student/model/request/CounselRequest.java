package kr.jeet.edu.student.model.request;

import com.google.gson.annotations.SerializedName;

public class CounselRequest {
    @SerializedName("memberSeq")
    public int memberSeq;

    @SerializedName("memberName")
    public String memberName;

    @SerializedName("counselDate")
    public String counselDate;

    @SerializedName("acaCode")
    public String acaCode;

    @SerializedName("acaName")
    public String acaName;

    @SerializedName("sfCode")
    public int sfCode;

    @SerializedName("sfName")
    public String sfName;

    @SerializedName("memo")
    public String memo;

    @SerializedName("clsName")
    public String clsName;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("managerPhoneNumber")
    public String managerPhoneNumber;
}
