package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.student.model.data.SystemNoticeData;

public class SystemNoticeResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public SystemNoticeData data;
}
