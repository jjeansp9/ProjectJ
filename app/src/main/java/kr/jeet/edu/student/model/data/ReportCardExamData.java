package kr.jeet.edu.student.model.data;

import static kr.jeet.edu.student.adapter.ReportCardShowType0Adapter.LAYOUT_CONTENT;

import kr.jeet.edu.student.activity.ReportCardShowActivity;

public class ReportCardExamData implements ReportCardShowActivity.ExamListTypeItem {
    public String esTitle;
    public String esName;
    public int esGubun;
    public int esNum;
    public String esScore;
    public ReportCardExamData(int esGubun, int esNum, String esName, String esScore) {
        this.esGubun = esGubun;
        this.esNum = esNum;
        this.esName = esName;
        this.esScore = esScore;
    }

    public ReportCardExamData(int esNum) {this.esNum = esNum;}
    public ReportCardExamData() {}

    @Override
    public int getEsGubun() {
        return 0;
    }

    @Override
    public int getType() {
        return LAYOUT_CONTENT;
    }

    @Override
    public int compareTo(ReportCardShowActivity.ExamListTypeItem item) {
        int comparisonGubun = 0;
        comparisonGubun = this.esGubun - item.getEsGubun();
        if(comparisonGubun == 0) {
            int comparisonType =  getType() - item.getType();
            if(comparisonType == 0) {
                ReportCardExamData castItem = (ReportCardExamData)item;
                if (this.esNum == 0) return 1;
                return this.esNum - castItem.esNum;
            }else{
                return comparisonType;
            }
        }else{
            return comparisonGubun;
        }
    }
}
