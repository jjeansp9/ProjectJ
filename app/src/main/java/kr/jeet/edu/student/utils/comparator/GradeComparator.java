package kr.jeet.edu.student.utils.comparator;

import java.util.Comparator;

public class GradeComparator implements Comparator<String> {
    private static final String[] GRADE_ORDER = {"초등부", "중등부", "고등부"};

    @Override
    public int compare(String grade1, String grade2) {
        int index1 = getIndex(grade1);
        int index2 = getIndex(grade2);

        return Integer.compare(index1, index2);
    }

    private int getIndex(String grade) {
        for (int i = 0; i < GRADE_ORDER.length; i++) {
            if (GRADE_ORDER[i].equals(grade)) {
                return i;
            }
        }
        return GRADE_ORDER.length; // 다른 학년이라면 가장 뒤로 정렬 (서버에서 가져온 학년이 GRADE_ORDER 데이터와 다른경우 정렬하지 않음)
    }
}
