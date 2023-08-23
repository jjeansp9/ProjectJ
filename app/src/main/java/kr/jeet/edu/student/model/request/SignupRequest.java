package kr.jeet.edu.student.model.request;

import kr.jeet.edu.student.common.Constants;

public class SignupRequest {

    public String id;

    public String pw;

    public String name;

    public String phoneNumber;

    public String gender;

    public int userGubun = Constants.USER_TYPE_PARENTS;    // 0: 관리자, 1:강사, 2:학생, 3:학부모

}
