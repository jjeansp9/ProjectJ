package kr.jeet.edu.student.common;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.SchoolData;

/**
 * 앱에서 사용하는 JEET 관련 정보들을 저장하는 클래스
 */
public class DataManager {
    private static final String TAG = "dataMgr";

    public static DataManager getInstance() {
        return DataManager.LazyHolder.INSTANCE;
    }
    private static class LazyHolder {
        private static final DataManager INSTANCE = new DataManager();
    }
    // 캠퍼스 리스트
    private List<ACAData> ACAList = new ArrayList<>();
    private List<LTCData> LTCList = new ArrayList<>();
    private List<SchoolData> SchoolList = new ArrayList<>();

    /**
     * JEET 캠퍼스 리스트 정보 조회
     * @return List<ACAData>
     */
    public List<ACAData> getACAList() {
        return ACAList;
    }

    /**
     * JEET 캠퍼스 리스트 정보 저장
     * @param ACAList
     */
    public void setACAList(List<ACAData> ACAList) {
        this.ACAList = ACAList;
    }

    /**
     * JEET 캠퍼스 리스트 정보 조회 (테스트예약)
     * @return List<ACAData>
     */
    public List<LTCData> getLTCList() {
        return LTCList;
    }

    /**
     * JEET 캠퍼스 리스트 정보 저장 (테스트예약)
     * @param LTList
     */
    public void setLTCList(List<LTCData> LTList) {
        this.LTCList = LTList;
    }

    /**
     * JEET 학교 리스트 정보 조회
     * @return List<SchoolData>
     */
    public List<SchoolData> getSchoolList() {
        return SchoolList;
    }

    /**
     * JEET 학교 리스트 정보 저장
     * @param SchoolList
     */
    public void setSchoolList(List<SchoolData> SchoolList) {
        this.SchoolList = SchoolList;
    }
}
