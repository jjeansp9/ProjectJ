package kr.jeet.edu.student.model.data;

import java.util.ArrayList;

public class ReportCardShowListItemData {
    public int esGubun;                   // 단계구분
    public int totalScore;                // 총점
    public int totalCount;                // 총 개수
    public int correctCount;              // 맞은 개수
    public String correctRate;            // 정답률
    public ArrayList<ReportCardExamData> dataList; // 단계구분 리스트
}
