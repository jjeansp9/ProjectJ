package kr.jeet.edu.student.model.data;

import kr.jeet.edu.student.activity.ReportCardShowActivity;

public class ReportCardExamData implements ReportCardShowActivity.ExamListTypeItem {
    public String esTitle;
    public String esName;
    public int esGubun;
    public int esNum;
    public String esScore;

    public int contentType; //header / content /footer

    @Override
    public int getType() {
        return contentType;
    }

    @Override
    public int compareTo(ReportCardShowActivity.ExamListTypeItem o) {
        return 0;
    }
}
