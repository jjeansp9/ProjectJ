package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import kr.jeet.edu.student.model.data.ChildStudentInfo;

public class SearchChildStudentsResponse extends BaseResponse {
    @SerializedName("data")
    @Expose
    public ArrayList<ChildStudentInfo> data;
}
