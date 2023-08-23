package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import kr.jeet.edu.student.model.data.TuitionData;

public class TuitionResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public ArrayList<TuitionData> data;
}
