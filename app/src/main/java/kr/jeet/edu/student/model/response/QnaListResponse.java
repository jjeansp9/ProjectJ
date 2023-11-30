package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import kr.jeet.edu.student.model.data.QnaData;

public class QnaListResponse extends BaseResponse{
    @SerializedName("data")
    @Expose
    public QnaListData data;

    public static class QnaListData {
        public List<QnaData> mainList;
        public List<QnaData> list;
    }
}
