package kr.jeet.edu.student.model.data;

import java.util.List;

public class ReportCardShowData {
    public String etGubun;             // 응시과정
    public String etName;              // 제목
    public String stName;              // 원생명
    public String scName;              // 학교명
    public String stGrade;             // 학년
    public String regDate;             // 응시일
    public List<ReportCardShowListItemData> list; // 원생 성적표 리스트
}

