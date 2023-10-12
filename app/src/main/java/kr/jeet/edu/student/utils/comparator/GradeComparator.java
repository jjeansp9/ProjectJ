package kr.jeet.edu.student.utils.comparator;

import java.util.Comparator;

public class GradeComparator implements Comparator<String> {
    private static final String[] GRADE_ORDER = {"초등", "중등", "고등"};

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
        return GRADE_ORDER.length; // 다른 학년이라면 가장 뒤로 정렬
    }
}
