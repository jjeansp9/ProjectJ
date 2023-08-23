package kr.jeet.edu.student.model.data;

import com.google.gson.annotations.SerializedName;

public class BoardAttributeData {
    @SerializedName("boardType")
    private String boardType;

    @SerializedName("boardNm")
    private String boardNm;

    @SerializedName("atchPosblFileNum")
    private int atchPosblFileNum;

    @SerializedName("commentAt")
    private String commentAt;

    @SerializedName("cntPerPage")
    private int cntPerPage;
}
