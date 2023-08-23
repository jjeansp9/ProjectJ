package kr.jeet.edu.student.model.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {

    @SerializedName("msg")
    public String msg;

    @Override
    public String toString() {
        return "BaseResponse{" +
                "msg='" + msg + '\'' +
                '}';
    }
}
