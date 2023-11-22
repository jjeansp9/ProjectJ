package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.student.model.data.ReportCardSummaryData;

public class ReportCardSummaryResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public ReportCardSummaryData data;
}
