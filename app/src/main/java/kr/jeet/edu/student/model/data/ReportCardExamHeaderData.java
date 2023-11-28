package kr.jeet.edu.student.model.data;

import static kr.jeet.edu.student.adapter.TuitionListAdapter.LAYOUT_HEADER;

import kr.jeet.edu.student.activity.menu.reportcard.ReportCardShowActivity;

public class ReportCardExamHeaderData implements ReportCardShowActivity.ExamListTypeItem{
    private int esGubun;
    public String esTitle;

    public ReportCardExamHeaderData(int esGubun, String title) {
        this.esGubun = esGubun;
        this.esTitle = title;
    }
    @Override
    public int getEsGubun() {
        return esGubun;
    }

    @Override
    public int getType() {
        return LAYOUT_HEADER;
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
