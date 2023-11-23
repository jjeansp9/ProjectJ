package kr.jeet.edu.student.model.data;

import static kr.jeet.edu.student.adapter.ReportCardShowType0Adapter.LAYOUT_FOOTER;

import kr.jeet.edu.student.activity.ReportCardShowActivity;

public class ReportCardExamFooterData implements ReportCardShowActivity.ExamListTypeItem{
    public int esGubun;
    public int totalScore;
    public int totalCount;
    public int correctCount;
    public String correctRate;
    public String esTitle;
    public int esNum;
    public ReportCardExamFooterData(int esGubun, int totalScore, int totalCount, int correctCount, String correctRate, String esTitle, int esNum){
        this.esGubun = esGubun;
        this.totalScore = totalScore;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.correctRate = correctRate;
        this.esTitle = esTitle;
        this.esNum = esNum;
    }
    public ReportCardExamFooterData(int esGubun, int totalScore, int totalCount, int correctCount, String correctRate, String esTitle){
        this.esGubun = esGubun;
        this.totalScore = totalScore;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.correctRate = correctRate;
        this.esTitle = esTitle;
    }
    @Override
    public int getEsGubun() {
        return esGubun;
    }

    @Override
    public int getType() {
        return LAYOUT_FOOTER;
    }

    @Override
    public int compareTo(ReportCardShowActivity.ExamListTypeItem item) {
        int comparisonGubun = 0;
        comparisonGubun = this.esGubun - item.getEsGubun();
        if(comparisonGubun == 0) {
            return getType() - item.getType();
        }else{
            return comparisonGubun;
        }
    }
}
