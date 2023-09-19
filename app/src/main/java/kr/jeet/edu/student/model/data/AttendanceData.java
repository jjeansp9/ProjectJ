package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

import kotlin.jvm.Transient;

public class AttendanceData implements Cloneable{
    @SerializedName("seq")
    public int seq;

    @SerializedName("stName")
    public String stName;

    @SerializedName("stCode")
    public int stCode;

    @SerializedName("attendGubun")
    public int attendGubun;

    @SerializedName("attendBigo")
    public String attendBigo;

    @SerializedName("attendDate")
    public String attendDate;

    @SerializedName("stSex")
    public String stSex;

    @SerializedName("stBirth")
    public String stBirth;

    @SerializedName("parentPhoneNumber")
    public String parentPhoneNumber;

    @Transient
    public int attendGubunWrite = -1;   //출결여부 변경값
    @Transient
    public String attendBigoWrite = ""; // 비고 작성 내용
    @Transient
    public boolean isChecked;   // 체크박스 설정 여부
    @Transient
    public int updateResult;    //업데이트 결과
}
