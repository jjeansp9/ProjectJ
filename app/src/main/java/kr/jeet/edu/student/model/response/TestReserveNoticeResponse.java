package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TestReserveNoticeResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public String data;
}
