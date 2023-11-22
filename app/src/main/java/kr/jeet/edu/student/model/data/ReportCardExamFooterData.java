package kr.jeet.edu.student.model.data;

import kr.jeet.edu.student.activity.ReportCardShowActivity;

public class ReportCardExamFooterData implements ReportCardShowActivity.ExamListTypeItem{
    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int compareTo(ReportCardShowActivity.ExamListTypeItem o) {
        return 0;
    }
}
