package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import kr.jeet.edu.student.model.data.NoticeData;

public class NoticeListResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public List<NoticeData> data;
}
