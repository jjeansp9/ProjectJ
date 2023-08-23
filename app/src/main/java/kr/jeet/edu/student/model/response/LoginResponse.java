package kr.jeet.edu.student.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kr.jeet.edu.student.model.data.LoginData;

public class LoginResponse extends BaseResponse {
    @SerializedName("data")
    @Expose
    public LoginData data;
}
