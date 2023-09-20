package kr.jeet.edu.student.model.request;

import java.util.List;

public class PushConfirmRequest {
    public List<String> pushId;
    public String pushType;
    public int seq;
    public int stCode;
    public int userGubun;
}
