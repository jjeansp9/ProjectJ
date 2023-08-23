package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import kr.jeet.edu.student.model.data.AnnouncementData;

public class BoardDetailResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public AnnouncementData data;
}
