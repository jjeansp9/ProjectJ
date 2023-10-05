package kr.jeet.edu.student.model.request;

import com.google.gson.annotations.SerializedName;

public class BriefingReserveRequest {
    @SerializedName("ptSeq")
    public int ptSeq; // 설명회 예약 글 seq

    @SerializedName("memberSeq")
    public int memberSeq; // 예약자 memberSeq

    @SerializedName("name")
    public String name; // 원생명

    @SerializedName("phoneNumber")
    public String phoneNumber; // 연락처

    @SerializedName("email")
    public String email; // 이메일

    @SerializedName("participantsCnt")
    public int participantsCnt; // 참여인원수

    @SerializedName("schoolNm")
    public String schoolNm; // (선택)학교명

    @SerializedName("grade")
    public String grade; // (선택)학년
}
