package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import kr.jeet.edu.student.model.data.BriefingData;

public class BriefingReserveResponse {
    @SerializedName("msg")
    public String msg;

    @SerializedName("errMsg")
    public String errMsg;
}
