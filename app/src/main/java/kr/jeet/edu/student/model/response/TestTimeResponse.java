package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import kr.jeet.edu.student.model.data.TestTimeData;

public class TestTimeResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public ArrayList<TestTimeData> data;
}
