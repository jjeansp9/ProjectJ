package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

public class StudentInfo { // 원생 정보
    @SerializedName("name")
    public String name;

    @SerializedName("phoneNumber")
    public String phoneNumber;

    @SerializedName("gender")
    public String gender;

    @SerializedName("acaCode")
    public String acaCode;

    @SerializedName("deptCode")
    public int deptCode;

    @SerializedName("clstCode")
    public int clstCode;

    @SerializedName("acaName")
    public String acaName;

    @SerializedName("deptName")
    public String deptName;

    @SerializedName("clstName")
    public String clstName;

    @SerializedName("birth")
    public String birth;

    @SerializedName("scName")
    public String scName;

    @SerializedName("stGrade")
    public String stGrade;

    @SerializedName("parentPhoneNumber")
    public String parentPhoneNumber;

    @SerializedName("pushStatus")
    public PushStatus pushStatus;

    public static class PushStatus {
        @SerializedName("seq")
        public int seq;

        @SerializedName("pushNotice")
        public String pushNotice;

        @SerializedName("pushInformationSession")
        public String pushInformationSession;

        @SerializedName("pushAttendance")
        public String pushAttendance;

        @SerializedName("pushSystem")
        public String pushSystem;
    }
}
