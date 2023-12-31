package kr.jeet.edu.student.common;

import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.student.model.data.ACAData;
import kr.jeet.edu.student.model.data.BoardAttributeData;
import kr.jeet.edu.student.model.data.LTCData;
import kr.jeet.edu.student.model.data.LTCSubjectData;
import kr.jeet.edu.student.model.data.SchoolData;
import kr.jeet.edu.student.model.data.TeacherClsData;

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
    //서버에서 주는 항목
    public static final String BOARD_NOTICE = "notice";    //공지사항
    public static final String BOARD_PT = "pt";    //설명회
    public static final String BOARD_SYSTEM_NOTICE = "systemNotice";   //알림
    public static final String BOARD_LEVELTEST = "levelTest";  //테스트예약
    public static final String BOARD_REPORT = "report";    //성적표
    public static final String BOARD_QNA = "qna";  //Q&A
    public static final String BOARD_TIME_TABLE = "시간표";  //시간표
    ////서버에서 주지 않는 항목
//    public static final String BOARD_STUDENT_INFO = "studentinfo"; //원생정보
    public static final String BOARD_ATTENDANCE = "attendance"; //출석부
    public static final String BOARD_BUS = "bus";  //차량정보
    public static final String BOARD_SCHEDULE = "schedule";
    //Dot size
    public float DOT_SIZE = 10f;
    public boolean isSelectedChild = false;
    // 캠퍼스, 레벨캠퍼스, 레벨테스트 과목, 학교 리스트
    private List<ACAData> ACAList = new ArrayList<>();
    private List<LTCData> LTCList = new ArrayList<>();
    private List<LTCSubjectData> LTCSubjectList = new ArrayList<>();
    private List<SchoolData> SchoolList = new ArrayList<>();
    //지역별 캠퍼스 리스트
    private ArrayMap<String, ACAData> LocalACAListMap = new ArrayMap<>();
    //학교 리스트
    private ArrayMap<Integer, SchoolData> SchoolListMap = new ArrayMap<>();

    // 게시판 정보 리스트
    private ArrayMap<String, BoardAttributeData> BoardListMap = new ArrayMap<>();

    private ArrayMap<Integer, TeacherClsData> clsListMap = new ArrayMap<>();

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

    /**
     * JEET 테스트 과목 리스트 정보 조회
     * @return List<LTCSubjectData>
     */
    public List<LTCSubjectData> getLTCSubjectList() {
        return LTCSubjectList;
    }

    /**
     * JEET 테스트 과목 리스트 정보 저장
     * @param LTCSubjectList
     */
    public void setLTCSubjectList(List<LTCSubjectData> LTCSubjectList) {
        this.LTCSubjectList = LTCSubjectList;
    }

    /**
    * 게시판 속성목록 저장 및 조회
    * */
    public ArrayMap<String, BoardAttributeData> getBoardInfoArrayMap() {
        return BoardListMap;
    }
    public void setBoardInfoMap(ArrayMap<String, BoardAttributeData> map) { this.BoardListMap =  map; }

    public BoardAttributeData getBoardInfo(String boardType){
        if (BoardListMap.containsKey(boardType)) return BoardListMap.get(boardType);
        return null;
    }

    public boolean setBoardInfo(BoardAttributeData data){
        if (data == null) return false;
        String key = data.boardType;
        if (!BoardListMap.containsKey(key)){
            BoardListMap.put(key, data);
            return true;
        }
        return false;
    }

    /**
    * 학교 ListMap
    * */
    public ArrayMap<Integer, SchoolData> getSchoolListMap() {
        return SchoolListMap;
    }
    public void setSchoolListMap(ArrayMap<Integer, SchoolData> map) {
        this.SchoolListMap =  map;
    }
    public boolean initSchoolListMap(List<SchoolData> list)
    {
        if(list == null) return false;
        if(!SchoolListMap.isEmpty()) SchoolListMap.clear();
        for(SchoolData item : list) {
            int key = item.scCode;
            if (!SchoolListMap.containsKey(key)) {
                SchoolListMap.put(key, item);            }
        }
        return true;
    }
    public SchoolData getSchoolData(int scCode) {
        if(SchoolListMap.containsKey(scCode)) {
            return SchoolListMap.get(scCode);
        }
        return null;
    }
    public boolean setSchoolData(SchoolData data) {
        if(data == null) return false;
        int key = data.scCode;
        if(!SchoolListMap.containsKey(key)) {
            SchoolListMap.put(key, data);
            return true;
        }
        return false;
    }

    /**
     * 지도강사 ListMap
     * */
    public ArrayMap<Integer, TeacherClsData> getClsListMap() {
        return clsListMap;
    }
    public void setClsListMap(ArrayMap<Integer, TeacherClsData> map) {
        this.clsListMap =  map;
    }
    public boolean initClsListMap(List<TeacherClsData> list)
    {
        if(list == null) return false;
        if(!clsListMap.isEmpty()) clsListMap.clear();
        for(TeacherClsData item : list) {
            int key = item.clsCode;
            if (!clsListMap.containsKey(key)) {
                clsListMap.put(key, item);            }
        }
        return true;
    }
    public TeacherClsData getClsData(int clsCode) {
        if(clsListMap.containsKey(clsCode)) {
            return clsListMap.get(clsCode);
        }
        return null;
    }
    public boolean setClsData(TeacherClsData data) {
        if(data == null) return false;
        int key = data.clsCode;
        if(!clsListMap.containsKey(key)) {
            clsListMap.put(key, data);
            return true;
        }
        return false;
    }

    public ArrayMap<String, ACAData> getLocalACAListMap() {
        return LocalACAListMap;
    }
    public void setLocalACAListMap(ArrayMap<String, ACAData> map) {
        this.LocalACAListMap =  map;
    }
    public boolean initLocalACAListMap(List<ACAData> list)
    {
        if(list == null) return false;
        if(!LocalACAListMap.isEmpty()) LocalACAListMap.clear();
        for(ACAData item : list) {
            String key = item.acaCode;
            if (!LocalACAListMap.containsKey(key)) {
                LocalACAListMap.put(key, item);
            }
        }
        return true;
    }
    public ACAData getLocalACAData(String acaCode) {
        if(LocalACAListMap.containsKey(acaCode)) {
            return LocalACAListMap.get(acaCode);
        }
        return null;
    }
    public boolean setLocalACAData(ACAData data) {
        if(data == null) return false;
        String key = data.acaCode;
        if(!LocalACAListMap.containsKey(key)) {
            LocalACAListMap.put(key, data);
            return true;
        }
        return false;
    }
}
