package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

public class TuitionData {
    @SerializedName("acaName")
    public String acaName;

    @SerializedName("clsName")
    public String clsName;

    @SerializedName("payment")
    public String payment;

    @SerializedName("accountNO")
    public String accountNO;

    @SerializedName("gubun")
    public String gubun;
}
