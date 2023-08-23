package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

public class LoginData {

    @SerializedName("seq")
    public int seq;
    @SerializedName("isOriginalMember")
    public String isOriginalMember;
    @SerializedName("userGubun")
    public int userGubun;
    @SerializedName("stCode")
    public int stCode;
    @SerializedName("sfCode")
    public int sfCode;
    @SerializedName("pushStatus")
    public StudentInfo.PushStatus pushStatus;
}
