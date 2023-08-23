package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.student.model.data.IDData;

public class FindIDResponse extends BaseResponse {

    @SerializedName("data")
    @Expose
    public IDData data;
}

