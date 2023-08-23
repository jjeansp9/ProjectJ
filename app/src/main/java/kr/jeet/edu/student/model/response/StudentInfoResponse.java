package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import kr.jeet.edu.student.model.data.StudentInfo;

public class StudentInfoResponse extends BaseResponse {
    @SerializedName("data")
    @Expose
    public StudentInfo data;
}
